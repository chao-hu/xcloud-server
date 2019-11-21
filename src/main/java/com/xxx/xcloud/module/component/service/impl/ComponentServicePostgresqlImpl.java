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
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;
import com.xxx.xcloud.utils.StringUtils;

@Service
public class ComponentServicePostgresqlImpl extends BaseComponentServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentServicePostgresqlImpl.class);

    @Override
    protected Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {
        LOG.info("集群创建个性化参数校验，tenantName:" + tenantName + ",jsonObject:" + JSON.toJSONString(jsonObject));

        Map<String, String> map = new HashMap<>();

        // 1、参数校验
        String serviceName = jsonObject.getString("serviceName");
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        String type = jsonObject.getString("type");
        String version = jsonObject.getString("version");
        String password = jsonObject.getString("password");
        String replicas = jsonObject.getString("replicas");
        JSONObject configJSONObject = jsonObject.getJSONObject("configuration");

        String projectId = jsonObject.getString("projectId");
        String orderId = jsonObject.getString("orderId");
        String performance = jsonObject.getString("performance");
        String createdBy = jsonObject.getString("createdBy");

        // 2、参数校验
        checkPostgresqlCreateParams(version, type, password, cpu, memory, capacity, replicas);

        if (jsonObject.containsKey("configuration")) {
            componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_POSTGRESQL, version,
                    configJSONObject);
            map.put("configuration", JSON.toJSONString(configJSONObject));
        }

        map.put("tenantName", tenantName);
        map.put("serviceName", serviceName);
        map.put("cpu", cpu);
        map.put("memory", memory);
        map.put("capacity", capacity);
        map.put("type", type.toLowerCase());
        map.put("version", version);
        map.put("password", password);
        map.put("replicas", replicas);
        map.put("projectId", projectId);
        map.put("orderId", orderId);
        map.put("performance", performance);
        map.put("createdBy", createdBy);
        return map;
    }

    private void checkPostgresqlCreateParams(String version, String type, String password, String cpu, String memory,
            String capacity, String replicas) {
        // 校验version
        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_POSTGRESQL, version);

        // 校验cpu，memory,capacity
        checkCpuAndMemory(cpu, memory);
        checkCapacity(capacity);

        // 校验type,replicas,password
        checkType(CommonConst.APPTYPE_POSTGRESQL, type);
        checkReplicas(replicas);
        checkPassword(password);
        int typeInt = Integer.parseInt(replicas);
        if (typeInt == 1 && !PostgresqlClusterConst.TYPE_SINGLE.equals(type)) {
            LOG.error("Postgresql集群的replicas与type不匹配，replicas为1的type应为single，而接收的参数type为" + type);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "Postgresql集群的replicas与type不匹配，replicas为1的type应为single，而接收的参数type为" + type + "");
        }

        if (typeInt > 1 && PostgresqlClusterConst.TYPE_SINGLE.equals(type)) {
            LOG.error("Postgresql集群的replicas与type不匹配，type为single的replicas应为1，而接收的参数replicas为" + replicas);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "Postgresql集群的replicas与type不匹配，type为single的replicas应为1，而接收的参数replicas为" + replicas);
        }
    }

    @Override
    public StatefulService clusterCreate(Map<String, String> data) throws ErrorMessageException {
        LOG.info("创建集群，接收的参数=>data:" + JSON.toJSONString(data));

        // 1、接收参数
        String tenantName = data.get("tenantName");
        String serviceName = data.get("serviceName");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));

        String type = data.get("type");
        String version = data.get("version");
        String password = data.get("password");
        int replicas = Integer.parseInt(data.get("replicas"));

        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String configUpdated = data.get("configuration");
        String createdBy = data.get("createdBy");

        Double cpuTotal = cpu * replicas;
        Double memoryTotal = memory * replicas;
        Double capacityTotal = capacity * replicas;

        // 2、 修改tenant表
