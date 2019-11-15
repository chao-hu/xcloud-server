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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceAffinity;
import com.xxx.xcloud.module.application.entity.ServiceHealth;
import com.xxx.xcloud.module.application.entity.ServiceHostpath;
import com.xxx.xcloud.module.application.repository.ServiceAffinityRepository;
import com.xxx.xcloud.module.application.repository.ServiceHealthRepository;
import com.xxx.xcloud.module.application.repository.ServiceHostpathRepository;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.ceph.repository.ServiceCephRbdRepository;
import com.xxx.xcloud.module.ceph.service.CephFileService;
import com.xxx.xcloud.module.ceph.service.CephRbdService;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;
import com.xxx.xcloud.module.configmap.service.ConfigService;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.service.model.ServiceDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceRequest;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月4日 上午11:35:10
 */

@org.springframework.stereotype.Service
public class AppCreateServiceImpl implements com.xxx.xcloud.module.application.service.IAppCreateService {
    private static final Logger LOG = LoggerFactory.getLogger(AppCreateServiceImpl.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceHealthRepository serviceHealthRepository;

    @Autowired
    private ServiceAffinityRepository serviceAffinityRepository;

    @Autowired
    private ServiceHostpathRepository serviceHostpathRepository;

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CephFileService cephFileService;

    @Autowired
    private ServiceCephRbdRepository serviceCephRbdRepository;

    @Autowired
    private CephRbdService cephRbdService;

    private static final String COMMA = ",";

    private static final String RAW_TYPES = "rawtypes";

    @Override
    public Service createService(ServiceRequest apiService) {
        // 1.信息校验
        // 1.0 检查租户是否存在
        Tenant tenant = tenantService.findTenantByTenantName(apiService.getTenantName());
        if (tenant == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "指定的租户不存在");
        }
        // 1.1 服务名称是否重复
        if (!apiService.getServiceName().matches(Global.CHECK_SERVICE_NAME)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "服务名称不符合正则规范");
        }

