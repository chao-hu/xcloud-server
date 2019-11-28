package com.xxx.xcloud.module.application.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.HttpData;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceAffinity;
import com.xxx.xcloud.module.application.entity.ServiceContainerLifecycle;
import com.xxx.xcloud.module.application.entity.ServiceHealth;
import com.xxx.xcloud.module.application.entity.ServiceHostpath;
import com.xxx.xcloud.module.application.repository.ServiceAffinityRepository;
import com.xxx.xcloud.module.application.repository.ServiceContainerLifecycleRepository;
import com.xxx.xcloud.module.application.repository.ServiceHealthRepository;
import com.xxx.xcloud.module.application.repository.ServiceHostpathRepository;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.application.service.CheckPodStatusScheduledThreadPool;
import com.xxx.xcloud.module.application.service.IAppCreateService;
import com.xxx.xcloud.module.application.service.IAppListService;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.ceph.service.CephFileService;
import com.xxx.xcloud.module.ceph.service.CephRbdService;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;
import com.xxx.xcloud.module.configmap.service.ConfigService;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.ingress.service.IngressProxyService;
import com.xxx.xcloud.rest.v1.service.model.ServiceHostAliasesModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceInitContainerCommandModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceInitContainerModelDTO;
import com.xxx.xcloud.threadpool.XcloudSyncService;
import com.xxx.xcloud.utils.FileUtils;
import com.xxx.xcloud.utils.StringUtils;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.CephFSVolumeSource;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ExecAction;
import io.fabric8.kubernetes.api.model.HTTPGetAction;
import io.fabric8.kubernetes.api.model.HTTPHeader;
import io.fabric8.kubernetes.api.model.Handler;
import io.fabric8.kubernetes.api.model.HostAlias;
import io.fabric8.kubernetes.api.model.HostPathVolumeSource;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.KeyToPath;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.Lifecycle;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.NodeAffinity;
import io.fabric8.kubernetes.api.model.NodeSelector;
import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;
import io.fabric8.kubernetes.api.model.NodeSelectorTerm;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodAffinity;
import io.fabric8.kubernetes.api.model.PodAffinityTerm;
import io.fabric8.kubernetes.api.model.PodAntiAffinity;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.RBDVolumeSource;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午3:30:50
 */
@org.springframework.stereotype.Service
public class AppListServiceImpl implements IAppListService {

    private static final Logger LOG = LoggerFactory.getLogger(AppListServiceImpl.class);

    /**
     * Harbor的Secret
     */
    private static final String HARBOR_SECRET = "harbor-secret";

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceHealthRepository serviceHealthRepository;

    @Autowired
    private ServiceAffinityRepository serviceAffinityRepository;

    @Autowired
    private ServiceHostpathRepository serviceHostpathRepository;

    @Autowired
    private ServiceContainerLifecycleRepository serviceContainerLifecycleRepository;

    @Autowired
    private XcloudSyncService bdosSyncService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private IngressProxyService ingressProxyService;

    @Autowired
    private IAppCreateService appCreateService;

    // @Autowired
    // private SelectorLabelRepository selectorLabelRepository;

    @Autowired
    private CephRbdService cephRbdService;

    @Autowired
    private CephFileService cephFileService;

    public static final String NULL_STR = "[null]";

    @Override
    public Page<Service> getServiceList(String tenantName, String projectId, String serviceName, Pageable pageable) {
        Page<Service> serviceListPage = null;
        try {
            if (StringUtils.isNotEmpty(projectId)) {
                if (StringUtils.isNotEmpty(serviceName)) {

                    serviceListPage = serviceRepository
                            .findByTenantNameAndProjectIdAndServiceNameLikeOrderByCreateTimeDesc(tenantName, projectId,
                                    "%" + serviceName + "%", pageable);
                } else {
                    serviceListPage = serviceRepository.findByTenantNameAndProjectIdOrderByCreateTimeDesc(tenantName,
                            projectId, pageable);
                }
            } else {
                if (StringUtils.isNotEmpty(serviceName)) {
                    serviceListPage = serviceRepository.findByTenantNameAndServiceNameLikeOrderByCreateTimeDesc(
                            tenantName, "%" + serviceName + "%", pageable);
                } else {
                    serviceListPage = serviceRepository.findByTenantNameOrderByCreateTimeDesc(tenantName, pageable);
                }

            }
            if (null != serviceListPage && serviceListPage.getContent().size() > 0) {
                for (int i = 0; i < serviceListPage.getContent().size(); i++) {
                    String registryImageName = imageService
                            .getRegistryImageName(serviceListPage.getContent().get(i).getImageVersionId());
                    if (!StringUtils.isEmpty(registryImageName)) {
                        serviceListPage.getContent().get(i)
                                .setRegistryImageName(registryImageName.substring(registryImageName.indexOf("/") + 1)
                                        .replace(getPublicLibRepositoryName(), ""));
                    } else {
                        serviceListPage.getContent().get(i).setRegistryImageName("");
                    }
                    Image image = imageService
                            .getDetailByImageVersionId(serviceListPage.getContent().get(i).getImageVersionId())
                            .getImage();
                    serviceListPage.getContent().get(i).setImageName(image.getImageName());
                }
            }
        } catch (Exception e) {
            LOG.error("获取服务列表失败", e);
        }
        // LOG.info("获取服务列表成功");
        return serviceListPage;
    }

