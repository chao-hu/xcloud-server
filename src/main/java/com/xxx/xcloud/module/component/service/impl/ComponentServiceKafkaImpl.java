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
import com.xxx.xcloud.module.component.consts.KafkaClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.kafka.KafkaCluster;
import com.xxx.xcloud.utils.StringUtils;

@Service
public class ComponentServiceKafkaImpl extends BaseComponentServiceImpl {

    private static Logger LOG = LoggerFactory.getLogger(ComponentServiceKafkaImpl.class);

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {
        Map<String, String> map = new HashMap<>();

        LOG.info("kafka创建集群参数校验，tenantName：" + tenantName + ",jsonObject:" + JSON.toJSONString(jsonObject));
        // 1、参数校验
        String serviceName = jsonObject.getString("serviceName");
        String version = jsonObject.getString("version");
        String zkServiceName = jsonObject.getString("zkServiceName");

        String replicas = jsonObject.getString("replicas");
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        String projectId = jsonObject.getString("projectId");
        String orderId = jsonObject.getString("orderId");
        JSONObject configJSONObject = jsonObject.getJSONObject("configuration");
        String performance = jsonObject.getString("performance");
        String createdBy = jsonObject.getString("createdBy");

        // 参数校验
        StatefulService zkService = checkKafkaCreateParams(tenantName, version, replicas, cpu, memory, capacity,
                zkServiceName);

        if (null == zkService) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,获取zkService失败，tenantName:" + tenantName + ",zkServiceName:" + zkServiceName);
        }

