package com.xxx.xcloud.module.component.service.worker.storm;

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
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.storm.StormCluster;
import com.xxx.xcloud.module.component.model.storm.StormExporter;
import com.xxx.xcloud.module.component.model.storm.StormHealthCheck;
import com.xxx.xcloud.module.component.model.storm.StormNode;
import com.xxx.xcloud.module.component.model.storm.StormOp;
import com.xxx.xcloud.module.component.model.storm.StormSpec;
import com.xxx.xcloud.utils.StringUtils;

@Service
@Scope("prototype")
public class StormClusterCreateWorker extends BaseStormClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(StormClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============StormClusterCreateWorker====================");
        // 1、获取数据
        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        String zkInterAddress = data.get("zkInterAddress");
        String version = data.get("version");
        int nimbusNum = Integer.parseInt(data.get("nimbusNum"));
        Double nimbusCpu = Double.parseDouble(data.get("nimbusCpu"));
        Double nimbusMemory = Double.parseDouble(data.get("nimbusMemory"));
        int supervisorNum = Integer.parseInt(data.get("supervisorNum"));
        Double supervisorCpu = Double.parseDouble(data.get("supervisorCpu"));
        Double supervisorMemory = Double.parseDouble(data.get("supervisorMemory"));
        Double capacity = Double.parseDouble(data.get("capacity"));

        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String configUpdated = data.get("configuration");
        String performance = data.get("performance");

        // 2、获取zk连接串
        zkInterAddress = zkInterAddress.replaceAll(":2181", "").substring(5);

        // 3、 获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (Exception e) {
            LOG.error("集群创建时获取service失败，error：" + e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群创建时获取的service：" + JSON.toJSONString(service));

        // 4、拼接stormCluster
        StormCluster stormCluster = buildStormCluster(tenantName, projectId, orderId, version, serviceName, nimbusNum,
                nimbusCpu, nimbusMemory, supervisorNum, supervisorCpu, supervisorMemory, capacity, configUpdated,
                zkInterAddress, performance);

        // 5、调用k8s client创建
        if (!createAndRetry(tenantName, stormCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.error("storm集群" + serviceName + "创建失败！");
            return;
        }

        // 6、循环获取创建结果
        checkCreateResult(tenantName, service, nimbusCpu, nimbusMemory, supervisorCpu, supervisorMemory, capacity);
    }

    /**
     * 拼接stormcluster
     * 
     * @param tenantName
     * @param projectId
     * @param orderId
     * @param version
     * @param serviceName
     * @param nimbusNum
     * @param nimbusCpu
     * @param nimbusMemory
     * @param supervisorNum
     * @param supervisorCpu
     * @param supervisorMemory
     * @param capacity
     * @param configUpdated
     * @param zkInterAddress
     * @param performance
     * @return
     */
    private StormCluster buildStormCluster(String tenantName, String projectId, String orderId, String version,
            String serviceName, int nimbusNum, Double nimbusCpu, Double nimbusMemory, int supervisorNum,
            Double supervisorCpu, Double supervisorMemory, Double capacity, String configUpdated, String zkInterAddress,
            String performance) {

        StormCluster stormCluster = new StormCluster();
        stormCluster.getMetadata().setName(serviceName);

        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            stormCluster.getMetadata().setLabels(labels);
        }

        // spec
        StormSpec spec = new StormSpec();
        String stormImage = getRepoPath(CommonConst.APPTYPE_STORM, null, version);

        StormOp stormOp = new StormOp();
        stormOp.setOperator(StormClusterConst.OPERATOR_CLUSTER_CREATE);
        stormOp.setNodeName("");
        spec.setStormOp(stormOp);
        spec.setVersion(version);
        spec.setImage(stormImage);

        /* healthCheck */
        StormHealthCheck healthCheck = new StormHealthCheck();
        healthCheck.setEnable(false);
        healthCheck.setLivenessDelayTimeout(CommonConst.NUMBER_THIRTY);
        healthCheck.setLivenessFailureThreshold(CommonConst.NUMBER_TEN);
        healthCheck.setReadinessDelayTimeout(CommonConst.NUMBER_THIRTY);
        healthCheck.setReadinessFailureThreshold(CommonConst.NUMBER_TEN);
        spec.setHealthCheck(healthCheck);

        /* nodeNum */
        Map<String, Integer> nodeNum = new HashMap<>(2);
        nodeNum.put(StormClusterConst.ROLE_NIMBUS, nimbusNum);
        nodeNum.put(StormClusterConst.ROLE_SUPERVISOR, supervisorNum);
        spec.setReplicas(nodeNum);

        /* resources */
        Map<String, Resources> resources = new HashMap<>(2);
        resources.put(StormClusterConst.ROLE_NIMBUS,
                componentOperationsClientUtil.getResources(nimbusCpu, nimbusMemory, CommonConst.UNIT_GI));
        resources.put(StormClusterConst.ROLE_SUPERVISOR,
                componentOperationsClientUtil.getResources(supervisorCpu, supervisorMemory, CommonConst.UNIT_GI));
        spec.setResources(resources);

        spec.setCapacity(capacity + CommonConst.UNIT_GI);
        spec.setVolume(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        Map<String, String> config = buildConfig(configUpdated, zkInterAddress, version);
        if (null != config && !config.isEmpty()) {
            spec.setConfig(config);
        }

        /* nodeSelector */
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_STORM);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        boolean nodeSelectorPerformance = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.NODESELECTOR_PERFORMANCE));
        if (StringUtils.isNotEmpty(performance) && nodeSelectorPerformance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }

        if (!nodeSelector.isEmpty()) {
            spec.setNodeSelector(nodeSelector);
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            spec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        /* exporter */
        StormExporter exporter = new StormExporter();
        String exporterImage = getRepoPath(CommonConst.APPTYPE_STORM, CommonConst.EXPORTER_STRING, version);
        exporter.setImage(exporterImage);
        exporter.setRefreshRate(CommonConst.NUMBER_FIVE);
        exporter.setMaxRetries(CommonConst.NUMBER_TWELVE);
        spec.setExporter(exporter);

        stormCluster.setSpec(spec);
        return stormCluster;
    }

    /**
     * 生成yaml中的配置
     * 
     * @param configUpdated
     * @param zkInterAddress
     * @param version
     * @return
     */
    private Map<String, String> buildConfig(String configUpdated, String zkInterAddress, String version) {
        Map<String, String> changedUnitMap = new HashMap<>();
        changedUnitMap.put(StormClusterConst.ZOOKEEPER_SERVERS, zkInterAddress);
        if (null == configUpdated || StringUtils.isEmpty(configUpdated)) {
            LOG.info("创建时没有修改参数");
        } else {
            LOG.info("configUpdated" + configUpdated);
            JSONObject configUpdatedJson = JSON.parseObject(configUpdated);

            LOG.info("configUpdatedMap:" + JSON.toJSONString(configUpdatedJson));
            if (null != configUpdatedJson && !configUpdatedJson.isEmpty()) {
                Map<String, String> mergedUnitMap = componentOperationsDataBaseUtil
                        .parseConfigUpdatedForYaml(configUpdatedJson, CommonConst.APPTYPE_STORM, version);
                changedUnitMap.putAll(mergedUnitMap);
            }
        }
        return changedUnitMap;
    }

    /**
     * 创建stormcluster集群
     * 
     * @param tenantName
     * @param stormCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, StormCluster stormCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建stormCluster：" + JSON.toJSONString(stormCluster));
        if (null != stormCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = createStormCluster(tenantName, stormCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("创建storm集群" + stormCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检查创建结果
     * 
     * @param tenantName
     * @param service
     * @param nimbusCpu
     * @param nimbusMemory
     * @param supervisorCpu
     * @param supervisorMemory
     * @param capacity
     */
    private void checkCreateResult(String tenantName, StatefulService service, Double nimbusCpu, Double nimbusMemory,
            Double supervisorCpu, Double supervisorMemory, Double capacity) {
        long start = System.currentTimeMillis();
        boolean hasNodes = false;
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时：" + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                break;
            }
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("创建超时，service：" + JSON.toJSONString(service));
                break;
            }
            StormCluster stormCluster = componentOperationsClientUtil.getStormCluster(tenantName,
                    service.getServiceName());
            LOG.info("判断stormCluster中数据是否存在");
            LOG.info("stormCluster:" + JSON.toJSONString(stormCluster));
            LOG.info("status:" + stormCluster.getStatus());
            if (null != stormCluster && null != stormCluster.getStatus()
                    && null != stormCluster.getStatus().getServerNodes()
                    && null != stormCluster.getStatus().getServerNodes().keySet() && !hasNodes
                    && service.getNodeNum() <= stormCluster.getStatus().getServerNodes().keySet().size()) {
                if (buildNodes(stormCluster, service, nimbusCpu, nimbusMemory, supervisorCpu, supervisorMemory,
                        capacity)) {
                    LOG.info("节点表插入数据成功,集群名称：" + stormCluster.getMetadata().getName());
                    hasNodes = true;
                }
            }
            if (null != stormCluster && null != stormCluster.getStatus()) {
                LOG.info("cluster此时的状态：" + stormCluster.getStatus().getPhase());
            }
            if (hasNodes && null != stormCluster && null != stormCluster.getStatus()
                    && null != stormCluster.getStatus().getServerNodes()
                    && null != stormCluster.getStatus().getServerNodes().keySet()
                    && service.getNodeNum() <= stormCluster.getStatus().getServerNodes().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(stormCluster.getStatus().getPhase())) {
                LOG.info("storm集群创建成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;

            }
        }

        // 检查结束
        optClusterCreate(tenantName, service.getId(), service.getServiceName(), hasNodes);

    }

    /**
     * storm存储节点信息
     * 
     * @param stormCluster
     * @param service
     * @param nimbusCpu
     * @param nimbusMemory
     * @param supervisorCpu
     * @param supervisorMemory
     * @param capacity
     * @return
     */
    private boolean buildNodes(StormCluster stormCluster, StatefulService service, Double nimbusCpu,
            Double nimbusMemory, Double supervisorCpu, Double supervisorMemory, Double capacity) {
        if (null == stormCluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");

        StatefulNode node = null;
        for (Map.Entry<String, StormNode> entry : stormCluster.getStatus().getServerNodes().entrySet()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(service.getId(),
                        entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                if (StormClusterConst.ROLE_NIMBUS.equals(entry.getValue().getRole())) {
                    node.setCpu(nimbusCpu);
                    node.setMemory(nimbusMemory);
                    // node.setStorage(capacity);
                    node.setLvmName(entry.getValue().getVolumeid());
                } else {
                    node.setCpu(supervisorCpu);
                    node.setMemory(supervisorMemory);
                }

                node.setAppType(CommonConst.APPTYPE_STORM);
                node.setServiceId(service.getId());
                node.setServiceName(service.getServiceName());

                node.setNodeName(entry.getKey());
                node.setRole(entry.getValue().getRole());

                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());

                statefulNodeRepository.save(node);
            } catch (Exception e) {
                LOG.error("保存节点记录信息失败,data:" + JSON.toJSONString(node) + ",error:", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 处理集群创建
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optClusterCreate(String tenantName, String serviceId, String serviceName, boolean hasNodes) {
        // 获取stormCluster
        StormCluster stormCluster = componentOperationsClientUtil.getStormCluster(tenantName, serviceName);

        if (hasNodes) {
            // 注册逻辑卷
            registeClusterLvm(tenantName, stormCluster);
        }
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(stormCluster);
        int uiPort = 0;
        String host = "";
        Map<String, String> clusterExtendedField = new HashMap<>();
        if (null != stormCluster && null != stormCluster.getStatus()
                && null != stormCluster.getStatus().getServerNodes()) {
            Map<String, StormNode> serverNodes = stormCluster.getStatus().getServerNodes();
            for (Map.Entry<String, StormNode> entry : serverNodes.entrySet()) {
                if (StormClusterConst.ROLE_NIMBUS.equals(entry.getValue().getRole())) {
                    host = entry.getValue().getHostip();
                    uiPort = entry.getValue().getNodePort();
                }
            }
        }
        if (StringUtils.isNotEmpty(host) && 0 != uiPort) {
            clusterExtendedField.put(StormClusterConst.NIMBUS_UI_URL, "http://" + host + ":" + uiPort);
        }
        clusterExtendedField.put(StormClusterConst.JAR_UPLOAD_PATH_KEY, StormClusterConst.JAR_UPLOAD_PATH_VALUE);

        // 修改数据库中集群、节点状态
        componentOperationsClientUtil.changeStormClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, clusterExtendedField, nodesExtendedField);

    }

}
