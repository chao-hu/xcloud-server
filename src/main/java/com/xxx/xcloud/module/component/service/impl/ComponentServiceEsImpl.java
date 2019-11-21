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
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.model.es.EsInstanceGroup;
import com.xxx.xcloud.module.component.model.lvm.Lvm;
import com.xxx.xcloud.utils.StringUtils;

@Service
public class ComponentServiceEsImpl extends BaseComponentServiceImpl {

    private static Logger LOG = LoggerFactory.getLogger(ComponentServiceEsImpl.class);

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
        int replicas = Integer.parseInt(data.get("replicas"));
        String password = data.get("password");
        String configuration = data.get("configuration");
        String masterSeparateFlag = data.get("masterSeparateFlag");
        int totalReplicas = replicas;

        Map<String, String> serviceExtendedFieldMap = new HashMap<>();

        serviceExtendedFieldMap.put("masterSeparateFlag", data.get("masterSeparateFlag"));
        serviceExtendedFieldMap.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        serviceExtendedFieldMap.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        if (EsClusterConst.ES_MASTER_SEPARATE_FLAG_TRUE.equals(masterSeparateFlag)) {
            serviceExtendedFieldMap.put("dataReplicas", data.get("dataReplicas"));
            serviceExtendedFieldMap.put("masterCpu", data.get("cpu"));
            serviceExtendedFieldMap.put("masterMemory", data.get("memory"));
            serviceExtendedFieldMap.put("masterCapacity", data.get("capacity"));
            serviceExtendedFieldMap.put("masterReplicas", data.get("replicas"));
            totalReplicas = replicas + Integer.parseInt(data.get("dataReplicas"));
        }
        String extendedFiled = JSON.toJSONString(serviceExtendedFieldMap);
        Map<String, Double> totalResource = new HashMap<>();
        totalResource = getTotalResource(data);
        Double cpuTotal = totalResource.get("cpuTotal");
        Double memoryTotal = totalResource.get("memoryTotal");
        Double capacityTotal = totalResource.get("capacityTotal");

        // 2、 修改tenant表
        // boolean result = tenantService.updateUsedResource(tenantName,
        // cpuTotal, memoryTotal, capacityTotal);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal +
        // ",updateMemory:" + memoryTotal
        // + ",updateCapacity:" + capacityTotal);

