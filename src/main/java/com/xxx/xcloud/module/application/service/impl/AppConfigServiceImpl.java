package com.xxx.xcloud.module.application.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceContainerLifecycle;
import com.xxx.xcloud.module.application.entity.ServiceHealth;
import com.xxx.xcloud.module.application.entity.ServiceHostpath;
import com.xxx.xcloud.module.application.exception.ConfigMapException;
import com.xxx.xcloud.module.application.repository.ServiceContainerLifecycleRepository;
import com.xxx.xcloud.module.application.repository.ServiceHealthRepository;
import com.xxx.xcloud.module.application.repository.ServiceHostpathRepository;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.application.service.IAppConfigService;
import com.xxx.xcloud.module.application.service.IAppDetailService;
import com.xxx.xcloud.module.ceph.entity.CephFile;
import com.xxx.xcloud.module.ceph.entity.CephRbd;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.ceph.service.CephFileService;
import com.xxx.xcloud.module.ceph.service.CephRbdService;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;
import com.xxx.xcloud.module.configmap.service.ConfigService;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.CrossVersionObjectReference;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscalerSpec;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.apps.Deployment;

/**
 * @ClassName: ApplicationConfigServiceImpl
 * @Description: 服务配置项
 * @author zyh
 * @date 2019年11月12日
 *
 */
