package com.xxx.xcloud.module.component.service.worker.zookeeper;

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
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;
import com.xxx.xcloud.module.component.model.zookeeper.ZkExporter;
import com.xxx.xcloud.module.component.model.zookeeper.ZkInstance;
import com.xxx.xcloud.module.component.model.zookeeper.ZkSpec;
import com.xxx.xcloud.utils.StringUtils;

@Service
@Scope("prototype")
public class ZkClusterCreateWorker extends BaseZkClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(ZkClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============ZkClusterCreateWorker====================");
        // 1、获取数据
        String tenantName = data.get("tenantName");
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        String version = data.get("version");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        int replicas = Integer.parseInt(data.get("replicas"));
        String configuration = data.get("configuration");
        String performance = data.get("performance");

        // 2、 获取service
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

        // 3、拼接zkCluster
        ZkCluster zkCluster = buildZkCluster(tenantName, projectId, orderId, version, serviceName, cpu, memory,
                replicas, configuration, performance);

        // 4、调用k8s client创建
        if (!createAndRetry(tenantName, zkCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.error("zk集群" + serviceName + "创建失败！");
            return;
        }

        // 5、循环获取创建结果
        checkCreateResult(tenantName, service, cpu, memory);
    }

    /**
     * 拼接zk cluster
     * 
     * @param tenantName
     * @param projectId
     * @param orderId
     * @param version
     * @param serviceName
     * @param cpu
     * @param memory
     * @param replicas
     * @param configUpdated
     * @param performance
     * @return
     */
    private ZkCluster buildZkCluster(String tenantName, String projectId, String orderId, String version,
            String serviceName, Double cpu, Double memory, int replicas, String configuration, String performance) {
        ZkCluster zkCluster = new ZkCluster();

        zkCluster.getMetadata().setName(serviceName);
        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            zkCluster.getMetadata().setLabels(labels);
        }

        ZkSpec spec = new ZkSpec();
        spec.setImage(getRepoPath(CommonConst.APPTYPE_ZK, null, version));
        spec.setOpt(ZkClusterConst.OPERATOR_CLUSTER_CREATE);
        spec.setReplicas(replicas);
        ZkExporter zkExporter = new ZkExporter();
        zkExporter.setImage(getRepoPath(CommonConst.APPTYPE_ZK, CommonConst.EXPORTER, version));
        spec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        spec.setExporter(zkExporter);
        spec.setVersion(version);
        spec.setVgName(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));

        if (StringUtils.isNotEmpty(configuration)) {
            JSONObject configurationJson = JSON.parseObject(configuration);
            Map<String, String> config = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configurationJson,
                    CommonConst.APPTYPE_ZK, version);
            if (null != config && !config.isEmpty()) {
                spec.setConfig(config);
            }
        }

        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_ZK);
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

        zkCluster.setSpec(spec);
        LOG.info("创建拼接的zkCluster：" + JSON.toJSONString(zkCluster));

        return zkCluster;
    }

    /**
     * 发送创建请求
     *
     * @param user
     * @param zkCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, ZkCluster zkCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建zkCluster：" + JSON.toJSONString(zkCluster));
        if (null != zkCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = createZkCluster(tenantName, zkCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("创建zk集群" + zkCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检查创建集群结果
     * 
     * @param tenantName
     * @param service
     * @param cpu
     * @param memory
     */
    private void checkCreateResult(String tenantName, StatefulService service, Double cpu, Double memory) {

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
            ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, service.getServiceName());
            LOG.info("判断zkCluster中数据是否存在");
            LOG.info("zkCluster:" + JSON.toJSONString(zkCluster));
            LOG.info("status:" + zkCluster.getStatus());
            if (null != zkCluster && null != zkCluster.getStatus() && null != zkCluster.getStatus().getInstances()
                    && null != zkCluster.getStatus().getInstances().keySet() && !hasNodes
                    && service.getNodeNum() <= zkCluster.getStatus().getInstances().keySet().size()) {
                if (buildNodes(tenantName, zkCluster, service, cpu, memory)) {
                    LOG.info("节点表插入数据成功,集群名称：" + zkCluster.getMetadata().getName());
                    hasNodes = true;
                }
            }
            if (null != zkCluster && null != zkCluster.getStatus()) {
                LOG.info("cluster此时的状态：" + zkCluster.getStatus().getPhase());
            }
            if (hasNodes && null != zkCluster && null != zkCluster.getStatus()
                    && null != zkCluster.getStatus().getInstances()
                    && null != zkCluster.getStatus().getInstances().keySet()
                    && service.getNodeNum() <= zkCluster.getStatus().getInstances().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(zkCluster.getStatus().getPhase())) {
                LOG.info("Zk集群创建成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;
            }
        }

        // 检查结束
        optClusterCreate(tenantName, service.getId(), service.getServiceName(), hasNodes);

    }

    private void optClusterCreate(String tenantName, String serviceId, String serviceName, boolean hasNodes) {
        // 获取zkCluster
        ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, serviceName);

        if (hasNodes) {
            // 注册逻辑卷
            registeClusterLvm(tenantName, zkCluster);
        }
        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(zkCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(zkCluster);

        // 修改集群和节点状态
        componentOperationsClientUtil.changeZkClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName,
                serviceExtendedField, nodesExtendedField);
    }

    /**
     * 存节点表
     * 
     * @param cluster
     * @param service
     * @param cpu
     * @param memory
     * @return
     */
    private boolean buildNodes(String tenantName, ZkCluster cluster, StatefulService service, Double cpu,
            Double memory) {
        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");

        StatefulNode node = null;
        for (Map.Entry<String, ZkInstance> entry : cluster.getStatus().getInstances().entrySet()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(service.getId(),
                        entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                node.setCpu(cpu);
                node.setMemory(memory);

                node.setAppType(CommonConst.APPTYPE_ZK);
                node.setServiceId(service.getId());
                node.setServiceName(service.getServiceName());

                node.setNodeName(entry.getKey());
                node.setRole(entry.getValue().getRole());
                node.setLvmName(entry.getValue().getLvName());

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
}
