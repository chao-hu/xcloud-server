package com.xxx.xcloud.module.springcloud.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
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
 * @ClassName: ZuulServiceImpl
 * @Description: zuul 接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Component
public class ZuulServiceImpl extends BaseSpringClouldService {

    private static Logger LOG = LoggerFactory.getLogger(ZuulServiceImpl.class);

    /**
     * Harbor的Secret
     */
    private static final String HARBOR_SECRET = "harbor-secret";

    @Override
    public ApiResult create(String serviceId) {
        // 1. 获取数据库信息
        LOG.info("创建操作， 接收SpringCloudService id : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        LOG.info("接收SpringCloudService 参数 : " + springCloudService.toString());

        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String version = springCloudService.getVersion();
        double cpu = springCloudService.getCpu();
        double memory = springCloudService.getMemory();
        String nodeName = springCloudService.getNodeNames();
        String eurekaUrl = null;
        try {
            SpringCloudService springCloudServiceEureka = springCloudServiceRepository
                    .findByAppIdAndAppType(springCloudService.getAppId(), SpringCloudCommonConst.APPTYPE_EUREKA);
            eurekaUrl = springCloudServiceEureka.getInteriorUrl();
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

        try {
            // build configMap资源
            ConfigMap zuulConfigMap = buildZuulConfigmap(nodeName, eurekaUrl);
            // 创建 configMap资源
            createConfigMap(tenantName, zuulConfigMap);
            // build deployment资源
            Deployment zuulDeployment = buildZuulDeployment(nodeName, serviceName, tenantName, version, cpu, memory,
                    eurekaUrl);
            // 创建 deployment资源
            createDeployment(tenantName, zuulDeployment);
            // build svc资源
            Service zuulService = buildZuulService(nodeName, serviceName, tenantName);
            // 创建 svc资源
            createSvc(tenantName, zuulService);
        } catch (Exception e) {
            LOG.error("创建zuul服务对象失败", e);
            try {
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
            } catch (Exception e1) {
                LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
            return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, springCloudService, "创建zuul服务对象失败");
        }

        // 等待创建成功，修改状态, 及外部访问地址

        if (!checkRunning(nodeName, serviceName, tenantName, springCloudService)) {
            try {
                deleteDeployment(tenantName, nodeName);
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
                return new ApiResult(ReturnCode.CODE_OPT_OVERTIME_FAILED, springCloudService, "创建zuul服务失败");
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");

            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "创建zuul服务成功");
    }

    @Override
    public ApiResult start(String serviceId) {
        // 1. 获取数据库信息
        LOG.info("启动操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        LOG.info("接收SpringCloudService 参数 : " + springCloudService.toString());

        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String version = springCloudService.getVersion();
        double cpu = springCloudService.getCpu();
        double memory = springCloudService.getMemory();
        String nodeName = springCloudService.getNodeNames();
        String eurekaUrl = null;
        try {
            SpringCloudService springCloudServiceEureka = springCloudServiceRepository
                    .findByAppIdAndAppType(springCloudService.getAppId(), SpringCloudCommonConst.APPTYPE_EUREKA);
            eurekaUrl = springCloudServiceEureka.getInteriorUrl();
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

        try {
            // build deployment资源
            Deployment zuulDeployment = buildZuulDeployment(nodeName, serviceName, tenantName, version, cpu, memory,
                    eurekaUrl);
            // 创建 deployment资源
            createDeployment(tenantName, zuulDeployment);

        } catch (Exception e) {
            LOG.error("启动zuul服务对象失败", e);
            try {
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
            } catch (Exception e1) {
                LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
        }

        // 等待创建成功，修改状态
        if (!checkRunning(nodeName, serviceName, tenantName, springCloudService)) {
            try {
                deleteDeployment(tenantName, nodeName);
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
                return new ApiResult(ReturnCode.CODE_OPT_OVERTIME_FAILED, springCloudService, "创建zuul服务失败");
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "启动zuul服务成功");
    }

    @Override
    public ApiResult delete(String serviceId) {
        // 1. 获取数据库信息
        LOG.info("删除操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        LOG.info("接收SpringCloudService 参数 : " + springCloudService.toString());

        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String nodeNames = springCloudService.getNodeNames();

        boolean result = false;

        List<String> nodeNameList = Arrays.asList(nodeNames.split(","));
        if (null != nodeNameList && nodeNameList.size() > 0) {

            for (String nodeName : nodeNameList) {

                try {
                    // 检查deployment资源是否存在，如果存在，就删除deployment
                    result = deleteDeployment(tenantName, nodeName);

                    // 删除svc资源
                    result = deleteSvc(tenantName, nodeName);

                    // 删除cm资源
                    result = deleteConfigMap(tenantName, nodeName);

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
                        return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                                "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                    }
                    return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, springCloudService, "停止deployment或svc对象失败");
                }

            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "删除服务成功");
    }

    public boolean checkRunning(String nodeName, String serviceName, String tenantName,
            SpringCloudService springCloudService) {
        Map<String, String> labelSelector = new HashMap<String, String>();
        labelSelector.put("serviceName", serviceName);
        labelSelector.put("nodeName", nodeName);
        PodList podList = null;
        long start = System.currentTimeMillis();

        while (true) {
            try {
                Thread.sleep(3000);
                podList = KubernetesClientFactory.getClient().pods().inNamespace(tenantName).withLabels(labelSelector)
                        .list();
                if (podList == null || podList.getItems().size() == 0) {
                    return false;
                }

                Integer exterport;
                String exterIp;
                String zuulExternaUri;
                for (Pod pod : podList.getItems()) {
                    if ("Running".equals(pod.getStatus().getPhase())) {
                        Service svc = getSvc(tenantName, nodeName);
                        exterport = svc.getSpec().getPorts().get(0).getNodePort();
                        exterIp = pod.getStatus().getHostIP();
                        zuulExternaUri = "http://" + exterIp + ":" + exterport.toString();
                        LOG.info("服务" + nodeName + "启动用时： " + (System.currentTimeMillis() - start));
                        springCloudService.setExternalUrl(zuulExternaUri);
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_RUNNING);
                        springCloudService = springCloudServiceRepository.save(springCloudService);
                        return true;
                    }
                }

                // 设置超时时间为5min
                if (System.currentTimeMillis() - start > 300000) {
                    LOG.error("检查服务: " + serviceName + "的状态超时");
                    return false;
                }
            } catch (InterruptedException e) {
                LOG.error("检查服务: " + serviceName + "的状态失败, error: ", e);
                return false;
            }

        }
    }

    private ConfigMap buildZuulConfigmap(String name, String eurekaUrl) {
        ConfigMap configMap = new ConfigMap();
        Map<String, String> data = new HashMap<String, String>(16);
        Map<String, String> applicationDataMap = new HashMap<String, String>(16);
        applicationDataMap.put("server.port", String.valueOf(SpringCloudCommonConst.DEFAULT_ZUUL_INNER_PORT));
        applicationDataMap.put("spring.application.name", SpringCloudCommonConst.APPTYPE_ZUUL);
        applicationDataMap.put("eureka.instance.hostname", name);
        applicationDataMap.put("eureka.client.serviceUrl.defaultZone", eurekaUrl);
        String applicationData = "";
        for (Map.Entry<String, String> entry : applicationDataMap.entrySet()) {
            applicationData += entry.getKey() + " = " + entry.getValue() + "\n";
        }
        data.put("application.properties", applicationData);
        configMap.setData(data);
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(name);
        configMap.setMetadata(objectMeta);

        return configMap;
    }

    private Deployment buildZuulDeployment(String nodeName, String serviceName, String tenantName, String version,
            double cpu, double memory, String eurekaUrl) {
        String imageName = getRepoPath(SpringCloudCommonConst.APPTYPE_ZUUL, null, version);

        Deployment zuulDeployment = new Deployment();
        // deployment.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(serviceName);
        meta.setNamespace(tenantName);
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("serviceName", serviceName);
        labels.put("nodeName", nodeName);
        meta.setLabels(labels);
        zuulDeployment.setMetadata(meta);

        // deployment.spec
        DeploymentSpec spec = new DeploymentSpec();
        spec.setReplicas(SpringCloudCommonConst.DEFAULT_REPLICAS);
        LabelSelector labelSelector = new LabelSelector();
        Map<String, String> matchLabels = new HashMap<String, String>(16);
        matchLabels.put("serviceName", serviceName);
        matchLabels.put("nodeName", nodeName);
        labelSelector.setMatchLabels(matchLabels);
        spec.setSelector(labelSelector);

        // deployment.spec.template
        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        ObjectMeta podTemplateMeta = new ObjectMeta();
        podTemplateMeta.setName(nodeName);
        podTemplateMeta.setNamespace(tenantName);
        Map<String, String> podTemplateLabels = new HashMap<String, String>(16);
        podTemplateLabels.put("serviceName", serviceName);
        podTemplateLabels.put("nodeName", nodeName);
        podTemplateMeta.setLabels(podTemplateLabels);
        podTemplateSpec.setMetadata(podTemplateMeta);

        PodSpec podSpec = buildZuulPodSpec(imageName, cpu, memory, nodeName, eurekaUrl);

        podTemplateSpec.setSpec(podSpec);
        spec.setTemplate(podTemplateSpec);
        zuulDeployment.setSpec(spec);

        return zuulDeployment;
    }

    private PodSpec buildZuulPodSpec(String imageName, double cpu, double memory, String nodeName, String eurekaUrl) {
        PodSpec podSpec = new PodSpec();
        try {

            List<Container> containerList = new ArrayList<Container>(10);
            Container container = new Container();
            container.setName(SpringCloudCommonConst.APPTYPE_ZUUL);
            container.setImage(imageName);
            container.setImagePullPolicy("Always");

            // ConfigMap
            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>(10);
            List<Volume> volumes = new ArrayList<Volume>();
            VolumeMount volumeMount = new VolumeMount();
            volumeMount.setName("configdir");
            volumeMount.setMountPath("/conf");
            volumeMounts.add(volumeMount);
            ConfigMapVolumeSource configMapTemplate = new ConfigMapVolumeSource();
            configMapTemplate.setName(nodeName);
            Volume volume = new Volume();
            volume.setName("configdir");
            volume.setConfigMap(configMapTemplate);
            volumes.add(volume);
            container.setVolumeMounts(volumeMounts);
            podSpec.setVolumes(volumes);

            // 资源
            ResourceRequirements requirements = new ResourceRequirements();
            Map<String, Quantity> request = new HashMap<String, Quantity>(16);
            request.put("cpu", new Quantity(
                    cpu / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTCPU)) + ""));
            request.put("memory",
                    new Quantity(memory
                            / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTMEMORY))
                            + "Gi"));
            requirements.setRequests(request);
            Map<String, Quantity> limit = new HashMap<String, Quantity>(16);
            limit.put("cpu", new Quantity(cpu + ""));
            limit.put("memory", new Quantity(memory + "Gi"));
            requirements.setLimits(limit);
            container.setResources(requirements);

            containerList.add(container);
            podSpec.setContainers(containerList);

            List<LocalObjectReference> imagePullSecrets = new ArrayList<LocalObjectReference>(10);
            LocalObjectReference localObjectReference = new LocalObjectReference();
            localObjectReference.setName(HARBOR_SECRET);
            imagePullSecrets.add(localObjectReference);
            podSpec.setImagePullSecrets(imagePullSecrets);
        } catch (

        Exception e) {
            e.printStackTrace();
        }

        return podSpec;
    }

    private Service buildZuulService(String nodeName, String serviceName, String tenantName) {
        Service k8sService = new io.fabric8.kubernetes.api.model.Service();
        // service.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(nodeName);
        meta.setNamespace(tenantName);
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("serviceName", serviceName);
        labels.put("nodeName", nodeName);
        meta.setLabels(labels);
        k8sService.setMetadata(meta);

        // service.spec
        ServiceSpec spec = new ServiceSpec();
        // 默认使用会话粘连
        spec.setSessionAffinity("ClientIP");
        Map<String, String> selector = new HashMap<String, String>(16);
        selector.put("serviceName", serviceName);
        selector.put("nodeName", nodeName);
        spec.setSelector(selector);
        spec.setType(SpringCloudCommonConst.K8S_SERVICE_TYPE_NODEPORT);

        List<ServicePort> ports = new ArrayList<ServicePort>();
        ServicePort servicePort = new ServicePort();
        servicePort.setName(SpringCloudCommonConst.APPTYPE_ZUUL);
        servicePort.setPort(SpringCloudCommonConst.DEFAULT_ZUUL_INNER_PORT);
        servicePort.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        ports.add(servicePort);

        spec.setPorts(ports);
        k8sService.setSpec(spec);

        return k8sService;
    }

}
