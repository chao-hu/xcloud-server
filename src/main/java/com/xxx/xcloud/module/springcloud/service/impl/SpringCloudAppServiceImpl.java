package com.xxx.xcloud.module.springcloud.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudApplication;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.model.ServiceInfo;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudApplicationRepository;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudServiceRepository;
import com.xxx.xcloud.module.springcloud.service.CheckPodStatusScheduledThreadPool;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudAppService;
import com.xxx.xcloud.module.springcloud.thread.SpringCloudSyncService;
import com.xxx.xcloud.utils.DateUtil;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;


/**
 * @ClassName: SpringCloudAppServiceImpl
 * @Description: spring cloud 应用接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Service
@Order(value = 3)
public class SpringCloudAppServiceImpl implements ISpringCloudAppService, CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SpringCloudAppServiceImpl.class);

    @Autowired
    private SpringCloudSyncService springCloudSyncService;
    @Autowired
    private SpringCloudApplicationRepository springCloudApplicationRepository;
    @Autowired
    private SpringCloudServiceRepository springCloudServiceRepository;

    @Override
    @Transactional
    public JSONObject saveServiceList(Map<String, Map<String, String>> resourceMap, SpringCloudApplication app,
            String version) {
        JSONObject json = new JSONObject();

        for (String appType : resourceMap.keySet()) {
            if (SpringCloudCommonConst.APPTYPE_APP.contentEquals(appType)) {
                continue;
            }
            SpringCloudService springCloudService = new SpringCloudService();
            springCloudService = saveService(appType, app, resourceMap.get(appType), version);
            json.put(appType, springCloudService.getId());
            if (SpringCloudCommonConst.APPTYPE_EUREKA.equals(appType)) {
                json.put("interiorUrl", springCloudService.getInteriorUrl());
            }
        }

        return json;
    }

    private SpringCloudService saveService(String appType, SpringCloudApplication app, Map<String, String> map,
            String version) {
        SpringCloudService springCloudService = new SpringCloudService();
        springCloudService.setAppId(app.getId());
        String nodeNames = null;
        String serviceType = null;
        String interiorUrl = null;
        JSONObject configbusInteriorUrl = new JSONObject();
        int num = Integer.parseInt(map.get(SpringCloudCommonConst.RESOURCE_NODE_NUM));
        LOG.info("-------------num-------------" + num);
        if (SpringCloudCommonConst.APPTYPE_EUREKA.equals(appType)) {
            for (int i = 0; i < num; i++) {
                if (null == nodeNames) {
                    nodeNames = appType + "-" + app.getAppName() + "-" + i;
                    interiorUrl = "http://" + nodeNames + ":8761/eureka";
                    continue;
                }
                interiorUrl += "," + "http://" + appType + "-" + app.getAppName() + "-" + i + ":8761/eureka";
                nodeNames += "," + appType + "-" + app.getAppName() + "-" + i;
            }
            if (num > 1) {
                serviceType = SpringCloudCommonConst.SERVICE_TYPE_HA;
            } else {
                serviceType = SpringCloudCommonConst.SERVICE_TYPE_SINGLE;
            }
        } else if (SpringCloudCommonConst.APPTYPE_CONFIG_BUS.equals(appType)) {
            serviceType = SpringCloudCommonConst.SERVICE_TYPE_SINGLE;
            nodeNames = appType + "-" + app.getAppName();
            configbusInteriorUrl.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_CONFIG_URI,
                    "http://" + nodeNames + ":" + SpringCloudCommonConst.DEFAULT_CONFIGBUS_CONTAINERPORT);
            configbusInteriorUrl.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_HOST,
                    SpringCloudCommonConst.APPTYPE_RABBITMQ);
            configbusInteriorUrl.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PORT,
                    SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_ONE);
            configbusInteriorUrl.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_UI_PORT,
                    SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_TWO);
            configbusInteriorUrl.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_USER,
                    SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_USER);
            configbusInteriorUrl.put(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PASSWORD,
                    SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_PASSWORD);
            interiorUrl = JSONObject.toJSONString(configbusInteriorUrl);
        } else if (SpringCloudCommonConst.APPTYPE_ZUUL.equals(appType)) {
            serviceType = SpringCloudCommonConst.SERVICE_TYPE_SINGLE;
            nodeNames = appType + "-" + app.getAppName();
            interiorUrl = "http://" + nodeNames + ":" + SpringCloudCommonConst.DEFAULT_ZUUL_INNER_PORT;

        }

        springCloudService.setAppName(app.getAppName());
        springCloudService.setAppType(appType);
        springCloudService.setCpu(Double.parseDouble(map.get(SpringCloudCommonConst.RESOURCE_CPU)));
        springCloudService.setCreatedBy(app.getTenantName());
        springCloudService.setCreateTime(new Date());
        springCloudService.setMemory(Double.parseDouble(map.get(SpringCloudCommonConst.RESOURCE_MEMORY)));
        springCloudService.setNodeNames(nodeNames);
        springCloudService.setOrderId(app.getOrderId());
        springCloudService.setProjectId(app.getProjectId());
        springCloudService.setServiceName(appType + "-" + app.getAppName());
        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_WAITING);
        springCloudService.setVersion(version);

        /*
         * String storage = map.get(SpringCloudCommonConst.RESOURCE_STORAGE); if
         * (StringUtils.isNotEmpty(storage)) {
         * springCloudService.setStorage(Double.parseDouble(storage)); }
         */
        springCloudService.setTenantName(app.getTenantName());
        springCloudService.setNodeNum(num);
        springCloudService.setServiceType(serviceType);
        springCloudService.setInteriorUrl(interiorUrl);

        if (SpringCloudCommonConst.APPTYPE_CONFIG_BUS.equals(appType)) {
            springCloudService.setCephfileId(map.get(SpringCloudCommonConst.CEPHFILEIDCONFIGBUS));
        }
        try {
            springCloudServiceRepository.save(springCloudService);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "保存服务：" + appType + "-" + app.getAppName() + " 失败");
        }

        return springCloudService;
    }

    @Override
    public SpringCloudApplication saveApp(String appName, String tenantName, String projectId, String orderId,
            Map<String, Map<String, String>> resourceMap, String version) {
        SpringCloudApplication app = new SpringCloudApplication();
        app.setAppName(appName);
        app.setTenantName(tenantName);
        app.setOrderId(orderId);
        app.setProjectId(projectId);
        app.setCpu(Double.parseDouble(
                resourceMap.get(SpringCloudCommonConst.APPTYPE_APP).get(SpringCloudCommonConst.RESOURCE_CPU)));
        app.setMemory(Double.parseDouble(
                resourceMap.get(SpringCloudCommonConst.APPTYPE_APP).get(SpringCloudCommonConst.RESOURCE_MEMORY)));
        /*
         * app.setStorage(Double.parseDouble(
         * resourceMap.get(SpringCloudCommonConst.APPTYPE_APP).get(
         * SpringCloudCommonConst.RESOURCE_STORAGE)));
         */
        app.setState(SpringCloudCommonConst.STATE_APP_WAITING);
        app.setCreateTime(new Date());
        app.setCreatedBy(tenantName);
        app.setVersion(version);
        try {
            springCloudApplicationRepository.save(app);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存应用：" + appName + " 失败");
        }
        return app;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpringCloudApplication> findAppPage(String tenantName, String projectId, int page, int size) {

        Page<SpringCloudApplication> appPage = null;
        Page<SpringCloudApplication> newAppPage = null;

        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        // 获取租户列表
        LOG.info("-------------projectId-----------" + projectId);
        try {
            if (StringUtils.isEmpty(projectId)) {
                appPage = springCloudApplicationRepository.findByTenantNameAndStateNot(tenantName,
                        SpringCloudCommonConst.STATE_APP_DELETED, pageable);
            } else {
                appPage = springCloudApplicationRepository.findByTenantNameAndProjectIdAndStateNot(tenantName,
                        projectId, SpringCloudCommonConst.STATE_APP_DELETED, pageable);
            }

            // 增加展示字段

            List<SpringCloudApplication> appList = appPage.getContent();
            for (SpringCloudApplication app : appList) {
                int appTotalNum = 0;
                int deletedNum = 0;
                Page<SpringCloudService> servicePage = findServicePage(app.getTenantName(), app.getId(), 0, 2000);
                List<SpringCloudService> serviceList = servicePage.getContent();
                JSONObject infoJsonElement = new JSONObject();
                for (SpringCloudService service : serviceList) {
                    if (SpringCloudCommonConst.STATE_APP_DELETED.equals(service.getServiceState())) {
                        deletedNum = deletedNum + 1;
                    }
                    if (SpringCloudCommonConst.APPTYPE_EUREKA.equals(service.getAppType())) {
                        if (SpringCloudCommonConst.SERVICE_TYPE_HA.equals(service.getServiceType())) {
                            appTotalNum = 4;
                        } else {
                            appTotalNum = 3;
                        }
                        infoJsonElement.put("eurekaExternalUrl", service.getExternalUrl());
                        infoJsonElement.put("eurekaInteriorUrl", service.getInteriorUrl());
                    } else if (SpringCloudCommonConst.APPTYPE_CONFIG_BUS.equals(service.getAppType())) {
                        if (StringUtils.isNotEmpty(service.getExternalUrl())) {
                            JSONObject configbusExternal = JSONObject.parseObject(service.getExternalUrl());
                            infoJsonElement.put("configbusExternalUrl", configbusExternal
                                    .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_CONFIG_URI));
                            infoJsonElement.put("rabbitMqUrl", configbusExternal
                                    .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_HOST) + ":"
                                    + configbusExternal
                                            .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PORT));
                            infoJsonElement.put("rabbitMqUiUrl", "http://"
                                    + configbusExternal
                                            .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_HOST)
                                    + ":" + configbusExternal.get(
                                            SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_UI_PORT));
                            infoJsonElement.put("rabbitMqUserName", configbusExternal
                                    .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_USER));
                            infoJsonElement.put("rabbitMqPassword", configbusExternal
                                    .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PASSWORD));
                        }
                        if (StringUtils.isNotEmpty(service.getInteriorUrl())) {
                            JSONObject configbusInterior = JSONObject.parseObject(service.getInteriorUrl());
                            infoJsonElement.put("configbusInteriorUrl", configbusInterior
                                    .get(SpringCloudCommonConst.DEFAULT_RABBITMQ_CONTAINERPORT_CONFIG_URI));
                        }
                    } else if (SpringCloudCommonConst.APPTYPE_ZUUL.equals(service.getAppType())) {
                        infoJsonElement.put("zuulExternalUrl", service.getExternalUrl());
                    }
                }

                app.setInfoJson(infoJsonElement.toString());
                Map<String, Integer> podNumMap = getPodNum(app.getTenantName(), app.getId());
                if (null != podNumMap) {
                    Integer pendingPodNum = podNumMap.get(SpringCloudCommonConst.STATE_SERVICE_PENDING);
                    Integer runningPodNum = podNumMap.get(SpringCloudCommonConst.STATE_SERVICE_RUNNING);
                    Integer succeededPodNum = podNumMap.get(SpringCloudCommonConst.STATE_SERVICE_SUCCEEDED);
                    Integer failedPodNum = podNumMap.get(SpringCloudCommonConst.STATE_SERVICE_FAILED);
                    Integer unknownPodNum = podNumMap.get(SpringCloudCommonConst.STATE_SERVICE_UNKNOWN);
                    int existPodNum = pendingPodNum + runningPodNum + succeededPodNum + failedPodNum + unknownPodNum;
                    if (existPodNum == 0) {
                        if (SpringCloudCommonConst.STATE_APP_STOPPED.equals(app.getState())) {
                            app.setState(SpringCloudCommonConst.STATE_APP_STOPPED);
                        } else if (SpringCloudCommonConst.STATE_APP_WAITING.equals(app.getState())) {
                            app.setState(SpringCloudCommonConst.STATE_APP_WAITING);
                        } else if (SpringCloudCommonConst.STATE_APP_DELETED.equals(app.getState())) {
                            app.setState(SpringCloudCommonConst.STATE_APP_DELETED);
                        } else {
                            app.setState(SpringCloudCommonConst.STATE_APP_FAILED);
                        }
                    } else if (pendingPodNum > 0) {
                        app.setState(SpringCloudCommonConst.STATE_APP_WAITING);
                    } else if (appTotalNum == runningPodNum) {
                        app.setState(SpringCloudCommonConst.STATE_APP_RUNNING);
                    } else if (runningPodNum == 0) {
                        app.setState(SpringCloudCommonConst.STATE_APP_FAILED);
                    } else {
                        app.setState(SpringCloudCommonConst.STATE_APP_WARNING);
                    }
                }

                if (deletedNum == appTotalNum) {
                    app.setState(SpringCloudCommonConst.STATE_APP_DELETED);
                }
                LOG.info("APP的状态为：" + app.getState());
            }

            // 当前页第一条数据在List中的位置
            // int start = (int) pageable.getOffset();
            // // 当前页最后一条数据在List中的位置
            // int end = (start + pageable.getPageSize()) > appList.size() ?
            // appList.size()
            // : (start + pageable.getPageSize());

            // 配置分页数据
            newAppPage = new PageImpl<SpringCloudApplication>(appList, pageable, appList.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询应用列表数据失败");
        }

        return newAppPage;
    }

    public Map<String, Integer> getPodNum(String tenantName, String appId) {
        int runningPodNum = 0;
        int pendingPodNum = 0;
        int succeededPodNum = 0;
        int failedPodNum = 0;
        int unknownPodNum = 0;
        SpringCloudApplication app = findByTenantNameAndId(tenantName, appId);
        if (null == app) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CROSS_TENANT_NOT_ALLOWED, "app为空");
        }
        Map<String, Integer> map = new HashMap<String, Integer>();
        io.fabric8.kubernetes.api.model.Service k8sService = null;
        try {
            Page<SpringCloudService> servicePage = findServicePage(tenantName, appId, 0, 2000);
            List<SpringCloudService> dbServiceList = servicePage.getContent();
            for (SpringCloudService dbService : dbServiceList) {
                String[] serviceNameList = dbService.getNodeNames().split(",");
                for (String serviceName : serviceNameList) {
                    k8sService = KubernetesClientFactory.getClient().inNamespace(tenantName).services()
                            .withName(serviceName).get();
                    LOG.info("--------获取----------" + serviceName);
                    if (null != k8sService) {
                        PodList podList = KubernetesClientFactory.getClient().inNamespace(tenantName).pods()
                                .withLabels(k8sService.getSpec().getSelector()).list();
                        if (null != podList) {
                            for (Pod pod : podList.getItems()) {
                                if ("Running".equals(pod.getStatus().getPhase())) {
                                    // pod中的容器状态
                                    List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                                    ContainerStateTerminated terminated = null;
                                    ContainerStateWaiting waiting = null;
                                    if (containerStatuses.size() > 0) {
                                        for (ContainerStatus containerStatus : containerStatuses) {
                                            ContainerState containerState = containerStatus.getState();
                                            if (null == terminated) {
                                                terminated = containerState.getTerminated();
                                            }
                                            if (null == waiting) {
                                                waiting = containerState.getWaiting();
                                            }
                                        }
                                    }
                                    if (null == terminated && null == waiting) {
                                        String terminatedTime = pod.getMetadata().getDeletionTimestamp();
                                        if (StringUtils.isNotEmpty(terminatedTime)) {
                                            pendingPodNum++;
                                        } else {
                                            runningPodNum++;
                                        }
                                    } else {
                                        pendingPodNum++;
                                    }
                                } else if (SpringCloudCommonConst.STATE_SERVICE_PENDING
                                        .equals(pod.getStatus().getPhase())) {
                                    pendingPodNum++;
                                } else if (SpringCloudCommonConst.STATE_SERVICE_SUCCEEDED
                                        .equals(pod.getStatus().getPhase())) {
                                    succeededPodNum++;
                                } else if (SpringCloudCommonConst.STATE_SERVICE_FAILED
                                        .equals(pod.getStatus().getPhase())) {
                                    failedPodNum++;
                                } else {
                                    unknownPodNum++;
                                }
                            }

                        } else {
                            LOG.info("获取服务:" + serviceName + "pod详情为null");
                        }
                    } else {
                        LOG.info("获取服务:" + serviceName + "service详情为null");
                    }
                }
            }
            map.put(SpringCloudCommonConst.STATE_SERVICE_SUCCEEDED, succeededPodNum);
            map.put(SpringCloudCommonConst.STATE_SERVICE_RUNNING, runningPodNum);
            map.put(SpringCloudCommonConst.STATE_SERVICE_PENDING, pendingPodNum);
            map.put(SpringCloudCommonConst.STATE_SERVICE_FAILED, failedPodNum);
            map.put(SpringCloudCommonConst.STATE_SERVICE_UNKNOWN, unknownPodNum);
            LOG.info("获取应用:" + app.getAppName() + "获取服务实例的不同状态个数成功");
            return map;
        } catch (Exception e) {
            LOG.error("获取spring clould应用:" + appId + "的服务详情失败", e);
            return map;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpringCloudService> findServicePage(String tenantName, String appId, int page, int size) {
        Page<SpringCloudService> servicePage = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        LOG.info("------------------获取--------------------");
        // 获取服务列表
        try {
            servicePage = springCloudServiceRepository.findByTenantNameAndAppIdAndServiceStateNot(tenantName, appId,
                    SpringCloudCommonConst.STATE_APP_DELETED, pageable);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询应用下所有服务数据失败");
        }
        return servicePage;
    }

    @Override
    public List<ServiceInfo> findSeviceList(String tenantName, String appId) {
        SpringCloudApplication app = findByTenantNameAndId(tenantName, appId);
        if (null == app) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CROSS_TENANT_NOT_ALLOWED, "不允许跨租户获取服务的实例详情");
        }

        List<ServiceInfo> serviceInfoList = new ArrayList<ServiceInfo>();

        io.fabric8.kubernetes.api.model.Service k8sService = null;
        try {
            Page<SpringCloudService> servicePage = findServicePage(tenantName, appId, 0, 2000);
            List<SpringCloudService> dbServiceList = servicePage.getContent();

            for (SpringCloudService dbService : dbServiceList) {
                String[] serviceNameList = dbService.getNodeNames().split(",");
                for (String serviceName : serviceNameList) {
                    k8sService = KubernetesClientFactory.getClient().inNamespace(tenantName).services()
                            .withName(serviceName).get();
                    LOG.info("--------获取----------" + serviceName);
                    if (null != k8sService) {
                        String nodePhase;
                        PodList podList = KubernetesClientFactory.getClient().inNamespace(tenantName).pods()
                                .withLabels(k8sService.getSpec().getSelector()).list();
                        if (null != podList) {
                            for (Pod pod : podList.getItems()) {
                                nodePhase = pod.getStatus().getPhase();
                                if ("Running".equals(nodePhase)) {
                                    // pod中的容器状态
                                    List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                                    ContainerStateTerminated terminated = null;
                                    ContainerStateWaiting waiting = null;
                                    if (containerStatuses.size() > 0) {
                                        for (ContainerStatus containerStatus : containerStatuses) {
                                            ContainerState containerState = containerStatus.getState();
                                            if (null == terminated) {
                                                terminated = containerState.getTerminated();
                                            }
                                            if (null == waiting) {
                                                waiting = containerState.getWaiting();
                                            }
                                        }
                                    }
                                    if (null == terminated && null == waiting) {
                                        String terminatedTime = pod.getMetadata().getDeletionTimestamp();
                                        if (StringUtils.isNotEmpty(terminatedTime)) {
                                            nodePhase = SpringCloudCommonConst.STATE_APP_WAITING;
                                        } else {
                                            nodePhase = SpringCloudCommonConst.STATE_APP_RUNNING;
                                        }
                                    } else {
                                        nodePhase = SpringCloudCommonConst.STATE_APP_WAITING;
                                    }
                                } else if (SpringCloudCommonConst.STATE_SERVICE_PENDING.equals(nodePhase)) {
                                    nodePhase = SpringCloudCommonConst.STATE_APP_WAITING;
                                } else if (SpringCloudCommonConst.STATE_SERVICE_SUCCEEDED.equals(nodePhase)) {
                                    nodePhase = SpringCloudCommonConst.STATE_APP_STOPPED;
                                } else if (SpringCloudCommonConst.STATE_SERVICE_FAILED.equals(nodePhase)) {
                                    nodePhase = SpringCloudCommonConst.STATE_APP_FAILED;
                                } else if (SpringCloudCommonConst.STATE_SERVICE_UNKNOWN.equals(nodePhase)) {
                                    nodePhase = SpringCloudCommonConst.STATE_SERVICE_UNKNOWN;
                                } else {
                                    nodePhase = SpringCloudCommonConst.STATE_APP_WAITING;
                                }
                                String podName = pod.getMetadata().getName();
                                String createTime = DateUtil.parseStandardDate(pod.getStatus().getStartTime());
                                ServiceInfo serviceInfo = new ServiceInfo();
                                serviceInfo.setAppId(dbService.getAppId());
                                serviceInfo.setServiceId(dbService.getId());
                                serviceInfo.setNodeName(podName);
                                serviceInfo.setNodeState(nodePhase);
                                serviceInfo.setCreateTime(createTime);
                                serviceInfo.setExterUrl(dbService.getExternalUrl());
                                serviceInfo.setInnerUrl(dbService.getInteriorUrl());
                                serviceInfo.setType(dbService.getServiceType());
                                serviceInfo.setCpu(dbService.getCpu());
                                serviceInfo.setMemory(dbService.getMemory());
                                // serviceInfo.setStorage(dbService.getStorage());
                                serviceInfoList.add(serviceInfo);
                            }
                        } else {
                            LOG.info("获取服务:" + serviceName + "pod详情失败");

                        }
                    } else {
                        LOG.info("获取服务:" + serviceName + "service详情失败");
                    }
                }
            }
        } catch (

        Exception e) {
            LOG.error("获取spring clould应用:" + appId + "的服务详情失败", e);
            // throw new
            // ErrorMessageException(ReturnCode.CODE_K8S_GET_POD_FAILED,
            // "获取spring clould应用:" + appId + "的服务详情失败");
        }
        LOG.info("获取spring clould应用:" + appId + "的服务详情成功");
        return serviceInfoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateAppStateWaiting(SpringCloudApplication app) {
        String appId = app.getId();
        List<SpringCloudService> serviceList = new ArrayList<SpringCloudService>();
        // 1. 修改应用状态Waiting
        // 1.1 修改APP状态Waiting
        app.setState(SpringCloudCommonConst.STATE_APP_WAITING);
        try {
            springCloudApplicationRepository.save(app);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存应用" + appId + "失败");
        }
        // 2. 修改服务状态Waiting
        // 2.1 查找服务list

        try {
            serviceList = springCloudServiceRepository.findByAppId(appId);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查找应用" + appId + "下的所有服务失败");
        }
        if (serviceList.isEmpty()) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "应用" + appId + "下没有服务");
        }

        // 2.2 修改服务状态Waiting
        try {
            for (SpringCloudService service : serviceList) {
                service.setServiceState(SpringCloudCommonConst.STATE_APP_WAITING);
                springCloudServiceRepository.save(service);
            }
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存应用" + appId + "下服务Waiting状态失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceList, "");
    }

    @Override
    public void updateAppState(SpringCloudApplication app, String appState) {
        try {
            app.setState(appState);
            springCloudApplicationRepository.save(app);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "保存应用" + app.getAppName() + "下服务" + appState + "状态失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SpringCloudApplication findByTenantNameAndId(String tenantName, String id) {
        return springCloudApplicationRepository.findByTenantNameAndIdAndStateNot(tenantName, id,
                SpringCloudCommonConst.STATE_APP_DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpringCloudApplication> findByTenantNameAndAppNameAndStateNot(String tenantName, String appName,
            String state) {
        List<SpringCloudApplication> appList = null;
        try {
            appList = springCloudApplicationRepository.findByTenantNameAndAppNameAndStateNot(tenantName, appName,
                    SpringCloudCommonConst.STATE_APP_DELETED);
        } catch (Exception e) {
            LOG.error("校验应用名称" + appName + "是否存在时，查询数据库失败");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "校验应用名称" + appName + "是否存在时，查询数据库失败");
        }
        return appList;
    }

    @Override
    public void createService(Map<String, Map<String, String>> resourceMap, JSONObject json, String id) {

        List<Future<ApiResult>> result = new ArrayList<Future<ApiResult>>();
        for (String appType : resourceMap.keySet()) {
            switch (appType) {
            case SpringCloudCommonConst.APPTYPE_EUREKA:
                result.add(springCloudSyncService.createEureka(json.getString(SpringCloudCommonConst.APPTYPE_EUREKA)));
                break;
            case SpringCloudCommonConst.APPTYPE_CONFIG_BUS:
                result.add(springCloudSyncService
                        .createConfigBus(json.getString(SpringCloudCommonConst.APPTYPE_CONFIG_BUS)));
                break;
            case SpringCloudCommonConst.APPTYPE_ZUUL:
                result.add(springCloudSyncService.createZuul(json.getString(SpringCloudCommonConst.APPTYPE_ZUUL)));
                break;
            default:
                break;
            }
        }

        springCloudSyncService.updateState(id, SpringCloudCommonConst.OPERATOR_CREATE, result);

        springCloudSyncService.checkPodState(id);

    }

    @Override
    public void startService(List<SpringCloudService> serviceList, String appId) {
        List<Future<ApiResult>> result = new ArrayList<Future<ApiResult>>();
        for (SpringCloudService service : serviceList) {
            switch (service.getAppType()) {
            case SpringCloudCommonConst.APPTYPE_EUREKA:
                result.add(springCloudSyncService.startEureka(service.getId()));

                break;
            case SpringCloudCommonConst.APPTYPE_CONFIG_BUS:
                result.add(springCloudSyncService.startConfigBus(service.getId()));
                break;
            case SpringCloudCommonConst.APPTYPE_ZUUL:
                result.add(springCloudSyncService.startZuul(service.getId()));
                break;
            default:
                break;
            }
        }
        springCloudSyncService.updateState(appId, SpringCloudCommonConst.OPERATOR_START, result);
        springCloudSyncService.checkPodState(appId);
    }

    @Override
    public void stopService(List<SpringCloudService> serviceList, String appId) {
        List<Future<ApiResult>> result = new ArrayList<Future<ApiResult>>();
        for (SpringCloudService springCloudService : serviceList) {
            result.add(springCloudSyncService.stop(springCloudService.getId()));
            try {
                CheckPodStatusScheduledThreadPool.getInstance()
                        .remove(springCloudService.getTenantName() + Global.CONCATENATE
                                + springCloudService.getServiceName() + Global.CONCATENATE + appId + Global.CONCATENATE
                                + springCloudService.getServiceType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        springCloudSyncService.updateState(appId, SpringCloudCommonConst.OPERATOR_STOP, result);
    }

    @Override
    @Transactional(readOnly = true)
    public SpringCloudApplication findByTenantNameAndIdAndStateNot(String tenantName, String appId,
            String stateAppDeleted) {
        SpringCloudApplication app = null;
        try {
            app = springCloudApplicationRepository.findByTenantNameAndIdAndStateNot(tenantName, appId,
                    SpringCloudCommonConst.STATE_APP_DELETED);
        } catch (Exception e) {
            LOG.error("校验应用ID" + appId + "是否存在时，查询数据库失败");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "校验应用ID" + appId + "是否存在时，查询数据库失败");
        }
        return app;
    }

    @Override
    public void deleteService(List<SpringCloudService> serviceList, String appId) {
        List<Future<ApiResult>> result = new ArrayList<Future<ApiResult>>();

        for (SpringCloudService springCloudService : serviceList) {
            try {
                CheckPodStatusScheduledThreadPool.getInstance()
                        .remove(springCloudService.getTenantName() + Global.CONCATENATE
                                + springCloudService.getServiceName() + Global.CONCATENATE + appId + Global.CONCATENATE
                                + springCloudService.getServiceType());
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (springCloudService.getAppType()) {
            case SpringCloudCommonConst.APPTYPE_EUREKA:
                result.add(springCloudSyncService.deleteEureka(springCloudService.getId()));
                break;
            case SpringCloudCommonConst.APPTYPE_CONFIG_BUS:
                result.add(springCloudSyncService.deleteConfigBus(springCloudService.getId()));
                break;
            case SpringCloudCommonConst.APPTYPE_ZUUL:
                result.add(springCloudSyncService.deleteZuul(springCloudService.getId()));
                break;
            default:
                break;
            }
        }
        springCloudSyncService.updateState(appId, SpringCloudCommonConst.OPERATOR_DELETE, result);

    }

    /**
     * bdos应用重启后加载运行中应用到当前线程池中
     */
    @Override
    public void run(String... args) throws Exception {
        List<SpringCloudApplication> appList = new ArrayList<SpringCloudApplication>();
        try {
            appList = springCloudApplicationRepository.findByState(SpringCloudCommonConst.STATE_APP_RUNNING);
        } catch (Exception e) {
            LOG.error("bdos应用重启后加载运行中服务到 bdos-springCloud-checkPodState 线程池中   ---> 异常", e);
        }
        if (appList.size() > 0) {
            for (SpringCloudApplication springCloudApplication : appList) {
                List<SpringCloudService> springCloudServiceList = new ArrayList<SpringCloudService>();
                try {
                    springCloudServiceList = springCloudServiceRepository.findByAppId(springCloudApplication.getId());
                    if (springCloudServiceList.size() > 0) {
                        for (SpringCloudService springCloudService : springCloudServiceList) {
                            if (SpringCloudCommonConst.STATE_APP_RUNNING.equals(springCloudService.getServiceState())) {
                                CheckPodStatusScheduledThreadPool.getInstance()
                                        .add(springCloudService.getTenantName() + Global.CONCATENATE
                                                + springCloudService.getServiceName() + Global.CONCATENATE
                                                + springCloudApplication.getId() + Global.CONCATENATE
                                                + springCloudService.getServiceType());
                            }

                        }
                    }
                } catch (Exception e) {
                    LOG.error("bdos应用重启后加载运行中服务到 bdos-springCloud-checkPodState 线程池中   ---> 异常", e);
                }
            }
        }
        LOG.info("bdos应用重启后加载运行中服务到 bdos-springCloud-checkPodState 线程池中   ---> 成功");
    }

    @Override
    public boolean checkServiceNameIsExist(String tenantName, String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            LOG.error("参数校验失败，serviceName为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，serviceName为空");
        }

        if (!serviceName.matches(CommonConst.CHECK_CLUSTER_NAME)) {
            LOG.error("参数校验失败，serviceName不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，serviceName不符合校验规则");
        }

        List<SpringCloudApplication> appList = findByTenantNameAndAppNameAndStateNot(tenantName, serviceName,
                SpringCloudCommonConst.STATE_APP_DELETED);

        if (null != appList && !appList.isEmpty()) {
            LOG.error("参数校验失败，应用名称" + serviceName + "已存在");
            return false;
        }
        return true;
    }

}
