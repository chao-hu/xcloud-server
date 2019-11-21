package com.xxx.xcloud.module.component.service.worker.memcached;

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
import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.model.memcached.MemcachedCluster;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterGroupInfo;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterServer;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterSpec;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
@Scope("prototype")
public class MemcachedClusterCreateWorker extends BaseMemcachedClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MemcachedClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MemcachedClusterCreateWorker====================");
        // 1、获取数据
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String performance = data.get("performance");
        String type = data.get("type");
        String version = data.get("version");
        String serviceName = data.get("serviceName");
        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        int replicas = Integer.parseInt(data.get("replicas"));

        String configuration = data.get("configuration");

        // 2、拼接memcachedCluster
        MemcachedCluster memcachedClusterYaml = null;
        memcachedClusterYaml = buildMemcachedCluster(projectId, orderId, version, type, serviceName, cpu, memory,
                replicas, configuration, tenantName, performance);

        LOG.info("创建拼接后的memcachedClusterYaml：" + JSON.toJSONString(memcachedClusterYaml));
        // 3、调用k8sAPI创建
        if (!createAndRetry(tenantName, memcachedClusterYaml)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.info("memcached集群创建失败！");
            return;
        }

        // 4、循环获取创建结果
        if (!checkClusterCreateResult(tenantName, serviceId, serviceName)) {
            componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
        }
    }

    private MemcachedCluster buildMemcachedCluster(String projectId, String orderId, String version, String type,
            String serviceName, Double cpu, Double memory, int replicas, String configuration, String tenantName,
            String performance) {
        LOG.info("===============开始构建memcachedCluster==================");
        MemcachedCluster memcachedCluster = new MemcachedCluster();
        ObjectMeta metaData = new ObjectMeta();
        memcachedCluster.setKind(MemcachedClusterConst.MEMCACHED_KIND);
        memcachedCluster.setApiVersion(MemcachedClusterConst.MEMCACHED_API_VERSION);
        metaData.setName(serviceName);
        metaData.setNamespace(tenantName);
        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            metaData.setLabels(labels);
        }

        memcachedCluster.setMetadata(metaData);

        MemcachedClusterSpec memcachedClusterSpec = new MemcachedClusterSpec();
        memcachedClusterSpec.setStopped(false);
        memcachedClusterSpec.setVersion(version);
        memcachedClusterSpec.setImage(getRepoPath(CommonConst.APPTYPE_MEMCACHED, type, version));
        memcachedClusterSpec.setExporterImage(getRepoPath(CommonConst.APPTYPE_MEMCACHED, "exporter", version));
        memcachedClusterSpec.setLogDir(MemcachedClusterConst.MEMCACHED_EXPORTER_LOGDIR);
        memcachedClusterSpec.setReplicas(replicas);
        memcachedClusterSpec.setType(type);
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_MEMCACHED);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        if (null != performance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        memcachedClusterSpec.setNodeSelector(nodeSelector);

        memcachedClusterSpec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        if (StringUtils.isNotEmpty(configuration)) {
            JSONObject configMapJson = JSON.parseObject(configuration);
            if (null != configMapJson && !configMapJson.isEmpty()) {
                Map<String, String> returnConfigMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configMapJson,
                        CommonConst.APPTYPE_MEMCACHED, version);
                memcachedClusterSpec.setConfigMap(returnConfigMap);
            }
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            memcachedClusterSpec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        memcachedCluster.setSpec(memcachedClusterSpec);

        return memcachedCluster;
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

            MemcachedCluster memcachedCluster = componentOperationsClientUtil.getMemcachedCluster(tenantName,
                    serviceName);

            LOG.info("memcachedCluster:" + JSON.toJSONString(memcachedCluster));
            LOG.info("status:" + memcachedCluster.getStatus());
            if (null == memcachedCluster || null == memcachedCluster.getStatus()
                    || null == memcachedCluster.getStatus().getGroups()) {
                LOG.info("memcachedCluster中数据不存在");
                continue;
            }
            if (isNodeNumEqualsReplicas(memcachedCluster)) {
                if (!hasNodes) {
                    LOG.info("节点表插入数据");
                    // 创建WAITING的节点
                    if (buildNodes(memcachedCluster, serviceId)) {
                        LOG.info("节点表插入数据成功！===" + memcachedCluster.getMetadata().getName());
                        hasNodes = true;
                    } else {
                        continue;
                    }
                } else {
                    if (CommonConst.STATE_CLUSTER_RUNNING.equals(memcachedCluster.getStatus().getPhase())
                            && null != memcachedCluster.getStatus().getGroups()) {
                        // 注册逻辑卷 获取节点ip、port
                        Map<String, Map<String, String>> nodesMessageMap = new HashMap<>();
                        for (MemcachedClusterGroupInfo memcachedClusterGroupInfo : memcachedCluster.getStatus()
                                .getGroups().values()) {
                            for (MemcachedClusterServer memcachedClusterServer : memcachedClusterGroupInfo.getServer()
                                    .values()) {
                                Map<String, String> nodeMessageMap = new HashMap<>();
                                String nodeName = memcachedClusterServer.getName();
                                LOG.info("节点: " + nodeName + "修改状态为RUNNING");
                                String nodeIp = memcachedClusterServer.getNodeIp();
                                int nodePort = memcachedClusterServer.getService().getNodePort();

                                nodeMessageMap.put("ip", nodeIp);
                                nodeMessageMap.put("port", String.valueOf(nodePort));
                                nodesMessageMap.put(nodeName, nodeMessageMap);
                            }
                        }

                        // 修改集群状态、扩展字段与节点状态、ip、port
                        Map<String, String> serviceExtendedFieldMap = new HashMap<>();

                        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId,
                                CommonConst.STATE_CLUSTER_RUNNING, CommonConst.STATE_NODE_RUNNING,
                                serviceExtendedFieldMap, nodesMessageMap);
                        return true;

                    }

                }
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
    private boolean buildNodes(MemcachedCluster cluster, String serviceId) {

        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");
        StatefulNode node = null;
        try {
            for (MemcachedClusterGroupInfo memcachedClusterGroupInfo : cluster.getStatus().getGroups().values()) {
                for (Entry<String, MemcachedClusterServer> entry : memcachedClusterGroupInfo.getServer().entrySet()) {

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
                    node.setCpu(cpu);
                    node.setMemory(memory);

                    node.setAppType(CommonConst.APPTYPE_MEMCACHED);
                    node.setServiceId(serviceId);
                    node.setServiceName(cluster.getMetadata().getName());

                    node.setNodeName(entry.getValue().getName());
                    node.setRole(MemcachedClusterConst.MEMCACHED_ROLE_SERVER);

                    node.setNodeState(CommonConst.STATE_NODE_WAITING);
                    node.setCreateTime(new Date());

                    statefulNodeRepository.save(node);
                }
            }
        } catch (Exception e) {
            LOG.error("保存节点记录信息失败,data:" + JSON.toJSONString(node) + ",error:", e);
            return false;
        }
        return true;

    }

    /**
     * 
     * 
     * @param tenantName
     * @param memcachedCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, MemcachedCluster memcachedCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建memcachedCluster：" + JSON.toJSONString(memcachedCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = createMemcachedCluster(tenantName, memcachedCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("创建memcached集群" + memcachedCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }
}