//        boolean result = tenantService.updateUsedResource(tenantName, cpuTotal, memoryTotal, capacityTotal);
//        LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal + ",updateMemory:" + memoryTotal
//                + ",updateCapacity:" + capacityTotal);

        // 3、存表：StatefulService
        StatefulService service = null;
        Map<String, String> extendedField = new HashMap<>();
        extendedField.put("user", "postgres");
        extendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);

        try {
            service = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_POSTGRESQL, serviceName, version, replicas, password, type, cpuTotal,
                    memoryTotal, capacityTotal, configUpdated, JSON.toJSONString(extendedField),
                    CommonConst.ACTION_CLUSTER_CREATE);
        } catch (ErrorMessageException e) {
            LOG.error("创建集群失败", e.getMessage());
            // 集群创建失败tenent回滚
//            result = tenantService.updateUsedResource(tenantName, cpuTotal * (-1), memoryTotal * (-1),
//                    capacityTotal * (-1));
//            LOG.info("postgresql集群创建存表失败回滚，修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal * (-1)
//                    + ",updateMemory:" + memoryTotal * (-1) + ",updateCapacity:" + capacityTotal * (-1));
            throw e;
        }

        if (null == service) {
            LOG.error("创建集群时存StatefulService表返回为null，service:" + JSON.toJSONString(service));
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "创建集群时存StatefulService表返回为null,service:" + JSON.toJSONString(service));
        }

        // 4、拼接创建postgresqlCluster的参数
        data.put("serviceId", service.getId());
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.CLUSTER_CREATE_WORKER,
                data);

        return service;
    }

    @Override
    public boolean clusterStop(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("停止集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 1、 查看依赖，集群是否被其他框架使用
        boolean isDependence = false;
        try {
            isDependence = componentOperationsDataBaseUtil.isDependenceService(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("停止集群时，查询依赖表失败，error：", e.getMessage());
            throw e;
        }
        if (isDependence) {
            LOG.error("该服务正在被其他服务使用，不允许停止集群，serviceId：" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "该服务正在被其他服务使用，不允许停止集群，serviceId：" + serviceId);
        }

        // 2. 修改statefuleService、statefuleNode表状态为waiting
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("停止集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 3、 拼接postersqlCluster停止的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.CLUSTER_STOP_WORKER,
                data);

        return true;
    }

    @Override
    public boolean clusterStart(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("启动集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);
        // 1. 修改statefuleService、statefuleNode表状态为waiting
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_START);
        } catch (ErrorMessageException e) {
            LOG.error("启动集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 2、 拼接postersqlCluster启动的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.CLUSTER_START_WORKER,
                data);

        return true;
    }

    @Override
    public boolean clusterDelete(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("删除集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 1、 查看依赖，集群是否被其他框架使用
        boolean isDependence = false;
        try {
            isDependence = componentOperationsDataBaseUtil.isDependenceService(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("删除集群时，查询依赖表失败，error：", e.getMessage());
            throw e;
        }
        if (isDependence) {
            LOG.error("该服务正在被其他服务使用，不允许删除集群，serviceId：" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "该服务正在被其他服务使用，不允许删除集群，serviceId：" + serviceId);
        }

        // 2. 修改statefuleService、statefuleNode表状态为waiting
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_DELETE);
        } catch (ErrorMessageException e) {
            LOG.error("删除集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 3、 拼接postersqlCluster删除的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.CLUSTER_DELETE_WORKER,
                data);

        return true;
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.NODE_START_WORKER,
                params);
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

        // 2、 查看依赖，集群是否被其他框架使用
        boolean isDependence = false;
        try {
            isDependence = componentOperationsDataBaseUtil.isDependenceService(node.getServiceId());
        } catch (ErrorMessageException e) {
            LOG.error("停止节点时，查询依赖表失败，error：", e.getMessage());
            throw e;
        }
        if (isDependence) {
            LOG.error("该服务正在被其他服务使用，不允许停止节点，serviceId：" + node.getServiceId());
            throw new ErrorMessageException(ReturnCode.CODE_OPT_NODE_NOT_ALLOWED_FAILED,
                    "该服务正在被其他服务使用，不允许停止节点，serviceId：" + node.getServiceId());
        }

        // 3、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("停止节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.NODE_STOP_WORKER,
                params);
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

        // 2、 查看依赖，集群是否被其他框架使用
        boolean isDependence = false;
        try {
            isDependence = componentOperationsDataBaseUtil.isDependenceService(node.getServiceId());
        } catch (ErrorMessageException e) {
            LOG.error("删除节点时，查询依赖表失败，error：", e.getMessage());
            throw e;
        }
        if (isDependence) {
            LOG.error("该服务正在被其他服务使用，不允许删除节点，serviceId：" + node.getServiceId());
            throw new ErrorMessageException(ReturnCode.CODE_OPT_NODE_NOT_ALLOWED_FAILED,
                    "该服务正在被其他服务使用，不允许删除节点，serviceId：" + node.getServiceId());
        }

        // 3、修改数据库中集群和节点状态
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL, PostgresqlClusterConst.NODE_DELETE_WORKER,
                params);
        return true;
    }

    @Override
    protected Map<String, String> checkClusterChangeResourcePersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("poetgresql集群修改资源，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject"
                + JSON.toJSONString(jsonObject));

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

        // 1、获取旧的postgresqlCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改资源时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改资源时根据serviceId获取statefulService为null");
        }

        PostgresqlCluster oldCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取postgresqlcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取postgresqlcluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、 修改tenant表
