package com.xxx.xcloud.module.backup.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.backup.entity.Job;
import com.xxx.xcloud.module.backup.entity.JobHistory;
import com.xxx.xcloud.module.backup.repository.JobHistoryRepository;
import com.xxx.xcloud.module.backup.repository.JobRepository;
import com.xxx.xcloud.module.backup.service.IJobService;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlBackupConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlServer;
import com.xxx.xcloud.module.component.repository.StatefulServiceRepository;
import com.xxx.xcloud.module.component.util.ComponentOperationsClientUtil;
import com.xxx.xcloud.module.component.util.ComponentOperationsDataBaseUtil;
import com.xxx.xcloud.module.component.util.EtcdUtil;
import com.xxx.xcloud.utils.StringUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.KeyToPath;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.batch.JobCondition;
import io.fabric8.kubernetes.api.model.batch.JobSpec;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
 * @ClassName: JobServiceImpl
 * @Description: job接口实现类
 * @author lnn
 * @date 2019年11月18日
 *
 */
@Component
public class JobServiceImpl implements IJobService {

    private static Logger LOG = LoggerFactory.getLogger(JobServiceImpl.class);

    // private MixedOperation<io.fabric8.kubernetes.api.model.batch.Job,
    // io.fabric8.kubernetes.api.model.batch.JobList,
    // io.fabric8.kubernetes.api.model.batch.DoneableJob,
    // ScalableResource<io.fabric8.kubernetes.api.model.batch.Job,
    // io.fabric8.kubernetes.api.model.batch.DoneableJob>> jobClient =
    // KubernetesClientFactory
    // .getClient().batch().jobs();

    @Autowired
    ComponentOperationsDataBaseUtil componentOperationsDataBaseUtil;

    @Autowired
    ComponentOperationsClientUtil componentOperationsClientUtil;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobHistoryRepository jobHistoryRepository;

    @Autowired
    StatefulServiceRepository statefulServiceRepository;

    private Integer[] backUpTaskTypes = new Integer[] { MysqlBackupConst.JOB_TYPE_FULL_AMOUNT_BACKUP,
            MysqlBackupConst.JOB_TYPE_INCREMENTAL_BACKUP };

    private static ThreadFactory backupThreadFactory = new ThreadFactoryBuilder().setNameFormat("bdos-backup-worker-%d")
            .build();

