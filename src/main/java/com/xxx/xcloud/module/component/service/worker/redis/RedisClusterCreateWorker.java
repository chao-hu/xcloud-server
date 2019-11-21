package com.xxx.xcloud.module.component.service.worker.redis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.RedisClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.model.redis.RedisBindingNode;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;
import com.xxx.xcloud.module.component.model.redis.RedisSentinel;
import com.xxx.xcloud.module.component.model.redis.RedisSpec;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
@Scope("prototype")
public class RedisClusterCreateWorker extends BaseRedisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(RedisClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============RedisClusterCreateWorker====================");
        // 1、获取数据
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");

        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        String performance = data.get("performance");

        String type = data.get("type");
        String version = data.get("version");
        String serviceName = data.get("serviceName");
        String password = data.get("password");
        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));

        String configuration = data.get("configuration");

        // 2、拼接redisCluster
        RedisCluster redisClusterYaml = null;
        redisClusterYaml = buildRedisMsOrSingleCluster(projectId, orderId, version, type, serviceName, password, cpu,
                memory, capacity, replicas, configuration, tenantName, performance);
        if (RedisClusterConst.REDIS_TYPE_MS_SENTINEL.equals(type)) {
            redisClusterYaml = buildRedisMsSentinelCluster(RedisClusterConst.REDIS_SENTINEL_DEFAULT_CPU,
                    RedisClusterConst.REDIS_SENTINEL_DEFAULT_MEMORY, RedisClusterConst.REDIS_SENTINEL_REPLICAS,
                    redisClusterYaml);
        }

        LOG.info("创建拼接后的redisClusterYaml：" + JSON.toJSONString(redisClusterYaml));
        // 3、调用k8sAPI创建
        if (!createAndRetry(tenantName, redisClusterYaml)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.info("redis集群创建失败！");
            return;
        }

        // 4、循环获取创建结果
        if (!checkClusterCreateResult(tenantName, serviceId, serviceName)) {
            componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
        }
    }

    /**
     * 创建redislCluster的Sentine
     * 
     * @param sentinelCpu
     * @param sentinelMemory
     * @param sentinelReplicas
     * @param redisClusterYaml
     * @return
     */
    private RedisCluster buildRedisMsSentinelCluster(Double sentinelCpu, Double sentinelMemory, int sentinelReplicas,
            RedisCluster redisClusterYaml) {
        LOG.info("===============开始构建redisMsSentinelCluster==================");
        RedisSpec redisSpec = redisClusterYaml.getSpec();
        RedisSentinel sentinel = new RedisSentinel();
        sentinel.setResources(
                componentOperationsClientUtil.getResources(sentinelCpu, sentinelMemory, CommonConst.UNIT_MI));
        sentinel.setReplicas(sentinelReplicas);
        redisSpec.setSentinel(sentinel);
        redisClusterYaml.setSpec(redisSpec);

        return redisClusterYaml;
    }

    /**
     * buildRedisMsOrSingleCluster yaml
     * 
     * @param version
     * @param type
     * @param serviceName
     * @param password
     * @param cpu
     * @param memory
     * @param capacity
     * @param replicas
     * @param configuration
     * @param tenantName
     * @param performance
     * @return
     */
    private RedisCluster buildRedisMsOrSingleCluster(String projectId, String orderId, String version, String type,
            String serviceName, String password, Double cpu, Double memory, Double capacity, int replicas,
            String configuration, String tenantName, String performance) {
        LOG.info("===============开始构建redisMsOrSingleCluster==================");
        RedisCluster redisCluster = new RedisCluster();
        ObjectMeta metaData = new ObjectMeta();
        redisCluster.setKind(RedisClusterConst.REDIS_KIND);
        redisCluster.setApiVersion(RedisClusterConst.REDIS_API_VERSION);
        metaData.setName(serviceName);
        metaData.setNamespace(tenantName);

        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            metaData.setLabels(labels);
        }

        redisCluster.setMetadata(metaData);

        RedisSpec redisSpec = new RedisSpec();
        redisSpec.setStopped(false);
        redisSpec.setVersion(version);
        redisSpec.setImage(getRepoPath(CommonConst.APPTYPE_REDIS, null, version));
        redisSpec.setExporterImage(getRepoPath(CommonConst.APPTYPE_REDIS, "exporter", version));
        redisSpec.setLogDir(RedisClusterConst.REDIS_EXPORTER_LOGDIR);
        redisSpec.setReplicas(replicas);
        redisSpec.setCapacity(capacity + CommonConst.UNIT_GI);
        redisSpec.setStorageClass(RedisClusterConst.REDIS_STORAGE_CLASS_LVM);
        redisSpec.setVolume(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        redisSpec.setType(type);
        redisSpec.setPassword(password);
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_REDIS);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        if (null != performance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        redisSpec.setNodeSelector(nodeSelector);

        redisSpec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        if (StringUtils.isNotEmpty(configuration)) {
            JSONObject configMapJson = JSON.parseObject(configuration);
            if (null != configMapJson && !configMapJson.isEmpty()) {
                Map<String, String> returnConfigMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configMapJson,
                        CommonConst.APPTYPE_REDIS, version);
                redisSpec.setConfigMap(returnConfigMap);
            }
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            redisSpec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        redisCluster.setSpec(redisSpec);
        return redisCluster;
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
                LOG.error("=================创建超时================");
                return false;
            }

            RedisCluster redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);

            if (null == redisCluster || null == redisCluster.getStatus()
                    || null == redisCluster.getStatus().getBindings()
                    || redisCluster.getSpec().getReplicas() != redisCluster.getStatus().getBindings().keySet().size()) {
                LOG.info("redisCluster中节点未初始化完成");
                LOG.info("redisCluster:" + JSON.toJSONString(redisCluster));
                continue;
            }
            if (!hasNodes) {
                LOG.info("节点表插入数据");
                // 创建WAITING的节点
                if (buildNodes(redisCluster, serviceId)) {
                    LOG.info("节点表插入数据成功,  serviceName, " + redisCluster.getMetadata().getName());
                    hasNodes = true;
                }
            }
            if (CommonConst.STATE_CLUSTER_RUNNING.equals(redisCluster.getStatus().getPhase()) && hasNodes
                    && null != redisCluster.getStatus().getServices()) {
                // 注册逻辑卷 获取节点ip、port
                Map<String, Map<String, String>> nodesMessageMap = new HashMap<>();
                try {
                    for (String nodeName : redisCluster.getStatus().getBindings().keySet()) {
                        Map<String, String> nodeMessageMap = new HashMap<>();
                        LOG.info("节点: " + nodeName + "修改状态为RUNNING");
                        String nodeRole = redisCluster.getStatus().getBindings().get(nodeName).getRole();
                        String nodeIp = redisCluster.getStatus().getBindings().get(nodeName).getBindIp();
                        int nodePort = redisCluster.getStatus().getServices().get(nodeRole.toLowerCase()).getNodePort();

                        nodeMessageMap.put("ip", nodeIp);
                        nodeMessageMap.put("port", String.valueOf(nodePort));

                        String storage = redisCluster.getSpec().getCapacity();
                        componentOperationsClientUtil.registerLvm(tenantName, nodeName, nodeIp, storage);
                        nodesMessageMap.put(nodeName, nodeMessageMap);

                    }
                } catch (Exception e) {
                    LOG.error("节点表存节点ip、port、running状态失败, serviceName：" + redisCluster.getMetadata().getName(), e);
                }

                componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_RUNNING,
                        CommonConst.STATE_NODE_RUNNING, null, nodesMessageMap);
                return true;

            }

        }
    }

    /**
     * 创建节点表数据
     * 
     * @param cluster
     * @param serviceId
     * @return
     */
    private boolean buildNodes(RedisCluster cluster, String serviceId) {

        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");
        StatefulNode node = null;
        for (Entry<String, RedisBindingNode> entry : cluster.getStatus().getBindings().entrySet()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(serviceId,
                        entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                Double cpu = Double.parseDouble(cluster.getSpec().getResources().getLimits().getCpu());
                String memoryStr = cluster.getSpec().getResources().getLimits().getMemory();
                Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                String capacityStr = cluster.getSpec().getCapacity();
                Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                node.setCpu(cpu);
                node.setMemory(memory);
                node.setStorage(capacity);

                node.setAppType(CommonConst.APPTYPE_REDIS);
                node.setServiceId(serviceId);
                node.setServiceName(cluster.getMetadata().getName());

                node.setNodeName(entry.getKey());
                node.setRole(entry.getValue().getRole());
                node.setLvmName(entry.getValue().getName());

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
     * 
     * 
     * @param tenantName
     * @param redisCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, RedisCluster redisCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建redisCluster：" + JSON.toJSONString(redisCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = createRedisCluster(tenantName, redisCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("创建redis集群" + redisCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }
}
