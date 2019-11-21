package com.xxx.xcloud.module.component.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;
import com.xxx.xcloud.module.component.entity.StatefulServiceDependence;
import com.xxx.xcloud.module.component.repository.StatefulNodeRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceComponentDefaultConfigRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceDependenceRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceUnitVersionRepository;
import com.xxx.xcloud.module.component.service.worker.base.BaseThreadWorker;
import com.xxx.xcloud.utils.StringUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * @ClassName: ComponentOperationsDataBaseUtil
 * @Description: 组件数据库操作
 * @author lnn
 * @date 2019年11月21日
 *
 */
@Service
public class ComponentOperationsDataBaseUtil {

    private static Logger LOG = LoggerFactory.getLogger(ComponentOperationsDataBaseUtil.class);

    private static ThreadFactory statefulServiceThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("bdos-stateful-service-worker-%d").build();

    private static final ExecutorService POOL = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), statefulServiceThreadFactory);
    @Autowired
    private StatefulServiceRepository statefulServiceRepository;

    @Autowired
    private StatefulNodeRepository statefulNodeRepository;

    @Autowired
    private StatefulServiceDependenceRepository statefulServiceDependenceRepository;

    @Autowired
    private StatefulServiceComponentDefaultConfigRepository statefulServiceComponentDefaultConfigRepository;

    @Autowired
    private StatefulServiceUnitVersionRepository statefulServiceUnitVersionRepository;

    /**
     * 添加到线程池
     *
     * @param appType
     * @param optWorker
     * @param params
     */
    @SuppressWarnings("unchecked")
    public void addPool(String appType, String optWorker, Map<String, String> params) {
        LOG.info("接收参数=>appType:" + appType + ", optWorker:" + optWorker + ", params:" + JSON.toJSONString(params));
        BaseThreadWorker<Map<String, String>> worker = null;
        String bean = getFullBean(appType, optWorker);
        try {
            worker = (BaseThreadWorker<Map<String, String>>) SpringApplicationContext.getBean(Class.forName(bean));
            worker.setData(params);
        } catch (Exception e) {
            LOG.error("实例化bean失败,bean:" + bean + ",error:", e);
            return;
        }
        LOG.info("提交到线程池：" + JSON.toJSONString(worker));
        POOL.submit(worker);

    }

    /**
     * 获取组件worker的完整路径
     *
     * @param appType
     * @param optWorker
     * @return
     */
    private String getFullBean(String appType, String optWorker) {
        return "com.xxx.xcloud.module.component.service.worker." + appType + "." + optWorker;
    }

    /**
     * 生成StatefulService
     *
     * @param tenantName
     * @param projectId
     * @param appType
     * @param serviceName
     * @param version
     * @param replicas
     * @param password
     * @param type
     * @param cpuTotal
     * @param memoryTotal
     * @param capacityTotal
     * @param configUpdated
     * @param extendedField
     * @return
     * @throws ErrorMessageException
     */
    public StatefulService buildStatefulService(String tenantName, String createdBy, String projectId, String orderId,
            String appType, String serviceName, String version, int replicas, String password, String type,
            Double cpuTotal, Double memoryTotal, Double capacityTotal, String configuration, String extendedField,
            String latestOpt) throws ErrorMessageException {

        StatefulService statefulService = new StatefulService();
        if (StringUtils.isNotEmpty(projectId)) {
            statefulService.setProjectId(projectId);
        }
        if (StringUtils.isNotEmpty(orderId)) {
            statefulService.setOrderId(orderId);
        }

        statefulService.setAppType(appType);
        statefulService.setServiceName(serviceName);
        statefulService.setServiceState(CommonConst.STATE_CLUSTER_WAITING);
        statefulService.setNamespace(tenantName);
        if (StringUtils.isNotEmpty(createdBy)) {
            statefulService.setCreatedBy(createdBy);
        }
        statefulService.setNodeNum(replicas);
        statefulService.setVersion(version);
        if (StringUtils.isNotEmpty(type)) {
            statefulService.setType(type);
        }
        if (StringUtils.isNotEmpty(password)) {
            statefulService.setPassword(password);
        }
        statefulService.setCpu(cpuTotal);
        statefulService.setMemory(memoryTotal);
        statefulService.setStorage(capacityTotal);
        statefulService.setCreateTime(new Date());
        statefulService.setLastoptTime(new Date());
        if (StringUtils.isNotEmpty(configuration)) {
            statefulService.setConfigUpdated(configuration);
        }

        if (StringUtils.isNotEmpty(extendedField)) {
            statefulService.setExtendedField(extendedField);
        }

        if (StringUtils.isNotEmpty(latestOpt)) {
            statefulService.setLastopt(latestOpt);
        }

        StatefulService service = null;
        try {
            service = statefulServiceRepository.save(statefulService);
        } catch (Exception e) {
            LOG.error("保存StatefulService失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "保存StatefulService失败, data: " + service.toString() + ", error: " + e.getMessage());
        }
        return service;
    }

    /**
     * 根据serviceId获取StatefulService
     *
     * @param serviceId
     * @return
     */
    public StatefulService getStatefulServiceById(String serviceId) throws ErrorMessageException {
        LOG.info("根据serviceId获取StatefulService，serviceId：" + serviceId);
        StatefulService service = null;
        try {
            service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
        } catch (Exception e) {
            LOG.error("根据serviceId获取StatefulService失败,serviceId：" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "根据serviceId获取StatefulService失败,serviceId：" + serviceId + ",error:" + e.getMessage());
        }
        return service;
    }

    /**
     * 修改集群内所有节点状态为指定状态
     *
     * @param service
     * @param state
     */
    public void updateNodesState(String serviceId, String state, Map<String, Map<String, String>> nodesExtendedField,
            String clusterLastOpt, String oldLastOpt) throws ErrorMessageException {
        LOG.info("根据serviceId修改节点状态，serviceId：" + serviceId + ",state:" + state);
        try {
            List<StatefulNode> nodes = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            LOG.info("serviceId" + serviceId + ",修改节点状态时查询到的nodes：" + JSON.toJSONString(nodes));
            if (!nodes.isEmpty()) {
                for (StatefulNode statefulNode : nodes) {
                    statefulNode.setNodeState(state);
                    if (null != nodesExtendedField && !nodesExtendedField.isEmpty()) {
                        Map<String, String> nodeExtended = nodesExtendedField.get(statefulNode.getNodeName());
                        updateNodeExtendedField(statefulNode, nodeExtended);
                    }
                    statefulNodeRepository.save(statefulNode);
                }
                return;
            }
            if (CommonConst.ACTION_CLUSTER_CREATE.equals(oldLastOpt)
                    && CommonConst.ACTION_CLUSTER_DELETE.equals(clusterLastOpt)) {
                return;
            }
            LOG.error("根据serviceId修改节点状态失败，获取的nodeList为empty，serviceId：" + serviceId + ",state:" + state);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "根据serviceId修改节点状态失败，获取的nodeList为empty，serviceId：" + serviceId + ",state:" + state);

        } catch (Exception e) {
            LOG.error("修改节点状态失败,serviceId：" + serviceId + ", " + "state:" + state + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "根据serviceId修改节点状态失败,serviceId：" + serviceId + ",state:" + state + ",error:" + e.getMessage());
        }
    }

    /**
     * 修改集群内单个节点状态为state
     *
     * @param nodeId
     * @param state
     */
    public void updateNodeState(String nodeId, String state, Map<String, String> nodeExtended)
            throws ErrorMessageException {
        LOG.info("根据nodeId修改节点状态,nodeId：" + nodeId + ",state:" + state + ",nodeExtended:" + nodeExtended);
        StatefulNode node = null;
        try {
            node = statefulNodeRepository.findByIdAndNodeStateNot(nodeId, CommonConst.STATE_NODE_DELETED);
            if (null != node) {
                node.setNodeState(state);
                updateNodeExtendedField(node, nodeExtended);
                statefulNodeRepository.save(node);
            } else {
                LOG.info("根据nodeId修改节点状态失败,获取的node为null，nodeId：" + nodeId + ", state:" + state + ", nodeExtended"
                        + nodeExtended);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                        "根据nodeId修改节点状态失败,获取的node为null，nodeId：" + nodeId + ", state:" + state + ", nodeExtended"
                                + nodeExtended);
            }
        } catch (Exception e) {
            LOG.error("修改节点状态失败,nodeId：" + nodeId + ", state:" + state + ", nodeExtended" + nodeExtended + ",error:",
                    e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "根据nodeId修改节点状态失败,nodeId：" + nodeId
                    + ", state:" + state + ", nodeExtended" + nodeExtended + ",error:" + e.getMessage());
        }
    }

    /**
     * 根据serviceId获取statefulNodeList
     *
     * @param serviceId
     * @return
     */
    public List<StatefulNode> getStatefulNodeListById(String serviceId) throws ErrorMessageException {
        LOG.info("根据serviceId获取StatefulNodeList，serviceId：" + serviceId);
        List<StatefulNode> statefulNodeList = null;
        try {
            statefulNodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
        } catch (Exception e) {
            LOG.error("根据serviceId获取StatefulNodeList失败,serviceId：" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "根据serviceId获取StatefulNodeList失败,serviceId：" + serviceId + ",error:" + e.getMessage());
        }
        return statefulNodeList;
    }

    /**
     * 修改集群状态
     *
     * @param serviceId
     * @param clusterState
     */
    public String updateClusterState(String serviceId, String state, Map<String, String> serviceExtendedField,
            String latestOpt) throws ErrorMessageException {
        LOG.info("根据serviceId修改集群状态，serviceId：" + serviceId + ",state:" + state);
        try {
            StatefulService service = getStatefulServiceById(serviceId);
            if (null != service) {
                String oldLatestOpt = service.getLastopt();
                service.setServiceState(state);
                if (CommonConst.STATE_CLUSTER_WAITING.equals(state)) {
                    service.setLastoptTime(new Date());
                }
                if (StringUtils.isNotEmpty(latestOpt)) {
                    service.setLastopt(latestOpt);
                }
                updateClusterExtendedField(service, serviceExtendedField);
                statefulServiceRepository.save(service);
                LOG.info("修改集群状态成功，serviceId：" + serviceId + ",state:" + state);
                return oldLatestOpt;
            }
            LOG.error("根据serviceId修改集群状态失败，获取的service为null" + serviceId + ",state:" + state);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "根据serviceId修改集群状态失败，获取的service为null" + serviceId + ",state:" + state);

        } catch (Exception e) {
            LOG.error("根据serviceId修改集群状态失败，serviceId：" + serviceId + ",state:" + state + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "根据serviceId修改集群状态失败，serviceId:" + serviceId + ",state:" + state + ",error:" + e.getMessage());

        }
    }

    /**
     * 同时修改集群和节点状态
     *
     * @param serviceId
     * @param clusterState
     * @param nodeState
     */
    public void updateClusterAndNodesState(String serviceId, String clusterState, String nodeState,
            String clusterLastOpt) throws ErrorMessageException {
        // 1、修改集群状态
        String oldLatestOpt = updateClusterState(serviceId, clusterState, null, clusterLastOpt);
        // 2、修改节点状态
        updateNodesState(serviceId, nodeState, null, clusterLastOpt, oldLatestOpt);
    }

    /**
     * 同时修改集群和节点状态和扩展字段
     *
     * @param serviceId
     * @param clusterState
     * @param nodeState
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void updateClusterAndNodesState(String serviceId, String clusterState, String nodeState,
            Map<String, String> serviceExtendedField, Map<String, Map<String, String>> nodesExtendedField)
            throws ErrorMessageException {
        // 1、修改集群状态
        updateClusterState(serviceId, clusterState, serviceExtendedField, null);
        // 2、修改节点状态
        updateNodesState(serviceId, nodeState, nodesExtendedField, null, null);
    }

    /**
     * 修改集群和单个节点的状态
     *
     * @param serviceId
     * @param clusterState
     * @param nodeState
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void updateClusterAndNodeState(String serviceId, String clusterState, String nodeId, String nodeState,
            String clusterLastOpt) {
        // 1、修改集群状态
        updateClusterState(serviceId, clusterState, null, clusterLastOpt);
        // 2、修改节点状态
        updateNodeState(nodeId, nodeState, null);
    }

    /**
     * 修改集群和单个节点的状态,和扩展字段
     *
     * @param serviceId
     * @param clusterState
     * @param nodeState
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void updateClusterAndNodeState(String serviceId, String clusterState, String nodeId, String nodeState,
            Map<String, String> serviceExtendedField, Map<String, String> nodeExtendedField) {
        // 1、修改集群状态
        updateClusterState(serviceId, clusterState, serviceExtendedField, null);
        // 2、修改节点状态
        updateNodeState(nodeId, nodeState, nodeExtendedField);
    }

    /**
     * 判断该集群是否是其他集群的依赖
     *
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */
    public boolean isDependenceService(String serviceId) throws ErrorMessageException {
        LOG.info("根据serviceId查询依赖表，serviceId：" + serviceId);
        List<StatefulServiceDependence> serviceList = null;
        try {
            serviceList = statefulServiceDependenceRepository.findByDependenceServiceId(serviceId);
        } catch (Exception e) {
            LOG.error("获取集群依赖表失败，serviceId:" + serviceId + ",error：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取集群依赖表失败，serviceId:" + serviceId + ",error：" + e.getMessage());
        }
        if (null == serviceList || serviceList.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 生成statefulServiceDependence
     *
     * @param serviceId
     * @param dependenceServiceId
     * @throws ErrorMessageException
     */
    public void buildStatefulServiceDependency(String serviceId, String dependenceServiceId)
            throws ErrorMessageException {
        LOG.info("根据serviceId和dependenceServiceId保存statefulServiceDependence，serviceId: " + serviceId
                + "dependenceServiceId: " + dependenceServiceId);
        StatefulServiceDependence statefulServiceDependence = new StatefulServiceDependence();
        try {
            statefulServiceDependence.setServiceId(serviceId);
            statefulServiceDependence.setDependenceServiceId(dependenceServiceId);
            statefulServiceDependenceRepository.save(statefulServiceDependence);
        } catch (Exception e) {
            LOG.error("保存statefulServiceDependence失败,serviceId：" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "保存statefulServiceDependence失败,serviceId：" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * 根据nodeId获取node
     *
     * @param nodeId
     * @return
     */
    public StatefulNode getStatefulNodeById(String nodeId) {
        LOG.info("根据nodeId获取StatefulNode，nodeId：" + nodeId);
        StatefulNode node = null;
        try {
            node = statefulNodeRepository.findByIdAndNodeStateNot(nodeId, CommonConst.STATE_NODE_DELETED);
        } catch (Exception e) {
            LOG.error("根据nodeId获取StatefulNode失败,nodeId：" + nodeId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "根据nodeId获取StatefulNode失败,nodeId：" + nodeId + ",error:" + e.getMessage());
        }
        return node;
    }

    /**
     * 根据serviceId及nodeName获取node
     *
     * @param serviceId
     * @param nodeName
     * @return
     */
    public StatefulNode getStatefulNodeByServiceIdAndNodeName(String serviceId, String nodeName) {
        LOG.info("根据serviceId及nodeName获取StatefulNode，serviceId：" + serviceId + "，nodeName：" + nodeName);
        StatefulNode node = null;
        try {
            node = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeStateNot(serviceId, nodeName,
                    CommonConst.STATE_NODE_DELETED);
        } catch (Exception e) {
            LOG.error("根据nodeId获取StatefulNode失败，serviceId：" + serviceId + "，nodeName：" + nodeName + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "根据nodeId获取StatefulNode失败，serviceId："
                    + serviceId + "，nodeName：" + nodeName + ",error:" + e.getMessage());
        }
        return node;
    }

    /**
     * k8s获取job
     *
     * @param tenantName
     * @param jobName
     * @return
     */
    public io.fabric8.kubernetes.api.model.batch.Job getJob(String tenantName, String jobName) {
        LOG.info("获取job，tanantName:" + tenantName + ",jobName:" + jobName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(jobName)) {
            LOG.error("jobName is empty!");
            return null;
        }
        io.fabric8.kubernetes.api.model.batch.Job job = null;
        try {
            job = KubernetesClientFactory.getClient().batch().jobs().inNamespace(tenantName).withName(jobName).get();
        } catch (Exception e) {
            LOG.error("通过K8S获取Job失败,nameSpace" + tenantName + ",jobName:" + jobName + ",error:", e);
            return null;
        }
        return job;
    }

    /**
     * 合并两个json串，只增不减
     *
     * @param object1
     * @param object2
     * @return
     */
    public JSONObject mergeJson(Object object1, Object object2) {
        if (object1 == null && object2 == null) {
            return null;
        }
        try {
            if (object1 == null) {
                return JSON.parseObject(object2.toString());
            }
            if (object2 == null) {
                return JSON.parseObject(object1.toString());
            }
            JSONObject jsonObject1 = JSON.parseObject(object1.toString());
            JSONObject jsonObject2 = JSON.parseObject(object2.toString());
            Iterator<String> iterator = jsonObject2.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value2 = jsonObject2.get(key);
                if (jsonObject1.containsKey(key)) {
                    Object value1 = jsonObject1.get(key);
                    if (!(value1 instanceof JSONObject)) {
                        jsonObject1.put(key, value2);
                    } else {
                        jsonObject1.put(key, mergeJson(value1, value2));
                    }
                } else {
                    jsonObject1.put(key, value2);
                }
            }
            return jsonObject1;
        } catch (JSONException e) {
            LOG.error("合并jsonObject失败，obj1：" + JSON.toJSONString(object1) + ",obj2：" + JSON.toJSONString(object2)
                    + ",error:" + e.getMessage());
            return null;
        }
    }

    /**
     * 解析修改的参数，从{group:{level:{key:value}}}中解析{key：value}
     *
     * @param configUpdatedJson
     * @param appType
     * @param version
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> parseConfigUpdatedForYaml(JSONObject configUpdatedJson, String appType, String version) {
        Map<String, String> config = new HashMap<>();
        for (Map.Entry<String, Object> entryGroup : configUpdatedJson.entrySet()) {
            JSONObject configUpdatedGroup = JSON.parseObject(JSON.toJSONString(entryGroup.getValue()));
            for (Map.Entry<String, Object> entryLevel : configUpdatedGroup.entrySet()) {
                Map<String, String> configUpdatedMap = JSON.parseObject(JSON.toJSONString(entryLevel.getValue()),
                        Map.class);
                if (null != configUpdatedMap && !configUpdatedMap.isEmpty()) {
                    config.putAll(configUpdatedMap);
                }
            }
        }
        config = multiplyUnitConfigMap(config, appType, version);
        return config;
    }

    /**
     * 在config key-value的基础上乘product，加单位
     *
     * @param map
     * @param appType
     * @param version
     * @return
     */
    public Map<String, String> multiplyUnitConfigMap(Map<String, String> map, String appType, String version) {

        List<StatefulServiceComponentDefaultConfig> configList = statefulServiceComponentDefaultConfigRepository
                .findByAppTypeAndVersion(appType, version);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            for (StatefulServiceComponentDefaultConfig statefulServiceComponentDefaultConfig : configList) {
                if (entry.getKey().equals(statefulServiceComponentDefaultConfig.getKey())) {
                    if (!"0".equals(String.valueOf(statefulServiceComponentDefaultConfig.getProduct()))
                            && !"null".equalsIgnoreCase(entry.getValue()) && StringUtils.isNotEmpty(entry.getValue())) {
                        entry.setValue(String.valueOf(Integer.parseInt(entry.getValue())
                                * statefulServiceComponentDefaultConfig.getProduct()));
                    }
                    if (StringUtils.isNotEmpty(statefulServiceComponentDefaultConfig.getUnit())
                            && !"NULL".equalsIgnoreCase(statefulServiceComponentDefaultConfig.getUnit())) {
                        entry.setValue(entry.getValue() + statefulServiceComponentDefaultConfig.getUnit());
                    }
                }
            }
        }
        return map;
    }

    public void checkClusterChangeConfigJsonObject(String appType, String version, JSONObject jsonObject)
            throws ErrorMessageException {
        // if (null == jsonObject ||
        // StringUtils.isEmpty(JSON.toJSONString(jsonObject))) {
        // throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
        // "集群修改配置时，配置参数为空,jsonObject:" + JSON.toJSONString(jsonObject));
        // }
        //
        // if (null == jsonObject || jsonObject.isEmpty()) {
        // throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
        // "集群修改配置时，配置参数为空,jsonObject:" + JSON.toJSONString(jsonObject));
        // }
        if (appType.equals(CommonConst.APPTYPE_REDIS) || appType.equals(CommonConst.APPTYPE_CODIS)
                || appType.equals(CommonConst.APPTYPE_MEMCACHED) || CommonConst.APPTYPE_POSTGRESQL.equals(appType)) {
            List<String> cnfFileList = statefulServiceComponentDefaultConfigRepository.getCnfFileList(appType, version);
            if (!cnfFileList.containsAll(jsonObject.keySet())) {
                LOG.error("集群修改配置时，配置参数不符合规范,jsonObject:" + JSON.toJSONString(jsonObject));
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "集群修改配置时，配置参数不符合规范,jsonObject:" + JSON.toJSONString(jsonObject));
            }
        } else {
            List<String> groupList = statefulServiceComponentDefaultConfigRepository.getGroupList(appType, version);
            if (!groupList.containsAll(jsonObject.keySet())) {
                LOG.error("集群修改配置时，配置参数不符合规范,jsonObject:" + JSON.toJSONString(jsonObject));
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "集群修改配置时，配置参数不符合规范,jsonObject:" + JSON.toJSONString(jsonObject));

            }
        }

    }

    /**
     * 创建集群时校验appType和version
     *
     * @param appType
     * @param version
     * @throws ErrorMessageException
     */
    public void checkVersion(String appType, String version) throws ErrorMessageException {
        if (StringUtils.isEmpty(version)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "创建集群参数校验失败，version为null");
        }

        List<String> versionList = new ArrayList<String>();
        try {
            versionList = statefulServiceUnitVersionRepository.findByAppType(appType);
        } catch (Exception e) {
            LOG.error("创建集群获取versionList失败！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "创建集群参数校验失败，获取versionList失败，appType:"
                    + appType + ",version:" + version + ",error：" + e.getMessage());
        }

        if (null != versionList && !versionList.isEmpty() && versionList.contains(version)) {
            LOG.info("创建集群参数校验，version校验成功");
        } else {
            LOG.error("创建集群参数校验失败，version不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "创建集群参数校验失败，version不存在,appType:" + appType + ",verison:" + version);
        }

    }

    /**
     *
     * @param appType
     * @param version
     * @param extendedField
     * @throws ErrorMessageException
     */
    public void checkVersion(String appType, String version, String extendedField) throws ErrorMessageException {
        if (StringUtils.isEmpty(version)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "创建集群参数校验失败，version为null");
        }

        List<String> versionList = null;
        try {
            versionList = statefulServiceUnitVersionRepository.findByAppTypeAndExtendedField(appType, extendedField);
        } catch (Exception e) {
            LOG.error("创建集群获取versionList失败！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "创建集群参数校验失败，获取versionList失败，appType:"
                    + appType + ",version:" + version + ",error：" + e.getMessage());
        }

        if (null != versionList && !versionList.isEmpty() && versionList.contains(version)) {
            LOG.info("创建集群参数校验，version校验成功");
        } else {
            LOG.error("创建集群参数校验失败，version不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "创建集群参数校验失败，version不存在,appType:" + appType + ",verison:" + version);
        }
    }

    /**
     * 删除集群时，根据serviceId删除依赖表
     *
     * @param serviceId
     * @throws ErrorMessageException
     */
    public void deleteStatefulServiceDependency(String serviceId) throws ErrorMessageException {
        LOG.info("根据serviceId和dependenceServiceId删除statefulServiceDependence，serviceId: " + serviceId);
        List<StatefulServiceDependence> statefulServiceDependenceList = null;
        try {
            statefulServiceDependenceList = statefulServiceDependenceRepository.findByServiceId(serviceId);
            if (null != statefulServiceDependenceList && !statefulServiceDependenceList.isEmpty()) {
                for (StatefulServiceDependence statefulServiceDependence : statefulServiceDependenceList) {
                    statefulServiceDependenceRepository.delete(statefulServiceDependence);
                    LOG.info("删除依赖表数据成功，service：" + JSON.toJSONString(statefulServiceDependence));
                }
            }
        } catch (Exception e) {
            LOG.error("根据serviceId删除statefulServiceDependence失败,serviceId：" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED,
                    "根据serviceId删除statefulServiceDependence失败,serviceId：" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * 对于扩展字段为key：value格式的集群，获取集群的扩展字段
     *
     * @param extendedField
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getServiceExtendedField(String extendedField) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotEmpty(extendedField)) {
            Map<String, String> extendedFieldMap = JSON.parseObject(extendedField, Map.class);
            if (null != extendedFieldMap && !extendedFieldMap.isEmpty()) {
                map.putAll(extendedFieldMap);
            }
        }
        return map;
    }

    /**
     * 对于扩展字段为key：value格式的集群，修改集群的扩展字段（ftp除外，在ftp自己的worker中有修改扩展字段函数）
     * 
     * @param service
     * @param serviceExtendedField
     */
    @SuppressWarnings("unchecked")
    public void updateClusterExtendedField(StatefulService service, Map<String, String> serviceExtendedField) {
        if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
            if (StringUtils.isNotEmpty(service.getExtendedField())) {
                String extendedField = service.getExtendedField();
                Map<String, String> extendedFieldMap = JSON.parseObject(extendedField, Map.class);
                extendedFieldMap.putAll(serviceExtendedField);
                service.setExtendedField(JSON.toJSONString(extendedFieldMap));
            } else {
                service.setExtendedField(JSON.toJSONString(serviceExtendedField));
            }
        }
    }

    /**
     * 对于扩展字段为key：value格式的节点，修改节点的扩展字段
     * 
     * @param service
     * @param serviceExtendedField
     */
    @SuppressWarnings("unchecked")
    public void updateNodeExtendedField(StatefulNode node, Map<String, String> nodeExtendedField) {
        if (null != nodeExtendedField && !nodeExtendedField.isEmpty()) {
            if (nodeExtendedField.containsKey("ip")) {
                if (StringUtils.isEmpty(node.getIp()) && StringUtils.isNotEmpty(nodeExtendedField.get("ip"))) {
                    node.setIp(nodeExtendedField.get("ip"));
                }
                nodeExtendedField.remove("ip");
            }
            if (nodeExtendedField.containsKey("port")) {
                if ((StringUtils.isEmpty(String.valueOf(node.getPort())) || 0 == node.getPort())
                        && StringUtils.isNotEmpty(nodeExtendedField.get("port"))
                        && !"0".equals(nodeExtendedField.get("port"))) {
                    node.setPort(Integer.parseInt(nodeExtendedField.get("port")));
                }
                nodeExtendedField.remove("port");
            }
            if (!nodeExtendedField.isEmpty()) {
                String extendedField = node.getExtendedField();
                if (!StringUtils.isEmpty(extendedField)) {
                    Map<String, String> extendedFieldMap = JSON.parseObject(extendedField, Map.class);
                    extendedFieldMap.putAll(nodeExtendedField);
                    node.setExtendedField(JSON.toJSONString(extendedFieldMap));
                }
                node.setExtendedField(JSON.toJSONString(nodeExtendedField));
            }
        }
    }

}
