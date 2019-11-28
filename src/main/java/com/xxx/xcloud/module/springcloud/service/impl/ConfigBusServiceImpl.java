package com.xxx.xcloud.module.springcloud.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.ceph.entity.CephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.repository.ServiceAndCephFileRepository;
import com.xxx.xcloud.module.ceph.service.CephFileService;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudServiceRepository;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.CephFSVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateRunning;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;


/**
 * @ClassName: ConfigBusServiceImpl
 * @Description: SpringCloud ConfigBus组件接口实现类
 * @author lnn
 * @date 2019年11月26日
 *
 */
@org.springframework.stereotype.Service
public class ConfigBusServiceImpl extends BaseSpringClouldService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigBusServiceImpl.class);

    @Autowired
    private SpringCloudServiceRepository springCloudServiceRepository;

    @Autowired
    private CephFileService cephFileService;

    @Autowired
    private ServiceAndCephFileRepository serviceAndCephFileRepository;

    @Override
    public ApiResult create(String serviceId) {
        LOG.info("创建操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String version = springCloudService.getVersion();
        Double cpu = springCloudService.getCpu() / 2;
        Double memory = springCloudService.getMemory() / 2;
        // Double storage = springCloudService.getStorage() / 2;
        String nodeNames = springCloudService.getNodeNames();
        String eurekaUrl = null;
        String cephfileId = springCloudService.getCephfileId();
        CephFile cephFile = null;
        try {
            cephFile = cephFileService.get(cephfileId);
        } catch (Exception e) {
            LOG.error("获取id为" + cephfileId + "的文件存储失败", e);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_CEPH_NOT_FOUND, springCloudService, "获取id为" + cephfileId + "的文件存储失败");
        }
        if (cephFile == null) {
            LOG.error("id为" + cephfileId + "的文件存储卷不存在");
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_CEPH_NOT_FOUND, springCloudService, "id为" + cephfileId + "的文件存储卷不存在");
        } else {
            if (!springCloudService.getTenantName().equals(cephFile.getTenantName())) {
                LOG.error("不允许跨租户操作文件存储资源");
                Boolean updateServiceState = updateServiceState(springCloudService,
                        SpringCloudCommonConst.STATE_APP_FAILED);
                if (!updateServiceState) {
                    return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, springCloudService,
                            "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                }
                return new ApiResult(ReturnCode.CODE_OPT_CROSS_TENANT_NOT_ALLOWED, springCloudService,
                        "不允许跨租户操作文件存储资源");
            }
        }
        try {
            SpringCloudService springCloudServiceEureka = springCloudServiceRepository
                    .findByAppIdAndAppType(springCloudService.getAppId(), SpringCloudCommonConst.APPTYPE_EUREKA);
            eurekaUrl = springCloudServiceEureka.getInteriorUrl();
        } catch (Exception e) {
            LOG.error("查询Eureka失败, error: ", e);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService, "查询Eureka失败");
        }
        try {
            // 保存SpringCloud-configbus和ceph存储卷关联表
            ServiceAndCephFile serviceAndCephFile = new ServiceAndCephFile();
            serviceAndCephFile.setCephFileId(cephfileId);
            serviceAndCephFile.setServiceId(serviceId);
            serviceAndCephFile.setMountPath(SpringCloudCommonConst.CEPHFS_MOUNTPATH);
            serviceAndCephFile.setServiceType(Global.MOUNTCEPHFILE_CONFIGBUS);
            serviceAndCephFileRepository.save(serviceAndCephFile);
        } catch (Exception e) {
            LOG.error("保存SpringCloud-configbus和ceph存储卷关联表失败, error: ", e);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, springCloudService,
                    "保存SpringCloud-configbus和ceph存储卷关联表失败");
        }
        // 创建存储卷
        /*
         * String cephName = SpringCloudCommonConst.APPTYPE_CONFIG_BUS + "_" +
         * StringUtils.randomPassword(4);
         * LOG.info("------------cephName-------------" + cephName); try {
         * CephFile cephFile = cephFileService.add(tenantName,
         * springCloudService.getCreatedBy(), springCloudService.getProjectId(),
         * cephName, springCloudService.getStorage(), null);
         * springCloudService.setCephfileId(cephFile.getId()); } catch
         * (AbstractCephException e) { LOG.error("存储卷创建文件夹失败, error: ", e);
         * Boolean updateServiceState = updateServiceState(springCloudService,
         * SpringCloudCommonConst.STATE_APP_FAILED); if (!updateServiceState) {
         * return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
         * springCloudService, "数据库连接异常 ，无法更新服务: " + serviceName + "的状态"); }
         * return new ApiResult(e.getCode(), springCloudService,
         * e.getMessage()); }
         */
        // cephfs存储卷地址
        // 获取镜像地址
        String configBusImage = null;
        String rabbitmqImage = null;
        try {
            configBusImage = getRepoPath(SpringCloudCommonConst.APPTYPE_CONFIG_BUS, null, version);
            rabbitmqImage = getRepoPath(SpringCloudCommonConst.APPTYPE_CONFIG_BUS,
                    SpringCloudCommonConst.APPTYPE_RABBITMQ, version);
        } catch (Exception e) {
            LOG.error("数据库连接异常，无法获取镜像信息, error: ", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService, "数据库连接异常，无法获取镜像信息");

        }
        if (null == configBusImage) {
            LOG.error("未查询到" + SpringCloudCommonConst.APPTYPE_CONFIG_BUS + "镜像信息");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, springCloudService,
                    "未查询到" + SpringCloudCommonConst.APPTYPE_CONFIG_BUS + "镜像信息");
        }
        if (null == rabbitmqImage) {
            LOG.error("未查询到" + SpringCloudCommonConst.APPTYPE_RABBITMQ + "镜像信息");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, springCloudService,
                    "未查询到" + SpringCloudCommonConst.APPTYPE_RABBITMQ + "镜像信息");
        }
        // 组装资源对象service、deployment
        io.fabric8.kubernetes.api.model.Service service = null;
        Deployment deployment = null;
        try {
            service = generateK8SService(serviceName, tenantName);
            deployment = generateK8SDeployment(serviceName, tenantName, cpu, memory, nodeNames, eurekaUrl,
                    cephFile.getName(), configBusImage, rabbitmqImage);
        } catch (Exception e) {
            LOG.error("组装资源对象失败, error: ", e);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, springCloudService, "组装资源对象失败");
        }
        if (!createSvc(tenantName, service)) {
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService, "创建Service失败");
        }
        if (!createDeployment(tenantName, deployment)) {
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, springCloudService, "创建Deployment失败");
        }
        if (!checkRunning(tenantName, serviceName, springCloudService)) {
            deleteDeployment(tenantName, serviceName);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_OPT_OVERTIME_FAILED, springCloudService,
                    "服务: " + serviceName + "创建失败");
        }

        Boolean updateServiceState = updateServiceState(springCloudService, SpringCloudCommonConst.STATE_APP_RUNNING);
        if (!updateServiceState) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                    "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "服务:" + serviceName + "创建成功");
    }

    @Override
    public ApiResult start(String serviceId) {
        LOG.info("启动操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String version = springCloudService.getVersion();
        Double cpu = springCloudService.getCpu() / 2;
        Double memory = springCloudService.getMemory() / 2;
        // Double storage = springCloudService.getStorage() / 2;
        String nodeNames = springCloudService.getNodeNames();
        String eurekaUrl = null;
        try {
            SpringCloudService springCloudServiceEureka = springCloudServiceRepository
                    .findByAppIdAndAppType(springCloudService.getAppId(), SpringCloudCommonConst.APPTYPE_EUREKA);
            eurekaUrl = springCloudServiceEureka.getExternalUrl();
        } catch (Exception e) {
            LOG.error("查询Eureka信息失败, error: ", e);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService, "查询Eureka信息失败");
        }
        // cephfs存储卷地址
        CephFile cephFile = null;
        try {
            cephFile = cephFileService.get(springCloudService.getCephfileId());
        } catch (Exception e1) {
            LOG.error("停止springcloud应用，获取存储卷名称失败, error: ", e1);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService, "获取存储卷名称失败");
        }

        if (null == cephFile) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                    "存储卷不存在，存储ID：" + springCloudService.getCephfileId());
        }
        String cephPath = cephFile.getName();
        // 获取镜像地址
        String configBusImage = null;
        String rabbitmqImage = null;
        try {
            configBusImage = getRepoPath(SpringCloudCommonConst.APPTYPE_CONFIG_BUS, null, version);
            rabbitmqImage = getRepoPath(SpringCloudCommonConst.APPTYPE_CONFIG_BUS,
                    SpringCloudCommonConst.APPTYPE_RABBITMQ, version);
        } catch (Exception e) {
            LOG.error("数据库连接异常，无法获取镜像信息, error: ", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService, "数据库连接异常，无法获取镜像信息");
        }
        if (null == configBusImage) {
            LOG.error("未查询到" + SpringCloudCommonConst.APPTYPE_CONFIG_BUS + "镜像信息");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, springCloudService,
                    "未查询到" + SpringCloudCommonConst.APPTYPE_CONFIG_BUS + "镜像信息");
        }
        if (null == rabbitmqImage) {
            LOG.error("未查询到" + SpringCloudCommonConst.APPTYPE_RABBITMQ + "镜像信息");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, springCloudService,
                    "未查询到" + SpringCloudCommonConst.APPTYPE_RABBITMQ + "镜像信息");
        }
        // 组装资源对象 deployment
        Deployment deployment = null;
        try {
            deployment = generateK8SDeployment(serviceName, tenantName, cpu, memory, nodeNames, eurekaUrl, cephPath,
                    configBusImage, rabbitmqImage);
        } catch (Exception e) {
            LOG.error("组装资源对象失败, error: ", e);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "组装资源对象失败");
        }
        if (!createDeployment(tenantName, deployment)) {
            LOG.error("创建Deployment失败");
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, springCloudService, "创建Deployment失败");
        }
        if (!checkRunning(tenantName, serviceName, springCloudService)) {
            deleteDeployment(tenantName, serviceName);
            Boolean updateServiceState = updateServiceState(springCloudService,
                    SpringCloudCommonConst.STATE_APP_FAILED);
            if (!updateServiceState) {
                return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_OPT_OVERTIME_FAILED, springCloudService,
                    "服务: " + serviceName + "启动失败");
        }
        Boolean updateServiceState = updateServiceState(springCloudService, SpringCloudCommonConst.STATE_APP_RUNNING);
        if (!updateServiceState) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                    "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "服务:" + serviceName + "启动成功");
    }

    @Override
    @Transactional
    public ApiResult delete(String serviceId) {
        LOG.info("删除操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }

        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        if (StringUtils.isNotEmpty(springCloudService.getCephfileId())) {
            try {
                // cephFileService.delete(springCloudService.getCephfileId());
                serviceAndCephFileRepository.deleteByCephFileIdAndServiceId(springCloudService.getCephfileId(),
                        serviceId);
            } catch (Exception e) {
                LOG.error("存储卷关联表删除失败, error: ", e);
                Boolean updateServiceState = updateServiceState(springCloudService,
                        SpringCloudCommonConst.STATE_APP_FAILED);
                if (!updateServiceState) {
                    return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, springCloudService,
                            "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                }
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_DELETE_FAILED, springCloudService, e.getMessage());
            }
        }
        boolean result = false;
        try {
            // 检查deployment资源是否存在，如果存在，就删除deployment
            result = deleteDeployment(tenantName, serviceName);
            // 删除svc资源
            result = deleteSvc(tenantName, serviceName);

            // 修改状态
            if (result) {
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_DELETED);
            } else {
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
            }
            springCloudService = springCloudServiceRepository.save(springCloudService);
        } catch (Exception e) {
            LOG.error("删除deployment或svc对象失败", e);
            try {
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
            } catch (Exception e1) {
                LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, null,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, null, "停止deployment或svc对象失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "删除服务成功");
    }

    /**
     * Descption: 组装k8s对象--deployment
     * 
     * @param serviceName
     * @param tenantName
     * @param cpu
     * @param memory
     * @param storage
     * @param nodeName
     * @param eurekaUrl
     * @param mountPath
     * @param monitors
     * @param cephPath
     * @param configBusImage
     * @param rabbitmqImage
     * @return Deployment
     */
    private Deployment generateK8SDeployment(String serviceName, String tenantName, Double cpu, Double memory,
            String nodeName, String eurekaUrl, String cephPath, String configBusImage, String rabbitmqImage) {
        Deployment k8sDeployment = new Deployment();
        // deployment.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceName);
        meta.setNamespace(tenantName);
        Map<String, String> labels = new HashMap<String, String>(10);
        labels.put("serviceName", serviceName);
        meta.setLabels(labels);
        k8sDeployment.setMetadata(meta);
        // deployment.spec
        DeploymentSpec spec = new DeploymentSpec();
        spec.setReplicas(1);
        LabelSelector labelSelector = new LabelSelector();
        Map<String, String> matchLabels = new HashMap<String, String>(10);
        matchLabels.put("serviceName", serviceName);
        labelSelector.setMatchLabels(matchLabels);
        spec.setSelector(labelSelector);
        // deployment.spec.template
        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        ObjectMeta podTemplateMeta = new ObjectMeta();
        podTemplateMeta.setName(serviceName);
        podTemplateMeta.setNamespace(tenantName);
        Map<String, String> podTemplateLabels = new HashMap<String, String>(10);
        podTemplateLabels.put("serviceName", serviceName);
        podTemplateMeta.setLabels(podTemplateLabels);
        podTemplateSpec.setMetadata(podTemplateMeta);

        PodSpec podSpec = generateK8SPodSpec(serviceName, tenantName, cpu, memory, nodeName, eurekaUrl, configBusImage,
                rabbitmqImage);
        podTemplateSpec.setSpec(podSpec);
        spec.setTemplate(podTemplateSpec);
        k8sDeployment.setSpec(spec);
        // ceph存储卷
        mountCephFile(k8sDeployment, cephPath, tenantName, serviceName);

        return k8sDeployment;
    }

    /**
     * Description: 组装k8s对象-- CephFile
     * 
     * @param k8sDeployment
     * @param mountPath
     * @param cephfsMonitor
     * @param cephPath
     * @param tenantName
     * @param serviceName
     *            void
     */
    private Deployment mountCephFile(Deployment k8sDeployment, String cephPath, String tenantName, String serviceName) {

        PodSpec podSpec = k8sDeployment.getSpec().getTemplate().getSpec();
        List<Volume> volumes = new ArrayList<Volume>(10);
        List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>(10);

        Volume volume = new Volume();
        volume.setName("cephfs");
        CephFSVolumeSource cephfs = new CephFSVolumeSource();
        List<String> monitors = new ArrayList<String>(10);
        String[] cephMonitors = XcloudProperties.getConfigMap().get(Global.CEPH_MONITOR).split(",");
        for (String cephMonitor : cephMonitors) {
            monitors.add(cephMonitor);
        }
        cephfs.setMonitors(monitors);
        cephfs.setPath(tenantName + "/" + cephPath);
        cephfs.setUser("admin");
        LocalObjectReference secretRef = new LocalObjectReference();
        secretRef.setName("ceph-secret");
        cephfs.setSecretRef(secretRef);
        cephfs.setReadOnly(false);
        volume.setCephfs(cephfs);
        volumes.add(volume);

        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setMountPath(SpringCloudCommonConst.CEPHFS_MOUNTPATH);
        volumeMount.setName("cephfs");
        volumeMounts.add(volumeMount);

        podSpec.setVolumes(volumes);
        List<Container> containers = podSpec.getContainers();
        for (Container container : containers) {
            String containerName = container.getName();
            if (SpringCloudCommonConst.APPTYPE_CONFIG_BUS.equals(containerName)) {
                container.setVolumeMounts(volumeMounts);
            }
        }

        return k8sDeployment;

    }

    /**
     * Description: 组装k8s对象--Containers
     * 
     * @param serviceName
     * @param tenantName
     * @param cpu
     * @param memory
     * @param storage
     * @param nodeName
     * @param eurekaUrl
     * @param imagePath
     * @param configBusImage
     * @param rabbitmqImage
     * @return PodSpec
     */
    private PodSpec generateK8SPodSpec(String serviceName, String tenantName, Double cpu, Double memory,
            String nodeName, String eurekaUrl, String configBusImage, String rabbitmqImage) {
        PodSpec podSpec = new PodSpec();

        // 添加用来区分服务的主机标签操作
        /*
         * List<SelectorLabel> labelList =
         * selectorLabelRepository.findByTypeAndEnableTrue(Global.
         * SELECTOR_LABEL_SERVICE); if (labelList != null && labelList.size() >
         * 0) { Map<String, String> nodeSelector = new HashMap<String,
         * String>(16); for (SelectorLabel label : labelList) {
         * nodeSelector.put(label.getLabelKey(), label.getLabelValue()); }
         * podSpec.setNodeSelector(nodeSelector); }
         */

        List<Container> containerList = new ArrayList<Container>(2);
        // BusConfig容器配置
        Container containerConfig = new Container();
        containerConfig.setName(SpringCloudCommonConst.APPTYPE_CONFIG_BUS);
        containerConfig.setImage(configBusImage);
        containerConfig.setImagePullPolicy("Always");
        List<EnvVar> envVars = new ArrayList<EnvVar>(10);
        EnvVar envVar = new EnvVar();
        envVar.setName(SpringCloudCommonConst.DEFAULT_ENV_JAVA_OPTS);
        envVar.setValue("--spring.application.name=" + nodeName + " " + "--eureka.instance.hostname=" + nodeName + " "
                + "--spring.rabbitmq.host=" + nodeName + " " + "--spring.cloud.config.server.native.search-locations="
                + SpringCloudCommonConst.CEPHFS_MOUNTPATH + " " + "--eureka.client.serviceUrl.defaultZone=" + eurekaUrl
                + " " + SpringCloudCommonConst.DEFAULT_CONFIGBUS_ENV_VALUE);
        envVars.add(envVar);
        containerConfig.setEnv(envVars);
        ResourceRequirements requirements = new ResourceRequirements();
        Map<String, Quantity> request = new HashMap<String, Quantity>(16);
        request.put(SpringCloudCommonConst.RESOURCE_CPU, new Quantity(
                cpu / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTCPU)) + ""));
        request.put(SpringCloudCommonConst.RESOURCE_MEMORY,
                new Quantity(
                        memory / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTMEMORY))
                                + "Gi"));
        requirements.setRequests(request);
        Map<String, Quantity> limit = new HashMap<String, Quantity>(16);
        limit.put(SpringCloudCommonConst.RESOURCE_CPU, new Quantity(cpu + ""));
        limit.put(SpringCloudCommonConst.RESOURCE_MEMORY, new Quantity(memory + "Gi"));
        requirements.setLimits(limit);
        containerConfig.setResources(requirements);
        containerList.add(containerConfig);
        // RabbitMQ容器配置
        Container containerMq = new Container();
        containerMq.setName(SpringCloudCommonConst.APPTYPE_RABBITMQ);
        containerMq.setImage(rabbitmqImage);
        containerMq.setImagePullPolicy("Always");
        List<ContainerPort> ports = new ArrayList<ContainerPort>();
        ContainerPort containerPort1 = new ContainerPort();
        containerPort1.setContainerPort(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_ONE);
        containerPort1.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        ports.add(containerPort1);

        ContainerPort containerPort2 = new ContainerPort();
        containerPort2.setContainerPort(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_TWO);
        containerPort2.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        ports.add(containerPort2);

        containerMq.setPorts(ports);

        ResourceRequirements requirementsMq = new ResourceRequirements();
        Map<String, Quantity> requestMq = new HashMap<String, Quantity>(16);
        requestMq.put(SpringCloudCommonConst.RESOURCE_CPU, new Quantity(
                cpu / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTCPU)) + ""));
        requestMq.put(SpringCloudCommonConst.RESOURCE_MEMORY,
                new Quantity(
                        memory / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTMEMORY))
                                + "Gi"));
        requirementsMq.setRequests(requestMq);
        Map<String, Quantity> limitMq = new HashMap<String, Quantity>(16);
        limitMq.put(SpringCloudCommonConst.RESOURCE_CPU, new Quantity(cpu + ""));
        limitMq.put(SpringCloudCommonConst.RESOURCE_MEMORY, new Quantity(memory + "Gi"));
        requirementsMq.setLimits(limitMq);
        containerMq.setResources(requirementsMq);
        containerList.add(containerMq);

        podSpec.setContainers(containerList);

        List<LocalObjectReference> imagePullSecrets = new ArrayList<LocalObjectReference>(10);
        LocalObjectReference localObjectReference = new LocalObjectReference();
        localObjectReference.setName(SpringCloudCommonConst.HARBOR_SECRET);
        imagePullSecrets.add(localObjectReference);
        podSpec.setImagePullSecrets(imagePullSecrets);

        return podSpec;
    }

    /**
     * Descption: 组装k8s对象-- service
     * 
     * @param serviceName
     * @param tenantName
     * @return Service
     */
    private Service generateK8SService(String serviceName, String tenantName) {
        io.fabric8.kubernetes.api.model.Service k8sService = new io.fabric8.kubernetes.api.model.Service();
        // service.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceName);
        meta.setNamespace(tenantName);
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("serviceName", serviceName);
        meta.setLabels(labels);
        k8sService.setMetadata(meta);
        // service.spec
        ServiceSpec spec = new ServiceSpec();
        // 默认使用会话粘连
        // spec.setSessionAffinity(SpringCloudCommonConst.K8S_SERVICE_TYPE_CLUSTERIP);
        Map<String, String> selector = new HashMap<String, String>(16);
        selector.put("serviceName", serviceName);
        spec.setSelector(selector);
        // 访问方式为NodePort
        spec.setType(SpringCloudCommonConst.K8S_SERVICE_TYPE_NODEPORT);
        List<ServicePort> ports = new ArrayList<ServicePort>(10);
        // 指定的暴露端口
        ServicePort servicePort = new ServicePort();
        servicePort.setName("configserver");
        servicePort.setPort(SpringCloudCommonConst.DEFAULT_CONFIGBUS_CONTAINERPORT);
        servicePort.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        IntOrString targetPort = new IntOrString(SpringCloudCommonConst.DEFAULT_CONFIGBUS_CONTAINERPORT);
        servicePort.setTargetPort(targetPort);
        ServicePort servicePort1 = new ServicePort();
        servicePort1.setName("mq");
        servicePort1.setPort(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_ONE);
        servicePort1.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        IntOrString targetPort1 = new IntOrString(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_ONE);
        servicePort1.setTargetPort(targetPort1);
        ServicePort servicePort2 = new ServicePort();
        servicePort2.setName("mq1");
        servicePort2.setPort(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_TWO);
        servicePort2.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        IntOrString targetPort2 = new IntOrString(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_TWO);
        servicePort2.setTargetPort(targetPort2);

        ports.add(servicePort);
        ports.add(servicePort1);
        ports.add(servicePort2);
        spec.setPorts(ports);
        k8sService.setSpec(spec);
        return k8sService;
    }

    /**
     * Description: 检查实例是否运行
     * 
     * @param tenantName
     * @param serviceName
     * @param springCloudService
     * @return boolean
     */
    private boolean checkRunning(String tenantName, String serviceName, SpringCloudService springCloudService) {
        Map<String, String> labelSelector = new HashMap<String, String>(1);
        labelSelector.put("serviceName", serviceName);
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(3000);
                PodList podList = getPodList(tenantName, serviceName);
                if (podList == null || podList.getItems().size() == 0) {
                    return false;
                } else {
                    for (Pod pod : podList.getItems()) {
                        if (SpringCloudCommonConst.STATE_APP_RUNNING.equals(pod.getStatus().getPhase())) {
                            List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                            ContainerStatus containerStatus1 = containerStatuses.get(0);
                            ContainerState state1 = containerStatus1.getState();
                            ContainerStateRunning running1 = null;
                            running1 = state1.getRunning();
                            ContainerStatus containerStatus2 = containerStatuses.get(0);
                            ContainerState state2 = containerStatus2.getState();
                            ContainerStateRunning running2 = null;
                            running2 = state2.getRunning();
                            LOG.info("---------running1------" + running1 + "--------running2-------" + running2);
                            LOG.info("--------containerStatuses-------" + JSON.toJSONString(containerStatuses));
                            if (null != running1 && null != running2) {
                                try {
                                    Service svc = getSvc(tenantName, serviceName);
                                    if (null != svc && null != podList) {
                                        Integer nodePort = 0;
                                        Integer mqNodePort = 0;
                                        Integer mqUiNodePort = 0;
                                        for (ServicePort servicePort : svc.getSpec().getPorts()) {
                                            if (SpringCloudCommonConst.DEFAULT_CONFIGBUS_CONTAINERPORT == servicePort
                                                    .getPort()) {
                                                nodePort = servicePort.getNodePort();
                                            }
                                            if (SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_ONE == servicePort
                                                    .getPort()) {
                                                mqNodePort = servicePort.getNodePort();
                                            }
                                            if (SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_TWO == servicePort
                                                    .getPort()) {
                                                mqUiNodePort = servicePort.getNodePort();
                                            }
                                            
                                        }
                                        String hostIP = podList.getItems().get(0).getStatus().getHostIP();

                                        JSONObject external = new JSONObject();
                                        external.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_CONFIG_URI,
                                                "http://" + hostIP + ":" + nodePort);
                                        external.put(
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_HOST,
                                                hostIP);
                                        external.put(
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PORT,
                                                mqNodePort);
                                        external.put(
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_UI_PORT,
                                                mqUiNodePort);
                                        external.put(
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_USER,
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_USER);
                                        external.put(
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PASSWORD,
                                                SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_PASSWORD);

                                        springCloudService.setExternalUrl(JSON.toJSONString(external));
                                    }
                                } catch (Exception e) {
                                    LOG.error("获取访问IP地址失败, error: ", e);
                                }
                                return true;
                            }
                        }
                    }
                }
                long endTime = System.currentTimeMillis();
                LOG.info("服务" + serviceName + "启动用时： " + (System.currentTimeMillis() - startTime));
                if (endTime - startTime > 300000) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

}