    @Override
    @Transactional(rollbackFor = ErrorMessageException.class, propagation = Propagation.REQUIRES_NEW)
    public boolean deleteService(String serviceId) {
        // 1.校验服务存在与否及状态的正确性
        Service service = appCreateService.getServiceById(serviceId);
        // 2.调用k8s接口删除已创建的资源对象
        if (!StringUtils.isEmpty(service.getHpa())) {
            try {
                Map<String, Object> hpa = JSON.parseObject(service.getHpa(), new TypeReference<Map<String, Object>>() {
                });
                Boolean isTurnOn = (Boolean) hpa.get("isTurnOn");
                if (isTurnOn) {
                    cancleServiceAutomaticScale(serviceId);
                }
            } catch (Exception e) {
                LOG.error("删除hpa失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_HPA_FAILED, "删除hpa失败");
            }
        }
        try {
            if (null != ingressProxyService.getIngressProxy(serviceId, service.getTenantName()).getServiceIngress()) {
                ingressProxyService.deleteIngressProxy(service.getId(), service.getTenantName());
            }
        } catch (Exception e) {
            LOG.error("删除服务代理时发生错误 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_INGRESS_FAILED, "删除服务代理时发生错误");
        }
        try {
            KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                    .withName(service.getServiceName()).cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除deployment失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, "删除deployment失败");
        }
        try {
            KubernetesClientFactory.getClient().services().inNamespace(service.getTenantName())
                    .withName(service.getServiceName()).cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除svc失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_SERVICE_FAILED, "删除svc失败");
        }
        // 3.删除服务相关记录，释放占用资源
        try {
            serviceAffinityRepository.deleteByServiceId(service.getId());
            serviceHealthRepository.deleteByServiceId(service.getId());
            serviceHostpathRepository.deleteByServiceId(service.getId());
            configService.mountClear(service.getId());
            cephFileService.mountClear(service.getId());
            cephRbdService.mountClear(service.getId());
            List<ServiceContainerLifecycle> serviceContainerLifecycleList = serviceContainerLifecycleRepository
                    .findByServiceId(serviceId);
            if (!serviceContainerLifecycleList.isEmpty()) {
                for (ServiceContainerLifecycle serviceContainerLifecycle : serviceContainerLifecycleList) {
                    serviceContainerLifecycleRepository.delete(serviceContainerLifecycle);
                }
            }

            serviceRepository.delete(service);
            CheckPodStatusScheduledThreadPool.getInstance()
                    .remove(service.getTenantName() + Global.CONCATENATE + service.getServiceName());
            FileUtils.delFolder(XcloudProperties.getConfigMap().get(Global.CI_IMAGE_TEMP_PATH) + "containerFile/"
                    + service.getTenantName() + "/" + service.getServiceName());
            LOG.info("服务 " + service.getServiceName() + " 删除成功 ");
        } catch (Exception e) {
            LOG.error("删除服务相关信息时发生错误 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除服务相关信息时发生错误");
        }

        return true;
    }

    @Override
    public boolean startService(String serviceId) {
        // 1.校验服务存在与否及状态的正确性
        Service service = appCreateService.getServiceById(serviceId);
        serviceIsStopState(service);
        // boolean createServiceFlag = service.getStatus() !=
        // Global.OPERATION_STOPPED;
        // 2.更新服务状态为启动中，以防再次触发启动操作
        try {
            service.setStatus(Global.OPERATION_STARTING);
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("数据库连接异常，无法更新服务状态", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新服务状态异常");
        }
        // 3.组装资源对象service、deployment
        // io.fabric8.kubernetes.api.model.Service k8sService = null;
        Deployment deployment = null;
        try {
            /*
             * if (createServiceFlag) { k8sService =
             * generateK8SService(service); }
             */
            deployment = generateK8SDeployment(service);
        } catch (Exception e) {
            LOG.error("组装资源对象失败", e);
            try {
                service.setStatus(Global.OPERATION_START_FAILED);
                service = serviceRepository.save(service);
            } catch (Exception e1) {
                LOG.error("数据库连接异常，无法更新服务状态 ", e1);
            }
            throw e;
        }
        // 4.调用k8s接口创建资源对象
        try {
            deployment = KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                    .create(deployment);
        } catch (Exception e) {
            LOG.error("创建deployment失败", e);
            try {
                service.setStatus(Global.OPERATION_START_FAILED);
                service = serviceRepository.save(service);
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ", e1);
            }
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, "创建deployment失败");
        }
        // 5.异步检测服务是否运行
        bdosSyncService.checkServiceRunning(service);

        return true;
    }

    @Override
    public boolean stopService(String serviceId) {
        // 1.校验服务存在与否及状态的正确性
        Service service = appCreateService.getServiceById(serviceId);
        serviceIsRuningState(service);
        // 2.调用k8s接口删除deployment
        try {
            KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                    .withName(service.getServiceName()).cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除deployment失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, "删除deployment失败");
        }
        // 3.保存相关信息到数据库中
        try {
            service.setStatus(Global.OPERATION_STOPPED);
            service.setIsRestartEffect(false);
            service = serviceRepository.save(service);
            CheckPodStatusScheduledThreadPool.getInstance()
                    .remove(service.getTenantName() + Global.CONCATENATE + service.getServiceName());
            LOG.info("服务 " + service.getServiceName() + " 停止成功 ");
        } catch (Exception e) {
            LOG.error("更新服务状态异常 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新服务状态异常");
        }