//        Double updateCpu = cpu * service.getNodeNum() - service.getCpu();
//        Double updateMemory = memory * service.getNodeNum() - service.getMemory();
//        Double updateCapacity = capacity * service.getNodeNum() - service.getStorage();
//        boolean result = tenantService.updateUsedResource(tenantName, updateCpu, updateMemory, updateCapacity);
//        LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + updateCpu + ",updateMemory:" + updateMemory
//                + ",updateCapacity:" + updateCapacity);
//        if (!result) {
//            return false;
//        }

        // 3、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);

        PostgresqlCluster newCluster = buildClusterChangeResource(tenantName, service, cpu, memory, capacity,
                oldCluster);

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updatePostgresqlCluster(tenantName, newCluster);
        componentOperationsClientUtil.changePostgresqlClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());
        if (!updateResult) {
            // 修改yaml失败tenant表回滚
            // result = tenantService.updateUsedResource(tenantName, updateCpu *
            // (-1), updateMemory * (-1),
            // updateCapacity * (-1));
            // LOG.info("修改资源时修改yaml失败，修改tenant表结果：result:" + result +
            // ",updateCpu:" + updateCpu * (-1) + ",updateMemory:"
            //    + updateMemory * (-1) + ",updateCapacity:" + updateCapacity * (-1));
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                    "集群修改资源时，更新cluster失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",newCluster:"
                            + JSON.toJSONString(newCluster));
        }

        if (StringUtils.isNotEmpty(data.get("capacity"))) {
            componentOperationsClientUtil.repleacePostgresqlClusterLvm(tenantName, newCluster);
        }

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
                Map<String, String> extendedField = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
                extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                service.setExtendedField(JSON.toJSONString(extendedField));
                statefulServiceRepository.save(service);
            }
        } catch (ErrorMessageException e) {
            LOG.error("集群修改资源失败:", e);
            throw e;
        } catch (Exception e) {
            LOG.error("集群修改资源时修改集群资源和状态失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群修改资源时修改集群资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }

        List<StatefulNode> nodeList = null;
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
        } catch (Exception e) {
            LOG.error("集群修改资源时修改节点资源和状态失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群修改资源时修改节点资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }
        return true;
    }

    private PostgresqlCluster buildClusterChangeResource(String tenantName, StatefulService service, Double cpu,
            Double memory, Double capacity, PostgresqlCluster postgresqlCluster) {

        postgresqlCluster.getSpec().setOpt(PostgresqlClusterConst.OPERATOR_CHANGE_RESOURCE);

        Resources resources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        postgresqlCluster.getSpec().setResources(resources);

        if (StringUtils.isNotEmpty(String.valueOf(capacity))) {
            postgresqlCluster.getSpec().setStorage(capacity + CommonConst.UNIT_GI);
        }
        return postgresqlCluster;
    }

    @Override
    protected Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("postgresql集群修改配置个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + jsonObject);
        Map<String, String> map = new HashMap<>();

        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        String version = service.getVersion();

        componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_POSTGRESQL, version,
                jsonObject);

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("json", JSON.toJSONString(jsonObject));
        return map;
    }

    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        LOG.info("postgresql修改配置，data：" + JSON.toJSONString(data));

        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        JSONObject jsonObject = JSON.parseObject(data.get("json"));

        // 1、获取旧的postgresqlCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改配置时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改配置时根据serviceId获取statefulService为null");
        }

        PostgresqlCluster oldCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改配置时根据tenantName和serviceName获取postgresqlCluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改配置时根据tenantName和serviceName获取postgresqlCluster为空，tenantName：" + tenantName + ",serviceName:"
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

        // 3、修改postgresqlCluster
        PostgresqlCluster newCluster = buildClusterChangeConfig(tenantName, service.getServiceName(), oldCluster,
                mergedUpdatedJson, service.getVersion());

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updatePostgresqlCluster(tenantName, newCluster);

        componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId,
                newCluster.getMetadata().getName());
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

    /**
     * 集群修改配置，构造postgresqlCluster
     * 
     * @param tenantName
     * @param serviceName
     * @param mergedUpdatedJson
     * @param version
     * @return
     */
    private PostgresqlCluster buildClusterChangeConfig(String tenantName, String serviceName,
            PostgresqlCluster postgresqlCluster, JSONObject configUpdatedJson, String version) {
        Map<String, String> changedUnitMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configUpdatedJson,
                CommonConst.APPTYPE_POSTGRESQL, version);

        postgresqlCluster.getSpec().setPostgresqlClusterConfig(changedUnitMap);
        postgresqlCluster.getSpec().setOpt(PostgresqlClusterConst.OPERATOR_CHANGE_CONFIG);
        return postgresqlCluster;
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_POSTGRESQL,
                PostgresqlClusterConst.CLUSTER_EXPAND_NODE_WORKER, data);
        return true;
    }
}
