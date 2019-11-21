package com.xxx.xcloud.module.component.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PrometheusClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.lvm.Lvm;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusCluster;
import com.xxx.xcloud.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.ConfigMap;

@Service
public class ComponentServicePrometheusImpl extends BaseComponentServiceImpl {

    private static Logger LOG = LoggerFactory.getLogger(ComponentServicePrometheusImpl.class);

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

        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));
        double cpuTotal = cpu * replicas;
        double memoryTotal = memory * replicas;
        double capacityTotal = capacity * replicas;

        String apiServer = data.get("apiServer");
        String storageTsdbRetention = data.get("storageTsdbRetention");
        String kubeStateMetricsPort = data.get("kubeStateMetricsPort");
        String blackboxExporterPort = data.get("blackboxExporterPort");

        String configuration = "";
        Map<String, String> prometheusConfigurationMap = new HashMap<>();
        prometheusConfigurationMap.put("api_server", apiServer);
        prometheusConfigurationMap.put("storage_tsdb_retention", storageTsdbRetention);
        prometheusConfigurationMap.put("kube_state_metrics_port", kubeStateMetricsPort);

        prometheusConfigurationMap.put("blackbox_exporter_port", blackboxExporterPort);
        configuration = JSON.toJSONString(prometheusConfigurationMap);

        // 2、 修改tenant表
        // boolean result = tenantService.updateUsedResource(tenantName,
        // cpuTotal, memoryTotal, capacityTotal);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal +
        // ",updateMemory:" + memoryTotal
        // + ",updateCapacity:" + capacityTotal);

        // 3. 封装statefuleService存表

        Map<String, String> extendedField = new HashMap<>();
        extendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
        extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);

        StatefulService statefuleService = null;
        try {
            statefuleService = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_PROMETHEUS, serviceName, version, replicas, null,
                    PrometheusClusterConst.PROMETHEUS_TYPE, cpuTotal, memoryTotal, capacityTotal, configuration,
                    JSON.toJSONString(extendedField), CommonConst.ACTION_CLUSTER_CREATE);
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
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_PROMETHEUS,
                PrometheusClusterConst.PROMETHEUS_CLUSTER_CREATE_WORKER, data);

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

        // 2、 拼接prometheus停止的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_PROMETHEUS,
                PrometheusClusterConst.PROMETHEUS_CLUSTER_STOP_WORKER, data);

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

        // 2、 拼接prometheus启动的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_PROMETHEUS,
                PrometheusClusterConst.PROMETHEUS_CLUSTER_START_WORKER, data);

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

        // 2、 拼接prometheus删除的参数
        Map<String, String> data = new HashMap<>();
        data.put("tenantName", tenantName);
        data.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_PROMETHEUS,
                PrometheusClusterConst.PROMETHEUS_CLUSTER_DELETE_WORKER, data);

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
        double newTotalCpu, newTotalMemory, newTotalCapacity;

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

        // 4、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);
        // 5、拼接prometheusCluster、修改yaml
        PrometheusCluster prometheusCluster = buildPrometheusCluster(tenantName, service.getServiceName(), data);
        boolean updateYamlResult = componentOperationsClientUtil.updatePrometheusCluster(tenantName, prometheusCluster);
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
                    String capacityString = data.get("capacity");
                    if (!capacityString.equals(String.valueOf(statefulNode.getStorage()))) {
                        Lvm oldLvm = componentOperationsClientUtil.getLvm(tenantName, statefulNode.getLvmName());
                        LOG.info("获取旧的lvm：" + JSON.toJSONString(oldLvm));
                        if (null == oldLvm) {
                            String nodeIp = prometheusCluster.getStatus().getInstances().get(statefulNode.getNodeName())
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
                LOG.info("集群修改资源时修改节点资源成功");

                // 修改集群资源
                service.setCpu(newTotalCpu);
                service.setMemory(newTotalMemory);
                service.setStorage(newTotalCapacity);
                Map<String, String> extendedField = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
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
            componentOperationsClientUtil.changePrometheusClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
        }
        return true;
    }

    private PrometheusCluster buildPrometheusCluster(String tenantName, String serviceName, Map<String, String> data) {

        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));

        PrometheusCluster prometheusCluster = null;
        prometheusCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName, serviceName);
        if (null == prometheusCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取prometheusCluster为空，tenantName：" + tenantName + ",serviceName:"
                    + serviceName);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取prometheusCluster为空，tenantName：" + tenantName + ",serviceName:"
                            + serviceName);
        }
        Resources prometheusResources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        prometheusCluster.getSpec().setResources(prometheusResources);
        prometheusCluster.getSpec().setStorage(capacity + CommonConst.UNIT_GI);

        LOG.info("集群修改资源：构建prometheusCluster成功, serviceName:" + serviceName);
        return prometheusCluster;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        LOG.info("prometheus修改配置，data：" + JSON.toJSONString(data));

        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        String opt = data.get("opt");
        String configuration = data.get("configuration");
        Map<String, String> configurationMap = new HashMap<>();
        configurationMap = JSON.parseObject(configuration, Map.class);
        // 1、获取旧的prometheusCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改配置时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改配置时根据serviceId获取statefulService为null");
        }

        PrometheusCluster oldCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName,
                service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改配置时根据tenantName和serviceName获取prometheusCluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改配置时根据tenantName和serviceName获取prometheusCluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGECONFIG);

        // 3、修改prometheusCluster
        PrometheusCluster newCluster = buildClusterChangeConfig(tenantName, service.getServiceName(), opt,
                configuration);

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updatePrometheusCluster(tenantName, newCluster);

        componentOperationsClientUtil.changePrometheusClusterAndNodesStateByYaml(tenantName, serviceId,
                service.getServiceName());
        if (!updateResult) {
            return false;
        }

        Map<String, String> configDBMap = new HashMap<>();
        try {
            service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
            if (null != service) {
                String oldConfigDB = service.getConfigUpdated();
                if (StringUtils.isNotEmpty(oldConfigDB)) {
                    configDBMap = JSON.parseObject(oldConfigDB, Map.class);
                }
                switch (opt) {
                case PrometheusClusterConst.PROMETHEUS_CLUSTER_CONFIG_OPT_UPDATEPROMETHEUSYAML:
                    configDBMap.putAll(configurationMap);
                    break;
                default:
                    break;
                }
                service.setConfigUpdated(JSON.toJSONString(configDBMap));

                Map<String, String> extendedField = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
                extendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                service.setExtendedField(JSON.toJSONString(extendedField));
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("修改配置时修改StatefulService表配置失败，serviceId：" + serviceId + ",configUpdated:"
                    + JSON.toJSONString(configDBMap), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "修改配置失败，serviceId：" + serviceId
                    + ",configUpdated:" + JSON.toJSONString(configDBMap) + ",error:" + e.getMessage());
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private PrometheusCluster buildClusterChangeConfig(String tenantName, String serviceName, String opt,
            String configuration) throws ErrorMessageException {
        PrometheusCluster prometheusCluster = null;

        prometheusCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName, serviceName);

        Map<String, String> configurationMap = JSON.parseObject(configuration, Map.class);
        prometheusCluster.getSpec().setOpt(PrometheusClusterConst.PROMETHEUS_CLUSTER_OPT_CHANGE_CONFIG);
        prometheusCluster.getSpec().setConfigOpt(opt);
        switch (opt) {
        case PrometheusClusterConst.PROMETHEUS_CLUSTER_CONFIG_OPT_UPDATEPROMETHEUSYAML:
            prometheusCluster.getSpec().setPrometheusConfig(configurationMap);
            break;
        default:
            prometheusCluster.getSpec().setTargetConfig(configurationMap);
            break;
        }
        return prometheusCluster;
    }

    @Override
    public Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {

        Map<String, String> data = new HashMap<>();
        LOG.info("--------开始参数校验--------");
        // 1. 组件统一校验
        String serviceName = jsonObject.getString("serviceName");
        String performance = jsonObject.getString("performance");
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        checkCpuAndMemory(cpu, memory);

        // 2. prometheus参数统一校验
        String capacity = jsonObject.getString("capacity");
        String replicas = jsonObject.getString("replicas");
        checkCapacity(capacity);
        checkReplicas(replicas);

        String apiServer = jsonObject.getString("apiServer");
        String storageTsdbRetention = jsonObject.getString("storageTsdbRetention");
        String kubeStateMetricsPort = jsonObject.getString("kubeStateMetricsPort");
        String blackboxExporterPort = jsonObject.getString("blackboxExporterPort");

        String version = jsonObject.getString("version");
        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_PROMETHEUS, version);
        String projectId = jsonObject.getString("projectId");
        String orderId = jsonObject.getString("orderId");
        String createdBy = jsonObject.getString("createdBy");

        data.put("serviceName", serviceName);
        data.put("tenantName", tenantName);
        data.put("cpu", cpu);
        data.put("memory", memory);
        data.put("capacity", capacity);
        data.put("replicas", replicas);
        data.put("version", version);
        data.put("projectId", projectId);
        data.put("orderId", orderId);
        data.put("createdBy", createdBy);
        data.put("performance", performance);
        data.put("apiServer", apiServer);
        data.put("storageTsdbRetention", storageTsdbRetention);
        data.put("kubeStateMetricsPort", kubeStateMetricsPort);
        data.put("blackboxExporterPort", blackboxExporterPort);
        LOG.info("--------结束参数校验--------");

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

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId, String opt,
            JSONObject configuration) throws ErrorMessageException {
        LOG.info("prometheus集群修改配置个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:"
                + configuration);
        if (!PrometheusClusterConst.getPrometheusClusterConfigOptList().contains(opt)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "集群修改配置时，操作不符合规范,opt:" + opt);
        }
        if (null == configuration || StringUtils.isEmpty(JSON.toJSONString(configuration))) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
                    "集群修改配置时，配置参数为空,configuration:" + JSON.toJSONString(configuration));
        }

        Map<String, String> configurationMap = JSON.parseObject(JSON.toJSONString(configuration), Map.class);
        if (null == configurationMap || configurationMap.isEmpty()) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
                    "集群修改配置时，配置参数为空,configuration:" + JSON.toJSONString(configuration));
        }

        Map<String, String> map = new HashMap<>();
        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("opt", opt);
        map.put("configuration", JSON.toJSONString(configuration));
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getPrometheusConfig(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("获取service配置，接收的参数=>serviceId：" + serviceId);

        JSONObject returnMap = new JSONObject();
        // 校验tenantName
        checkTenantNameExist(tenantName);

        // 校验serviceId
        if (StringUtils.isEmpty(serviceId)) {
            LOG.error("获取配置失败, 要求serviceId不能为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "获取配置失败, 要求serviceId不能为空");
        }

        LOG.info("集群创建后获取配置");
        StatefulService service;
        try {
            service = statefulServiceRepository.findByNamespaceAndIdAndServiceStateNot(tenantName, serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
        } catch (Exception e) {
            LOG.error("获取配置失败，根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                    + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "获取配置失败，根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                            + serviceId + ",error:" + e.getMessage());
        }
        if (null == service) {
            LOG.error("根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "获取配置失败，根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                            + serviceId);
        }
        String serviceName;
        serviceName = service.getServiceName();
        try {
            Map<String, String> prometheusConfig = new HashMap<>();
            String configDB = service.getConfigUpdated();
            if (StringUtils.isNotEmpty(configDB)) {
                prometheusConfig = JSON.parseObject(configDB, Map.class);
                returnMap.fluentPut(PrometheusClusterConst.PROMETHEUS_YAML_CONF_NAME, prometheusConfig);
            }
            Map<String, String> extendedFieldMap = componentOperationsDataBaseUtil
                    .getServiceExtendedField(service.getExtendedField());
            String configMapName = extendedFieldMap.get("configMapName");
            if (StringUtils.isEmpty(configMapName)) {
                LOG.error("获取serviceName: " + serviceName + "的configmapName为空");
                throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                        "获取serviceName: " + serviceName + "的configmapName为空");

            }
            ConfigMap prometheusConfigmap;
            try {
                prometheusConfigmap = KubernetesClientFactory.getClient().configMaps().inNamespace(tenantName)
                        .withName(configMapName).get();
            } catch (Exception e) {
                LOG.error("获取serviceName: " + serviceName + "的configmap失败: ", e);
                throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                        "获取serviceName: " + serviceName + "的configmap失败，error：" + e.getMessage());

            }

            if (null == prometheusConfigmap || null == prometheusConfigmap.getData()) {
                LOG.error("获取serviceName: " + serviceName + "的configmap失败");
                throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                        "获取serviceName: " + serviceName + "的configmap失败");

            }

            String targetConfig = prometheusConfigmap.getData()
                    .get(PrometheusClusterConst.PROMETHEUS_TARGETS_CONF_NAME);

            LOG.info("返回的targets.json数据：" + targetConfig);
            List<Map<String, String>> targetConfigListReturn = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> targetConfigList = mapper.readValue(targetConfig,
                    new TypeReference<List<Map<String, Object>>>() {});
            Map<String, Object> target = new HashMap<>();
            Object labels;
            Object targetObject;
            for (int i = 0; i < targetConfigList.size(); i++) {
                target = targetConfigList.get(i);
                labels = target.get("labels");
                targetObject = target.get("targets");
                Map<String, String> lable = mapper.readValue(mapper.writeValueAsString(labels), Map.class);
                List<String> targetNameList = mapper.readValue(mapper.writeValueAsString(targetObject),
                        new TypeReference<List<String>>() {});
                lable.put("targets", targetNameList.get(0));
                targetConfigListReturn.add(lable);
            }
            LOG.info("返回的targets.json数据：" + JSON.toJSONString(targetConfigList));
            returnMap.fluentPut(PrometheusClusterConst.PROMETHEUS_TARGETS_CONF_NAME, targetConfigListReturn);
            return returnMap;
        } catch (Exception e) {
            LOG.error("获取serviceName: " + serviceName + "配置失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取serviceName: " + serviceName + "配置失败，error：" + e.getMessage());
        }
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

    @Override
    public boolean clusterExpand(Map<String, String> data) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Map<String, String> checkClusterExpandPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    
    }

    @Override
    public Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }
}