        return true;
    }

    @Override
    public boolean cancleServiceAutomaticScale(String serviceId) {
        Service service = appCreateService.getServiceById(serviceId);
        // serviceIsRuningState(service);
        try {
            KubernetesClientFactory.getClient().autoscaling().horizontalPodAutoscalers()
                    .inNamespace(service.getTenantName()).withName(service.getServiceName()).cascading(true).delete();
        } catch (Exception e) {
            LOG.error("HPA:" + service.getServiceName() + "删除失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_HPA_FAILED, "删除自动伸缩配置失败");
        }
        try {
            // 使用hpa后k8s自动更新deployment的副本数，故而需要重置
            Deployment deployment = KubernetesClientFactory.getClient().apps().deployments()
                    .inNamespace(service.getTenantName()).withName(service.getServiceName()).get();
            if (null != deployment) {
                deployment.getSpec().setReplicas(service.getInstance());
                KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                        .withName(service.getServiceName()).replace(deployment);
            }
        } catch (Exception e) {
            LOG.error("删除HPA后更新Deployment副本数失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, "删除HPA后更新Deployment副本数失败");
        }
        /*
         * if (!StringUtils.isEmpty(service.getProjectId())) { // 释放占用的租户资源
         * Map<String, Object> hpa = JSON.parseObject(service.getHpa(), new
         * TypeReference<Map<String, Object>>() { }); Integer maxReplicas =
         * Integer.parseInt(hpa.get("maxReplicas").toString()); Double cpuUsed =
         * (service.getInstance() - maxReplicas) * service.getCpu(); Double
         * memoryUsed = (service.getInstance() - maxReplicas) *
         * service.getMemory(); try {
         * tenantService.updateUsedResource(service.getTenantName(), cpuUsed,
         * memoryUsed, 0d); } catch (ErrorMessageException e) { throw new
         * ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_QUOTA_INSUFFICIENT,
         * "用户资源同步失败"); } } else { // 释放占用的项目资源 // TODO }
         */

        try {
            service.setHpa(null);
            serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("数据库删除hpa失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "删除hpa失败");
        }
        return true;
    }

    @Override
    public List<Service> getServicesByImageVersionId(String imageVersionId) {
        try {
            return serviceRepository.findByImageVersionId(imageVersionId);
        } catch (Exception e) {
            LOG.error("根据镜像版本ID查询服务失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "根据镜像ID查询使用镜像的服务信息失败!");
        }
    }

    /**
     * 获取公共仓库名称
     *
     * @return String
     * @date: 2019年4月24日 上午10:06:12
     */
    private String getPublicLibRepositoryName() {
        return XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/";
    }

    /**
     * Description:判断服务是否在运行状态下,如果不是则抛异常
     *
     * @param service
     *            void
     */
    private void serviceIsRuningState(Service service) {
        if (Global.OPERATION_RUNNING != service.getStatus()) {
            LOG.info("服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
        }
    }

    /**
     * Description:判断服务是否在未启动/已停止/启动失败状态下,如果不是则抛异常
     *
     * @param service
     *            void
     */
    private void serviceIsStopState(Service service) {
        if (!(Global.OPERATION_UNSTART == service.getStatus() || Global.OPERATION_STOPPED == service.getStatus()
                || Global.OPERATION_START_FAILED == service.getStatus())) {
            LOG.info("服务 " + service.getServiceName() + " 不在停止状态下,不允许进行操作");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "服务 " + service.getServiceName() + " 不在停止状态下,不允许进行操作");
        }
    }

    /**
     * 亲和状态是否合法
     * 
     * @param serviceAffinityType
     * @return boolean
     * @date: 2019年11月11日 下午5:22:01
     */
    private boolean isAffinityTypeLegal(Byte serviceAffinityType) {
        if (serviceAffinityType == Global.AFFINITY || serviceAffinityType == Global.ANTI_AFFINITY) {
            return true;
        }

        return false;
    }

    /**
     *
     * <p>
     * Description: 根据数据库信息组装k8s资源对象--deployment
     * </p>
     *
     * @param service
     *            服务信息
     * @return Deployment
     */
    private Deployment generateK8SDeployment(Service service) {
        String registryImageName = imageService.getRegistryImageName(service.getImageVersionId());
        List<ServiceConfig> configs = getServiceConfig(service.getId());
        ServiceAffinity serviceAffinity = getServiceAffinity(service.getId());
        // 判断亲和的依赖服务是否已经启动
        if (serviceAffinity != null) {
            if (!StringUtils.isEmpty(serviceAffinity.getServiceAffinity())
                    && (isAffinityTypeLegal(serviceAffinity.getServiceAffinityType()))) {
                Service dependentService = serviceRepository
                        .findByServiceNameAndTenantName(serviceAffinity.getServiceAffinity(), service.getTenantName());
                if (dependentService == null) {
                    throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                            "当前服务依赖的亲和或反亲和服务 " + serviceAffinity.getServiceAffinity() + " 不存在，无法启动 ");
                }
                try {
                    serviceIsRuningState(dependentService);
                } catch (Exception e) {
                    throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                            "当前服务依赖的亲和或反亲和服务 " + serviceAffinity.getServiceAffinity() + " 未运行，无法启动 ");
                }
            }
        }
        List<ServiceHealth> serviceHealths = getServiceHealth(service.getId());
        List<ServiceContainerLifecycle> serviceContainerLifecycles = getServiceContainerLifecycle(service.getId());
        Deployment deployment = generateK8SDeploymentWithoutVolume(service, registryImageName, configs, serviceAffinity,
                serviceHealths, serviceContainerLifecycles);

        List<ServiceAndCephFile> cephFiles = cephFileService.listMountInService(service.getId());
        if (cephFiles != null && cephFiles.size() > 0) {
            mountCephFile(deployment, cephFiles, service.getTenantName());
        }
        List<ServiceCephRbd> cephRbds = cephRbdService.mountInService(service.getId());
        if (cephRbds != null && cephRbds.size() > 0) {
            mountCephRdb(deployment, cephRbds, service.getTenantName());
        }
        List<ServiceHostpath> hostpaths = getServiceHostpath(service.getId());
        if (hostpaths != null && hostpaths.size() > 0) {
            mountHostPath(deployment, hostpaths);
        }

        return deployment;
    }

    public List<ServiceHostpath> getServiceHostpath(String serviceId) {
        List<ServiceHostpath> serviceHostpaths = null;
        Service service = appCreateService.getServiceById(serviceId);
        try {
            serviceHostpaths = serviceHostpathRepository.findByServiceId(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "本地存储查询失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "服务:" + service.getServiceName() + "本地存储查询失败");
        }
        return serviceHostpaths;
    }

    /**
     *
     * <p>
     * Description: 给服务添加存储卷--CephFile
     * </p>
     *
     * @param deployment
     * @param cephFiles
     * @param namespace
     * @return
     */
    private Deployment mountCephFile(Deployment deployment, List<ServiceAndCephFile> cephFiles, String namespace) {
        PodSpec podSpec = deployment.getSpec().getTemplate().getSpec();
        List<Volume> volumes = podSpec.getVolumes();
        List<VolumeMount> volumeMounts = podSpec.getContainers().get(0).getVolumeMounts();
        int i = 0;

        for (ServiceAndCephFile cephFile : cephFiles) {
            i++;
            Volume volume = new Volume();
            volume.setName("cephfs-" + i);
            CephFSVolumeSource cephfs = new CephFSVolumeSource();
            List<String> monitors = new ArrayList<String>(10);
            String[] cephMonitors = XcloudProperties.getConfigMap().get(Global.CEPH_MONITOR).split(",");
            for (String cephMonitor : cephMonitors) {
                monitors.add(cephMonitor);
            }
            cephfs.setMonitors(monitors);
            cephfs.setPath("/" + namespace + "/" + cephFile.getCephFile().getName());
            cephfs.setUser("admin");
            LocalObjectReference secretRef = new LocalObjectReference();
            secretRef.setName("ceph-secret");
            cephfs.setSecretRef(secretRef);
            cephfs.setReadOnly(false);
            volume.setCephfs(cephfs);
            volumes.add(volume);

            VolumeMount volumeMount = new VolumeMount();
            volumeMount.setMountPath(cephFile.getMountPath());
            volumeMount.setName("cephfs-" + i);
            volumeMounts.add(volumeMount);
        }

        podSpec.setVolumes(volumes);
        List<Container> containers = podSpec.getContainers();
        for (Container container : containers) {
            container.setVolumeMounts(volumeMounts);
        }

        return deployment;
    }

    /**
     *
     * <p>
     * Description: 给服务添加存储卷--CephRdb
     * </p>
     *
     * @param deployment
     * @param cephRbds
     * @param namespace
     * @return
     */
    private Deployment mountCephRdb(Deployment deployment, List<ServiceCephRbd> cephRbds, String namespace) {
        PodSpec podSpec = deployment.getSpec().getTemplate().getSpec();
        List<Volume> volumes = podSpec.getVolumes();
        List<VolumeMount> volumeMounts = podSpec.getContainers().get(0).getVolumeMounts();

        int i = 0;
        for (ServiceCephRbd serviceCephRbd : cephRbds) {
            i++;
            Volume volume = new Volume();
            volume.setName("rbd-" + i);
            RBDVolumeSource cephrbd = new RBDVolumeSource();
            List<String> monitors = new ArrayList<String>(10);
            String[] cephMonitors = XcloudProperties.getConfigMap().get(Global.CEPH_MONITOR).split(",");
            for (String cephMonitor : cephMonitors) {
                monitors.add(cephMonitor);
            }
            cephrbd.setMonitors(monitors);
            cephrbd.setPool(namespace);
            cephrbd.setImage(serviceCephRbd.getCephRbd().getName());
            cephrbd.setUser("admin");
            cephrbd.setFsType("ext4");
            cephrbd.setReadOnly(false);
            LocalObjectReference secretRef = new LocalObjectReference();
            secretRef.setName("ceph-secret");
            cephrbd.setSecretRef(secretRef);
            volume.setRbd(cephrbd);
            volumes.add(volume);

            VolumeMount volumeMount = new VolumeMount();
            volumeMount.setMountPath(serviceCephRbd.getMountPath());
            volumeMount.setName("rbd-" + i);
            volumeMounts.add(volumeMount);
        }

        podSpec.setVolumes(volumes);
        List<Container> containers = podSpec.getContainers();
        for (Container container : containers) {
            container.setVolumeMounts(volumeMounts);
        }

        return deployment;
    }

    /**
     *
     * <p>
     * Description: 给服务添加存储卷--hostpath
     * </p>
     *
     * @param deployment
     * @param hostpaths
     * @return
     */
    private Deployment mountHostPath(Deployment deployment, List<ServiceHostpath> hostpaths) {

        PodSpec podSpec = deployment.getSpec().getTemplate().getSpec();
        List<Volume> volumes = podSpec.getVolumes();
        List<VolumeMount> volumeMounts = podSpec.getContainers().get(0).getVolumeMounts();

        int i = 0;
        for (ServiceHostpath serviceHostPath : hostpaths) {
            i++;
            Volume volume = new Volume();
            volume.setName("hostpath-" + i);
            HostPathVolumeSource hostPath = new HostPathVolumeSource();
            hostPath.setPath(serviceHostPath.getHostPath());
            hostPath.setType("DirectoryOrCreate");
            volume.setHostPath(hostPath);
            volumes.add(volume);

            VolumeMount volumeMount = new VolumeMount();
            volumeMount.setMountPath(serviceHostPath.getMountPath());
            volumeMount.setName("hostpath-" + i);
            volumeMounts.add(volumeMount);
        }

        podSpec.setVolumes(volumes);
        List<Container> containers = podSpec.getContainers();
        for (Container container : containers) {
            container.setVolumeMounts(volumeMounts);
        }

        return deployment;
    }

    public List<ServiceConfig> getServiceConfig(String serviceId) {
        List<ServiceConfig> serviceConfigs = null;
        Service service = appCreateService.getServiceById(serviceId);
        try {
            serviceConfigs = configService.listMount(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "配置文件查询失败", e);
        }
        return serviceConfigs;
    }

    public ServiceAffinity getServiceAffinity(String serviceId) {
        Service service = appCreateService.getServiceById(serviceId);
        ServiceAffinity serviceAffinity = null;
        try {
            serviceAffinity = serviceAffinityRepository.findByServiceId(serviceId);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "服务亲和策略查询失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "服务:" + service.getServiceName() + "服务亲和策略查询失败");
        }
        LOG.info("服务:" + service.getServiceName() + "服务亲和策略查询成功");
        return serviceAffinity;
    }

    public List<ServiceHealth> getServiceHealth(String serviceId) {
        Service service = appCreateService.getServiceById(serviceId);
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

    public List<ServiceContainerLifecycle> getServiceContainerLifecycle(String serviceId) {
        Service service = appCreateService.getServiceById(serviceId);
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

    /**
     *
     * <p>
     * Description: 完成无存储信息的deployment的组装
     * </p>
     *
     * @param service
     *            服务信息
     * @param registryImageName
     *            镜像地址全拼
     * @param configs
     *            配置文件信息
     * @param serviceAffinity
     *            服务亲和属性
     * @param serviceHealths
     *            健康检查
     * @return Deployment(无存储信息)
     */
    private Deployment generateK8SDeploymentWithoutVolume(Service service, String registryImageName,
            List<ServiceConfig> configs, ServiceAffinity serviceAffinity, List<ServiceHealth> serviceHealths,
            List<ServiceContainerLifecycle> serviceContainerLifecycles) {
        Deployment k8sDeployment = new Deployment();
        // deployment.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(service.getServiceName());
        meta.setNamespace(service.getTenantName());
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("app", service.getServiceName());
        meta.setLabels(labels);
        k8sDeployment.setMetadata(meta);
        // deployment.spec
        DeploymentSpec spec = new DeploymentSpec();
        spec.setReplicas(service.getInstance());
        LabelSelector labelSelector = new LabelSelector();
        Map<String, String> matchLabels = new HashMap<String, String>(16);
        matchLabels.put("app", service.getServiceName());
        labelSelector.setMatchLabels(matchLabels);
        spec.setSelector(labelSelector);
        // deployment.spec.template
        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        ObjectMeta podTemplateMeta = new ObjectMeta();
        podTemplateMeta.setName(service.getServiceName());
        podTemplateMeta.setNamespace(service.getTenantName());
        Map<String, String> podTemplateLabels = new HashMap<String, String>(16);
        podTemplateLabels.put("app", service.getServiceName());
        podTemplateMeta.setLabels(podTemplateLabels);
        podTemplateSpec.setMetadata(podTemplateMeta);
        PodSpec podSpec = generateK8SPodSpec(service, registryImageName, configs, serviceHealths,
                serviceContainerLifecycles);
        Affinity affinity = generateK8SPodAffinity(service.getServiceName(), service.getIsPodMutex(), serviceAffinity);
        if (affinity != null) {
            podSpec.setAffinity(affinity);
        }
        podTemplateSpec.setSpec(podSpec);
        spec.setTemplate(podTemplateSpec);
        k8sDeployment.setSpec(spec);

        return k8sDeployment;
    }

    /**
     * <p>
     * Description: 根据数据库信息组装k8s对象--Affinity
     * </p>
     *
     * @param serviceName
     *            服务名称
     * @param isPodMutex
     *            pod是否互斥
     * @param serviceAffinity
     *            服务亲和属性
     * @return Affinity
     */
    private Affinity generateK8SPodAffinity(String serviceName, Boolean isPodMutex, ServiceAffinity serviceAffinity) {
        boolean flag = (isPodMutex == null || isPodMutex == false) && serviceAffinity == null;
        if (flag) {
            return null;
        }
        Affinity affinity = new Affinity();

        // pod亲和特性公有方法内容
        List<PodAffinityTerm> requiredDuringSchedulingIgnoredDuringExecution = new ArrayList<PodAffinityTerm>(10);
        PodAffinityTerm podAffinityTerm = new PodAffinityTerm();
        LabelSelector labelSelector = new LabelSelector();
        List<LabelSelectorRequirement> matchExpressions = new ArrayList<LabelSelectorRequirement>(10);
        LabelSelectorRequirement labelSelectorRequirement = new LabelSelectorRequirement();
        labelSelectorRequirement.setKey("app");
        labelSelectorRequirement.setOperator("In");
        List<String> values = new ArrayList<String>(10);
        if (serviceAffinity != null) {
            values.add(serviceAffinity.getServiceAffinity());
        } else {
            values.add(serviceName);
        }
        labelSelectorRequirement.setValues(values);
        matchExpressions.add(labelSelectorRequirement);
        labelSelector.setMatchExpressions(matchExpressions);
        podAffinityTerm.setLabelSelector(labelSelector);
        podAffinityTerm.setTopologyKey("kubernetes.io/hostname");
        requiredDuringSchedulingIgnoredDuringExecution.add(podAffinityTerm);

        setPodAffinityInfo(affinity, isPodMutex, requiredDuringSchedulingIgnoredDuringExecution, serviceAffinity);
        return affinity;
    }

    /**
     * 设置节点亲和信息
     * 
     * @param affinity
     * @param isPodMutex
     * @param requiredDuringSchedulingIgnoredDuringExecution
     * @param serviceAffinity
     * @date: 2019年11月15日 上午10:18:25
     */
    private void setPodAffinityInfo(Affinity affinity, Boolean isPodMutex,
            List<PodAffinityTerm> requiredDuringSchedulingIgnoredDuringExecution, ServiceAffinity serviceAffinity) {
        // 是否启用pod互斥
        if (isPodMutex != null && isPodMutex) {
            PodAntiAffinity podAntiAffinity = new PodAntiAffinity();
            podAntiAffinity
                    .setRequiredDuringSchedulingIgnoredDuringExecution(requiredDuringSchedulingIgnoredDuringExecution);
            affinity.setPodAntiAffinity(podAntiAffinity);
        } else {
            // pod(即服务)亲和特性
            if (serviceAffinity != null && serviceAffinity.getServiceAffinityType() != null
                    && serviceAffinity.getServiceAffinityType() != Global.NOT_USE_AFFINITY) {
                if (serviceAffinity.getServiceAffinityType() == Global.AFFINITY) {
                    PodAffinity podAffinity = new PodAffinity();
                    podAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(
                            requiredDuringSchedulingIgnoredDuringExecution);
                    affinity.setPodAffinity(podAffinity);
                } else if (serviceAffinity.getServiceAffinityType() == Global.ANTI_AFFINITY) {
                    PodAntiAffinity podAntiAffinity = new PodAntiAffinity();
                    podAntiAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(
                            requiredDuringSchedulingIgnoredDuringExecution);
                    affinity.setPodAntiAffinity(podAntiAffinity);
                }
            }
            // 节点亲和特性
            if (serviceAffinity != null && serviceAffinity.getNodeAffinityType() != null
                    && serviceAffinity.getNodeAffinityType() != Global.NOT_USE_AFFINITY) {
                NodeAffinity nodeAffinity = new NodeAffinity();
                NodeSelector nodeSelector = new NodeSelector();
                List<NodeSelectorTerm> nodeSelectorTerms = new ArrayList<NodeSelectorTerm>(10);
                NodeSelectorTerm nodeSelectorTerm = new NodeSelectorTerm();
                List<NodeSelectorRequirement> requirements = new ArrayList<NodeSelectorRequirement>(10);
                NodeSelectorRequirement requirement = new NodeSelectorRequirement();
                requirement.setKey("kubernetes.io/hostname");
                if (serviceAffinity.getNodeAffinityType() == Global.AFFINITY) {
                    requirement.setOperator("In");
                } else {
                    requirement.setOperator("NotIn");
                }
                List<String> nodeValues = new ArrayList<String>(10);
                String[] nodes = serviceAffinity.getNodeAffinity().split(",");
                for (String node : nodes) {
                    nodeValues.add(node);
                }
                requirement.setValues(nodeValues);
                requirements.add(requirement);
                nodeSelectorTerm.setMatchExpressions(requirements);
                nodeSelectorTerms.add(nodeSelectorTerm);
                nodeSelector.setNodeSelectorTerms(nodeSelectorTerms);
                nodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(nodeSelector);
                affinity.setNodeAffinity(nodeAffinity);
            }
        }
    }

    /**
     *
     * <p>
     * Description: 根据数据库信息组装k8s对象--Containers
     * </p>
     *
     * @param service
     *            服务信息
     * @param registryImageName
     *            镜像地址全拼
     * @param configs
     *            配置文件
     * @param serviceHealths
     *            健康检查
     * @param serviceContainerLifecycles
     *            容器生命周期
     * @return
     */
    private PodSpec generateK8SPodSpec(Service service, String registryImageName, List<ServiceConfig> configs,
            List<ServiceHealth> serviceHealths, List<ServiceContainerLifecycle> serviceContainerLifecycles) {
        PodSpec podSpec = new PodSpec();

        // 添加用来区分服务的主机标签操作
        // ************************************************
//        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(Global.SELECTOR_LABEL_SERVICE);
//        if (labelList != null && labelList.size() > 0) {
//            Map<String, String> nodeSelector = new HashMap<String, String>(16);
//            for (SelectorLabel label : labelList) {
//                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
//            }
//            podSpec.setNodeSelector(nodeSelector);
//        }

        List<Container> containerList = new ArrayList<Container>(10);
        Container container = new Container();
        container.setName(service.getServiceName());
        container.setImage(registryImageName);
        container.setImagePullPolicy("Always");
        if (!StringUtils.isEmpty(service.getCmd())) {
            List<String> command = new ArrayList<String>(10);
            List<String> args = new ArrayList<String>(10);
            String[] startCommandArray = service.getCmd().trim().replaceAll("\\s+", " ").trim().split(" ");
            for (String item : startCommandArray) {
                if (command.size() == 0) {
                    command.add(item);
                    continue;
                }
                args.add(item);
            }
            container.setArgs(args);
            container.setCommand(command);
        }

        List<EnvVar> envVars = getEnvVars(service);
        container.setEnv(envVars);

        if (serviceHealths != null) {
            for (ServiceHealth health : serviceHealths) {
                // 是否启动当前探针
                if (health.getIsTurnOn()) {
                    if (health.getProbeType() == Global.HEALTHCHECK_PROBE_LIVENESS) {
                        container.setLivenessProbe(generateK8SProbe(health));
                    } else if (health.getProbeType() == Global.HEALTHCHECK_PROBE_READINESS) {
                        container.setReadinessProbe(generateK8SProbe(health));
                    }
                }
            }
        }
        if (null != serviceContainerLifecycles) {
            for (ServiceContainerLifecycle serviceContainerLifecycle : serviceContainerLifecycles) {
                // 是否启动当前钩子
                if (serviceContainerLifecycle.getIsTurnOn()) {
                    container.setLifecycle(generateK8SProbeLifecycle(serviceContainerLifecycle));
                }
            }
        }
        ResourceRequirements requirements = new ResourceRequirements();
        Map<String, Quantity> request = new HashMap<String, Quantity>(16);
        request.put("cpu", new Quantity(
                service.getCpu() / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTCPU))
                        + ""));
        request.put("memory",
                new Quantity(service.getMemory()
                        / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTMEMORY))
                        + "Gi"));
        if (0 < service.getGpu()) {
            request.put("nvidia.com/gpu", new Quantity(service.getGpu() + ""));
        }
        requirements.setRequests(request);
        Map<String, Quantity> limit = new HashMap<String, Quantity>(16);
        limit.put("cpu", new Quantity(service.getCpu() + ""));
        limit.put("memory", new Quantity(service.getMemory() + "Gi"));
        if (0 < service.getGpu()) {
            limit.put("nvidia.com/gpu", new Quantity(service.getGpu() + ""));
        }
        requirements.setLimits(limit);
        container.setResources(requirements);

        if (configs != null) {
            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>(10);
            List<Volume> volumes = new ArrayList<Volume>();
            for (ServiceConfig serviceConfig : configs) {
                Map<String, String> configData = JSON.parseObject(serviceConfig.getConfigTemplate().getConfigData(),
                        new TypeReference<Map<String, String>>() {
                        });
                List<KeyToPath> keyToPaths = new ArrayList<KeyToPath>(10);
                for (String config : configData.keySet()) {
                    VolumeMount volumeMount = new VolumeMount();
                    volumeMount.setName("cf-" + serviceConfig.getConfigTemplate().getTemplateName());
                    volumeMount.setMountPath(serviceConfig.getPath() + "/" + config);
                    volumeMount.setSubPath(config);
                    volumeMounts.add(volumeMount);
                    // 是否要使用ConfigMapVolumeSource中的items，要看测试结果
                    KeyToPath keyToPath = new KeyToPath();
                    keyToPath.setKey(config);
                    keyToPath.setPath(config);
                    keyToPaths.add(keyToPath);
                }
                ConfigMapVolumeSource configMapTemplate = new ConfigMapVolumeSource();
                configMapTemplate.setName(serviceConfig.getConfigTemplate().getTemplateName());
                configMapTemplate.setItems(keyToPaths);
                Volume volume = new Volume();
                volume.setName("cf-" + serviceConfig.getConfigTemplate().getTemplateName());
                volume.setConfigMap(configMapTemplate);
                volumes.add(volume);
            }
            container.setVolumeMounts(volumeMounts);
            podSpec.setVolumes(volumes);
        }
        containerList.add(container);
        podSpec.setContainers(containerList);

        List<LocalObjectReference> imagePullSecrets = new ArrayList<LocalObjectReference>(10);
        LocalObjectReference localObjectReference = new LocalObjectReference();
        localObjectReference.setName(HARBOR_SECRET);
        imagePullSecrets.add(localObjectReference);
        podSpec.setImagePullSecrets(imagePullSecrets);

        // 容器内ip和域名映射
        // [{"domains":["a.com","b.com"],"ip":"127.0.0.1"},{"domains":["c.com","d.com"],"ip":"127.0.0.2"}]
        Boolean hostAliasesFlag = true;
        if (StringUtils.isNotEmpty(service.getHostAliases())) {
            List<ServiceHostAliasesModelDTO> hostAliasJsonList = JSON.parseObject(service.getHostAliases(),
                    new TypeReference<List<ServiceHostAliasesModelDTO>>() {
                    });
            List<HostAlias> hostAliases = new ArrayList<HostAlias>();
            for (ServiceHostAliasesModelDTO hostAliasJson : hostAliasJsonList) {
                if (null != hostAliasJson) {
                    HostAlias hostAlias = new HostAlias();
                    String ip = hostAliasJson.getIp();
                    List<String> hostNames = hostAliasJson.getDomains();
                    if (StringUtils.isEmpty(ip) || hostNames.size() <= 0) {
                        hostAliasesFlag = false;
                    }
                    if (hostAliasesFlag) {
                        hostAlias.setIp(ip);
                        hostAlias.setHostnames(hostNames);
                        hostAliases.add(hostAlias);
                    }
                }
            }
            podSpec.setHostAliases(hostAliases);
        }

        // initContainer
        // {"tcp":[{"host":"172.16.26.50","port":3306}],"http":[{"urlPath":"http://172.16.3.30/"}]}
        if (StringUtils.isNotEmpty(service.getInitContainer())) {
            ServiceInitContainerModelDTO serviceInitContainerModel = JSON.parseObject(service.getInitContainer(),
                    new TypeReference<ServiceInitContainerModelDTO>() {
                    });
            List<Container> initContainers = new ArrayList<Container>();
            Container initContainer = new Container();
            String harborUrl = XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS);
            initContainer.setName("service-dependency");
            initContainer.setImage(harborUrl + Global.INITCONTAINER_SERVICE_DEPENDENCY);
            initContainer.setImagePullPolicy("Always");
            List<String> commands = new ArrayList<String>();
            commands.add("/entrypoint.py");
            ServiceInitContainerCommandModelDTO command = serviceInitContainerModel.getCommand();
            List<Map<String, String>> http = command.getHttp();
            List<Map<String, String>> tcp = command.getTcp();
            HashMap<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>(16);
            if (http.size() > 0 && !NULL_STR.equals(http.toString())) {
                for (Map<String, String> httpMap : http) {
                    if (!httpMap.isEmpty()) {
                        map.put("http", http);
                    }
                }
            }
            if (tcp.size() > 0 && !NULL_STR.equals(tcp.toString())) {
                for (Map<String, String> tcpMap : tcp) {
                    if (!tcpMap.isEmpty()) {
                        map.put("tcp", tcp);
                    }
                }
            }
            commands.add(JSON.toJSONString(map));
            initContainer.setCommand(commands);
            initContainers.add(initContainer);
            podSpec.setInitContainers(initContainers);
        }
        return podSpec;
    }

    /**
     * 根据Service对象获取EnvVar
     * 
     * @param service
     * @return List<EnvVar>
     * @date: 2019年11月15日 上午10:20:34
     */
    private List<EnvVar> getEnvVars(Service service) {
        List<EnvVar> envVars = new ArrayList<EnvVar>(10);
        if (service.getIsUsedApm() != null && service.getIsUsedApm() == true) {
            EnvVar envVar1 = new EnvVar();
            envVar1.setName("PINPOINT_SWITCH");
            envVar1.setValue("on");
            envVars.add(envVar1);
            EnvVar envVar2 = new EnvVar();
            envVar2.setName("PINPOINT_SERVER_IP");
            envVar2.setValue(XcloudProperties.getConfigMap().get(Global.PINPOINT_SERVER_IP));
            envVars.add(envVar2);
            EnvVar envVar3 = new EnvVar();
            envVar3.setName("PINPOINT_TCP_PORT");
            envVar3.setValue(XcloudProperties.getConfigMap().get(Global.PINPOINT_TCP_PORT));
            envVars.add(envVar3);
            EnvVar envVar4 = new EnvVar();
            envVar4.setName("PINPOINT_STAT_PORT");
            envVar4.setValue(XcloudProperties.getConfigMap().get(Global.PINPOINT_STAT_PORT));
            envVars.add(envVar4);
            EnvVar envVar5 = new EnvVar();
            envVar5.setName("PINPOINT_SPAN_PORT");
            envVar5.setValue(XcloudProperties.getConfigMap().get(Global.PINPOINT_SPAN_PORT));
            envVars.add(envVar5);
            EnvVar envVar6 = new EnvVar();
            envVar6.setName("POD_TENANT_NAME");
            envVar6.setValue(service.getTenantName());
            envVars.add(envVar6);
            EnvVar envVar7 = new EnvVar();
            envVar7.setName("POD_SERVICE_NAME");
            envVar7.setValue(service.getServiceName());
            envVars.add(envVar7);
        } else {
            EnvVar envVar1 = new EnvVar();
            envVar1.setName("PINPOINT_SWITCH");
            envVar1.setValue("off");
            envVars.add(envVar1);
        }

        if (!StringUtils.isEmpty(service.getEnv())) {
            try {
                Map<String, String> env = JSON.parseObject(service.getEnv(), new TypeReference<Map<String, String>>() {
                });
                for (String key : env.keySet()) {
                    EnvVar envVar = new EnvVar();
                    envVar.setName(key);
                    envVar.setValue(env.get(key));
                    envVars.add(envVar);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return envVars;
    }

    /**
     *
     * <p>
     * Description: 根据数据库信息组装k8s对象--Probe
     * </p>
     *
     * @param health
     *            服务健康检查相关属性
     * @return
     */
    private Probe generateK8SProbe(ServiceHealth health) {
        Probe probe = new Probe();
        probe.setInitialDelaySeconds(health.getInitialDelay());
        probe.setPeriodSeconds(health.getPeriodDetction());
        probe.setTimeoutSeconds(health.getTimeoutDetction());
        if (health.getProbeType() == Global.HEALTHCHECK_PROBE_READINESS) {
            probe.setSuccessThreshold(Byte.toUnsignedInt(health.getSuccessThreshold()));
        }
        if (!StringUtils.isEmpty(health.getExec())) {
            ExecAction execAction = new ExecAction();
            List<String> command = new ArrayList<String>(10);
            String[] execArray = health.getExec().split(",");
            for (String exec : execArray) {
                command.add(exec.trim());
            }
            execAction.setCommand(command);
            probe.setExec(execAction);
        }
        if (!StringUtils.isEmpty(health.getTcp())) {
            TCPSocketAction tcpSocketAction = new TCPSocketAction();
            tcpSocketAction.setPort(new IntOrString(health.getTcp()));
            probe.setTcpSocket(tcpSocketAction);
        }
        if (health.getHttpData() != null) {
            HttpData httpData = health.getHttpData();
            HTTPGetAction httpGetAction = new HTTPGetAction();
            httpGetAction.setPath(httpData.getPath());
            httpGetAction.setPort(new IntOrString(httpData.getPort()));
            httpGetAction.setScheme("HTTP");
            Map<String, String> headData = httpData.getHttpHeade();
            if (headData != null) {
                List<HTTPHeader> headers = new ArrayList<HTTPHeader>(10);
                for (String headKey : headData.keySet()) {
                    HTTPHeader httpHeader = new HTTPHeader();
                    httpHeader.setName(headKey);
                    httpHeader.setValue(headData.get(headKey));
                    headers.add(httpHeader);
                }
                httpGetAction.setHttpHeaders(headers);
            }
            probe.setHttpGet(httpGetAction);
        }

        return probe;
    }

    /**
     * Description: 封装Lifecycle
     *
     * @param serviceContainerLifecycle
     * @return Lifecycle
     */
    private Lifecycle generateK8SProbeLifecycle(ServiceContainerLifecycle serviceContainerLifecycle) {
        Lifecycle lifecycle = new Lifecycle();
        if (serviceContainerLifecycle.getLifecycleType() == Global.CONTAINER_LIFECYCLE_POSTSTART) {
            setPostStartLifecycle(lifecycle, serviceContainerLifecycle);
        } else if (serviceContainerLifecycle.getLifecycleType() == Global.CONTAINER_LIFECYCLE_PRESTOP) {
            setPreStopLifecycle(lifecycle, serviceContainerLifecycle);
        }
        return lifecycle;
    }

    /**
     * 设置生命周期
     * 
     * @param lifecycle
     * @param serviceContainerLifecycle
     * @date: 2019年11月15日 上午10:25:21
     */
    private void setPostStartLifecycle(Lifecycle lifecycle, ServiceContainerLifecycle serviceContainerLifecycle) {
        Handler postStart = new Handler();
        if (!StringUtils.isEmpty(serviceContainerLifecycle.getExec())) {
            ExecAction execAction = new ExecAction();
            List<String> command = new ArrayList<String>(10);
            String[] execArray = serviceContainerLifecycle.getExec().split(",");
            for (String exec : execArray) {
                command.add(exec.trim());
            }
            execAction.setCommand(command);
            postStart.setExec(execAction);
        }
        if (!StringUtils.isEmpty(serviceContainerLifecycle.getTcp())) {
            TCPSocketAction tcpSocketAction = new TCPSocketAction();
            tcpSocketAction.setHost(serviceContainerLifecycle.getHost());
            tcpSocketAction.setPort(new IntOrString(serviceContainerLifecycle.getTcp()));
            postStart.setTcpSocket(tcpSocketAction);
        }
        if (null != serviceContainerLifecycle.getHttpData()) {
            HttpData httpData = serviceContainerLifecycle.getHttpData();
            HTTPGetAction httpGetAction = new HTTPGetAction();
            httpGetAction.setHost(serviceContainerLifecycle.getHost());
            httpGetAction.setPath(httpData.getPath());
            httpGetAction.setPort(new IntOrString(httpData.getPort()));
            httpGetAction.setScheme("HTTP");
            Map<String, String> headData = httpData.getHttpHeade();
            if (headData != null) {
                List<HTTPHeader> headers = new ArrayList<HTTPHeader>(10);
                for (String headKey : headData.keySet()) {
                    HTTPHeader httpHeader = new HTTPHeader();
                    httpHeader.setName(headKey);
                    httpHeader.setValue(headData.get(headKey));
                    headers.add(httpHeader);
                }
                httpGetAction.setHttpHeaders(headers);
            }
            postStart.setHttpGet(httpGetAction);
        }
        lifecycle.setPostStart(postStart);
    }

    /**
     * 设置声明周期
     * 
     * @param lifecycle
     * @param serviceContainerLifecycle
     *            void
     * @date: 2019年11月15日 上午10:26:31
     */
    private void setPreStopLifecycle(Lifecycle lifecycle, ServiceContainerLifecycle serviceContainerLifecycle) {
        Handler preStop = new Handler();
        if (!StringUtils.isEmpty(serviceContainerLifecycle.getExec())) {
            ExecAction execAction = new ExecAction();
            List<String> command = new ArrayList<String>(10);
            String[] execArray = serviceContainerLifecycle.getExec().split(",");
            for (String exec : execArray) {
                command.add(exec.trim());
            }
            execAction.setCommand(command);
            preStop.setExec(execAction);
        }
        if (!StringUtils.isEmpty(serviceContainerLifecycle.getTcp())) {
            TCPSocketAction tcpSocketAction = new TCPSocketAction();
            tcpSocketAction.setHost(serviceContainerLifecycle.getHost());
            tcpSocketAction.setPort(new IntOrString(serviceContainerLifecycle.getTcp()));
            preStop.setTcpSocket(tcpSocketAction);
        }
        if (null != serviceContainerLifecycle.getHttpData()) {
            HttpData httpData = serviceContainerLifecycle.getHttpData();
            HTTPGetAction httpGetAction = new HTTPGetAction();
            httpGetAction.setHost(serviceContainerLifecycle.getHost());
            httpGetAction.setPath(httpData.getPath());
            httpGetAction.setPort(new IntOrString(httpData.getPort()));
            httpGetAction.setScheme("HTTP");
            Map<String, String> headData = httpData.getHttpHeade();
            if (headData != null) {
                List<HTTPHeader> headers = new ArrayList<HTTPHeader>(10);
                for (String headKey : headData.keySet()) {
                    HTTPHeader httpHeader = new HTTPHeader();
                    httpHeader.setName(headKey);
                    httpHeader.setValue(headData.get(headKey));
                    headers.add(httpHeader);
                }
                httpGetAction.setHttpHeaders(headers);
            }
            preStop.setHttpGet(httpGetAction);
        }
        lifecycle.setPreStop(preStop);
    }
}
