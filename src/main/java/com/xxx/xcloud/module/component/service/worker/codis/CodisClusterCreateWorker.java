package com.xxx.xcloud.module.component.service.worker.codis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CodisClusterConst;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;
import com.xxx.xcloud.module.component.model.codis.CodisDashboardSpec;
import com.xxx.xcloud.module.component.model.codis.CodisGroupBindingNode;
import com.xxx.xcloud.module.component.model.codis.CodisGroupStatus;
import com.xxx.xcloud.module.component.model.codis.CodisGroupsSpec;
import com.xxx.xcloud.module.component.model.codis.CodisProxySpec;
import com.xxx.xcloud.module.component.model.codis.CodisSentinelSpec;
import com.xxx.xcloud.module.component.model.codis.CodisSpec;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
@Scope("prototype")
public class CodisClusterCreateWorker extends BaseCodisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(CodisClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============CodisClusterCreateWorker====================");
        // 1、获取数据
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");

        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        String version = data.get("version");
        String serviceName = data.get("serviceName");
        String password = data.get("password");
        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));

        double proxyCpu = Double.parseDouble(data.get("proxyCpu"));
        double proxyMemory = Double.parseDouble(data.get("proxyMemory"));
        int proxyReplicas = Integer.parseInt(data.get("proxyReplicas"));
        String configuration = data.get("configuration");

        String performance = data.get("performance");

        // 2、获取zk连接串
        String zkInterAddress = data.get("zkInterAddress");

        // 3、拼接codisCluster
        CodisCluster codisCluster = buildCodisCluster(projectId, orderId, tenantName, serviceName, version,
                zkInterAddress, password, cpu, memory, capacity, replicas, proxyCpu, proxyMemory, proxyReplicas,
                configuration, performance);

        LOG.info("创建拼接后的createCodisCluster：" + JSON.toJSONString(codisCluster));
        // 4、调用k8sAPI创建
        if (!createAndRetry(tenantName, codisCluster)) {
            LOG.info("创建集群失败！");
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            return;
        }
        // 5、循环获取创建结果
        if (!checkClusterCreateResult(tenantName, serviceId, serviceName)) {
            componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
        }

    }

    /**
     * 检查集群创建结果
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @return
     */
    private Boolean checkClusterCreateResult(String tenantName, String serviceId, String serviceName) {
        long start = System.currentTimeMillis();
        boolean hasNodes = false;
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                return false;
            }
            LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.info("=================创建超时================");
                return false;
            }

            CodisCluster codisCluster = componentOperationsClientUtil.getCodisCluster(tenantName, serviceName);
            LOG.info("判断codisCluster中数据是否存在");
            LOG.info("codisCluster:" + JSON.toJSONString(codisCluster));
            LOG.info("status:" + codisCluster.getStatus());

            if (null != codisCluster && null != codisCluster.getStatus() && null != codisCluster.getStatus().getGroup()
                    && codisCluster.getSpec().getServerGroups().getReplicas() == codisCluster.getStatus().getGroup()
                            .keySet().size()) {
                if (!hasNodes) {
                    LOG.info("节点表插入数据");
                    // 创建WAITING的节点
                    if (buildNodes(codisCluster, serviceId)) {
                        LOG.info("节点表插入数据成功！===" + codisCluster.getMetadata().getName());
                        hasNodes = true;
                    }
                }
                if (hasNodes && CommonConst.STATE_CLUSTER_RUNNING.equals(codisCluster.getStatus().getPhase())) {
                    // 注册逻辑卷 获取节点ip、port
                    String dashboardAndProxyIp = null;
                    Map<String, Map<String, String>> nodesMessageMap = new HashMap<>();
                    for (CodisGroupStatus codisGroup : codisCluster.getStatus().getGroup().values()) {
                        for (CodisGroupBindingNode bindingNode : codisGroup.getBindings().values()) {
                            Map<String, String> nodeMessageMap = new HashMap<>();
                            LOG.info("节点: " + bindingNode.getName() + "修改状态为RUNNING");
                            String nodeIp = bindingNode.getBindIp();
                            nodeMessageMap.put("ip", nodeIp);
                            dashboardAndProxyIp = nodeIp;
                            String storage = codisCluster.getSpec().getServerGroups().getCapacity();
                            componentOperationsClientUtil.registerLvm(tenantName, bindingNode.getName(), nodeIp,
                                    storage);
                            nodesMessageMap.put(bindingNode.getName(), nodeMessageMap);
                        }

                    }
                    if (null != codisCluster.getStatus().getProxy()) {
                        Map<String, String> proxyMessageMap = new HashMap<>();
                        proxyMessageMap.put("ip", dashboardAndProxyIp);
                        int proxyPort = codisCluster.getStatus().getProxy().getNodePort();
                        proxyMessageMap.put("port", String.valueOf(proxyPort));
                        nodesMessageMap.put(CodisClusterConst.CODIS_ROLE_PROXY, proxyMessageMap);
                    }
                    if (null != codisCluster.getStatus().getDashboard()) {
                        Map<String, String> proxyMessageMap = new HashMap<>();
                        proxyMessageMap.put("ip", dashboardAndProxyIp);
                        int dashboradPort = codisCluster.getStatus().getDashboard().getNodePort();
                        proxyMessageMap.put("port", String.valueOf(dashboradPort));
                        nodesMessageMap.put(CodisClusterConst.CODIS_ROLE_DASHBOARD, proxyMessageMap);
                    }

                    // 修改集群状态、扩展字段与节点状态、ip、port
                    Map<String, String> serviceExtendedFieldMap = new HashMap<>();
                    serviceExtendedFieldMap.put("proxyReplicas",
                            String.valueOf(codisCluster.getSpec().getProxy().getReplicas()));

                    componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_RUNNING,
                            CommonConst.STATE_NODE_RUNNING, serviceExtendedFieldMap, nodesMessageMap);
                    return true;

                }

            }
        }
    }

    private boolean buildNodes(CodisCluster codisCluster, String serviceId) {

        if (null == codisCluster.getStatus()) {
            LOG.info("节点表插入时从codisCluster中获取status为空");
            return false;
        }
        StatefulNode node = new StatefulNode();
        try {
            for (CodisGroupStatus codisGroup : codisCluster.getStatus().getGroup().values()) {
                for (CodisGroupBindingNode bindingNode : codisGroup.getBindings().values()) {
                    StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(serviceId,
                            bindingNode.getName(), CommonConst.STATE_NODE_WAITING);
                    if (null != oldNode) {
                        LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                        continue;
                    }
                    node = new StatefulNode();
                    Double cpu = Double
                            .parseDouble(codisCluster.getSpec().getServerGroups().getResources().getLimits().getCpu());
                    String memoryStr = codisCluster.getSpec().getServerGroups().getResources().getLimits().getMemory();
                    Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                    String capacityStr = codisCluster.getSpec().getServerGroups().getCapacity();
                    Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                    node.setCpu(cpu);
                    node.setMemory(memory);
                    node.setStorage(capacity);

                    node.setAppType(CommonConst.APPTYPE_CODIS);
                    node.setServiceId(serviceId);
                    node.setServiceName(codisCluster.getMetadata().getName());

                    node.setNodeName(bindingNode.getName());
                    node.setRole(CodisClusterConst.CODIS_ROLE_SERVER);
                    node.setLvmName(bindingNode.getName());

                    node.setNodeState(CommonConst.STATE_NODE_WAITING);
                    node.setCreateTime(new Date());
                    String extendedField = "";
                    Map<String, String> extendedFieldMap = new HashMap<>();
                    extendedFieldMap.put("groupId", String.valueOf(codisGroup.getId()));
                    extendedField = JSON.toJSONString(extendedFieldMap);
                    node.setExtendedField(extendedField);
                    statefulNodeRepository.save(node);

                }

            }

            if (null != codisCluster.getStatus().getProxy()) {
                node = new StatefulNode();
                Double cpu = Double.parseDouble(codisCluster.getSpec().getProxy().getResources().getLimits().getCpu());
                String memoryStr = codisCluster.getSpec().getProxy().getResources().getLimits().getMemory();
                Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));

                node.setCpu(cpu);
                node.setMemory(memory);

                node.setAppType(CommonConst.APPTYPE_CODIS);
                node.setServiceId(serviceId);
                node.setServiceName(codisCluster.getMetadata().getName());

                node.setNodeName(CodisClusterConst.CODIS_ROLE_PROXY);
                node.setRole(CodisClusterConst.CODIS_ROLE_PROXY);

                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());
                statefulNodeRepository.save(node);
            }

            if (null != codisCluster.getStatus().getSentinel()) {
                node = new StatefulNode();
                node.setAppType(CommonConst.APPTYPE_CODIS);
                node.setServiceId(serviceId);
                node.setServiceName(codisCluster.getMetadata().getName());
                node.setNodeName(CodisClusterConst.CODIS_ROLE_SENTINEL);
                node.setRole(CodisClusterConst.CODIS_ROLE_SENTINEL);
                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());
                statefulNodeRepository.save(node);
            }
            if (null != codisCluster.getStatus().getDashboard()) {
                node = new StatefulNode();
                node.setAppType(CommonConst.APPTYPE_CODIS);
                node.setServiceId(serviceId);
                node.setServiceName(codisCluster.getMetadata().getName());
                node.setNodeName(CodisClusterConst.CODIS_ROLE_DASHBOARD);
                node.setRole(CodisClusterConst.CODIS_ROLE_DASHBOARD);
                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());
                statefulNodeRepository.save(node);
            }
        } catch (Exception e) {
            LOG.error("保存节点记录信息失败,data:" + JSON.toJSONString(node) + ",error:", e);
            return false;
        }
        return true;
    }

    /**
     * build codis yaml
     *
     * @param tenantName
     * @param serviceName
     * @param version
     * @param zkAddr
     * @param password
     * @param cpu
     * @param memory
     * @param capacity
     * @param replicas
     * @param proxyCpu
     * @param proxyMemory
     * @param proxyReplicas
     * @param configuration
     * @param performance
     * @return
     */
    @SuppressWarnings("unchecked")
    private CodisCluster buildCodisCluster(String projectId, String orderId, String tenantName, String serviceName,
            String version, String zkInterAddress, String password, double cpu, double memory, double capacity,
            int replicas, double proxyCpu, double proxyMemory, int proxyReplicas, String configuration,
            String performance) {
        LOG.info("===============开始构建codisCluster==================");
        CodisCluster codisCluster = new CodisCluster();
        ObjectMeta metaData = new ObjectMeta();
        codisCluster.setKind(CodisClusterConst.KIND_CODIS);
        codisCluster.setApiVersion(CodisClusterConst.API_VERSION);
        metaData.setNamespace(tenantName);
        metaData.setName(serviceName);

        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            metaData.setLabels(labels);
        }

        codisCluster.setMetadata(metaData);

        CodisSpec codisSpec = new CodisSpec();
        codisSpec.setStopped(false);
        codisSpec.setVersion(version);
        codisSpec.setImage(getRepoPath(CommonConst.APPTYPE_CODIS, null, version));
        codisSpec.setExporterImage(getRepoPath(CommonConst.APPTYPE_CODIS, "exporter", version));
        codisSpec.setLogDir(CodisClusterConst.CODIS_EXPORTER_LOGDIR);
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_CODIS);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        if (null != performance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        codisSpec.setNodeSelector(nodeSelector);
        Map<String, Map<String, String>> allConfig = new HashMap<>();

        if (StringUtils.isNotEmpty(configuration)) {
            JSONObject configMapJson = JSON.parseObject(configuration);
            if (null != configMapJson && !configMapJson.isEmpty()) {
                Map<String, String> configMutil = new HashMap<>();
                for (Map.Entry<String, Object> entryFile : configMapJson.entrySet()) {
                    JSONObject configUpdatedFile = JSON.parseObject(JSON.toJSONString(entryFile.getValue()));
                    for (Map.Entry<String, Object> entryLevel : configUpdatedFile.entrySet()) {
                        Map<String, String> config = JSON.parseObject(JSON.toJSONString(entryLevel.getValue()),
                                Map.class);
                        configMutil = componentOperationsDataBaseUtil.multiplyUnitConfigMap(config, CommonConst.APPTYPE_CODIS,
                                version);
                    }
                    allConfig.put(entryFile.getKey(), configMutil);
                }
            }
        }

        // dashboard
        CodisDashboardSpec dashboard = new CodisDashboardSpec();
        Map<String, String> coordinator = new HashMap<>();
        coordinator.put("name", CommonConst.APPTYPE_ZK);
        coordinator.put("addr", zkInterAddress.replace("zk://", ""));
        dashboard.setCoordinator(coordinator);
        dashboard.setReplicas(CodisClusterConst.CODIS_DASHBOARD_REPLICAS);
        Resources dashboardResources = componentOperationsClientUtil.getResources(
                CodisClusterConst.CODIS_DASHBOARD_DEFAULT_CPU, CodisClusterConst.CODIS_DASHBOARDL_DEFAULT_MEMORY,
                CommonConst.UNIT_MI);
        dashboard.setResources(dashboardResources);
        Map<String, String> dashboardConfig = new HashMap<>();
        dashboardConfig = allConfig.get(CodisClusterConst.CODIS_DASHBOARD_CONF_NAME);
        if (null != dashboardConfig) {
            dashboard.setConfig(dashboardConfig);
        }
        codisSpec.setDashboard(dashboard);

        // proxy
        CodisProxySpec proxy = new CodisProxySpec();
        proxy.setReplicas(proxyReplicas);
        Resources proxyResources = componentOperationsClientUtil.getResources(proxyCpu, proxyMemory,
                CommonConst.UNIT_GI);
        proxy.setResources(proxyResources);
        proxy.setPassword(password);

        Map<String, String> proxyConfig = new HashMap<>();
        proxyConfig = allConfig.get(CodisClusterConst.CODIS_PROXY_CONF_NAME);
        if (null != proxyConfig) {
            proxy.setConfig(proxyConfig);
        }
        codisSpec.setProxy(proxy);

        // sentinel
        CodisSentinelSpec codisSentinel = new CodisSentinelSpec();
        codisSentinel.setReplicas(CodisClusterConst.CODIS_SENTINEL_SLAVE_REPLICAS);
        Resources sentinelResources = componentOperationsClientUtil.getResources(
                CodisClusterConst.CODIS_SENTINEL_DEFAULT_CPU, CodisClusterConst.CODIS_SENTINEL_DEFAULT_MEMORY,
                CommonConst.UNIT_MI);
        codisSentinel.setResources(sentinelResources);
        codisSpec.setSentinel(codisSentinel);

        // Groups
        CodisGroupsSpec serverGroups = new CodisGroupsSpec();
        Map<String, String> serverGroupsConfig = new HashMap<>();
        serverGroupsConfig = allConfig.get(CodisClusterConst.CODIS_SERVER_GROUPS_CONF_NAME);
        if (null != serverGroupsConfig) {
            serverGroups.setConfig(serverGroupsConfig);
        }
        serverGroups.setCapacity(capacity + CommonConst.UNIT_GI);
        LOG.info("codis创建时的replicas：" + replicas);
        serverGroups.setReplicas(replicas / (CodisClusterConst.CODIS_SERVER_GROUPS_SLAVE_REPLICAS + 1));
        serverGroups.setSlaves(CodisClusterConst.CODIS_SERVER_GROUPS_SLAVE_REPLICAS);
        serverGroups.setStorageClass(CodisClusterConst.CODIS_STORAGE_CLASS_LVM);
        serverGroups.setVolume(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        Resources resources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        serverGroups.setResources(resources);
        codisSpec.setServerGroups(serverGroups);

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            codisSpec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        codisCluster.setSpec(codisSpec);

        LOG.info("build成功的codisCluster:" + JSON.toJSONString(codisCluster));
        return codisCluster;
    }

    private boolean createAndRetry(String tenantName, CodisCluster codisCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建codisCluster：" + JSON.toJSONString(codisCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = createCodisCluster(tenantName, codisCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("创建codis集群" + codisCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

}