        if (!validateServiceName(apiService.getTenantName(), apiService.getServiceName())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "服务名称已存在");
        }

        // 1.2 块存储只允许单实例，且不可重复挂载
        if (!StringUtils.isEmpty(apiService.getStorageRbd()) && apiService.getInstance() > 1) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_RDB_MOUNT_REPEAT, "只允许单实例下选用块存储");
        }
        // 1.3 pod互斥相关验证操作
        if (apiService.getIsPodMutex() != null && apiService.getIsPodMutex()) {
            String nodelistStr = getK8SNodes();
            if (!"".equals(nodelistStr)) {
                if (apiService.getInstance() > nodelistStr.split(COMMA).length) {
                    throw new ErrorMessageException(ReturnCode.CODE_OPT_POD_PODMUTEX,
                            "pod互斥时，不允许副本数-" + apiService.getInstance() + "超出节点数-" + nodelistStr.split(",").length);
                }
            }
            if (!(StringUtils.isEmpty(apiService.getNodeAffinity())
                    && StringUtils.isEmpty(apiService.getServiceAffinity()))) {
                throw new ErrorMessageException(ReturnCode.CODE_OPT_POD_PODMUTEX, "pod互斥时，不允许使用相关调度策略");
            }

        }
        /*
         * // 1.4 资源是否足够 if (StringUtils.isEmpty(apiService.getProjectId())) {
         * // 无项目信息时，校验用户可用资源是否足够 try {
         * tenantService.updateUsedResource(apiService.getTenantName(),
         * apiService.getCpu() * apiService.getInstance(),
         * apiService.getMemory() * apiService.getInstance(), 0d); } catch
         * (Exception e) { throw new
         * ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_QUOTA_INSUFFICIENT,
         * "用户资源不足，无法创建"); } } else { // 有项目信息时，校验当前项目可用资源是否足够 // TODO do
         * something here }
         */

        // 组装资源对象service
        io.fabric8.kubernetes.api.model.Service k8sService = null;
        try {
            k8sService = generateK8SService(apiService);
            k8sService = KubernetesClientFactory.getClient().services().inNamespace(apiService.getTenantName())
                    .create(k8sService);
        } catch (Exception e) {
            LOG.error("创建svc失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_SERVICE_FAILED, "创建资源对象svc失败");
        }

        try {
            applicationContext.getBean(AppCreateServiceImpl.class).preserveServiceRelatedIdnfo(apiService);
            LOG.info("服务 " + apiService.getServiceName() + " 创建成功 ");
        } catch (Exception e) {
            LOG.error("保存服务信息失败 ", e);
            try {
                KubernetesClientFactory.getClient().services().inNamespace(apiService.getTenantName())
                        .withName(apiService.getServiceName()).cascading(true).delete();
            } catch (Exception e1) {
                LOG.error("删除svc失败", e1);
            }
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务信息失败");
        }

        return apiService.getService();
    }

    @Override
    public Boolean validateServiceName(String tenantName, String serviceName) {
        // 数据库验重操作
        try {
            Service service = serviceRepository.findByServiceNameAndTenantName(serviceName, tenantName);
            if (service != null) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("服务查询失败", e);
        }
        // k8s验重操作
        io.fabric8.kubernetes.api.model.Service k8sService = null;
        try {
            k8sService = KubernetesClientFactory.getClient().services().inNamespace(tenantName).withName(serviceName)
                    .get();
        } catch (Exception e) {
            LOG.error("k8s查询服务失败！", e);
            k8sService = null;
        }
        if (k8sService == null) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public String getK8SNodes() {
        StringBuffer buffer = new StringBuffer();
        NodeList list = null;
        try {
            list = KubernetesClientFactory.getClient().nodes().list();
        } catch (Exception e) {
            LOG.error("获取k8s节点列表失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_NODE_FAILED, "获取k8s节点列表失败");
        }
        if (list != null) {
            for (Node node : list.getItems()) {
                if (!node.getMetadata().getLabels().containsKey("node-role.kubernetes.io/master")) {
                    buffer.append(node.getMetadata().getName() + ",");
                }
            }
        } else {
            return "";
        }

        return buffer.substring(0, buffer.length() - 2);
    }

    @Override
    public ServiceDTO getServiceRequestById(String serviceId) {
        ServiceDTO swServiceModel = new ServiceDTO();

        try {
            Service service = getServiceById(serviceId);
            swServiceModel.setService(service);

            List<ServiceHealth> healthCheck = serviceHealthRepository.findByServiceId(serviceId);
            if (healthCheck != null && healthCheck.size() > 0) {
                for (int i = 0; i < healthCheck.size(); i++) {
                    healthCheck.get(i).setId(null);
                    healthCheck.get(i).setServiceId(null);
                }
                swServiceModel.setHealthCheck(JSON.toJSONString(healthCheck));
            }

            ServiceAffinity serviceAffinity = serviceAffinityRepository.findByServiceId(serviceId);
            if (serviceAffinity != null) {
                swServiceModel.setNodeAffinity(serviceAffinity.getNodeAffinity());
                swServiceModel.setNodeAffinityType(serviceAffinity.getNodeAffinityType());
                swServiceModel.setServiceAffinity(serviceAffinity.getServiceAffinity());
                swServiceModel.setServiceAffinityType(serviceAffinity.getServiceAffinityType());
            }

            List<ServiceConfig> configs = configService.listMount(serviceId);
            if (configs != null && configs.size() > 0) {
                JSONObject configMap = new JSONObject(16);
                for (ServiceConfig serviceConfig : configs) {
                    configMap.put(serviceConfig.getConfigTemplateId(), serviceConfig.getPath());
                }
                swServiceModel.setConfig(configMap);
            }

            List<ServiceAndCephFile> cephFiles = cephFileService.listMountInService(serviceId);
            if (cephFiles != null && cephFiles.size() > 0) {
                JSONObject cephFileMap = new JSONObject(16);
                for (ServiceAndCephFile serviceAndCephFile : cephFiles) {
                    cephFileMap.put(serviceAndCephFile.getCephFileId(), serviceAndCephFile.getMountPath());
                }
                swServiceModel.setStorageFile(cephFileMap);
            }

            List<ServiceCephRbd> cephRbds = serviceCephRbdRepository.findByServiceId(serviceId);
            if (cephRbds != null && cephRbds.size() > 0) {
                JSONObject cephRbdMap = new JSONObject(16);
                for (ServiceCephRbd serviceCephRbd : cephRbds) {
                    cephRbdMap.put(serviceCephRbd.getCephRbdId(), serviceCephRbd.getMountPath());
                }
                swServiceModel.setStorageRbd(cephRbdMap);
            }

            List<ServiceHostpath> hostpaths = serviceHostpathRepository.findByServiceId(serviceId);
            if (hostpaths != null && hostpaths.size() > 0) {
                JSONObject hostpathMap = new JSONObject(16);
                for (ServiceHostpath serviceHostpath : hostpaths) {
                    hostpathMap.put(serviceHostpath.getHostPath(), serviceHostpath.getMountPath());
                }
                swServiceModel.setStorageLocal(hostpathMap);
            }

        } catch (Exception e) {
            LOG.info("获取服务构建参数失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_RETURN_MORE_FAILED, "获取服务构建参数失败");
        }

        return swServiceModel;
    }

    @Override
    public Service getServiceById(String serviceId) {
        Service service = null;
        Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
        if (!serviceOptional.isPresent()) {
            LOG.info("服务不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        } else {
            service = serviceOptional.get();
            String registryImageName = imageService.getRegistryImageName(service.getImageVersionId());
            if (!StringUtils.isEmpty(registryImageName)) {
                service.setRegistryImageName(registryImageName.substring(registryImageName.indexOf("/") + 1)
                        .replace(getPublicLibRepositoryName(), ""));
            } else {
                service.setRegistryImageName("");
            }
            Image image = imageService.getDetailByImageVersionId(service.getImageVersionId()).getImage();
            service.setImageName(image.getImageName());
        }
        return service;
    }

    @Override
    public Service getServiceByNameAndTenantName(String serviceName, String tenantname) {
        Service service = null;
        try {
            service = serviceRepository.findByServiceNameAndTenantName(serviceName, tenantname);
            if (null != service) {
                String registryImageName = imageService.getRegistryImageName(service.getImageVersionId());
                if (!StringUtils.isEmpty(registryImageName)) {
                    service.setRegistryImageName(registryImageName.substring(registryImageName.indexOf("/") + 1)
                            .replace(getPublicLibRepositoryName(), ""));
                } else {
                    service.setRegistryImageName("");
                }
                Image image = imageService.getDetailByImageVersionId(service.getImageVersionId()).getImage();
                service.setImageName(image.getImageName());
            }
        } catch (Exception e) {
            LOG.error("查询服务失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询服务失败");
        }

        return service;
    }

    /**
     *
     * <p>
     * Description: 服务创建时，保存其相关信息
     * </p>
     *
     * @param apiService
     */
    @Transactional(rollbackFor = ErrorMessageException.class)
    public void preserveServiceRelatedIdnfo(ServiceRequest apiService) {
        Service service = apiService.getService();
        try {
            service.setStatus(Global.OPERATION_UNSTART);
            service.setCreateTime(new Date());
            service = serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("保存服务信息失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务信息失败");
        }
        // 创建服务时返回镜像名称供前端使用
        String registryImageName = imageService.getRegistryImageName(service.getImageVersionId());
        if (!StringUtils.isEmpty(registryImageName)) {
            apiService.setRegistryImageName(registryImageName.substring(registryImageName.indexOf("/") + 1)
                    .replace(getPublicLibRepositoryName(), ""));
        } else {
            apiService.setRegistryImageName("");
        }
        Image image = imageService.getDetailByImageVersionId(service.getImageVersionId()).getImage();
        apiService.setImageName(image.getImageName());
        apiService.setId(service.getId());
        apiService.setStatus(service.getStatus());
        apiService.setCreateTime(service.getCreateTime());

        udpateConfigInfo(apiService);

        updateStorageFile(apiService);

        updateStorageRbd(apiService);

        updateStorageLocal(apiService);

        updateNodeAffinity(apiService);

        updateHelthCheck(apiService);

        // 减小并发下同租户创建重复名称的服务的概率
        List<Service> sameNameServices = serviceRepository.findByTenantNameAndServiceName(apiService.getTenantName(),
                apiService.getServiceName());
        if (sameNameServices != null && sameNameServices.size() > 1) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "服务名称已存在");
        }

    }

    /**
     * update
     * 
     * @param apiService
     *            void
     * @date: 2019年11月15日 上午10:51:44
     */
    private void udpateConfigInfo(ServiceRequest apiService) {

        if (!StringUtils.isEmpty(apiService.getConfig())) {
            try {
                Map<String, String> configs = JSON.parseObject(apiService.getConfig(),
                        new TypeReference<Map<String, String>>() {
                        });
                configService.mountSave(apiService.getId(), configs);
            } catch (Exception e) {
                LOG.error("保存服务配置信息失败 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务配置信息失败");
            }
        }
    }

    /**
     * update
     * 
     * @param apiService
     *            void
     * @date: 2019年11月15日 上午10:43:24
     */
    private void updateStorageFile(ServiceRequest apiService) {
        if (!StringUtils.isEmpty(apiService.getStorageFile())) {
            try {
                Map<String, String> cephFiles = JSON.parseObject(apiService.getStorageFile(),
                        new TypeReference<Map<String, String>>() {
                        });
                for (String cephFileId : cephFiles.keySet()) {
                    ServiceAndCephFile serviceAndCephFile = new ServiceAndCephFile();
                    serviceAndCephFile.setServiceId(apiService.getId());
                    serviceAndCephFile.setMountPath(cephFiles.get(cephFileId));
                    serviceAndCephFile.setCephFileId(cephFileId);
                    serviceAndCephFile.setServiceType(Global.MOUNTCEPHFILE_SERVICE);
                    cephFileService.mountSave(serviceAndCephFile);
                }
            } catch (Exception e) {
                LOG.error("保存服务文件存储失败 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务文件存储信息失败");
            }
        }
    }

    /**
     * update
     * 
     * @param apiService
     * @date: 2019年11月15日 上午10:42:49
     */
    private void updateStorageRbd(ServiceRequest apiService) {
        if (!StringUtils.isEmpty(apiService.getStorageRbd())) {
            try {
                Map<String, String> cephRdbs = JSON.parseObject(apiService.getStorageRbd(),
                        new TypeReference<Map<String, String>>() {
                        });
                for (String cephRbdId : cephRdbs.keySet()) {
                    cephRbdService.mountSave(null, apiService.getId(), cephRbdId, cephRdbs.get(cephRbdId));
                }
            } catch (Exception e) {
                LOG.error("保存服务块存储信息失败 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务块存储信息失败");
            }
        }
    }

    /**
     * update StorageLocal
     * 
     * @param apiService
     * @date: 2019年11月15日 上午10:08:26
     */
    private void updateStorageLocal(ServiceRequest apiService) {
        if (!StringUtils.isEmpty(apiService.getStorageLocal())) {
            try {
                Map<String, String> hostpaths = JSON.parseObject(apiService.getStorageLocal(),
                        new TypeReference<Map<String, String>>() {
                        });
                for (String hostPath : hostpaths.keySet()) {
                    ServiceHostpath serviceHostpath = new ServiceHostpath();
                    serviceHostpath.setServiceId(apiService.getId());
                    serviceHostpath.setHostPath(hostPath);
                    serviceHostpath.setMountPath(hostpaths.get(hostPath));
                    updateServiceHostpath(serviceHostpath);
                }
            } catch (Exception e) {
                LOG.error("保存服务本地存储信息失败 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务本地存储信息失败");
            }
        }
    }

    /**
     * update NodeAffinity
     * 
     * @param apiService
     * @date: 2019年11月15日 上午10:03:20
     */
    private void updateNodeAffinity(ServiceRequest apiService) {
        if (!(StringUtils.isEmpty(apiService.getNodeAffinity())
                && StringUtils.isEmpty(apiService.getServiceAffinity()))) {
            try {
                ServiceAffinity serviceAffinity = apiService.getAffinity();
                serviceAffinity.setServiceId(apiService.getId());
                serviceAffinityRepository.save(serviceAffinity);
            } catch (Exception e) {
                LOG.error("保存服务亲和属性信息失败 ", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务亲和属性信息失败");
            }
        }
    }

    /**
     * 更新HelthCheck
     * 
     * @param apiService
     * @date: 2019年11月15日 上午10:00:58
     */
    private void updateHelthCheck(ServiceRequest apiService) {
        if (apiService.getHealthCheck() != null) {
            try {
                List<ServiceHealth> healthList = apiService.getHealthCheck();
                for (ServiceHealth serviceHealth : healthList) {
                    serviceHealth.setServiceId(apiService.getId());
                    updateServiceHealth(serviceHealth);
                }
            } catch (Exception e) {
                LOG.error("保存服务健康检查信息失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务健康检查信息失败 ");
            }
        }
    }

    @Override
    public Boolean updateServiceHostpath(ServiceHostpath serviceHostpath) {
        Service service = getServiceById(serviceHostpath.getServiceId());
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
     *
     * <p>
     * Description: 根据数据库信息组装k8s资源对象--service
     * </p>
     *
     * @param DBservice
     *            数据库中的服务信息
     * @return K8Sservice k8s的资源对象svc
     */
    private io.fabric8.kubernetes.api.model.Service generateK8SService(Service service) {
        io.fabric8.kubernetes.api.model.Service k8sService = new io.fabric8.kubernetes.api.model.Service();
        // service.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(service.getServiceName());
        meta.setNamespace(service.getTenantName());
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("app", service.getServiceName());
        meta.setLabels(labels);
        k8sService.setMetadata(meta);
        // service.spec
        ServiceSpec spec = new ServiceSpec();
        // 默认使用会话粘连
        spec.setSessionAffinity("ClientIP");
        Map<String, String> selector = new HashMap<String, String>(16);
        selector.put("app", service.getServiceName());
        spec.setSelector(selector);
        List<ServicePort> ports = new ArrayList<ServicePort>(10);
        int i = 0;
        if (!StringUtils.isEmpty(service.getPortAndProtocol())) {
            // 获取用户指定的暴露端口
            Map<String, String> portAndProtocol = JSON.parseObject(service.getPortAndProtocol(),
                    new TypeReference<Map<String, String>>() {
                    });
            Set<Entry<String, String>> entries = portAndProtocol.entrySet();
            for (@SuppressWarnings(RAW_TYPES)
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
            /*
             * // 获取当前镜像的暴露端口 String portsStr =
             * imageService.getImageExposedPorts(service.getImageId()); if
             * (!StringUtils.isEmpty(portsStr)) { for (String port :
             * portsStr.split(",")) { i++; ServicePort servicePort = new
             * ServicePort(); servicePort.setName("http" + i);
             * servicePort.setPort(Integer.parseInt(port));
             * servicePort.setProtocol("TCP"); ports.add(servicePort); } }
             */
        }
        spec.setPorts(ports);
        k8sService.setSpec(spec);

        return k8sService;
    }

    public static final int TWO = 2;

    @Override
    public Boolean updateServiceHealth(ServiceHealth serviceHealth) {
        Service service = getServiceById(serviceHealth.getServiceId());
        setRestartFlagTrue(service);
        List<ServiceHealth> serviceHealths = getServiceHealth(serviceHealth.getServiceId());
        if (serviceHealths != null && serviceHealths.size() > TWO) {
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
    public List<ServiceHealth> getServiceHealth(String serviceId) {
        Service service = getServiceById(serviceId);
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

    /**
     *
     * <p>
     * Description: 服务在启动中或运行中，设置服务重新生效字段为true
     * </p>
     *
     * @param service
     */
    private void setRestartFlagTrue(Service service) {
        if (Global.OPERATION_RUNNING == service.getStatus() || Global.OPERATION_STARTING == service.getStatus()
                || Global.OPERATION_UPDATING == service.getStatus()
                || Global.OPERATION_UPDATE_FAILED == service.getStatus()) {
            service.setIsRestartEffect(true);
        }
    }
}