        if (StringUtils.isEmpty(zkService.getExtendedField())) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "创建集群参数校验失败,获取zkService时未获取到zk扩展字段，tenantName:" + tenantName + ",appType：" + CommonConst.APPTYPE_ZK
                            + ",serviceName" + zkServiceName);
        }
        Map<String, String> extendedFieldMap = JSON.parseObject(zkService.getExtendedField(), Map.class);
        String zkExterAddress = extendedFieldMap.get("exterAddress");
        if (StringUtils.isEmpty(zkExterAddress)) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "创建集群参数校验失败,获取zkService时获取的zk外连接串为空，tenantName:" + tenantName + ",appType：" + CommonConst.APPTYPE_ZK
                            + ",serviceName" + zkServiceName);
        }

        if (jsonObject.containsKey("configuration")) {
            componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_KAFKA, version,
                    configJSONObject);
            map.put("configuration", jsonObject.getString("configuration"));
        }

        map.put("tenantName", tenantName);
        map.put("serviceName", serviceName);
        map.put("zkServiceId", zkService.getId());
        map.put("zkExterAddress", zkExterAddress);
        map.put("zkServiceName", zkServiceName);
        map.put("version", version);
        map.put("replicas", jsonObject.getString("replicas"));
        map.put("cpu", cpu);
        map.put("memory", memory);
        map.put("capacity", capacity);
        map.put("projectId", projectId);
        map.put("orderId", orderId);
        map.put("performance", performance);
        map.put("createdBy", createdBy);

        return map;
    }

    /**
     * 参数校验
     *
     * @param tenantName
     * @param version
     * @param replicas
     * @param cpu
     * @param memory
     * @param capacity
     * @param zkServiceName
     * @return
     */
    private StatefulService checkKafkaCreateParams(String tenantName, String version, String replicas, String cpu,
            String memory, String capacity, String zkServiceName) {
        StatefulService service = null;

        // 版本
        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_KAFKA, version);
        // 校验cpu，memory,capacity
        checkCpuAndMemory(cpu, memory);
        checkCapacity(capacity);
        // 校验其他参数
        checkReplicas(replicas);

        // 校验zkServiceName
        List<StatefulService> serviceList = null;
        try {
            serviceList = statefulServiceRepository.findByNamespaceAndAppTypeAndServiceNameAndServiceState(tenantName,
                    CommonConst.APPTYPE_ZK, zkServiceName, CommonConst.STATE_CLUSTER_RUNNING);
        } catch (Exception e) {
            LOG.error("创建集群参数校验失败,获取Running状态的zkService失败，tenantName:" + tenantName + ",appType："
                    + CommonConst.APPTYPE_ZK + ",serviceName" + zkServiceName);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "创建集群参数校验失败,获取Running状态的zkService失败，tenantName:" + tenantName + ",appType：" + CommonConst.APPTYPE_ZK
                            + ",serviceName" + zkServiceName);
        }
        if (null == serviceList || serviceList.isEmpty()) {
            LOG.error("创建集群参数校验失败,获取Running状态的zkService失败，获取的zkService为空，tenantName:" + tenantName + ",appType："
                    + CommonConst.APPTYPE_ZK + ",serviceName" + zkServiceName);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "创建集群参数校验失败,获取Running状态的zkService失败，获取的zkService为空，tenantName:" + tenantName + ",appType："
                            + CommonConst.APPTYPE_ZK + ",serviceName" + zkServiceName);
        }
        if (serviceList.size() > 1) {
            LOG.error("创建集群参数校验失败,获取的zkService返回多条");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_RETURN_MORE_FAILED,
                    "创建集群参数校验失败,获取Running状态的zkService返回了多条，tenantName:" + tenantName + ",appType："
                            + CommonConst.APPTYPE_ZK + ",serviceName" + zkServiceName);
        }
        if (serviceList.size() == 1) {
            LOG.info("zkService:" + JSON.toJSONString(service));
            service = serviceList.get(0);
        }
        return service;
    }

    @Override
    public StatefulService clusterCreate(Map<String, String> data) throws ErrorMessageException {
        LOG.info("创建集群，接收的参数=>" + JSON.toJSONString(data));

        // 1、获取参数
        String tenantName = data.get("tenantName");
        String serviceName = data.get("serviceName");
        String zkServiceId = data.get("zkServiceId");
        String zkExterAddress = data.get("zkExterAddress");
        String zkServiceName = data.get("zkServiceName");
        String version = data.get("version");
        int replicas = Integer.parseInt(data.get("replicas"));
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String configUpdated = data.get("configuration");
        String createdBy = data.get("createdBy");

        Double cpuTotal = cpu * replicas;
        Double memoryTotal = memory * replicas;
        Double capacityTotal = capacity * replicas;

        // 2、 修改tenant表

        // boolean result = tenantService.updateUsedResource(tenantName,
        // cpuTotal, memoryTotal, capacityTotal);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal +
        // ",updateMemory:" + memoryTotal
        // + ",updateCapacity:" + capacityTotal);

        // 3、存表：StatefulService
        StatefulService service = null;

        Map<String, String> extendedField = new HashMap<>();
        extendedField.put("zkServiceId", zkServiceId);
        extendedField.put("zkServiceName", zkServiceName);
        extendedField.put("zkExterAddress", zkExterAddress);
        extendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        extendedField.put(KafkaClusterConst.ZOOKEEPER_SERVERS,
                zkExterAddress.substring(5) + "/" + tenantName + "/" + CommonConst.APPTYPE_KAFKA + "/" + serviceName);

        try {
            service = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_KAFKA, serviceName, version, replicas, null, null, cpuTotal, memoryTotal,
                    capacityTotal, configUpdated, JSON.toJSONString(extendedField), CommonConst.ACTION_CLUSTER_CREATE);
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

        if (null == service) {
            LOG.error("创建集群时存StatefulService表返回为null，service:" + JSON.toJSONString(service));
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "创建集群时存StatefulService表返回为null,service:" + JSON.toJSONString(service));
        }

        // 4、存依赖表
        componentOperationsDataBaseUtil.buildStatefulServiceDependency(service.getId(), zkServiceId);

        // 5、拼接创建kafkaCluster的参数
        data.put("serviceId", service.getId());
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.CLUSTER_CREATE_WORKER, data);

        return service;
    }

    @Override
    public boolean clusterStop(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("停止集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 1、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("停止集群时修改集群和节点状态为waiting失败,serviceId:" + serviceId + ",error:", e);
            throw e;
        }

        // 2、 拼接kafkaCluster停止的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.CLUSTER_STOP_WORKER, params);
        return true;
    }

    @Override
    public boolean clusterStart(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("启动集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 1、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_START);
        } catch (ErrorMessageException e) {
            LOG.error("启动集群时修改集群和节点状态为waiting失败,serviceId:" + serviceId + ",error:", e);
            throw e;
        }

        // 3、 拼接kafkaCluster启动的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.CLUSTER_START_WORKER, params);
        return true;
    }

    @Override
    public boolean clusterDelete(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("删除集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 1、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_DELETE);
        } catch (ErrorMessageException e) {
            LOG.error("删除集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 4、 拼接kafkaCluster删除的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.CLUSTER_DELETE_WORKER, params);
        return true;
    }

    @Override
    protected Map<String, String> checkClusterExpandPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("集群扩展节点，接收的参数=>tenantName:" + tenantName + ",service:" + serviceId + ",josnObject:"
                + JSON.toJSONString(jsonObject));
        Map<String, String> map = new HashMap<>();

        if (null == jsonObject || StringUtils.isEmpty(JSON.toJSONString(jsonObject))) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
                    "集群扩展节点参数校验失败，jsonObject为空，jsonObject:" + JSON.toJSONString(jsonObject));
        }

        String addNum = jsonObject.getString("addNum");
        checkAddReplicas(addNum);

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("addNum", addNum);
        return map;
    }

    @Override
    public boolean clusterExpand(Map<String, String> data) throws ErrorMessageException {
        LOG.info("集群扩展节点，接收的参数=>data:" + JSON.toJSONString(data));

        String serviceId = data.get("serviceId");
        // 1、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_WAITING, null,
                    CommonConst.ACTION_CLUSTER_EXPAND);
        } catch (ErrorMessageException e) {
            LOG.error("集群扩展节点时修改集群状态为waiting失败,serviceId:" + serviceId + ",error:", e);
            throw e;
        }

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.CLUSTER_EXPAND_NODE_WORKER,
                data);
        return true;
    }

    @Override
    protected Map<String, String> checkClusterChangeResourcePersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("kafka集群修改资源个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + jsonObject);
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        checkCpuAndMemory(cpu, memory);
        checkCapacity(capacity);

        Map<String, String> map = new HashMap<>();
        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("cpu", cpu);
        map.put("memory", memory);
        map.put("capacity", capacity);
        return map;
    }

    @Override
    public boolean changeResource(Map<String, String> data) throws ErrorMessageException {
        LOG.info("集群修改资源，接收的参数=>data:" + JSON.toJSONString(data));

        String serviceId = data.get("serviceId");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        String tenantName = data.get("tenantName");

        // 1、获取旧的kafkaCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改资源时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改资源时根据serviceId获取statefulService为null");
        }

        KafkaCluster oldCluster = componentOperationsClientUtil.getKafkaCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取kafkacluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取kafkacluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、 修改tenant表
        // Double updateCpu = cpu * service.getNodeNum() - service.getCpu();
        // Double updateMemory = memory * service.getNodeNum() -
        // service.getMemory();
        // Double updateCapacity = capacity * service.getNodeNum() -
        // service.getStorage();
        // boolean result = tenantService.updateUsedResource(tenantName,
        // updateCpu, updateMemory, updateCapacity);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + updateCpu +
        // ",updateMemory:" + updateMemory
        // + ",updateCapacity:" + updateCapacity);
        // if (!result) {
        // return false;
        // }

        // 3、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);

        KafkaCluster newCluster = buildClusterChangeResource(tenantName, service, cpu, memory, capacity, oldCluster);

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateKafkaCluster(tenantName, newCluster);

        if (!updateResult) {
            // 修改yaml失败tenant表回滚
            // result = tenantService.updateUsedResource(tenantName, updateCpu *
            // (-1), updateMemory * (-1),
            // updateCapacity * (-1));
            // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
            // updateCpu * (-1) + ",updateMemory:"
            // + updateMemory * (-1) + ",updateCapacity:" + updateCapacity *
            // (-1));
            // 同步集群节点状态
            componentOperationsClientUtil.changeKafkaClusterAndNodesStateByYaml(tenantName, service.getId(),
                    service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                    "集群修改资源时，更新cluster失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",newCluster:"
                            + JSON.toJSONString(newCluster));
        }

        if (StringUtils.isNotEmpty(data.get("capacity"))) {
            componentOperationsClientUtil.repleaceKafkaClusterLvm(tenantName, newCluster);
        }

        boolean updateServiceFlag = false;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                int nodeNum = service.getNodeNum();
                if (StringUtils.isNotEmpty(String.valueOf(cpu))) {
                    service.setCpu(cpu * nodeNum);
                }
                if (StringUtils.isNotEmpty(String.valueOf(memory))) {
                    service.setMemory(memory * nodeNum);
                }
                if (StringUtils.isNotEmpty(String.valueOf(capacity))) {
                    service.setStorage(capacity * nodeNum);
                }
                statefulServiceRepository.save(service);
                updateServiceFlag = true;
            }
        } catch (ErrorMessageException e) {
            LOG.error("集群修改资源失败:", e);
            throw e;
        } catch (Exception e) {
            LOG.error("集群修改资源时修改集群资源和状态失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群修改资源时修改集群资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }
        finally {
            if (!updateServiceFlag) {
                LOG.error("集群修改资源时失败，同步集群和节点状态，tenantName：" + tenantName);
                // 同步集群节点状态
                componentOperationsClientUtil.changeKafkaClusterAndNodesStateByYaml(tenantName, service.getId(),
                        service.getServiceName());
            }
        }

        List<StatefulNode> nodeList = null;
        boolean updateNodeFlag = false;
        try {
            nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId, CommonConst.STATE_NODE_DELETED);
            if (null == nodeList || nodeList.isEmpty()) {
                LOG.error("修改资源时根据serviceId获取nodeList为空，serviceId：" + serviceId);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                        "修改资源时根据serviceId获取nodeList为空，serviceId：" + serviceId);
            }
            for (StatefulNode statefulNode : nodeList) {
                statefulNode.setCpu(cpu);
                statefulNode.setMemory(memory);
                statefulNode.setStorage(capacity);
                statefulNodeRepository.save(statefulNode);
            }
            updateNodeFlag = true;
        } catch (Exception e) {
            LOG.error("集群修改资源时修改节点资源和状态失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群修改资源时修改节点资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }
        finally {
            if (!updateNodeFlag) {
                LOG.error("集群修改资源时失败，同步集群和节点状态，tenantName：" + tenantName);
                // 同步集群节点状态
                componentOperationsClientUtil.changeKafkaClusterAndNodesStateByYaml(tenantName, service.getId(),
                        service.getServiceName());
            }
        }
        // 5、 拼接kafkaCluster修改资源的参数
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.CLUSTER_CHANGE_RESOURCE_WORKER,
                data);
        return true;
    }

    private KafkaCluster buildClusterChangeResource(String tenantName, StatefulService service, Double cpu,
            Double memory, Double capacity, KafkaCluster kafkaCluster) {
        kafkaCluster.getSpec().getKafkaop().setOperator(KafkaClusterConst.OPERATOR_CHANGE_RESOURCE);

        Resources resources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        kafkaCluster.getSpec().setResources(resources);

        if (StringUtils.isNotEmpty(String.valueOf(capacity))) {
            kafkaCluster.getSpec().setCapacity(capacity + CommonConst.UNIT_GI);
        }
        return kafkaCluster;
    }

    @Override
    protected Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("kafka集群修改配置个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + jsonObject);
        Map<String, String> map = new HashMap<>();

        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        String version = service.getVersion();
        componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_KAFKA, version, jsonObject);

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("json", JSON.toJSONString(jsonObject));
        return map;
    }

    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        LOG.info("kafka修改配置，data：" + JSON.toJSONString(data));

        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        JSONObject jsonObject = JSON.parseObject(data.get("json"));

        // 1、获取旧的kafkaCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改配置时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改配置时根据serviceId获取statefulService为null");
        }

        KafkaCluster oldCluster = componentOperationsClientUtil.getKafkaCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改配置时根据tenantName和serviceName获取kafkaCluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改配置时根据tenantName和serviceName获取kafkaCluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、修改数据库中集群和节点状态
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

        // 3、修改kafkaCluster
        KafkaCluster newCluster = buildClusterChangeConfig(tenantName, service.getServiceName(), oldCluster,
                mergedUpdatedJson, service.getVersion());

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateKafkaCluster(tenantName, newCluster);

        componentOperationsClientUtil.changeKafkaClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());
        if (!updateResult) {
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                    "集群修改配置时，更新cluster失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",newCluster:"
                            + JSON.toJSONString(newCluster));
        }

        try {
            service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
            if (null != service) {
                service.setConfigUpdated(JSON.toJSONString(mergedUpdatedJson));
                Map<String, String> extendedField = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
                extendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                service.setExtendedField(JSON.toJSONString(extendedField));
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("修改配置时修改StatefulService表配置失败，serviceId：" + serviceId + ",configUpdated:"
                    + JSON.toJSONString(mergedUpdatedJson), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "修改配置失败，serviceId：" + serviceId
                    + ",configUpdated:" + JSON.toJSONString(mergedUpdatedJson) + ",error:" + e.getMessage());
        }
        return true;
    }

    private KafkaCluster buildClusterChangeConfig(String tenantName, String serviceName, KafkaCluster kafkaCluster,
            JSONObject configUpdatedJson, String version) {
        Map<String, String> changedUnitMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configUpdatedJson,
                CommonConst.APPTYPE_KAFKA, version);

        kafkaCluster.getSpec().getConfig().setKafkacnf(changedUnitMap);
        kafkaCluster.getSpec().getKafkaop().setOperator(KafkaClusterConst.OPERATOR_CHANGE_CONFIG);
        return kafkaCluster;
    }

    @Override
    public boolean nodeStart(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("启动节点，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);

        // 1、获取statefulNode
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("启动节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "启动节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
        }

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_START);
        } catch (ErrorMessageException e) {
            LOG.error("启动节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.NODE_START_WORKER, params);
        return true;
    }

    @Override
    public boolean nodeStop(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("停止节点，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);
        // 1、获取statefulNode
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("停止节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "停止节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
        }

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("启动节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.NODE_STOP_WORKER, params);
        return true;
    }

    @Override
    public boolean nodeDelete(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("删除节点，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);
        // 1、获取statefulNode
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("删除节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "删除节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
        }

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_DELETE);
        } catch (ErrorMessageException e) {
            LOG.error("删除节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_KAFKA, KafkaClusterConst.NODE_DELETE_WORKER, params);
        return true;
    }

}