@org.springframework.stereotype.Service
public class AppConfigServiceImpl implements IAppConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(AppConfigServiceImpl.class);

    public static final int NUMBER_TWO = 2;

    @Autowired
    private ConfigService configService;
    @Autowired
    private CephRbdService cephRbdService;
    @Autowired
    private CephFileService cephFileService;
    @Autowired
    private IAppDetailService appDetailService;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceHealthRepository serviceHealthRepository;
    @Autowired
    private ServiceHostpathRepository serviceHostpathRepository;
    @Autowired
    private ServiceContainerLifecycleRepository serviceContainerLifecycleRepository;

    @Override
    public Boolean updateServicePorts(Service service) {
        io.fabric8.kubernetes.api.model.Service k8sService = null;
        try {
            k8sService = KubernetesClientFactory.getClient().services().inNamespace(service.getTenantName())
                    .withName(service.getServiceName()).get();
        } catch (Exception e) {
            k8sService = null;
            LOG.error("获取svc失败", e);
        }
        if (k8sService != null) {
            List<ServicePort> ports = new ArrayList<ServicePort>(10);
            int i = 0;
            if (!StringUtils.isEmpty(service.getPortAndProtocol())) {
                // 获取用户指定的暴露端口
                Map<String, String> portAndProtocol = JSON.parseObject(service.getPortAndProtocol(),
                        new TypeReference<Map<String, String>>() {});
                Set<Entry<String, String>> entries = portAndProtocol.entrySet();
                for (@SuppressWarnings("rawtypes")
                Entry entry : entries) {
                    i++;
                    ServicePort servicePort = new ServicePort();
                    servicePort.setName("http" + i);
                    int port = Integer.parseInt(entry.getKey().toString());
                    servicePort.setPort(port);
                    servicePort.setProtocol(entry.getValue().toString().toUpperCase());
                    ports.add(servicePort);
                }
            } else {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "容器端口不能为空");
            }
            try {
                k8sService.getSpec().setPorts(ports);
                KubernetesClientFactory.getClient().services().inNamespace(service.getTenantName())
                        .withName(service.getServiceName()).replace(k8sService);
            } catch (Exception e) {
                LOG.error("更新资源对象SVC失败：", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_SERVICE_FAILED, "更新资源对象SVC失败");
            }

        }
        return true;
    }

    @Override
    public List<ServiceAndCephFile> getServiceCephFile(String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        List<ServiceAndCephFile> serviceAndCephFiles = null;
        try {
            serviceAndCephFiles = cephFileService.listMountInService(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "文件存储列表查询失败", e);
        }
        return serviceAndCephFiles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteServiceCephFile(String cephFileId, String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        setRestartFlagTrue(service);
        try {
            cephFileService.mountCancel(serviceId, cephFileId);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "卷存储删除失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED,
                    "服务:" + service.getServiceName() + "卷存储删除失败");
        }
        LOG.info("服务:" + service.getServiceName() + "卷存储删除成功");
        return true;
    }

    @Override
    public Boolean updateServiceCephFile(ServiceAndCephFile serviceCephFile) {

        Service service = appDetailService.getServiceById(serviceCephFile.getServiceId());
        setRestartFlagTrue(service);
        // 校验是否是当前用户下的文件存储资源
        CephFile cephFile = null;
        try {
            cephFile = cephFileService.get(serviceCephFile.getCephFileId());
        } catch (ErrorMessageException e) {
            LOG.error("获取id为" + serviceCephFile.getCephFileId() + "的文件存储失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, e.getMessage());
        }
        if (cephFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                    "id为" + serviceCephFile.getCephFileId() + "的文件存储卷不存在");
        } else {
            if (!service.getTenantName().equals(cephFile.getTenantName())) {
                throw new ErrorMessageException(ReturnCode.CODE_OPT_CROSS_TENANT_NOT_ALLOWED, "不允许跨租户操作文件存储资源");
            }
        }

        try {
            serviceCephFile.setServiceType(Global.MOUNTCEPHFILE_SERVICE);
            cephFileService.mountSave(serviceCephFile);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "文件存储保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "文件存储保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "文件存储保存成功");

        return true;
    }

    @Override
    public List<ServiceHostpath> getServiceHostpath(String serviceId) {

        List<ServiceHostpath> serviceHostpaths = null;
        Service service = appDetailService.getServiceById(serviceId);
        try {
            serviceHostpaths = serviceHostpathRepository.findByServiceId(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "本地存储查询失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "服务:" + service.getServiceName() + "本地存储查询失败");
        }
        return serviceHostpaths;
    }

    @Override
    public Boolean updateServiceHostpath(ServiceHostpath serviceHostpath) {

        Service service = appDetailService.getServiceById(serviceHostpath.getServiceId());
        if (null == service) {
            LOG.info("服务不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        }
        setRestartFlagTrue(service);
        try {
            serviceHostpathRepository.save(serviceHostpath);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "本地存储保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "本地存储保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "本地存储保存成功");
        return true;
    }

    @Override
    public Boolean deleteServiceHostpath(String serviceHostpathId, String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        setRestartFlagTrue(service);
        try {
            serviceHostpathRepository.deleteById(serviceHostpathId);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "本地存储删除失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED,
                    "服务:" + service.getServiceName() + "本地存储删除失败");
        }
        LOG.info("服务:" + service.getServiceName() + "本地存储删除成功");
        return true;
    }

    @Override
    public List<ServiceCephRbd> getServiceCephRbd(String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        List<ServiceCephRbd> serviceCephRbds = null;
        try {
            serviceCephRbds = cephRbdService.mountInService(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "块存储列表查询失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "服务:" + service.getServiceName() + "块存储列表查询失败");
        }
        return serviceCephRbds;
    }

    @Override
    public Boolean updateServiceCephRbd(ServiceCephRbd serviceCephRbd) {

        Service service = appDetailService.getServiceById(serviceCephRbd.getServiceId());
        if (null == service) {
            LOG.info("服务不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        }
        setRestartFlagTrue(service);
        if (1 != service.getInstance()) {
            LOG.info("服务:" + service.getServiceName() + "不为单实例服务,不能挂载块存储");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "服务:" + service.getServiceName() + "不为单实例服务,不能挂载块存储");
        }
        // 校验是否是当前用户下的块存储资源
        CephRbd cephRbd = null;
        try {
            cephRbd = cephRbdService.get(serviceCephRbd.getCephRbdId());
        } catch (Exception e) {
            LOG.error("获取id为" + serviceCephRbd.getId() + "的块存储失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, "当前块存储不存在");
        }
        if (cephRbd == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, "当前块存储不存在");
        } else {
            if (!service.getTenantName().equals(cephRbd.getTenantName())) {
                throw new ErrorMessageException(ReturnCode.CODE_OPT_CROSS_TENANT_NOT_ALLOWED, "不允许跨租户操作块存储资源");
            }
        }

        cephRbdService.mountSave(serviceCephRbd.getId(), serviceCephRbd.getServiceId(), serviceCephRbd.getCephRbdId(),
                serviceCephRbd.getMountPath());
        try {
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "挂载块存储时设置服务重新生效字段失败：", e);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteServiceCephRbd(String serviceCephRbdId, String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        setRestartFlagTrue(service);
        try {
            cephRbdService.mountCancel(serviceId, serviceCephRbdId);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "块存储删除失败", e);
            throw e;
        }
        LOG.info("服务:" + service.getServiceName() + "块存储删除成功");
        return true;
    }

    @Override
    public List<ServiceConfig> getServiceConfig(String serviceId) {
        List<ServiceConfig> serviceConfigs = null;
        try {
            serviceConfigs = configService.listMount(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + serviceId + "配置文件查询失败", e);
        }
        return serviceConfigs;
    }

    /**
     * 服务在启动中或运行中，设置服务重新生效字段为true
     * @Title: setRestartFlagTrue
     * @Description: 服务在启动中或运行中，设置服务重新生效字段为true
     * @param service void
     * @throws
     */
    private void setRestartFlagTrue(Service service) {
        if (Global.OPERATION_RUNNING == service.getStatus() || Global.OPERATION_STARTING == service.getStatus()
                || Global.OPERATION_UPDATING == service.getStatus()
                || Global.OPERATION_UPDATE_FAILED == service.getStatus()) {
            service.setIsRestartEffect(true);
        }
    }

    @Override
    public Boolean updateServiceConfig(ServiceConfig serviceConfig) throws ConfigMapException {

        Service service = appDetailService.getServiceById(serviceConfig.getServiceId());
        setRestartFlagTrue(service);
        configService.mountSaveSingleTemplate(serviceConfig);
        service = serviceRepository.save(service);
        LOG.info("服务:" + service.getServiceName() + "配置文件修改成功");
        return true;
    }

    @Override
    public Boolean deleteServiceConfig(String serviceConfigId, String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        setRestartFlagTrue(service);
        try {
            configService.mountCancelById(serviceConfigId);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "配置文件删除失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED,
                    "服务:" + service.getServiceName() + "配置文件删除失败");
        }
        LOG.info("服务:" + service.getServiceName() + "配置文件删除成功");
        return true;
    }

    @Override
    public Map<String, Object> getServiceEnv(String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        Map<String, Object> env = new HashMap<String, Object>(16);
        if (null == service.getEnv()) {
            env = null;
        } else {
            try {
                env = JSON.parseObject(service.getEnv(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                LOG.error("服务:" + service.getServiceName() + "环境变量获取成功", e);
                env = null;
            }
        }
        return env;
    }

    @Override
    public Boolean updateServiceEnv(String serviceId, Map<String, Object> env) {

        Service service = appDetailService.getServiceById(serviceId);
        setRestartFlagTrue(service);
        String jsonStringEnv = JSON.toJSONString(env);
        service.setEnv(jsonStringEnv);
        service.setUpdateTime(new Date());
        try {
            serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "环境变量保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "环境变量保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "环境变量保存成功");
        return true;
    }

    @Override
    public List<ServiceHealth> getServiceHealth(String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        List<ServiceHealth> serviceHealths = null;
        try {
            serviceHealths = serviceHealthRepository.findByServiceId(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "健康检查查询失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "服务:" + service.getServiceName() + "健康检查查询失败");
        }
        LOG.info("服务:" + service.getServiceName() + "健康检查查询成功");
        return serviceHealths;
    }

    @Override
    public Boolean updateServiceHealth(ServiceHealth serviceHealth) {

        Service service = appDetailService.getServiceById(serviceHealth.getServiceId());
        setRestartFlagTrue(service);
        List<ServiceHealth> serviceHealths = getServiceHealth(serviceHealth.getServiceId());
        if (serviceHealths != null && serviceHealths.size() > NUMBER_TWO) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "只允许服务:" + service.getServiceName() + "保存至多两条健康检查数据");
        }
        try {
            serviceHealthRepository.save(serviceHealth);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "健康检查保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "健康检查保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "健康检查保存成功");
        return true;
    }

    @Override
    public Boolean deleteServiceHealth(String serviceHealthId, String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        if (null == service) {
            LOG.info("服务不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        }
        setRestartFlagTrue(service);
        Optional<ServiceHealth> optional = null;
        try {
            optional = serviceHealthRepository.findById(serviceHealthId);
            if (!optional.isPresent()) {
                LOG.info("服务:" + service.getServiceName() + "健康检查不存在或已经删除");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "健康检查不存在或已经删除");
            }
            serviceHealthRepository.deleteById(serviceHealthId);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            if (!optional.isPresent()) {
                LOG.info("服务:" + service.getServiceName() + "健康检查不存在或已经删除");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "健康检查不存在或已经删除");
            } else {
                LOG.error("服务:" + service.getServiceName() + "健康检查删除失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED,
                        "服务:" + service.getServiceName() + "健康检查删除失败");
            }
        }
        LOG.info("服务:" + service.getServiceName() + "健康检查删除成功");
        return true;
    }

    @Override
    public List<ServiceContainerLifecycle> getServiceContainerLifecycle(String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        List<ServiceContainerLifecycle> serviceContainerLifecycles = null;
        try {
            serviceContainerLifecycles = serviceContainerLifecycleRepository.findByServiceId(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "容器生命周期查询失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "服务:" + service.getServiceName() + "容器生命周期查询失败");
        }
        LOG.info("服务:" + service.getServiceName() + "容器生命周期查询成功");
        return serviceContainerLifecycles;
    }

    @Override
    public Boolean updataServiceContainerLifecycle(ServiceContainerLifecycle serviceContainerLifecycle) {

        Service service = appDetailService.getServiceById(serviceContainerLifecycle.getServiceId());
        setRestartFlagTrue(service);
        List<ServiceContainerLifecycle> serviceContainerLifecycles = getServiceContainerLifecycle(
                serviceContainerLifecycle.getServiceId());
        if (serviceContainerLifecycles != null && serviceContainerLifecycles.size() > NUMBER_TWO) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "只允许服务:" + service.getServiceName() + "保存至多两条容器生命周期数据");
        }
        try {
            serviceContainerLifecycleRepository.save(serviceContainerLifecycle);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "容器生命周期保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "容器生命周期保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "容器生命周期保存成功");
        return true;
    }

    @Override
    public Boolean deleteServiceContainerLifecycle(String serviceContainerLifecycleId, String serviceId) {

        Service service = appDetailService.getServiceById(serviceId);
        if (null == service) {
            LOG.info("服务不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        }
        setRestartFlagTrue(service);
        Optional<ServiceContainerLifecycle> optional = null;
        try {
            optional = serviceContainerLifecycleRepository.findById(serviceContainerLifecycleId);
            if (!optional.isPresent()) {
                LOG.info("服务:" + service.getServiceName() + "容器生命周期不存在或已经删除");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "容器生命周期不存在或已经删除");
            }
            serviceContainerLifecycleRepository.deleteById(serviceContainerLifecycleId);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            if (!optional.isPresent()) {
                LOG.info("服务:" + service.getServiceName() + "容器生命周期不存在或已经删除");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "容器生命周期不存在或已经删除");
            } else {
                LOG.error("服务:" + service.getServiceName() + "容器生命周期删除失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED,
                        "服务:" + service.getServiceName() + "容器生命周期删除失败");
            }
        }
        LOG.info("服务:" + service.getServiceName() + "容器生命周期删除成功");
        return true;
    }

    @Override
    public boolean serviceAutomaticScale(String serviceId, Integer minReplicas, Integer maxReplicas,
            Integer cpuThreshold, Boolean isTurnOn) {

        Service service = appDetailService.getServiceById(serviceId);
        serviceIsRuningState(service);

        if (minReplicas > maxReplicas) {
            LOG.info("最小实例数不能大于最大实例数");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED, "最小实例数不能大于最大实例数");
        }

        /*
         * try { if (null == service.getProjectId() && null == service.getHpa())
         * { Double cpuUsed = (maxReplicas - service.getInstance()) *
         * service.getCpu(); Double memoryUsed = (maxReplicas -
         * service.getInstance()) * service.getMemory();
         * tenantService.updateUsedResource(service.getTenantName(), cpuUsed,
         * memoryUsed, 0d); } else { // 有项目的功能时 } } catch (Exception e) {
         * LOG.error("CPU或Memory资源不足", e); throw new
         *
         *
         * ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_QUOTA_INSUFFICIENT,
         * "CPU或Memory资源不足"); }
         */
        HorizontalPodAutoscaler horizontalPodAutoscaler = null;
        if (isTurnOn) {
            try {
                HorizontalPodAutoscaler hpa = new HorizontalPodAutoscaler();
                ObjectMeta meta = new ObjectMeta();
                meta.setName(service.getServiceName());
                hpa.setMetadata(meta);
                HorizontalPodAutoscalerSpec spec = new HorizontalPodAutoscalerSpec();
                spec.setMaxReplicas(maxReplicas);
                spec.setMinReplicas(minReplicas);
                CrossVersionObjectReference scaleTargetRef = new CrossVersionObjectReference();
                scaleTargetRef.setName(service.getServiceName());
                scaleTargetRef.setKind("Deployment");
                scaleTargetRef.setApiVersion("extensions/v1beta1");
                spec.setScaleTargetRef(scaleTargetRef);
                spec.setTargetCPUUtilizationPercentage(cpuThreshold);
                /*
                 * List<MetricSpec> metrics = new ArrayList<MetricSpec>();
                 * MetricSpec metricSpec = new MetricSpec();
                 * metricSpec.setType("Resource"); ResourceMetricSource
                 * resourceMetricSource = new ResourceMetricSource();
                 * resourceMetricSource.setName("CPU");
                 * resourceMetricSource.setTargetAverageUtilization(cpuThreshold
                 * ); metricSpec.setResource(resourceMetricSource);
                 * metrics.add(metricSpec); spec.setMetrics(metrics);
                 */
                hpa.setSpec(spec);
                horizontalPodAutoscaler = KubernetesClientFactory.getClient().autoscaling().horizontalPodAutoscalers()
                        .inNamespace(service.getTenantName()).withName(service.getServiceName()).createOrReplace(hpa);
            } catch (Exception e) {
                LOG.error("HPA:" + service.getServiceName() + "创建或修改失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_HPA_FAILED,
                        "服务:" + service.getServiceName() + "自动伸缩配置失败");
            }
        } else {
            try {
                KubernetesClientFactory.getClient().autoscaling().horizontalPodAutoscalers()
                        .inNamespace(service.getTenantName()).withName(service.getServiceName()).cascading(true)
                        .delete();
            } catch (Exception e) {
                LOG.error("HPA:" + service.getServiceName() + "删除失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_HPA_FAILED, "删除HPA失败");
            }
            try {
                // 使用hpa后k8s自动更新deployment的副本数，故而需要重置
                Deployment deployment = KubernetesClientFactory.getClient().apps().deployments()
                        .inNamespace(service.getTenantName()).withName(service.getServiceName()).get();
                deployment.getSpec().setReplicas(service.getInstance());
                KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                        .withName(service.getServiceName()).replace(deployment);
            } catch (Exception e) {
                LOG.error("删除HPA后更新Deployment副本数失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, "删除HPA后更新Deployment副本数失败");
            }
        }

        if (null != horizontalPodAutoscaler || !isTurnOn) {
            try {
                Map<String, Object> hpaMap = new HashMap<String, Object>(16);
                hpaMap.put("cpuThreshold", cpuThreshold);
                hpaMap.put("maxReplicas", maxReplicas);
                hpaMap.put("minReplicas", minReplicas);
                hpaMap.put("isTurnOn", isTurnOn);
                service.setHpa(JSON.toJSONString(hpaMap));
                service.setUpdateTime(new Date());
                serviceRepository.save(service);
            } catch (Exception e) {
                LOG.error("HPA:" + service.getServiceName() + "创建或修改失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                        "服务:" + service.getServiceName() + "自动伸缩配置失败");
            }
        } else {
            LOG.info("HPA:" + service.getServiceName() + "创建或修改失败");
        }
        LOG.info("HPA:" + service.getServiceName() + "创建或修改成功");
        return true;
    }

    /**
     * 判断服务是否在运行状态下,如果不是则抛异常
     * @Title: serviceIsRuningState
     * @Description: 判断服务是否在运行状态下,如果不是则抛异常
     * @param service void
     * @throws
     */
    private void serviceIsRuningState(Service service) {
        if (Global.OPERATION_RUNNING != service.getStatus()) {
            LOG.info("服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
        }
    }

}
