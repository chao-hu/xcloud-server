package com.xxx.xcloud.module.component.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.RedisClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.lvm.Lvm;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;
import com.xxx.xcloud.utils.StringUtils;

@Service
public class ComponentServiceRedisImpl extends BaseComponentServiceImpl {

    private static Logger LOG = LoggerFactory.getLogger(ComponentServiceRedisImpl.class);

    @Override
    public StatefulService clusterCreate(Map<String, String> data) throws ErrorMessageException {
        // 1. 解析data参数
        LOG.info("接收data参数 : " + data.toString());

        String tenantName = data.get("tenantName");
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String createdBy = data.get("createdBy");
        String serviceName = data.get("serviceName");
        String version = data.get("version");
        String password = data.get("password");
        String type = data.get("type");
        String configuration = data.get("configuration");

        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));
        double cpuTotal = cpu * replicas;
        double memoryTotal = memory * replicas;
        double capacityTotal = capacity * replicas;

        // 2、 修改tenant表
        // boolean result = tenantService.updateUsedResource(tenantName,
        // cpuTotal, memoryTotal, capacityTotal);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal +
        // ",updateMemory:" + memoryTotal
        // + ",updateCapacity:" + capacityTotal);

        // 3. 封装statefuleService存表

        String extendedFiled = "";
        Map<String, String> extendedFiledMap = new HashMap<>();
        extendedFiledMap.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);

        extendedFiled = JSON.toJSONString(extendedFiledMap);

        StatefulService statefuleService = null;
        try {
            statefuleService = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_REDIS, serviceName, version, replicas, password, type, cpuTotal, memoryTotal,
                    capacityTotal, configuration, extendedFiled, CommonConst.ACTION_CLUSTER_CREATE);
        } catch (ErrorMessageException e) {
            LOG.error("创建集群失败", e.getMessage());
            // 集群创建失败tenent回滚
            // result = tenantService.updateUsedResource(tenantName, cpuTotal *
            // (-1), memoryTotal * (-1),
            // capacityTotal * (-1));
            // LOG.info("kafka集群创建存表失败回滚，修改tenant表结果：result:" + result +
            // ",updateCpu:" + cpuTotal * (-1) + ",updateMemory:"
            // + memoryTotal * (-1) + ",updateCapacity:" + capacityTotal *
            // (-1));
            throw e;
        }

        if (null == statefuleService) {
            LOG.error("创建集群时存StatefulService表返回为null，service:" + JSON.toJSONString(statefuleService));
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "创建集群时存StatefulService表返回为null,service:" + JSON.toJSONString(statefuleService));
        }

        // 4. 将创建任务加入线程池
        data.put("serviceId", statefuleService.getId());
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_CREATE_WORKER,
                data);

        // 5. 返回结果
        return statefuleService;
    }

    @Override
    public boolean clusterStop(String tenantName, String serviceId) throws ErrorMessageException {
        // 1. 修改statefuleService、statefuleNode表状态为waiting
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("停止集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 2、 拼接redisCluster停止的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_STOP_WORKER, data);

        return true;
    }

    @Override
    public boolean clusterStart(String tenantName, String serviceId) throws ErrorMessageException {
        // 1. 修改statefuleService、statefuleNode表状态为waiting
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_START);
        } catch (ErrorMessageException e) {
            LOG.error("启动集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 2、 拼接redisCluster启动的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_START_WORKER,
                data);

        return true;
    }

    @Override
    public boolean clusterExpand(Map<String, String> data) throws ErrorMessageException {

        // 1. 修改statefuleService表状态为waiting
        String serviceId = data.get("serviceId");
        try {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_WAITING, null,
                    CommonConst.ACTION_CLUSTER_EXPAND);
        } catch (ErrorMessageException e) {
            LOG.error("集群增加节点时修改集群状态为waiting失败,error:", e);
            throw e;
        }

        // 2、添加redisCluster增加节点的线程
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_EXPAND_NODE_WORKER,
                data);

        return true;
    }

    @Override
    public boolean clusterDelete(String tenantName, String serviceId) throws ErrorMessageException {

        // 1. 修改statefuleService、statefuleNode表状态为waiting
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_DELETE);
        } catch (ErrorMessageException e) {
            LOG.error("删除集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 2、 拼接redisCluster删除的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_DELETE_WORKER,
                data);

        return true;
    }

    @Override
    public boolean changeResource(Map<String, String> data) throws ErrorMessageException {

        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));

        // 1、获取旧的service
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改资源时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改资源时根据serviceId获取statefulService为null");
        }
        // 2、计算修改资源后集群总资源
        // double oldTotalCpu = service.getCpu();
        // double oldTotalMemory = service.getMemory();
        // double oldTotalCapacity = service.getStorage();
        int replicas = service.getNodeNum();
        double newTotalCpu;
        double newTotalMemory;
        double newTotalCapacity;

        newTotalCpu = cpu * replicas;
        newTotalMemory = memory * replicas;
        newTotalCapacity = capacity * replicas;

        // 3、 修改tenant表资源
        // Double updateCpu = newTotalCpu - oldTotalCpu;
        // Double updateMemory = newTotalMemory - oldTotalMemory;
        // Double updateCapacity = newTotalCapacity - oldTotalCapacity;
        // boolean result = tenantService.updateUsedResource(tenantName,
        // updateCpu, updateMemory, updateCapacity);
        // LOG.info("修改tenant表结果：" + result);

        // 4、获取集群与节点修改资源前状态
        String oldClusterState = service.getServiceState();
        Map<String, String> oldNodesState = new HashMap<>();
        List<StatefulNode> nodeList = null;
        nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId, CommonConst.STATE_NODE_DELETED);
        if (null == nodeList || nodeList.isEmpty()) {
            LOG.error("修改资源时根据serviceId获取oldNodesList为空，serviceId：" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "修改资源时根据serviceId获取oldNodesList为空，serviceId：" + serviceId);
        }
        for (StatefulNode statefulNode : nodeList) {
            oldNodesState.put(statefulNode.getNodeName(), statefulNode.getNodeState());
        }

        // 5、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);
        // 6、拼接redisCluster、修改yaml
        RedisCluster redisCluster = buildRedisCluster(tenantName, service.getServiceName(), data);
        boolean updateYamlResult = componentOperationsClientUtil.updateRedisCluster(tenantName, redisCluster);
        try {
            if (updateYamlResult) {

                // 修改节点资源
                for (StatefulNode statefulNode : nodeList) {
                    String capacityString = data.get("capacity");
                    if (!capacityString.equals(String.valueOf(statefulNode.getStorage()))) {
                        Lvm oldLvm = componentOperationsClientUtil.getLvm(tenantName, statefulNode.getLvmName());
                        LOG.info("获取旧的lvm：" + JSON.toJSONString(oldLvm));
                        if (null == oldLvm) {
                            String nodeIp = redisCluster.getStatus().getBindings().get(statefulNode.getLvmName())
                                    .getBindIp();
                            componentOperationsClientUtil.registerLvm(tenantName, statefulNode.getLvmName(), nodeIp,
                                    capacityString);
                        } else {
                            oldLvm.getSpec().setSize(capacityString);
                            componentOperationsClientUtil.replaceLvm(tenantName, oldLvm);
                        }
                    }

                    statefulNode.setCpu(cpu);
                    statefulNode.setMemory(memory);
                    statefulNode.setStorage(capacity);
                    if (!CommonConst.STATE_CLUSTER_RUNNING.equals(oldClusterState)) {
                        statefulNode.setNodeState(oldNodesState.get(statefulNode.getNodeName()));
                    }
                    statefulNodeRepository.save(statefulNode);
                }
                LOG.info("集群修改资源时修改节点资源成功");

                // 修改集群资源
                service.setCpu(newTotalCpu);
                service.setMemory(newTotalMemory);
                service.setStorage(newTotalCapacity);
                if (!CommonConst.STATE_CLUSTER_RUNNING.equals(oldClusterState)) {
                    Map<String, String> extendedField = componentOperationsDataBaseUtil
                            .getServiceExtendedField(service.getExtendedField());
                    extendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    service.setExtendedField(JSON.toJSONString(extendedField));
                    service.setServiceState(oldClusterState);
                }
                statefulServiceRepository.save(service);
                LOG.info("集群修改资源时修改集群资源成功");
            } else {
                // 修改节点状态
                for (StatefulNode statefulNode : nodeList) {
                    statefulNode.setNodeState(oldNodesState.get(statefulNode.getNodeName()));
                    statefulNodeRepository.save(statefulNode);
                }
                // 修改集群状态
                service.setServiceState(oldClusterState);
                statefulServiceRepository.save(service);
                LOG.info("集群修改资源失败，将集群与节点状态修改为之前的状态");

                // 修改tenantName表资源
                // boolean rollBackResult =
                // tenantService.updateUsedResource(tenantName, updateCpu *
                // (-1),
                // updateMemory * (-1), updateCapacity * (-1));
                // LOG.info("集群修改资源失败后，修改tenant表结果：" + rollBackResult);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                        "集群修改资源时，更新cluster失败，tenantName：" + tenantName + ",serviceId:" + serviceId);
            }
        } catch (ErrorMessageException e) {
            LOG.error("集群修改资源时修改集群状态与资源失败,error:", e);
            // 修改节点状态
            for (StatefulNode statefulNode : nodeList) {
                statefulNode.setNodeState(oldNodesState.get(statefulNode.getNodeName()));
                statefulNodeRepository.save(statefulNode);
            }
            // 修改集群状态
            service.setServiceState(oldClusterState);
            statefulServiceRepository.save(service);
            LOG.info("集群修改资源失败，将集群与节点状态修改为之前的状态");
            throw e;
        }

        if (CommonConst.STATE_CLUSTER_RUNNING.equals(oldClusterState)) {
            componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_REDIS,
                    RedisClusterConst.REDIS_CLUSTER_CHANGERESOURCE_WORKER, data);
        }
        return true;
    }

    private RedisCluster buildRedisCluster(String tenantName, String serviceName, Map<String, String> data) {

        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));

        RedisCluster redisCluster = null;
        redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);
        if (null == redisCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取redisCluster为空，tenantName：" + tenantName + ",serviceName:"
                    + serviceName);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取redisCluster为空，tenantName：" + tenantName + ",serviceName:"
                            + serviceName);
        }
        Resources redisResources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        redisCluster.getSpec().setResources(redisResources);
        redisCluster.getSpec().setCapacity(capacity + CommonConst.UNIT_GI);

        LOG.info("集群修改资源：构建redisCluster成功, serviceName:" + serviceName);
        return redisCluster;
    }

    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        LOG.info("redis修改配置，data：" + JSON.toJSONString(data));

        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        JSONObject jsonObject = JSON.parseObject(data.get("json"));

        // 1、获取旧的redisCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改配置时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改配置时根据serviceId获取statefulService为null");
        }

        RedisCluster oldCluster = componentOperationsClientUtil.getRedisCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改配置时根据tenantName和serviceName获取rediscluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改配置时根据tenantName和serviceName获取rediscluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、获取集群与节点修改资源前状态
        String oldClusterState = service.getServiceState();
        Map<String, String> oldNodesState = new HashMap<>();
        List<StatefulNode> nodeList = null;
        nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId, CommonConst.STATE_NODE_DELETED);
        if (null == nodeList || nodeList.isEmpty()) {
            LOG.error("修改配置时根据serviceId获取oldNodesList为空，serviceId：" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "修改配置时根据serviceId获取oldNodesList为空，serviceId：" + serviceId);
        }
        for (StatefulNode statefulNode : nodeList) {
            oldNodesState.put(statefulNode.getNodeName(), statefulNode.getNodeState());
        }

        // 3、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGECONFIG);

        JSONObject mergedUpdatedJson = null;

        if (StringUtils.isNotEmpty(service.getConfigUpdated())) {
            JSONObject oldJsonObject = JSON.parseObject(service.getConfigUpdated());
            mergedUpdatedJson = componentOperationsDataBaseUtil.mergeJson(oldJsonObject, jsonObject);
        } else {
            mergedUpdatedJson = JSON.parseObject(data.get("json"));
        }

        LOG.info("集群merge后的config为：" + JSON.toJSONString(mergedUpdatedJson) + ",tenantName:" + tenantName
                + ",serviceName:" + service.getServiceName());

        // 3、修改rediscluster
        RedisCluster newCluster = buildClusterChangeConfig(tenantName, service.getServiceName(), oldCluster,
                mergedUpdatedJson, service.getVersion());

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateRedisCluster(tenantName, newCluster);

        if (!updateResult) {
            // 修改节点状态
            for (StatefulNode statefulNode : nodeList) {
                statefulNode.setNodeState(oldNodesState.get(statefulNode.getNodeName()));
                statefulNodeRepository.save(statefulNode);
            }
            // 修改集群状态
            service.setServiceState(oldClusterState);
            statefulServiceRepository.save(service);
            LOG.info("集群修改配置失败，将集群与节点状态修改为之前的状态");
            return false;
        }

        try {
            service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
            if (null != service) {
                service.setConfigUpdated(JSON.toJSONString(mergedUpdatedJson));
                Map<String, String> extendedField = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
                extendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                service.setExtendedField(JSON.toJSONString(extendedField));
                service.setServiceState(oldClusterState);
                statefulServiceRepository.save(service);
            }
            // 修改节点状态
            for (StatefulNode statefulNode : nodeList) {
                statefulNode.setNodeState(oldNodesState.get(statefulNode.getNodeName()));
                statefulNodeRepository.save(statefulNode);
            }
        } catch (Exception e) {
            LOG.error("修改配置时修改StatefulService表配置失败，serviceId：" + serviceId + ",configUpdated:"
                    + JSON.toJSONString(mergedUpdatedJson), e);
            // 修改节点状态
            for (StatefulNode statefulNode : nodeList) {
                statefulNode.setNodeState(oldNodesState.get(statefulNode.getNodeName()));
                statefulNodeRepository.save(statefulNode);
            }
            // 修改集群状态
            service.setServiceState(oldClusterState);
            statefulServiceRepository.save(service);
            LOG.info("集群修改配置失败，将集群与节点状态修改为之前的状态");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "修改配置失败，serviceId：" + serviceId
                    + ",configUpdated:" + JSON.toJSONString(mergedUpdatedJson) + ",error:" + e.getMessage());
        }
        return true;
    }

    private RedisCluster buildClusterChangeConfig(String tenantName, String serviceName, RedisCluster redisCluster,
            JSONObject configUpdatedJson, String version) throws ErrorMessageException {

        Map<String, String> changedUnitMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configUpdatedJson,
                CommonConst.APPTYPE_REDIS, version);

        redisCluster.getSpec().setConfigMap(changedUnitMap);
        return redisCluster;
    }

    @Override
    public Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {

        Map<String, String> data = new HashMap<>();
        LOG.info("--------开始参数校验--------");
        // 1. 组件统一校验
        String serviceName = jsonObject.getString("serviceName");
        String performance = jsonObject.getString("performance");
        String projectId = jsonObject.getString("projectId");
        String orderId = jsonObject.getString("orderId");
        String createdBy = jsonObject.getString("createdBy");
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        checkCpuAndMemory(cpu, memory);

        // 2. redis所有type统一校验
        String type = jsonObject.getString("type");
        String password = jsonObject.getString("password");
        String capacity = jsonObject.getString("capacity");
        String replicas = jsonObject.getString("replicas");
        checkCapacity(capacity);
        checkPassword(password);
        checkReplicas(replicas);
        checkType(CommonConst.APPTYPE_REDIS, type);

        JSONObject configuration = jsonObject.getJSONObject("configuration");

        String version = jsonObject.getString("version");
        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_REDIS, version);
        if (jsonObject.containsKey("configuration")) {
            componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_REDIS, version,
                    configuration);
            data.put("configuration", JSON.toJSONString(configuration));
        }

        data.put("tenantName", tenantName);
        data.put("serviceName", serviceName);
        data.put("cpu", cpu);
        data.put("memory", memory);
        data.put("type", type.toLowerCase());
        data.put("password", password);
        data.put("capacity", capacity);
        data.put("replicas", replicas);
        data.put("version", version);
        data.put("projectId", projectId);
        data.put("orderId", orderId);
        data.put("createdBy", createdBy);
        data.put("performance", performance);
        LOG.info("--------结束参数校验--------");

        return data;
    }

    @Override
    protected Map<String, String> checkClusterExpandPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        String type = null;
        type = service.getType();
        String replicas = jsonObject.getString("replicas");
        checkReplicas(replicas);

        if (RedisClusterConst.REDIS_TYPE_SINGLE.equals(type)) {
            LOG.error("参数校验失败,type: " + type + "，不允许节点操作");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "参数校验失败,type: " + type + "，不允许节点操作");

        }
        data.put("replicas", replicas);
        return data;
    }

    @Override
    protected Map<String, String> checkClusterChangeResourcePersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("jsonObject" + JSON.toJSONString(jsonObject));

        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        checkCpuAndMemory(cpu, memory);
        checkCapacity(capacity);

        data.put("cpu", cpu);
        data.put("memory", memory);
        data.put("capacity", capacity);
        return data;
    }

    @Override
    protected Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("redis集群修改配置个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + jsonObject);
        Map<String, String> map = new HashMap<>();
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        String version = service.getVersion();
        componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_REDIS, version, jsonObject);
        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("json", JSON.toJSONString(jsonObject));
        return map;
    }

    @Override
    public boolean nodeStart(String tenantName, String nodeId) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean nodeStop(String tenantName, String nodeId) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean nodeDelete(String tenantName, String nodeId) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }
}
