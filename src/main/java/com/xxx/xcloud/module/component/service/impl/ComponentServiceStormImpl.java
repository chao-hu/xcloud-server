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
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.storm.StormCluster;
import com.xxx.xcloud.utils.StringUtils;

@Service
public class ComponentServiceStormImpl extends BaseComponentServiceImpl {

    private static Logger LOG = LoggerFactory.getLogger(ComponentServiceStormImpl.class);

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {
        Map<String, String> map = new HashMap<>();

        LOG.info("storm创建集群参数校验，tenantName：" + tenantName + ",jsonObject:" + JSON.toJSONString(jsonObject));
        // 1、参数校验
        String serviceName = jsonObject.getString("serviceName");
        String version = jsonObject.getString("version");
        String zkServiceName = jsonObject.getString("zkServiceName");

        int nimbusNum = CommonConst.NUMBER_ONE;
        String nimbusCpu = jsonObject.getString("nimbusCpu");
        String nimbusMemory = jsonObject.getString("nimbusMemory");
        // supervisorNum至少为2
        String supervisorNum = jsonObject.getString("supervisorNum");
        String supervisorCpu = jsonObject.getString("supervisorCpu");
        String supervisorMemory = jsonObject.getString("supervisorMemory");

        String projectId = jsonObject.getString("projectId");
        String orderId = jsonObject.getString("orderId");
        JSONObject configJSONObject = jsonObject.getJSONObject("configuration");
        String performance = jsonObject.getString("performance");
        String createdBy = jsonObject.getString("createdBy");

        // 参数校验
        StatefulService zkService = checkStormCreateParams(tenantName, version, nimbusNum, nimbusCpu, nimbusMemory,
                supervisorNum, supervisorCpu, supervisorMemory, zkServiceName);

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
        String zkInterAddress = extendedFieldMap.get("interAddress");
        if (StringUtils.isEmpty(zkInterAddress)) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "创建集群参数校验失败,获取zkService时获取的zk内部连接串为空，tenantName:" + tenantName + ",appType："
                            + CommonConst.APPTYPE_ZK + ",serviceName" + zkServiceName);
        }

        if (jsonObject.containsKey("configuration")) {
            componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_STORM, version,
                    configJSONObject);
            map.put("configuration", jsonObject.getString("configuration"));
        }

        map.put("tenantName", tenantName);
        map.put("serviceName", serviceName);
        map.put("zkServiceId", zkService.getId());
        map.put("zkInterAddress", zkInterAddress);
        map.put("zkServiceName", zkServiceName);
        map.put("version", version);
        map.put("nimbusNum", String.valueOf(nimbusNum));
        map.put("nimbusCpu", nimbusCpu);
        map.put("nimbusMemory", nimbusMemory);
        map.put("supervisorNum", supervisorNum);
        map.put("supervisorCpu", supervisorCpu);
        map.put("supervisorMemory", supervisorMemory);
        map.put("projectId", projectId);
        map.put("orderId", orderId);
        map.put("performance", performance);
        map.put("createdBy", createdBy);

        return map;
    }

    /**
     * 创建集群参数校验
     * 
     * @param tenantName
     * @param version
     * @param nimbusNum
     * @param nimbusCpu
     * @param nimbusMemory
     * @param supervisorNum
     * @param supervisorCpu
     * @param supervisorMemory
     * @param zkServiceName
     * @return
     * @throws ErrorMessageException
     */
    private StatefulService checkStormCreateParams(String tenantName, String version, int nimbusNum, String nimbusCpu,
            String nimbusMemory, String supervisorNum, String supervisorCpu, String supervisorMemory,
            String zkServiceName) throws ErrorMessageException {

        StatefulService service = null;
        // 1、版本
        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_STORM, version);

        // 2、校验是否为空
        if (StringUtils.isEmpty(nimbusCpu)) {
            LOG.error("参数校验失败，nimbusCpu为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，nimbusCpu为空");
        }
        if (StringUtils.isEmpty(nimbusMemory)) {
            LOG.error("参数校验失败，nimbusMemory为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，nimbusMemory为空");
        }
        if (StringUtils.isEmpty(supervisorNum)) {
            LOG.error("参数校验失败，supervisorNum为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，supervisorNum为空");
        }
        if (StringUtils.isEmpty(supervisorCpu)) {
            LOG.error("参数校验失败，supervisorCpu为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，supervisorCpu为空");
        }
        if (StringUtils.isEmpty(supervisorMemory)) {
            LOG.error("参数校验失败，supervisorMemory为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，supervisorMemory为空");
        }
        if (StringUtils.isEmpty(zkServiceName)) {
            LOG.error("参数校验失败，zkServiceName为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，zkServiceName为空");
        }

        // 3、nimbusNum(主节点)
        if (nimbusNum < CommonConst.NUMBER_ONE || nimbusNum > CommonConst.NUMBER_SIXTY_FOUR) {
            LOG.error("创建集群参数校验失败,nimbusNum节点个数不符合规范，nimbusNum:" + nimbusNum + ",规定范围[1,64]！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,nimbusNum节点个数不符合规范，nimbusNum:" + nimbusNum + ",规定范围[1,64]！");
        }

        // 4、supervisorNum(工作节点)
        if (Integer.parseInt(supervisorNum) < CommonConst.NUMBER_TWO
                || Integer.parseInt(supervisorNum) > CommonConst.NUMBER_SIXTY_FOUR) {
            LOG.error("创建集群参数校验失败,supervisorNum节点个数不符合规范，supervisorNum:" + supervisorNum + ",规定范围[2,64]！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,supervisorNum节点个数不符合规范，supervisorNum:" + supervisorNum + ",规定范围[2,64]！");

        }

        // 5、cpu---nimbusCpu|supervisorCpu
        double dbNimbusCpu = Double.parseDouble(nimbusCpu);
        if (dbNimbusCpu < CommonConst.NUMBER_TWO || dbNimbusCpu > CommonConst.NUMBER_SIXTY_FOUR) {
            LOG.error("创建集群参数校验失败,nimbusCpu不符合规范，nimbusCpu:" + nimbusCpu + ",规定范围[2,64]！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,nimbusCpu不符合规范，nimbusCpu:" + nimbusCpu + ",规定范围[2,64]！");

        }

        double dbSupervisorCpu = Double.parseDouble(supervisorCpu);
        if (dbSupervisorCpu < CommonConst.NUMBER_TWO || dbSupervisorCpu > CommonConst.NUMBER_SIXTY_FOUR) {
            LOG.error("创建集群参数校验失败,supervisorCpu不符合规范，supervisorCpu:" + supervisorCpu + ",规定范围[2,64]！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,supervisorCpu不符合规范，supervisorCpu:" + supervisorCpu + ",规定范围[2,64]！");
        }

        // 6、memory---nimbusMemory | supervisorMemory
        double dbNimbusMemory = Double.parseDouble(nimbusMemory);
        if (dbNimbusMemory < CommonConst.NUMBER_TWO || dbNimbusMemory > CommonConst.NUMBER_SIXTY_FOUR) {
            LOG.error("创建集群参数校验失败,nimbusMemory不符合规范，nimbusMemory:" + nimbusMemory + ",规定范围[2,64]！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,nimbusMemory不符合规范，nimbusMemory:" + nimbusMemory + ",规定范围[2,64]！");
        }

        double dbSupervisorMemory = Double.parseDouble(supervisorMemory);
        if (dbSupervisorMemory < CommonConst.NUMBER_TWO || dbSupervisorMemory > CommonConst.NUMBER_SIXTY_FOUR) {
            LOG.error("创建集群参数校验失败,supervisorMemory不符合规范，supervisorMemory:" + supervisorMemory + ",规定范围[2,64]！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "创建集群参数校验失败,supervisorMemory不符合规范，supervisorMemory:" + supervisorMemory + ",规定范围[2,64]！");
        }

        // 7、ZK服务名称是否存在--是否可用由页面限制
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
            service = serviceList.get(0);
            LOG.info("zkService:" + JSON.toJSONString(service));
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
        String zkServiceName = data.get("zkServiceName");
        String zkInterAddress = data.get("zkInterAddress");
        String version = data.get("version");
        int nimbusNum = Integer.parseInt(data.get("nimbusNum"));
        Double nimbusCpu = Double.parseDouble(data.get("nimbusCpu"));
        Double nimbusMemory = Double.parseDouble(data.get("nimbusMemory"));
        int supervisorNum = Integer.parseInt(data.get("supervisorNum"));
        Double supervisorCpu = Double.parseDouble(data.get("supervisorCpu"));
        Double supervisorMemory = Double.parseDouble(data.get("supervisorMemory"));
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String configUpdated = data.get("configuration");
        Double capacity = StormClusterConst.NIMBUS_CAPACITY_DEFAULT_GI;
        String createdBy = data.get("createdBy");

        Double cpuTotal = nimbusNum * nimbusCpu + supervisorNum * supervisorCpu;
        Double memoryTotal = nimbusNum * nimbusMemory + supervisorNum * supervisorMemory;
        // Double capacityTotal = nimbusNum *
        // StormClusterConst.NIMBUS_CAPACITY_DEFAULT_GI;

        // 2、 修改tenant表

//        boolean result = tenantService.updateUsedResource(tenantName, cpuTotal, memoryTotal, 0.0);
//        LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal + ",updateMemory:" + memoryTotal
//                + ",updateCapacity: 0.0");

        // 3、存表：StatefulService
        StatefulService service = null;

        Map<String, String> extendedField = new HashMap<>();
        extendedField.put("zkServiceId", zkServiceId);
        extendedField.put("zkServiceName", zkServiceName);
        extendedField.put("interAddress", zkInterAddress);
        extendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        String zkInterAddressUpdated = zkInterAddress.replaceAll(":2181", "").substring(5);
        extendedField.put(StormClusterConst.ZOOKEEPER_SERVERS, zkInterAddressUpdated);

        try {
            service = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_STORM, serviceName, version, nimbusNum + supervisorNum, null, null, cpuTotal,
                    memoryTotal, 0.0, configUpdated, JSON.toJSONString(extendedField),
                    CommonConst.ACTION_CLUSTER_CREATE);
        } catch (ErrorMessageException e) {
            LOG.error("创建集群失败", e.getMessage());
            // 集群创建失败tenent回滚
//            result = tenantService.updateUsedResource(tenantName, cpuTotal * (-1), memoryTotal * (-1), 0.0);
//            LOG.info("storm集群创建存表失败修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal * (-1) + ",updateMemory:"
//                    + memoryTotal * (-1) + ",updateCapacity: 0.0");
            throw e;
        }

        if (null == service) {
            LOG.error("创建集群时存StatefulService表返回为null，service:" + JSON.toJSONString(service));
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "创建集群时存StatefulService表返回为null,service:" + JSON.toJSONString(service));
        }

        // 4、存依赖表
        componentOperationsDataBaseUtil.buildStatefulServiceDependency(service.getId(), zkServiceId);

        // 5、拼接创建stormCluster的参数
        data.put("serviceId", service.getId());
        data.put("capacity", String.valueOf(capacity));
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.CLUSTER_CREATE_WORKER, data);

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

        // 2、 拼接stormCluster停止的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.CLUSTER_STOP_WORKER, params);
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

        // 3、 拼接stormCluster启动的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.CLUSTER_START_WORKER, params);
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

        // 4、 拼接stormCluster删除的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.CLUSTER_DELETE_WORKER, params);
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

        String supervisorAddNum = jsonObject.getString("supervisorAddNum");
        checkAddReplicas(supervisorAddNum);

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("supervisorAddNum", supervisorAddNum);
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.CLUSTER_EXPAND_NODE_WORKER,
                data);
        return true;
    }

    @Override
    protected Map<String, String> checkClusterChangeResourcePersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("storm集群修改资源个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + jsonObject);
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");

        checkCpuAndMemory(cpu, memory);

        Map<String, String> map = new HashMap<>();
        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("cpu", cpu);
        map.put("memory", memory);
        return map;
    }

    @Override
    public boolean changeResource(Map<String, String> data) throws ErrorMessageException {
        LOG.info("集群修改资源，接收的参数=>data:" + JSON.toJSONString(data));

        String serviceId = data.get("serviceId");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        String tenantName = data.get("tenantName");

        // 1、获取旧的stormCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改资源时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改资源时根据serviceId获取statefulService为null");
        }

        StormCluster oldCluster = componentOperationsClientUtil.getStormCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取stormcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取stormcluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、 修改tenant表
        int supervisorNum = 0;
        Double supervisorCpuOld = 0.0, supervisorMemoryOld = 0.0;
        Map<String, Integer> roleReplicas = oldCluster.getSpec().getReplicas();
        if (roleReplicas.containsKey(StormClusterConst.ROLE_SUPERVISOR)) {
            supervisorNum = roleReplicas.get(StormClusterConst.ROLE_SUPERVISOR);
        }
        Map<String, Resources> roleResources = oldCluster.getSpec().getResources();
        if (roleResources.containsKey(StormClusterConst.ROLE_SUPERVISOR)) {
            Resources resources = roleResources.get(StormClusterConst.ROLE_SUPERVISOR);
            supervisorCpuOld = Double.parseDouble(resources.getLimits().getCpu());
            String supervisorMemoryOldStr = resources.getLimits().getMemory();
            supervisorMemoryOld = Double
                    .parseDouble(supervisorMemoryOldStr.substring(0, supervisorMemoryOldStr.length() - 2));
        }

        Double updateCpu = (cpu - supervisorCpuOld) * supervisorNum;
        Double updateMemory = (memory - supervisorMemoryOld) * supervisorNum;

