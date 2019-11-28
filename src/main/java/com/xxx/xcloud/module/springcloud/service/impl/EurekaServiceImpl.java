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
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
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
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;


/**
 * @ClassName: EurekaServiceImpl
 * @Description: eureka 服务接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Component
public class EurekaServiceImpl extends BaseSpringClouldService {

    private static Logger LOG = LoggerFactory.getLogger(EurekaServiceImpl.class);

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
        String nodeNames = springCloudService.getNodeNames();
        String serviceType = springCloudService.getServiceType();

        List<String> nodeNameList = Arrays.asList(nodeNames.split(","));
        if (null != nodeNameList && nodeNameList.size() > 0) {

            for (String nodeName : nodeNameList) {

                try {
                    // build deployment资源
                    Deployment eurekaDeployment = buildEurekaDeployment(nodeName, serviceName, tenantName, version, cpu,
                            memory, nodeNameList, serviceType);
                    // 创建 deployment资源
                    createDeployment(tenantName, eurekaDeployment);
                    // build svc资源
                    Service eurekaService = buildEurekaService(nodeName, serviceName, tenantName);
                    // 创建 svc资源
                    createSvc(tenantName, eurekaService);
                } catch (Exception e) {
                    LOG.error("创建eureka服务对象失败", e);
                    try {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                        springCloudService = springCloudServiceRepository.save(springCloudService);
                    } catch (Exception e1) {
                        LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                        return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                                "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                    }
                    return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, springCloudService, "创建eureka服务对象失败");
                }

            }
        }

        // 等待创建成功，修改状态, 及外部访问地址

        if (!checkRunning(nodeNameList, serviceName, tenantName, springCloudService)) {
            try {
                for (String nodeName : nodeNameList) {
                    deleteDeployment(tenantName, nodeName);
                }
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
                return new ApiResult(ReturnCode.CODE_OPT_OVERTIME_FAILED, springCloudService, "创建eureka服务失败");
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");

            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "创建eureka服务成功");
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
        String nodeNames = springCloudService.getNodeNames();
        String serviceType = springCloudService.getServiceType();

        List<String> nodeNameList = Arrays.asList(nodeNames.split(","));
        if (null != nodeNameList && nodeNameList.size() > 0) {

            for (String nodeName : nodeNameList) {

                try {
                    // build deployment资源
                    Deployment eurekaDeployment = buildEurekaDeployment(nodeName, serviceName, tenantName, version, cpu,
                            memory, nodeNameList, serviceType);
                    // 创建 deployment资源
                    createDeployment(tenantName, eurekaDeployment);

                } catch (Exception e) {
                    LOG.error("启动eureka服务对象失败", e);
                    try {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                        springCloudService = springCloudServiceRepository.save(springCloudService);
                    } catch (Exception e1) {
                        LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                        return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                                "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                    }
                }

            }
        }

        // 等待创建成功，修改状态
        if (!checkRunning(nodeNameList, serviceName, tenantName, springCloudService)) {
            try {
                for (String nodeName : nodeNameList) {
                    deleteDeployment(tenantName, nodeName);
                }
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudService = springCloudServiceRepository.save(springCloudService);
                return new ApiResult(ReturnCode.CODE_OPT_OVERTIME_FAILED, springCloudService, "启动eureka服务失败");
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ，无法更新服务: " + serviceName + "的状态, error: ", e1);
                return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, springCloudService,
                        "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "启动eureka服务成功");
    }

    public boolean checkRunning(List<String> nodeNameList, String serviceName, String tenantName,
            SpringCloudService springCloudService) {
        Map<String, String> labelSelector = new HashMap<String, String>();
        Map<String, String> exterIp = new HashMap<String, String>();
        Map<String, Integer> exterport = new HashMap<String, Integer>();
        labelSelector.put("serviceName", serviceName);
        PodList podList = null;
        long start = System.currentTimeMillis();

        while (true) {
            try {
                Thread.sleep(3000);
                for (String nodeName : nodeNameList) {
                    labelSelector.put("nodeName", nodeName);
                    podList = KubernetesClientFactory.getClient().pods().inNamespace(tenantName)
                            .withLabels(labelSelector).list();
                    if (podList == null || podList.getItems().size() == 0) {
                        return false;
                    }

                    for (Pod pod : podList.getItems()) {
                        if ("Running".equals(pod.getStatus().getPhase())) {
                            Service svc = getSvc(tenantName, nodeName);
                            exterport.put(nodeName, svc.getSpec().getPorts().get(0).getNodePort());
                            exterIp.put(nodeName, pod.getStatus().getHostIP());
                            LOG.info("服务" + nodeName + "启动用时： " + (System.currentTimeMillis() - start));
                            break;
                        }
                    }

                }
                if (exterIp.size() == nodeNameList.size() && exterport.size() == nodeNameList.size()) {
                    List<String> eurekaUriList = new ArrayList<String>();
                    if (StringUtils.isEmpty(springCloudService.getExternalUrl())) {
                        for (String nodeName : nodeNameList) {
                            String eurekaUri = "http://" + exterIp.get(nodeName) + ":"
                                    + exterport.get(nodeName).toString() + "/eureka";
                            eurekaUriList.add(eurekaUri);
                        }

                        if (null != eurekaUriList && eurekaUriList.size() == nodeNameList.size()) {
                            String exterUri = String.join(",", eurekaUriList);
                            springCloudService.setExternalUrl(exterUri);
                        }
                    }

                    springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_RUNNING);
                    springCloudService = springCloudServiceRepository.save(springCloudService);

                    return true;
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

    private Deployment buildEurekaDeployment(String nodeName, String serviceName, String tenantName, String version,
            double cpu, double memory, List<String> nodeNameList, String serviceType) {
        String imageName = getRepoPath(SpringCloudCommonConst.APPTYPE_EUREKA, null, version);

        Deployment eurekaDeployment = new Deployment();
        // deployment.metadata
        ObjectMeta meta = new ObjectMeta();
        meta.setName(nodeName);
        meta.setNamespace(tenantName);
        Map<String, String> labels = new HashMap<String, String>(16);
        labels.put("serviceName", serviceName);
        labels.put("nodeName", nodeName);
        meta.setLabels(labels);
        eurekaDeployment.setMetadata(meta);

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

        PodSpec podSpec = buildEurekaPodSpec(imageName, cpu, memory, nodeName, nodeNameList, serviceType);

        podTemplateSpec.setSpec(podSpec);
        spec.setTemplate(podTemplateSpec);
        eurekaDeployment.setSpec(spec);

        return eurekaDeployment;
    }

    private PodSpec buildEurekaPodSpec(String imageName, double cpu, double memory, String nodeName,
            List<String> nodeNameList, String serviceType) {
        PodSpec podSpec = new PodSpec();
        try {

            List<Container> containerList = new ArrayList<Container>(10);
            Container container = new Container();
            container.setName(SpringCloudCommonConst.APPTYPE_EUREKA);
            container.setImage(imageName);
            container.setImagePullPolicy("Always");

            // ENV
            List<EnvVar> envVars = new ArrayList<EnvVar>();
            List<String> eurekaUriList = new ArrayList<String>();
            String eurekaUri = "";
            String defaultZone = "http://" + nodeName + ":8761/eureka";
            for (String name : nodeNameList) {
                if (!nodeName.equals(name)) {
                    eurekaUri = "http://" + name + ":8761/eureka";
                    eurekaUriList.add(eurekaUri);
                }
            }
            if (null != eurekaUriList && eurekaUriList.size() > 0) {
                defaultZone = String.join(",", eurekaUriList);
            }

            EnvVar envVar = new EnvVar();
            envVar.setName(SpringCloudCommonConst.DEFAULT_ENV_JAVA_OPTS);
            if (SpringCloudCommonConst.SERVICE_TYPE_HA.equals(serviceType)) {
                envVar.setValue(SpringCloudCommonConst.DEFAULT_HA_EUREKA_ENV_VALUE + " --eureka.instance.hostname="
                        + nodeName + " --eureka.client.serviceUrl.defaultZone=" + defaultZone);
            } else {
                envVar.setValue(SpringCloudCommonConst.DEFAULT_SINGLE_EUREKA_ENV_VALUE + " --eureka.instance.hostname="
                        + nodeName + " --eureka.client.serviceUrl.defaultZone=" + defaultZone);
            }

            envVars.add(envVar);

            container.setEnv(envVars);

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return podSpec;
    }

    private Service buildEurekaService(String nodeName, String serviceName, String tenantName) {
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
        servicePort.setName(SpringCloudCommonConst.APPTYPE_EUREKA);
        servicePort.setPort(SpringCloudCommonConst.DEFAULT_EUREKA_INNER_PORT);
        servicePort.setProtocol(SpringCloudCommonConst.PORT_PROTOCOL_TCP);
        ports.add(servicePort);

        spec.setPorts(ports);
        k8sService.setSpec(spec);

        return k8sService;
    }

}
