package com.xxx.xcloud.module.component.service.worker.kafka;

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
import com.xxx.xcloud.module.component.consts.KafkaClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.kafka.KafkaCluster;
import com.xxx.xcloud.module.component.model.kafka.KafkaConfig;
import com.xxx.xcloud.module.component.model.kafka.KafkaNode;
import com.xxx.xcloud.module.component.model.kafka.KafkaOp;
import com.xxx.xcloud.module.component.model.kafka.KafkaSpec;
import com.xxx.xcloud.utils.StringUtils;

@Service
@Scope("prototype")
public class KafkaClusterCreateWorker extends BaseKafkaClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(KafkaClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============KafkaClusterCreateWorker====================");
        // 1、获取数据
        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        String zkExterAddress = data.get("zkExterAddress");
        String version = data.get("version");
        int replicas = Integer.parseInt(data.get("replicas"));
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));

        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String configUpdated = data.get("configuration");
        String performance = data.get("performance");

        // 2、获取zk连接串
        zkExterAddress = zkExterAddress.substring(5);

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

        // 4、拼接kafkaCluster
        KafkaCluster kafkaCluster = buildKafkaCluster(tenantName, projectId, orderId, version, serviceName, replicas,
                cpu, memory, capacity, configUpdated, zkExterAddress, performance);

        // 5、调用k8s client创建
        if (!createAndRetry(tenantName, kafkaCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.error("kafka集群" + serviceName + "创建失败！");
            return;
        }

        // 6、循环获取创建结果
        checkCreateResult(tenantName, service, cpu, memory, capacity);

    }

    /**
     * 构造kafkacluster
     * 
     * @param tenantName
     * @param projectId
     * @param orderId
     * @param version
     * @param serviceName
     * @param replicas
     * @param cpu
     * @param memory
     * @param capacity
     * @param configUpdated
     * @param zkExterAddress
     * @param performance
     * @return
     */
    private KafkaCluster buildKafkaCluster(String tenantName, String projectId, String orderId, String version,
            String serviceName, int replicas, Double cpu, Double memory, Double capacity, String configUpdated,
            String zkExterAddress, String performance) {

        KafkaCluster kafkaCluster = new KafkaCluster();

        kafkaCluster.getMetadata().setName(serviceName);

        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            kafkaCluster.getMetadata().setLabels(labels);
        }

        KafkaSpec spec = new KafkaSpec();
        String kafkaImage = getRepoPath(CommonConst.APPTYPE_KAFKA, null, version);

        KafkaOp kafkaOp = new KafkaOp();
        kafkaOp.setOperator(KafkaClusterConst.OPERATOR_CLUSTER_CREATE);
        kafkaOp.setNodename("");
        spec.setKafkaop(kafkaOp);

        spec.setVersion(version);
        spec.setImage(kafkaImage);
        spec.setHealthcheck(false);
        spec.setUpdatetime(String.valueOf(new Date()));
        spec.setReplicas(replicas);
        spec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        spec.setCapacity(capacity + CommonConst.UNIT_GI);
        spec.setVolume(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        KafkaConfig config = new KafkaConfig();
        config = buildConfig(config, configUpdated, zkExterAddress, version, tenantName, serviceName);
        spec.setConfig(config);
        /* nodeSelector */
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_KAFKA);
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

        kafkaCluster.setSpec(spec);

        return kafkaCluster;
    }

    /**
     * 构造config
     * 
     * @param config
     * @param configUpdated
     * @param zkExterAddress
     * @param version
     * @return
     */
    private KafkaConfig buildConfig(KafkaConfig config, String configUpdated, String zkExterAddress, String version,
            String tenantName, String serviceName) {
        Map<String, String> changedUnitMap = new HashMap<>();
        changedUnitMap.put(KafkaClusterConst.ZOOKEEPER_SERVERS,
                zkExterAddress + "/" + tenantName + "/" + CommonConst.APPTYPE_KAFKA + "/" + serviceName);
        if (null == configUpdated || StringUtils.isEmpty(configUpdated)) {
            LOG.info("创建时没有修改参数");
        } else {
            LOG.info("configUpdated" + configUpdated);
            JSONObject configUpdatedJson = JSON.parseObject(configUpdated);

            LOG.info("configUpdatedMap:" + JSON.toJSONString(configUpdatedJson));
            if (null != configUpdatedJson && !configUpdatedJson.isEmpty()) {
                Map<String, String> mergedUnitMap = componentOperationsDataBaseUtil
                        .parseConfigUpdatedForYaml(configUpdatedJson, CommonConst.APPTYPE_KAFKA, version);
                changedUnitMap.putAll(mergedUnitMap);
            }
        }
        config.setKafkacnf(changedUnitMap);
        config.setLivenessDelayTimeout(CommonConst.NUMBER_THIRTY);
        config.setLivenessFailureThreshold(CommonConst.NUMBER_TEN);
        config.setReadinessDelayTimeout(CommonConst.NUMBER_THIRTY);
        config.setReadinessFailureThreshold(CommonConst.NUMBER_TEN);
        return config;
    }

    /**
     * 调k8s创建
     * 
     * @param tenantName
     * @param kafkaCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, KafkaCluster kafkaCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建kafkaCluster：" + JSON.toJSONString(kafkaCluster));
        if (null != kafkaCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = createKafkaCluster(tenantName, kafkaCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("创建kafka集群" + kafkaCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        }
        return false;
    }

    private void checkCreateResult(String tenantName, StatefulService service, Double cpu, Double memory,
            Double capacity) {
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
            KafkaCluster kafkaCluster = componentOperationsClientUtil.getKafkaCluster(tenantName,
                    service.getServiceName());
            LOG.info("判断kafkaCluster中数据是否存在");
            LOG.info("kafkaCluster:" + JSON.toJSONString(kafkaCluster));
            LOG.info("status:" + kafkaCluster.getStatus());
            if (null != kafkaCluster && null != kafkaCluster.getStatus()
                    && null != kafkaCluster.getStatus().getServerNodes()
                    && null != kafkaCluster.getStatus().getServerNodes().keySet() && !hasNodes
                    && service.getNodeNum() <= kafkaCluster.getStatus().getServerNodes().keySet().size()) {
                if (buildNodes(kafkaCluster, service, cpu, memory, capacity)) {
                    LOG.info("节点表插入数据成功,集群名称：" + kafkaCluster.getMetadata().getName());
                    hasNodes = true;
                }
            }
            if (null != kafkaCluster && null != kafkaCluster.getStatus()) {
                LOG.info("cluster此时的状态：" + kafkaCluster.getStatus().getPhase());
            }
            if (hasNodes && null != kafkaCluster && null != kafkaCluster.getStatus()
                    && null != kafkaCluster.getStatus().getServerNodes()
                    && null != kafkaCluster.getStatus().getServerNodes().keySet()
                    && service.getNodeNum() <= kafkaCluster.getStatus().getServerNodes().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(kafkaCluster.getStatus().getPhase())) {
                LOG.info("kafka集群创建成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;

            }
        }

        // 检查结束
        optClusterCreate(tenantName, service.getId(), service.getServiceName(), hasNodes);

    }

    /**
     * 存节点表
     * 
     * @param kafkaCluster
     * @param service
     * @param cpu
     * @param memory
     * @param capacity
     * @return
     */
    private boolean buildNodes(KafkaCluster kafkaCluster, StatefulService service, Double cpu, Double memory,
            Double capacity) {
        if (null == kafkaCluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");

        StatefulNode node = null;
        for (Map.Entry<String, KafkaNode> entry : kafkaCluster.getStatus().getServerNodes().entrySet()) {
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
                node.setStorage(capacity);
                node.setLvmName(entry.getValue().getVolumeid());

                node.setAppType(CommonConst.APPTYPE_KAFKA);
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
        // 获取kafkaCluster
        KafkaCluster kafkaCluster = componentOperationsClientUtil.getKafkaCluster(tenantName, serviceName);

        if (hasNodes) {
            // 注册逻辑卷
            registeClusterLvm(tenantName, kafkaCluster);
        }
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(kafkaCluster);

        // 修改数据库中集群、节点状态
        componentOperationsClientUtil.changeKafkaClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, null, nodesExtendedField);

    }

}