//        boolean result = tenantService.updateUsedResource(tenantName, updateCpu, updateMemory, 0.0);
//        LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + updateCpu + ",updateMemory:" + updateMemory
//                + ",updateCapacity:0.0");
//        if (!result) {
//            return false;
//        }

        // 3、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);

        StormCluster newCluster = buildClusterChangeResource(tenantName, service, cpu, memory, oldCluster);

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateStormCluster(tenantName, newCluster);
        if (!updateResult) {
            // 修改yaml失败tenant表回滚
            // result = tenantService.updateUsedResource(tenantName, updateCpu *
            // (-1), updateMemory * (-1), 0.0);
            // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
            // updateCpu * (-1) + ",updateMemory:"
            //   + updateMemory * (-1) + ",updateCapacity:0.0");
            // 同步集群节点状态
            componentOperationsClientUtil.changeStormClusterAndNodesStateByYaml(tenantName, service.getId(),
                    service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                    "集群修改资源时，更新cluster失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",newCluster:"
                            + JSON.toJSONString(newCluster));
        }

        // 5、修改数据库中集群和节点资源
        boolean updateServiceFlag = false;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                if (StringUtils.isNotEmpty(String.valueOf(cpu))) {
                    service.setCpu(service.getCpu() + updateCpu);
                }
                if (StringUtils.isNotEmpty(String.valueOf(memory))) {
                    service.setMemory(service.getMemory() + updateMemory);
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
                componentOperationsClientUtil.changeStormClusterAndNodesStateByYaml(tenantName, service.getId(),
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
                if (StormClusterConst.ROLE_SUPERVISOR.equals(statefulNode.getRole())) {
                    statefulNode.setCpu(cpu);
                    statefulNode.setMemory(memory);
                    statefulNodeRepository.save(statefulNode);
                }
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
                componentOperationsClientUtil.changeStormClusterAndNodesStateByYaml(tenantName, service.getId(),
                        service.getServiceName());
            }
        }
        // 6、 拼接stormCluster修改资源的参数
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.CLUSTER_CHANGE_RESOURCE_WORKER,
                data);
        return true;

    }

    @Override
    protected Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("storm集群修改配置个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + jsonObject);
        Map<String, String> map = new HashMap<>();

        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        String version = service.getVersion();

        componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_STORM, version, jsonObject);

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("json", JSON.toJSONString(jsonObject));
        return map;
    }

    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        LOG.info("storm修改配置，data：" + JSON.toJSONString(data));

        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        JSONObject jsonObject = JSON.parseObject(data.get("json"));

        // 1、获取旧的stormCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改配置时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改配置时根据serviceId获取statefulService为null");
        }

        StormCluster oldCluster = componentOperationsClientUtil.getStormCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改配置时根据tenantName和serviceName获取stormcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改配置时根据tenantName和serviceName获取stormcluster为空，tenantName：" + tenantName + ",serviceName:"
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

        // 3、修改stormcluster
        StormCluster newCluster = buildClusterChangeConfig(tenantName, service.getServiceName(), oldCluster,
                mergedUpdatedJson, service.getVersion());

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateStormCluster(tenantName, newCluster);

        componentOperationsClientUtil.changeStormClusterAndNodesStateByYaml(tenantName, service.getId(),
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.NODE_START_WORKER, params);
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
            LOG.error("启动节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.NODE_STOP_WORKER, params);
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_STORM, StormClusterConst.NODE_DELETE_WORKER, params);
        return true;
    }

    private StormCluster buildClusterChangeResource(String tenantName, StatefulService service, Double cpu,
            Double memory, StormCluster stormCluster) {
        stormCluster.getSpec().getStormOp().setOperator(StormClusterConst.OPERATOR_CHANGE_RESOURCE);

        Map<String, Resources> roleResources = stormCluster.getSpec().getResources();
        if (roleResources.containsKey(StormClusterConst.ROLE_SUPERVISOR)) {
            Resources resources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
            roleResources.put(StormClusterConst.ROLE_SUPERVISOR, resources);
            stormCluster.getSpec().setResources(roleResources);
        }
        return stormCluster;
    }

    private StormCluster buildClusterChangeConfig(String tenantName, String serviceName, StormCluster stormCluster,
            JSONObject configUpdatedJson, String version) {
        Map<String, String> changedUnitMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configUpdatedJson,
                CommonConst.APPTYPE_STORM, version);
        stormCluster.getSpec().setConfig(changedUnitMap);
        stormCluster.getSpec().getStormOp().setOperator(StormClusterConst.OPERATOR_CHANGE_CONFIG);
        return stormCluster;
    }

}