    private static final ExecutorService BACKUP_POOL = new ThreadPoolExecutor(0, 20, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), backupThreadFactory);

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Job save(Job job, String nameSpace) {
        job.setCreatetime(new Date());
        job.setUpdatetime(new Date());
        return saveDBAndETCD(job, nameSpace);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Job update(Job job, String nameSpace) {
        job.setUpdatetime(new Date());
        return saveDBAndETCD(job, nameSpace);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(String id, String nameSpace) {
        // 必须在删除之前获取ETCD需要的数据--JOB。
        Job job = getOneTask(id);

        // 1、删除JOB---从数据库
        jobRepository.deleteById(id);
        LOG.info("数据库删除 JOB正常！");

        // 2、删除JOB---从ETCD
        try {
            deleteFromETCD(job, nameSpace);
        } catch (Exception e) {
            throw new RuntimeException("ETCD errors delete ETCD failed!", e);
        }

        LOG.info("ETCD 删除 JOB正常！");
    }

    @Override
    public Job getOneTask(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        Job job = null;
        Optional<Job> jobOptional = jobRepository.findById(id);
        if (jobOptional.isPresent()) {
            job = jobOptional.get();
        }
        return job;
    }

    @Override
    public Job getByRelationJobId(String jobId) {
        return jobRepository.findByRelationJobId(jobId);
    }

    @Override
    public List<Job> getJobsByNodeId(String nodeId) {
        return jobRepository.findByNodeId(nodeId);
    }

    @Override
    public Page<Job> getTasks(String serviceId, Integer jobType, Integer status, Pageable pageable) {

        // 通过集群ID+任务类型查询所有任务！
        if (null != jobType && null == status) {

            return jobRepository.findByServiceIdAndJobTypeAndStatusNot(serviceId, jobType,
                    MysqlBackupConst.JOB_STATUS_DETETED, pageable);
        }
        // 通过集群ID+任务状态查询所有任务
        if (null == jobType && null != status) {

            return jobRepository.findByServiceIdAndStatusAndJobTypeIn(serviceId, status, backUpTaskTypes, pageable);
        }
        // 通过集群ID+任务类型+任务状态查询所有任务！
        if (null != jobType && null != status) {

            return jobRepository.findByServiceIdAndJobTypeAndStatus(serviceId, jobType, status, pageable);
        }

        // 默认通过集群ID查询所有类型是备份的任务！
        return jobRepository.findByServiceIdAndJobTypeInAndStatusNot(serviceId, backUpTaskTypes,
                MysqlBackupConst.JOB_STATUS_DETETED, pageable);
    }

    private Job saveDBAndETCD(Job job, String nameSpace) {

        // 1、新增/更新JOB---写数据库
        Job savedJob = jobRepository.save(job);
        LOG.info("数据库SAVE JOB正常！");

        // 2、新增/更新JOB---写ETCD
        try {
            saveToETCD(savedJob, nameSpace);
        } catch (Exception e) {
            throw new RuntimeException("ETCD errors save ETCD failed!", e);
        }
        LOG.info("ETCD SAVE JOB正常！");

        return savedJob;
    }

    /**
     * 新增/更新ETCD任务
     */
    private void saveToETCD(Job savedJob, String nameSpace) throws ErrorMessageException {
        String path = getEtcdPath(savedJob, nameSpace);

        try {
            LOG.info("存储etcd时的key:" + path);
            EtcdUtil.putEtcdValueByKey(path, JSON.toJSONString(savedJob));
        } catch (Exception e) {
            LOG.error("etcd创建备份任务失败，job:" + JSON.toJSONString(savedJob) + ",error：", e);
            throw new ErrorMessageException(ReturnCode.CODE_ETCD_CLIENT_FAILED,
                    "etcd创建备份任务失败，job:" + JSON.toJSONString(savedJob) + ",error：" + e.getMessage());

        }

    }

    /**
     * @param savedJob
     * @param nameSpace
     * @return
     */
    private String getEtcdPath(Job savedJob, String nameSpace) {
        return "/backup/" + nameSpace + "/" + savedJob.getNodeName() + "/" + savedJob.getId();
    }

    /**
     * 删除JOb从ETCD
     */
    private void deleteFromETCD(Job jobBeforeDelete, String nameSpace) throws ErrorMessageException {
        String path = getEtcdPath(jobBeforeDelete, nameSpace);

        LOG.info("删除etcd时的key:" + path);
        try {
            EtcdUtil.deleteEtcdValueByKey(path);
        } catch (Exception e) {
            LOG.error("etcd删除备份任务失败，job:" + JSON.toJSONString(jobBeforeDelete) + ",error：", e);
            throw new ErrorMessageException(ReturnCode.CODE_ETCD_CLIENT_FAILED,
                    "etcd删除备份任务失败，job:" + JSON.toJSONString(jobBeforeDelete) + ",error：" + e.getMessage());

        }

    }

    /**
     * 恢复任务
     */
    @Override
    public boolean recover(String jobHistoryId, String nameSpace) throws ErrorMessageException {

        LOG.info("==================开始执行恢复任务===================");
        // 1查询jobHistoryList
        JobHistory jobHistory = null;
        JobHistory lastJobHistory = null;
        List<JobHistory> jobHistoryList = new ArrayList<>();
        try {
            Optional<JobHistory> jobHistoryOptional = jobHistoryRepository.findById(jobHistoryId);
            if (jobHistoryOptional.isPresent()) {
                jobHistory = jobHistoryOptional.get();
            }
            if (null != jobHistory) {
                jobHistoryList.add(jobHistory);
            }

            if (null != jobHistory && MysqlBackupConst.JOB_TYPE_FULL_AMOUNT_BACKUP != jobHistory.getJobType()) {
                Optional<JobHistory> lastJobHistoryOptional = jobHistoryRepository
                        .findById(jobHistory.getLastJobHistoryId());
                if (lastJobHistoryOptional.isPresent()) {
                    lastJobHistory = lastJobHistoryOptional.get();
                }
                if (null != lastJobHistory) {
                    jobHistoryList.add(lastJobHistory);
                }
            }
        } catch (Exception e) {
            LOG.error("恢复任务根据jobHistoryId查询jobHistory失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "恢复任务根据jobHistoryId查询jobHistory失败!");
        }
        LOG.info("查询到的jobHistory：" + JSON.toJSONString(jobHistoryList));

        if (jobHistoryList.isEmpty()) {
            LOG.info("获取到的jobHistoryList为空！");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "获取到的jobHistoryList为空！");
        }

        // 2、查询job
        Job job = null;
        try {
            Optional<Job> jobOptional = jobRepository.findById(jobHistory.getJobId());
            if (jobOptional.isPresent()) {
                job = jobOptional.get();
            }
        } catch (Exception e) {
            LOG.error("恢复任务时查询当前job异常！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "恢复任务时查询当前job异常！");
        }
        if (null == job) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "恢复任务时查询当前job为空！");
        }

        // 3、查询当前cluster
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(jobHistory.getServiceId());
        } catch (Exception e) {
            LOG.error("恢复任务时查询当前service异常！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "恢复任务时查询当前service异常！");
        }
        LOG.info("查询到的service：" + JSON.toJSONString(service));

        // 4、查询mysqlcluster
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(nameSpace, service.getServiceName());
        if (null == mysqlCluster) {
            LOG.error("恢复任务时查询当前mysqlcluster异常！");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "恢复任务时查询当前mysqlcluster异常！");
        }

        String jobName = jobHistory.getNodeName() + "-" + jobHistory.getId();
        // 5、拼接job
        io.fabric8.kubernetes.api.model.batch.Job createRecoverJob = createRecoverJob(jobHistory, jobHistoryList,
                mysqlCluster, jobName);

        // 6、调j8s接口创建恢复job
        io.fabric8.kubernetes.api.model.batch.Job createJob = null;
        try {
            createJob = KubernetesClientFactory.getJobs().inNamespace(nameSpace).create(createRecoverJob);
        } catch (KubernetesClientException e) {
            LOG.error("恢复任务时调用k8s接口创建job异常！", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_JOB_FAILED, "恢复任务时调用k8s接口创建job异常！！");
        }

        // 7、查询job执行结果
        Runnable runnable = () -> checkJobResult(jobName, nameSpace);

        BACKUP_POOL.submit(runnable);
        LOG.info("创建返回的job:" + JSON.toJSONString(createJob));
        return true;
    }

    /**
     * @param name
     * @param nameSpace
     * @return
     */
    private void checkJobResult(String jobName, String nameSpace) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                return;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {

                LOG.info("=============循环获取操作结果超时==========");
                return;
            }

            io.fabric8.kubernetes.api.model.batch.Job job = componentOperationsDataBaseUtil.getJob(nameSpace, jobName);
            if (null != job && null != job.getStatus()) {
                List<JobCondition> conditions = job.getStatus().getConditions();
                if (null != conditions && !conditions.isEmpty()) {
                    for (JobCondition jobCondition : conditions) {
                        if (MysqlBackupConst.JOB_CONDITION_TYPE_FAILED.equals(jobCondition.getType())
                                || MysqlBackupConst.JOB_CONDITION_TYPE_COMPLETE.equals(jobCondition.getType())) {
                            try {
                                LOG.info("============开始删除job=========");
                                KubernetesClientFactory.getJobs().inNamespace(nameSpace).withName(jobName)
                                        .cascading(true).delete();
                            } catch (KubernetesClientException e) {
                                LOG.error("删除job异常！", e);
                            }
                        }
                        break;
                    }
                }
            } else {
                LOG.info("job删除成功！");
                return;
            }
        }
    }

    @Override
    public void checkJobExist(Job job) throws ErrorMessageException {

        Integer[] backUpTaskTypes = MysqlBackupConst.getBackUpTaskTypes();

        // 备份任务---是否已存在
        if (Arrays.asList(backUpTaskTypes).contains(job.getJobType())) {
            List<Job> backupJobs = jobRepository.findByServiceIdAndJobTypeIn(job.getServiceId(), backUpTaskTypes);

            for (Job backupJob : backupJobs) {
                if (backupJob.getJobType().equals(job.getJobType())) {
                    String jobTypeName = MysqlBackupConst.getJobTypeName(job.getJobType());
                    throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, jobTypeName + "任务已存在!");
                }
            }

        }

        // 增量备份任务是否合理
        if (MysqlBackupConst.JOB_TYPE_INCREMENTAL_BACKUP == job.getJobType()
                && null == getOneTask(job.getRelationJobId())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "增量任务:" + JSON.toJSONString(job) + "必须关联一个全量任务!");
        }

    }

    /**
     * @param job
     * @param jobHistoryList
     * @param pod
     */
    private io.fabric8.kubernetes.api.model.batch.Job createRecoverJob(JobHistory jobHistory,
            List<JobHistory> jobHistoryList, MysqlCluster mysqlCluster, String jobName) {
        LOG.info("===========开始拼接恢复job====================");
        io.fabric8.kubernetes.api.model.batch.Job recoverJob = new io.fabric8.kubernetes.api.model.batch.Job();
        // metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(jobName);
        metadata.setNamespace(mysqlCluster.getMetadata().getNamespace());
        recoverJob.setMetadata(metadata);
        // spec
        JobSpec spec = new JobSpec();
        // template
        PodTemplateSpec template = new PodTemplateSpec();
        ObjectMeta templateMetadata = new ObjectMeta();
        templateMetadata.setNamespace(mysqlCluster.getMetadata().getNamespace());
        templateMetadata.setName(jobHistory.getJobId());
        template.setMetadata(templateMetadata);
        // podspec
        template.setSpec(getPodSpec(jobHistory, jobHistoryList, mysqlCluster));
        spec.setTemplate(template);
        spec.setBackoffLimit(0);
        recoverJob.setSpec(spec);

        LOG.info("恢复任务拼接完整job：" + JSON.toJSONString(recoverJob));

        return recoverJob;
    }

    private PodSpec getPodSpec(JobHistory jobHistory, List<JobHistory> jobHistoryList, MysqlCluster mysqlCluster) {
        PodSpec spec = new PodSpec();
        MysqlServer server = getServer(jobHistory, mysqlCluster);
        if (null == server) {
            LOG.info("获取的server为空！serverNodes：" + JSON.toJSONString(mysqlCluster.getStatus().getServerNodes()));
            return null;
        }
        List<Container> containers = new ArrayList<>();
        Container container = new Container();
        container.setName(MysqlBackupConst.MYSQL_RECOVER_CONTAINER_NAME);
        container.setImage(mysqlCluster.getSpec().getMysqlbackup().getBackupimage());

        List<String> commandList = new ArrayList<>();
        commandList.add(MysqlBackupConst.MYSQL_RECOVER_COMMAND_NAME);
        commandList.add(MysqlBackupConst.MYSQL_RECOVER_COMMAND_PATH);
        container.setCommand(commandList);
        List<EnvVar> env = new ArrayList<>();
        // nodeName
        EnvVar envNodeVar = new EnvVar();
        envNodeVar.setName(MysqlBackupConst.MYSQL_RECOVER_MY_NODE_NAME);
        EnvVarSource envVarSource = new EnvVarSource();
        ObjectFieldSelector fieldRef = new ObjectFieldSelector();
        fieldRef.setFieldPath(MysqlBackupConst.MYSQL_RECOVER_NODE_NAME);
        envVarSource.setFieldRef(fieldRef);
        envNodeVar.setValueFrom(envVarSource);
        env.add(envNodeVar);
        // podname
        EnvVar envPodVar = new EnvVar();
        envPodVar.setName(MysqlBackupConst.MYSQL_RECOVER_MY_POD_NAME);
        EnvVarSource podEnvVarSource = new EnvVarSource();
        ObjectFieldSelector podFieldRef = new ObjectFieldSelector();
        podFieldRef.setFieldPath(MysqlBackupConst.MYSQL_RECOVER_POD_NAME);
        podEnvVarSource.setFieldRef(podFieldRef);
        envPodVar.setValueFrom(podEnvVarSource);
        env.add(envPodVar);

        // namespace
        EnvVar envNameSpaceVar = new EnvVar();
        envNameSpaceVar.setName(MysqlBackupConst.MYSQL_RECOVER_MY_POD_NAMESPACE);
        EnvVarSource nameSpaceEnvVarSource = new EnvVarSource();
        ObjectFieldSelector nameSpaceFieldRef = new ObjectFieldSelector();
        nameSpaceFieldRef.setFieldPath(MysqlBackupConst.MYSQL_RECOVER_NAME_SPACE);
        nameSpaceEnvVarSource.setFieldRef(nameSpaceFieldRef);
        envNameSpaceVar.setValueFrom(nameSpaceEnvVarSource);
        env.add(envNameSpaceVar);

        EnvVar ftpAddrVar = new EnvVar();
        ftpAddrVar.setName(MysqlBackupConst.MYSQL_RECOVER_FTP_ADDR);
        ftpAddrVar.setValue(XcloudProperties.getConfigMap().get(Global.FTP_HOST) + ":"
                + XcloudProperties.getConfigMap().get(Global.FTP_PORT));
        env.add(ftpAddrVar);

        EnvVar ftpUsernameVar = new EnvVar();
        ftpUsernameVar.setName(MysqlBackupConst.MYSQL_RECOVER_FTP_USERNAME);
        ftpUsernameVar.setValue(XcloudProperties.getConfigMap().get(Global.FTP_USERNAME));
        env.add(ftpUsernameVar);

        EnvVar ftpPasswordVar = new EnvVar();
        ftpPasswordVar.setName(MysqlBackupConst.MYSQL_RECOVER_FTP_PASSWORD);
        ftpPasswordVar.setValue(XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD));
        env.add(ftpPasswordVar);

        EnvVar bdosVar = new EnvVar();
        bdosVar.setName(XcloudProperties.getConfigMap().get(Global.BDOS_NAME));
        bdosVar.setValue(XcloudProperties.getConfigMap().get(Global.BDOS_ADDRESS));
        env.add(bdosVar);

        EnvVar etcdVar = new EnvVar();
        etcdVar.setName(MysqlBackupConst.MYSQL_RECOVER_ETCD_NAME);
        etcdVar.setValue(XcloudProperties.getConfigMap().get(Global.ETCD_API_ADDRESS));
        env.add(etcdVar);

        EnvVar jobHistoryVar = new EnvVar();
        jobHistoryVar.setName(MysqlBackupConst.MYSQL_RECOVER_JOB_HISTORY_LIST);
        jobHistoryVar.setValue(JSON.toJSONString(jobHistoryList));
        env.add(jobHistoryVar);

        container.setEnv(env);
        // 拼接 volumeMounts
        List<VolumeMount> volumeMountList = new ArrayList<>();
        VolumeMount vm1 = new VolumeMount();
        vm1.setMountPath(MysqlBackupConst.MYSQL_RECOVER_VOLUM_MOUNT_CONF_PATH);
        vm1.setName(MysqlBackupConst.MYSQL_RECOVER_CONFIG_DIR_NAME);
        vm1.setSubPath(MysqlBackupConst.MYSQL_RECOVER_VOLUM_MOUNT_CONF_SUBPATH);
        volumeMountList.add(vm1);
        VolumeMount vm2 = new VolumeMount();
        vm2.setMountPath(MysqlBackupConst.MYSQL_RECOVER_VOLUM_MOUNT_PATH);
        vm2.setName(server.getVolumeid());
        vm2.setSubPath(MysqlBackupConst.MYSQL_RECOVER_VOLUM_MOUNT_SUBPATH);
        volumeMountList.add(vm2);
        container.setVolumeMounts(volumeMountList);

        // container资源
        ResourceRequirements resources = new ResourceRequirements();
        Map<String, io.fabric8.kubernetes.api.model.Quantity> limits = new HashMap<>();
        limits.put(CommonConst.CPU,
                new io.fabric8.kubernetes.api.model.Quantity(MysqlBackupConst.MYSQL_RECOVER_CONTAINER_DEFAULT_CPU));
        limits.put(CommonConst.MEMORY, new io.fabric8.kubernetes.api.model.Quantity(
                MysqlBackupConst.MYSQL_RECOVER_CONTAINER_DEFAULT_MEMORY + CommonConst.UNIT_GI));
        Map<String, io.fabric8.kubernetes.api.model.Quantity> requests = new HashMap<>();
        requests.put(CommonConst.CPU,
                new io.fabric8.kubernetes.api.model.Quantity(MysqlBackupConst.MYSQL_RECOVER_CONTAINER_DEFAULT_CPU));
        requests.put(CommonConst.MEMORY, new io.fabric8.kubernetes.api.model.Quantity(
                MysqlBackupConst.MYSQL_RECOVER_CONTAINER_DEFAULT_MEMORY + CommonConst.UNIT_GI));
        resources.setLimits(limits);
        resources.setRequests(requests);

        container.setResources(resources);
        containers.add(container);

        spec.setNodeName(server.getNodeName());
        spec.setContainers(containers);
        spec.setRestartPolicy(MysqlBackupConst.MYSQL_RECOVER_DEFAULT_RESTART_POLICY);
        // volumes
        List<Volume> newVolumes = new ArrayList<>();
        Volume vlm1 = new Volume();
        ConfigMapVolumeSource configMap = new ConfigMapVolumeSource();
        configMap.setName(server.getConfigmapname());
        List<KeyToPath> items = new ArrayList<>();
        KeyToPath keyToPath = new KeyToPath();
        keyToPath.setKey(MysqlClusterConst.MYSQL_CONFIG_FILE_NAME_MYCNF);
        keyToPath.setPath(MysqlBackupConst.MYSQL_RECOVER_VOLUM_MOUNT_CONF_SUBPATH);
        items.add(keyToPath);
        configMap.setItems(items);
        vlm1.setConfigMap(configMap);
        vlm1.setName(MysqlBackupConst.MYSQL_RECOVER_CONFIG_DIR_NAME);
        newVolumes.add(vlm1);

        Volume vlm2 = new Volume();
        io.fabric8.kubernetes.api.model.FlexVolumeSource flexVolume = new io.fabric8.kubernetes.api.model.FlexVolumeSource();
        flexVolume.setDriver(MysqlBackupConst.MYSQL_RECOVER_FLEXVOLUME_DRIVER);
        flexVolume.setFsType(MysqlBackupConst.MYSQL_RECOVER_FSTYPE);

        Map<String, String> options = new HashMap<>();
        options.put("size", mysqlCluster.getSpec().getCapacity());
        options.put("mountoptions", MysqlBackupConst.MYSQL_RECOVER_VOLUM_MOUNT_OPTIONS);
        options.put("volumeID", server.getVolumeid());
        options.put("volumegroup", XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        flexVolume.setOptions(options);
        vlm2.setFlexVolume(flexVolume);
        vlm2.setName(server.getVolumeid());
        newVolumes.add(vlm2);

        spec.setVolumes(newVolumes);

        LOG.info("恢复任务创建的spec:" + JSON.toJSONString(spec));
        return spec;
    }

    /**
     * @param job
     * @param mysqlCluster
     * @return
     */
    private MysqlServer getServer(JobHistory jobHistory, MysqlCluster mysqlCluster) {
        LOG.info("开始获取volumeId");
        if (null != mysqlCluster.getStatus()) {
            Map<String, MysqlServer> serverNodes = mysqlCluster.getStatus().getServerNodes();
            for (Map.Entry<String, MysqlServer> entry : serverNodes.entrySet()) {
                if (entry.getKey().equals(jobHistory.getNodeName())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

}