        // 3. 封装statefuleService存表
        StatefulService statefuleService = null;
        try {
            statefuleService = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_ES, serviceName, version, totalReplicas, password, null, cpuTotal, memoryTotal,
                    capacityTotal, configuration, extendedFiled, CommonConst.ACTION_CLUSTER_CREATE);
        } catch (ErrorMessageException e) {
            LOG.error("创建集群失败", e.getMessage());
            // 集群创建失败tenent回滚
            // result = .updateUsedResource(tenantName, cpuTotal * (-1),
            // memoryTotal * (-1),
            // capacityTotal * (-1));
            // LOG.info("ktenantServiceafka集群创建存表失败回滚，修改tenant表结果：result:" +
            // result + ",updateCpu:" + cpuTotal * (-1) + ",updateMemory:"
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
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_CLUSTER_CREATE_WORKER, data);

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

        // 2、 拼接esCluster停止的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_CLUSTER_STOP_WORKER, data);

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

        // 2、 拼接esCluster启动的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_CLUSTER_START_WORKER, data);

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

        // 2、添加esCluster增加节点的线程
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_CLUSTER_EXPAND_NODE_WORKER, data);

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

        // 2、 拼接esCluster删除的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_CLUSTER_DELETE_WORKER, data);

        return true;
    }

    @Override
    public boolean changeResource(Map<String, String> data) throws ErrorMessageException {

        // 1. 获取service
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));
        double newTotalCpu;
        double newTotalMemory;
        double newTotalCapacity;
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("集群修改资源时获取集群失败，获取的service为null" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "集群修改资源时获取集群失败，获取的service为null" + serviceId);
        }

        // 2、计算集群新的总资源
        Map<String, String> extendedField = componentOperationsDataBaseUtil
                .getServiceExtendedField(service.getExtendedField());
        String masterSeparateFlag = extendedField.get("masterSeparateFlag");
        int replicas;
        if (EsClusterConst.ES_MASTER_SEPARATE_FLAG_TRUE.equals(masterSeparateFlag)) {
            replicas = Integer.parseInt(extendedField.get("dataReplicas"));
            double masterCpu = Double.parseDouble(extendedField.get("masterCpu"));
            double masterMemory = Double.parseDouble(extendedField.get("masterMemory"));
            double masterCapacity = Double.parseDouble(extendedField.get("masterCapacity"));
            int masterReplicas = Integer.parseInt(extendedField.get("masterReplicas"));
            newTotalCpu = cpu * replicas + masterCpu * masterReplicas;
            newTotalMemory = memory * replicas + masterMemory * masterReplicas;
            newTotalCapacity = capacity * replicas + masterCapacity * masterReplicas;
        } else {
            replicas = service.getNodeNum();
            newTotalCpu = cpu * replicas;
            newTotalMemory = memory * replicas;
            newTotalCapacity = capacity * replicas;
        }

        // 3、 修改tenant表资源
        // double oldTotalCpu = service.getCpu();
        // double oldTotalMemory = service.getMemory();
        // double oldTotalcapacity = service.getStorage();

        // double updateCpu = newTotalCpu - oldTotalCpu;
        // double updateMemory = newTotalMemory - oldTotalMemory;
        // double updateCapacity = newTotalCapacity - oldTotalcapacity;
        // boolean result = tenantService.updateUsedResource(tenantName,
        // updateCpu, updateMemory, updateCapacity);
        // LOG.info("修改tenant表结果：" + result);
        // if (!result) {
        // return false;
        // }

        // 4、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);

        // 5、拼接esCluster、修改yaml
        EsCluster esCluster = buildEsCluster(tenantName, service.getServiceName(), data);
        boolean updateYamlResult = componentOperationsClientUtil.updateEsCluster(tenantName, esCluster);
        try {
            if (updateYamlResult) {

                // 修改节点资源
                List<StatefulNode> nodeList = null;
                nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                        CommonConst.STATE_NODE_DELETED);
                if (null == nodeList || nodeList.isEmpty()) {
                    LOG.error("修改资源时根据serviceId获取nodeList为空，serviceId：" + serviceId);
                    throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                            "修改资源时根据serviceId获取nodeList为空，serviceId：" + serviceId);
                }
                for (StatefulNode statefulNode : nodeList) {
                    if (!EsClusterConst.ES_ROLE_MASTER.equals(statefulNode.getRole())) {
                        String capacityString = data.get("capacity");
                        if (!capacityString.equals(String.valueOf(statefulNode.getStorage()))) {
                            Lvm oldLvm = componentOperationsClientUtil.getLvm(tenantName, statefulNode.getLvmName());
                            LOG.info("获取旧的lvm：" + JSON.toJSONString(oldLvm));
                            if (null == oldLvm) {
                                String nodeIp = esCluster.getStatus().getInstances().get(statefulNode.getNodeName())
                                        .getExterHost();
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
                        statefulNodeRepository.save(statefulNode);
                    }
                }
                LOG.info("集群修改资源时修改节点资源成功");

                // 修改集群资源
                service.setCpu(newTotalCpu);
                service.setMemory(newTotalMemory);
                service.setStorage(newTotalCapacity);
                extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                service.setExtendedField(JSON.toJSONString(extendedField));
                statefulServiceRepository.save(service);
                LOG.info("集群修改资源时修改集群资源成功");
            } else {
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
            throw e;
        }
        finally {
            componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
        }

        return true;
    }

    private EsCluster buildEsCluster(String tenantName, String serviceName, Map<String, String> data) {

        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));

        EsCluster esCluster = null;
        esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        if (null == esCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取esCluster为空，tenantName：" + tenantName + ",serviceName:"
                    + serviceName);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取esCluster为空，tenantName：" + tenantName + ",serviceName:"
                            + serviceName);
        }
        Map<String, EsInstanceGroup> esInstanceGroups = esCluster.getSpec().getInstanceGroup();
        EsInstanceGroup esInstanceGroup = null;
        Resources esResources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        if (1 == esCluster.getSpec().getInstanceGroup().size()) {
            esInstanceGroup = esInstanceGroups.get(EsClusterConst.ES_ROLE_WORKER);
        } else if (2 == esCluster.getSpec().getInstanceGroup().size()) {
            esInstanceGroup = esInstanceGroups.get(EsClusterConst.ES_ROLE_DATA);
        }
        esInstanceGroup.setResources(esResources);
        esInstanceGroup.setStorage(capacity + CommonConst.UNIT_GI);
        esCluster.getSpec().setOpt(EsClusterConst.ES_CLUSTER_OPT_CHANGE_RESOURCE);
        esCluster.getSpec().setInstanceGroup(esInstanceGroups);
        LOG.info("集群修改资源：构建esCluster成功, serviceName:" + serviceName);
        return esCluster;
    }

    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        LOG.info("es修改配置，data：" + JSON.toJSONString(data));

        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        JSONObject jsonObject = JSON.parseObject(data.get("json"));

        // 1、获取旧的esCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改配置时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改配置时根据serviceId获取statefulService为null");
        }

        EsCluster oldCluster = componentOperationsClientUtil.getEsCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改配置时根据tenantName和serviceName获取escluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改配置时根据tenantName和serviceName获取escluster为空，tenantName：" + tenantName + ",serviceName:"
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

        // 3、修改escluster
        EsCluster newCluster = buildClusterChangeConfig(tenantName, service.getServiceName(), mergedUpdatedJson,
                service.getVersion());

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateEsCluster(tenantName, newCluster);

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

    private EsCluster buildClusterChangeConfig(String tenantName, String serviceName, JSONObject configUpdatedJson,
            String version) {
        Map<String, String> changedUnitMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configUpdatedJson,
                CommonConst.APPTYPE_ES, version);
        EsCluster esCluster = null;
        esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        if (null == esCluster) {
            LOG.error("获取esCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        esCluster.getSpec().setConfig(changedUnitMap);
        esCluster.getSpec().setOpt(EsClusterConst.ES_CLUSTER_OPT_CHANGE_CONFIG);
        return esCluster;
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

        // 2. es参数校验
        String capacity = jsonObject.getString("capacity");
        String replicas = jsonObject.getString("replicas");
        checkCapacity(capacity);
        checkReplicas(replicas);
        Boolean isMasterSeparate = jsonObject.getBoolean("isMasterSeparate");
        String masterSeparateFlag = EsClusterConst.ES_MASTER_SEPARATE_FLAG_FALSE;

        if (isMasterSeparate) {
            String dataCpu = jsonObject.getString("dataCpu");
            String dataMemory = jsonObject.getString("dataMemory");
            String dataCapacity = jsonObject.getString("dataCapacity");
            String dataReplicas = jsonObject.getString("dataReplicas");
            checkDataCpuAndDataMemoryAndDataCapacityAndDataReplicas(dataCpu, dataMemory, dataCapacity, dataReplicas);
            data.put("dataCpu", dataCpu);
            data.put("dataMemory", dataMemory);
            data.put("dataCapacity", dataCapacity);
            data.put("dataReplicas", dataReplicas);
            masterSeparateFlag = EsClusterConst.ES_MASTER_SEPARATE_FLAG_TRUE;
        }

        JSONObject configuration = jsonObject.getJSONObject("configuration");
        String version = jsonObject.getString("version");

        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_ES, version);
        if (jsonObject.containsKey("configuration")) {
            componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_ES, version,
                    configuration);
            data.put("configuration", JSON.toJSONString(configuration));
        }

        data.put("serviceName", serviceName);
        data.put("tenantName", tenantName);
        data.put("cpu", cpu);
        data.put("memory", memory);
        data.put("masterSeparateFlag", masterSeparateFlag);
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

    private void checkDataCpuAndDataMemoryAndDataCapacityAndDataReplicas(String dataCpu, String dataMemory,
            String dataCapacity, String dataReplicas) throws ErrorMessageException {
        if (StringUtils.isEmpty(dataCpu)) {
            LOG.error("参数校验失败，dataCpu为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，dataCpu为空");
        }
        if (StringUtils.isEmpty(dataMemory)) {
            LOG.error("参数校验失败，dataMemory为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，dataMemory为空");
        }
        if (StringUtils.isEmpty(dataCapacity)) {
            LOG.error("参数校验失败，dataCapacity为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，dataCapacity为空");
        }

        if (StringUtils.isEmpty(dataReplicas)) {
            LOG.error("参数校验失败，dataReplicas为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，dataReplicas为空");
        }

        if (!dataCpu.matches(CommonConst.CHECK_RESOURCE_CPU)) {
            LOG.error("参数校验失败，dataCpu不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，dataCpu不符合校验规则");
        }

        if (!dataMemory.matches(CommonConst.CHECK_RESOURCE_MEMORY)) {
            LOG.error("参数校验失败，dataMemory不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，dataMemory不符合校验规则");
        }

        if (!dataCapacity.matches(CommonConst.CHECK_RESOURCE_CAPACITY)) {
            LOG.error("参数校验失败，dataCapacity不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，dataCapacity不符合校验规则");
        }

        if (!dataReplicas.matches(CommonConst.CHECK_CLUSTER_REPLICAS)) {
            LOG.error("参数校验失败，dataReplicas不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，dataReplicas不符合校验规则");
        }
    }

    public Map<String, Double> getTotalResource(Map<String, String> data) throws ErrorMessageException {
        Map<String, Double> totalResource = new HashMap<>();
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));
        String masterSeparateFlag = data.get("masterSeparateFlag");

        Double cpuTotal;
        Double memoryTotal;
        Double capacityTotal;

        if (EsClusterConst.ES_MASTER_SEPARATE_FLAG_TRUE.equals(masterSeparateFlag)) {
            Double dataCpu = Double.parseDouble(data.get("dataCpu"));
            Double dataMemory = Double.parseDouble(data.get("dataMemory"));
            Double dataCapacity = Double.parseDouble(data.get("dataCapacity"));
            int dataReplicas = Integer.parseInt(data.get("dataReplicas"));
            cpuTotal = cpu * replicas + dataCpu * dataReplicas;
            memoryTotal = memory * replicas + dataMemory * dataReplicas;
            capacityTotal = capacity * replicas + dataCapacity * dataReplicas;
        } else {
            cpuTotal = cpu * replicas;
            memoryTotal = memory * replicas;
            capacityTotal = capacity * replicas;
        }
        totalResource.put("cpuTotal", cpuTotal);
        totalResource.put("memoryTotal", memoryTotal);
        totalResource.put("capacityTotal", capacityTotal);

        return totalResource;
    }

    @Override
    protected Map<String, String> checkClusterExpandPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);
        String addReplicas = jsonObject.getString("addReplicas");

        checkAddReplicas(addReplicas);

        data.put("addReplicas", addReplicas);
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
        LOG.info("es集群修改配置个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:" + jsonObject);
        Map<String, String> map = new HashMap<>();

        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        String version = service.getVersion();

        componentOperationsDataBaseUtil.checkClusterChangeConfigJsonObject(CommonConst.APPTYPE_ES, version, jsonObject);

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("json", JSON.toJSONString(jsonObject));
        return map;
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_NODE_START_WORKER, params);
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_NODE_STOP_WORKER, params);
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

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_ES, EsClusterConst.ES_NODE_DELETE_WORKER, params);
        return true;
    }

}
