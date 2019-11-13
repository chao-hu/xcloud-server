package com.xxx.xcloud.module.cronjob.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.cronjob.entity.Cronjob;
import com.xxx.xcloud.module.cronjob.model.JobInfoModel;
import com.xxx.xcloud.module.cronjob.repository.CronjobRepository;
import com.xxx.xcloud.module.cronjob.service.CronjobService;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.utils.DateUtil;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.CronJobSpec;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobCondition;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.api.model.batch.JobSpec;
import io.fabric8.kubernetes.api.model.batch.JobTemplateSpec;

/**
 * 
 * <p>
 * Description: 定时任务功能实现
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Service
public class CronjobServiceImpl implements CronjobService {

    private static final Logger LOG = LoggerFactory.getLogger(CronjobServiceImpl.class);

    /**
     * 定时任务持久层接口
     */
    @Autowired
    private CronjobRepository cronjobRepository;
    
    @Autowired
    private ImageService imageService;

    /**
     * 租户操作接口
     */
    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Override
    public Cronjob createCronjob(Cronjob cronjob) {
        // 0.检查租户是否存在
        Tenant tenant = tenantService.findTenantByTenantName(cronjob.getTenantName());
        if (tenant == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "指定的租户不存在");
        }
        // 1.验证任务名称是否可用
        if (!cronjob.getName().matches(Global.CHECK_CRONJOB_NAME)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "定时任务名称不符合正则规范");
        }
        if (!validateCronjobName(cronjob.getTenantName(), cronjob.getName())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "定时任务名称已存在");
        }
        // 2.校验并转化定时计划
        if (StringUtils.isEmpty(cronjob.getSchedule())) {
            try {
                String schedule = validateAndTransferscheduleCh(cronjob.getScheduleCh());
                cronjob.setSchedule(schedule);
            } catch (Exception e) {
                LOG.error("定时计划不符合规范，无法转化为cron表达式 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "定时执行计划不符合规范");
            }
        }

        // 3.保存相关信息到数据库
        try {
            cronjob = cronjobRepository.save(cronjob);
        } catch (Exception e) {
            LOG.error("定时任务保存失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "定时任务保存失败");
        }

        return cronjob;
    }

    @Override
    public boolean startCronjob(String cronjobId) {
        // 1.校验任务存在与否及状态的正确性
        Cronjob cronjob = getCronjobById(cronjobId);
        byte status = cronjob.getStatus();
        if (!(status == Global.OPERATION_UNSTART || status == Global.OPERATION_STOPPED
                || status == Global.OPERATION_START_FAILED)) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CRONJOBE_NOT_ALLOWED_FAILED, "定时任务状态不匹配，无法执行相关操作");
        }
        // 2.根据数据库信息组装k8s资源对象
        io.fabric8.kubernetes.api.model.batch.CronJob k8sCronjob = generateK8SCronJob(cronjob);
        if (k8sCronjob == null) {
            throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_CRONJOB_FAILED, "组装资源对象cronjob失败");
        }
        // 3.调用k8s接口创建cronjob
        try {
            k8sCronjob = KubernetesClientFactory.getClient().batch().cronjobs().inNamespace(cronjob.getTenantName())
                    .createOrReplace(k8sCronjob);
        } catch (Exception e) {
            LOG.error("启动定时任务失败 ", e);
            try {
                cronjob.setStatus(Global.OPERATION_START_FAILED);
                cronjobRepository.save(cronjob);
            } catch (Exception e1) {
                LOG.error("更新定时任务状态失败 ", e);
            }
            throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_CRONJOB_FAILED, "创建cronjob失败");
        }
        // 4.保存相关信息到数据库中
        try {
            cronjob.setStatus(Global.OPERATION_RUNNING);
            cronjobRepository.save(cronjob);
        } catch (Exception e) {
            LOG.error("更新定时任务状态失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "定时任务状态更新失败");
        }
        return true;
    }

    @Override
    public boolean stopCronjob(String cronjobId) {
        // 1.校验任务存在与否及状态的正确性
        Cronjob cronjob = getCronjobById(cronjobId);
        byte status = cronjob.getStatus();
        if (status != Global.OPERATION_RUNNING) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CRONJOBE_NOT_ALLOWED_FAILED, "定时任务状态不匹配，无法执行相关操作");
        }
        // 2.调用k8s接口删除cronjob(及job)
        try {
            KubernetesClientFactory.getClient().batch().cronjobs().inNamespace(cronjob.getTenantName())
                    .withName(cronjob.getName()).cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除资源对象cronjob失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_CRONJOB_FAILED, "删除cronjob失败");
        }
        // 3.保存相关信息到数据库中
        try {
            cronjob.setStatus(Global.OPERATION_STOPPED);
            cronjob = cronjobRepository.save(cronjob);
        } catch (Exception e) {
            LOG.error("更新定时任务状态失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "定时任务状态更新失败");
        }
        return true;
    }

    @Override
    public boolean deleteCronjob(String cronjobId) {
        // 1.校验任务存在与否
        Cronjob cronjob = getCronjobById(cronjobId);
        // 2.调用k8s接口删除cronjob(及job)
        if (cronjob.getStatus() == Global.OPERATION_RUNNING) {
            try {
                KubernetesClientFactory.getClient().batch().cronjobs().inNamespace(cronjob.getTenantName())
                        .withName(cronjob.getName()).cascading(true).delete();
                Thread.sleep(3 * 1000);
                deleteJobsWithlabel(cronjob.getTenantName(), cronjob.getName());
            } catch (Exception e) {
                LOG.error("删除资源对象cronjob失败 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_CRONJOB_FAILED, "删除cronjob失败");
            }
        }
        // 3.删除定时任务记录
        try {
            cronjobRepository.delete(cronjob);
        } catch (Exception e) {
            LOG.error("删除定时任务或更新用户资源失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除定时任务失败");
        }
        return false;
    }

    @Override
    public Cronjob updateCronjob(Cronjob cronjob) {
        // 1.校验任务存在与否及状态的正确性
        Cronjob cronjobDB = getCronjobById(cronjob.getId());
        byte status = cronjobDB.getStatus();
        if (!(status == Global.OPERATION_UNSTART || status == Global.OPERATION_STOPPED
                || status == Global.OPERATION_START_FAILED)) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CRONJOBE_NOT_ALLOWED_FAILED, "定时任务状态不匹配，无法执行相关操作");
        }
        // 2.校验是否需要生成cron表达式
        if (StringUtils.isEmpty(cronjob.getSchedule()) && !StringUtils.isEmpty(cronjob.getScheduleCh())) {
            try {
                String schedule = validateAndTransferscheduleCh(cronjob.getScheduleCh());
                cronjob.setSchedule(schedule);
            } catch (Exception e) {
                LOG.error("定时计划不符合规范，无法转化为cron表达式 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "无效的定时执行计划");
            }
        }

        // 3.保存相关信息到数据库中
        try {
            cronjob.setUpdateTime(new Date());
            cronjob = cronjobRepository.save(cronjob);
        } catch (Exception e) {
            LOG.error("更新定时任务信息失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新定时任务信息失败");
        }
        return cronjob;
    }

    @Override
    public Cronjob getCronjobById(String cronjobId) {
        Cronjob cronjob = null;
        try {
            Optional<Cronjob> cron = cronjobRepository.findById(cronjobId);
            if (cron.isPresent()) {
                cronjob = cron.get();
            }
        } catch (Exception e) {
            LOG.error("查询定时任务失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询定时任务失败");
        }

        if (cronjob == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_RETURN_MORE_FAILED, "未查询到定时任务");
        }
        return cronjob;
    }

    @Override
    public Cronjob getCronjobByNameAndTenantName(String cronjobName, String tenantName) {
        Cronjob cronjob = null;
        try {
            cronjob = cronjobRepository.findByNameAndTenantName(cronjobName, tenantName);
        } catch (Exception e) {
            LOG.error("查询定时任务失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询定时任务失败");
        }

        if (cronjob == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_RETURN_MORE_FAILED, "未查询到定时任务");
        }
        return cronjob;
    }

    @Override
    public Page<Cronjob> getCronjobList(String tenantName, String cronjobName, String projectId, Pageable pageable) {
        if (StringUtils.isEmpty(cronjobName)) {
            cronjobName = "";
        }
        cronjobName = "%" + cronjobName + "%";
        try {
            if (StringUtils.isEmpty(projectId)) {
                return cronjobRepository.findByNameLikeAndTenantNameOrderByCreateTimeDesc(cronjobName, tenantName,
                        pageable);
            }
            return cronjobRepository.findByNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc(cronjobName,
                    tenantName, projectId, pageable);
        } catch (Exception e) {
            LOG.error("查询定时任务列表失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询定时任务列表失败");
        }
    }

    @Override
    public boolean validateCronjobName(String tenantName, String cronjobName) {
        io.fabric8.kubernetes.api.model.batch.CronJob k8sCronjob = null;
        // 数据库验重操作
        try {
            Cronjob cronjob = cronjobRepository.findByNameAndTenantName(cronjobName, tenantName);
            if (cronjob != null) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("查询定时任务失败", e);
        }
        // k8s验重操作
        try {
            k8sCronjob = KubernetesClientFactory.getClient().batch().cronjobs().inNamespace(tenantName)
                    .withName(cronjobName).get();
        } catch (Exception e) {
            k8sCronjob = null;
        }
        if (k8sCronjob == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * <p>
     * Description: 将指定日期格式的输入转换为cron表达式
     * </p>
     *
     * @param scheduleCh
     *            待转换日期(X月X日X时X分 其中，高位不选择时X为0)
     * @return String cron表达式(五域格式输出)
     * @throws ErrorMessageException
     */
    private String validateAndTransferscheduleCh(String scheduleCh) {
        StringBuffer schedule = new StringBuffer();
        String month = "月";
        String day = "日";
        String hour = "时";
        String minute = "分";
        int two = 2;

        // 处理逻辑
        if (scheduleCh.contains(month)) {
            String scheduleTemp = scheduleCh.replace(month, " ").replace(day, " ").replace(hour, " ")
                    .replace(minute, " ").trim();
            String[] timeArr = scheduleTemp.split(" ");
            // 找出时间单位最大值不为0的值下标，替换为对应的 * 或 ？
            int index = -1;
            int length = timeArr.length;
            for (int i = 0; i < length - 1; i++) {
                if (Integer.parseInt(timeArr[i]) != 0) {
                    break;
                } else {
                    if (Integer.parseInt(timeArr[i + 1]) != 0) {
                        index = i;
                    }
                }
            }
            if (index > -1) {
                if (index == (length - two) && Integer.parseInt(timeArr[length - 1]) == 0) {
                    throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "定时计划输入不合法");
                }
                for (int i = 0; i < index + 1; i++) {
                    timeArr[i] = "*";
                }
            }
            // 拼接cron表达式
            for (int i = 0; i < length; i++) {
                schedule.append(timeArr[length - i - 1] + " ");
            }
            schedule.append("? ");
        } else {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "无法解析输入的定时计划");
        }

        return schedule.toString().trim();
    }

    /**
     *
     * <p>
     * Description: 根据数据库信息组装k8s资源对象--cronjob
     * </p>
     *
     * @param DBcronjob
     * @return K8SCronjob
     */
    private io.fabric8.kubernetes.api.model.batch.CronJob generateK8SCronJob(Cronjob cronjob) {
        io.fabric8.kubernetes.api.model.batch.CronJob k8sCronjob = new CronJob();
        String imageName = "";
        // 拼接镜像地址
        try {
            imageName = imageService.getRegistryImageName(cronjob.getImageVerisonId());
        } catch (Exception e) {
            LOG.error("获取当前镜像信息失败", e);
            return null;
        }
        // 初始化自定义启动命令
        List<String> args = new ArrayList<String>();
        List<String> command = new ArrayList<String>();
        boolean commandFlag = !StringUtils.isEmpty(cronjob.getCmd());
        if (commandFlag) {
            String[] startCommandArray = cronjob.getCmd().replaceAll("\\s+", " ").trim().split(" ");
            for (String item : startCommandArray) {
                if (command.isEmpty()) {
                    command.add(item);
                    continue;
                }
                args.add(item);
            }
        }
        ObjectMeta meta = new ObjectMeta();
        meta.setName(cronjob.getName());
        k8sCronjob.setMetadata(meta);
        CronJobSpec cronJobSpec = new CronJobSpec();
        cronJobSpec.setSchedule(cronjob.getSchedule());
        ObjectMeta jobTemplateMeta = new ObjectMeta();
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("relatedjob", cronjob.getName());
        jobTemplateMeta.setLabels(labels);
        JobTemplateSpec jobTemplate = new JobTemplateSpec();
        JobSpec jobSpec = new JobSpec();
        PodTemplateSpec template = new PodTemplateSpec();
        PodSpec podSpec = new PodSpec();
        List<Container> containers = new ArrayList<Container>();
        Container container = new Container();
        container.setName(cronjob.getName());
        container.setImage(imageName);
        ResourceRequirements requirements = new ResourceRequirements();
        requirements.getLimits();
        Map<String, Quantity> request = new HashMap<String, Quantity>(16);
        request.put("cpu", new Quantity((cronjob.getCpu()
                / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTCPU))) + ""));
        request.put("memory",
                new Quantity(cronjob.getMemory()
                        / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTMEMORY))
                        + "Gi"));
        requirements.setRequests(request);
        Map<String, Quantity> limit = new HashMap<String, Quantity>(16);
        limit.put("cpu", new Quantity(cronjob.getCpu() + ""));
        limit.put("memory", new Quantity(cronjob.getMemory() + "Gi"));
        requirements.setLimits(limit);
        container.setResources(requirements);
        if (commandFlag) {
            container.setCommand(command);
            container.setArgs(args);
        }
        containers.add(container);
        podSpec.setContainers(containers);
        podSpec.setRestartPolicy("OnFailure");
        template.setSpec(podSpec);

        jobSpec.setTemplate(template);
        jobTemplate.setMetadata(jobTemplateMeta);
        jobTemplate.setSpec(jobSpec);
        cronJobSpec.setJobTemplate(jobTemplate);
        // 设置成功或失败记录保存条数上限为100
        cronJobSpec.setFailedJobsHistoryLimit(100);
        cronJobSpec.setSuccessfulJobsHistoryLimit(100);
        k8sCronjob.setSpec(cronJobSpec);

        return k8sCronjob;
    }

    @Override
    public List<JobInfoModel> getRelatedJobs(String cronjobId) {
        List<JobInfoModel> jobInfos = new ArrayList<JobInfoModel>(10);
        // 1.校验任务存在与否及状态的正确性
        Cronjob cronjob = getCronjobById(cronjobId);
        byte status = cronjob.getStatus();
        if (status != Global.OPERATION_RUNNING) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CRONJOBE_NOT_ALLOWED_FAILED, "定时任务状态不匹配，无法执行相关操作");
        }
        // 2.获取当前cronjob创建的job
        Map<String, String> labelSelector = new HashMap<String, String>(16);
        labelSelector.put("relatedjob", cronjob.getName());
        JobList jobList = null;
        try {
            jobList = KubernetesClientFactory.getClient().batch().jobs().inNamespace(cronjob.getTenantName())
                    .withLabels(labelSelector).list();
            for (Job job : jobList.getItems()) {
                JobInfoModel jobInfo = new JobInfoModel();
                jobInfo.setName(job.getMetadata().getName());
                jobInfo.setStartTime(DateUtil.parseDate(DateUtil.parseStandardDate(job.getStatus().getStartTime())));
                jobInfo.setEndTime(DateUtil.parseDate(DateUtil.parseStandardDate(job.getStatus().getCompletionTime())));
                List<JobCondition> jobConditions = job.getStatus().getConditions();
                if (jobConditions != null && jobConditions.size() > 0) {
                    jobInfo.setStatus(jobConditions.get(0).getType());
                    jobInfos.add(jobInfo);
                }
            }
        } catch (Exception e) {
            LOG.error("获取job信息失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_JOB_FAILED, "获取job信息失败");
        }

        return jobInfos;
    }

    /**
     *
     * <p>
     * Description: 根据label标签删除job
     * </p>
     *
     * @param namespace
     *            命名空间
     * @param cronjobName
     *            定时任务名称
     */
    private void deleteJobsWithlabel(String namespace, String cronjobName) {
        Map<String, String> labelSelector = new HashMap<String, String>(16);
        labelSelector.put("relatedjob", cronjobName);

        JobList jobList = null;
        try {
            jobList = KubernetesClientFactory.getClient().batch().jobs().inNamespace(namespace)
                    .withLabels(labelSelector).list();
        } catch (Exception e) {
            LOG.error("根据label标签获取joblist失败", e);
        }

        if (null != jobList) {
            for (Job job : jobList.getItems()) {
                KubernetesClientFactory.getClient().batch().jobs().inNamespace(namespace)
                        .withName(job.getMetadata().getName()).cascading(true).delete();
            }
        }
    }

}
