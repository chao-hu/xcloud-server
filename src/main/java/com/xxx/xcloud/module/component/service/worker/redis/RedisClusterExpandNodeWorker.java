package com.xxx.xcloud.module.component.service.worker.redis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.redis.RedisBindingNode;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;

@Service
@Scope("prototype")
public class RedisClusterExpandNodeWorker extends BaseRedisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(RedisClusterExpandNodeWorker.class);

    @Override
    public void execute() {
        LOG.info("===============RedisClusterExpandNodeWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群增加节点时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群增加节点时获取的service：" + JSON.toJSONString(service));

        // 3、拼接redisCluster
        RedisCluster redisCluster = buildRedisCluster(tenantName, service.getServiceName(), data);

        // 4、调用k8sclient集群增加节点
        if (!updateAndRetry(tenantName, redisCluster)) {
            // 修改waiting状态节点为Failed
            componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
            LOG.info("redis集群增加节点失败！");
            return;
        }

        // 5、循环获取增加节点结果
        checkExpandNodeResult(tenantName, serviceId, service.getServiceName(), data);
    }

    private void checkExpandNodeResult(String tenantName, String serviceId, String serviceName,
            Map<String, String> data) {

        long start = System.currentTimeMillis();
        boolean hasNode = false;
        StatefulService statefulService;
        statefulService = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);

        if (null == statefulService) {
            LOG.error("获取集群表信息失败，集群名称：" + serviceName);
            return;
        }

        // double oldTotalCpu = statefulService.getCpu();
        // double oldTotalMemory = statefulService.getMemory();
        // double oldTotalcapacity = statefulService.getStorage();
        int oldReplicas = statefulService.getNodeNum();

        int replicas = Integer.parseInt(data.get("replicas"));
        List<String> changeNodeList = new ArrayList<String>();
        while (true) {

            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
                return;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("集群增加节点超时，serviceName：" + serviceName);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
                return;
            }
            RedisCluster redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);
            LOG.info("cluster:" + JSON.toJSONString(redisCluster));
            if (null == redisCluster) {
                LOG.info("redisCluster:" + JSON.toJSONString(redisCluster));
                componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                        CommonConst.STATE_NODE_FAILED, null);
                return;
            }

            double newTotalCpu;
            double newTotalMemory;
            double newTotalcapacity;

            Double cpu = Double.parseDouble(redisCluster.getSpec().getResources().getLimits().getCpu());
            String memoryStr = redisCluster.getSpec().getResources().getLimits().getMemory();
            Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
            String capacityStr = redisCluster.getSpec().getCapacity();
            Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

            newTotalcapacity = capacity * replicas;
            newTotalCpu = cpu * replicas;
            newTotalMemory = memory * replicas;

            if (!hasNode
                    && redisCluster.getSpec().getReplicas() == redisCluster.getStatus().getBindings().keySet().size()) {
                changeNodeList = buildNode(changeNodeList, serviceId, redisCluster);
                if (null == changeNodeList || changeNodeList.isEmpty()) {
                    continue;
                }

                // 修改tenant表资源
//                boolean result = tenantService.updateUsedResource(tenantName, newTotalCpu - oldTotalCpu,
//                        newTotalMemory - oldTotalMemory, newTotalcapacity - oldTotalcapacity);
//                LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + String.valueOf(newTotalCpu - oldTotalCpu)
//                        + ",updateMemory:" + String.valueOf(newTotalMemory - oldTotalMemory) + ",updateCapacity:"
//                        + String.valueOf(newTotalcapacity - oldTotalcapacity));

                // 修改节点表
                if (oldReplicas > replicas) {
                    for (String nodeId : changeNodeList) {
                        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
                        if (!redisCluster.getStatus().getBindings().keySet().contains(node.getNodeName())) {
                            node.setNodeState(CommonConst.STATE_NODE_DELETED);
                            statefulNodeRepository.save(node);
                        }
                        componentOperationsClientUtil.deleteLvm(tenantName, node.getNodeName());
                    }
                }

                // 修改集群表
                statefulService.setCpu(newTotalCpu);
                statefulService.setMemory(newTotalMemory);
                statefulService.setNodeNum(replicas);
                statefulService.setStorage(newTotalcapacity);
                try {
                    statefulServiceRepository.save(statefulService);
                } catch (Exception e) {
                    LOG.error("修改StatefulService集群节点个数失败！");
                }
                hasNode = true;
            }

            if (hasNode
                    && redisCluster.getSpec().getReplicas() == redisCluster.getStatus().getBindings().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(redisCluster.getStatus().getPhase())) {

                LOG.info("集群增加节点，修改数据库");
                if (null != redisCluster.getStatus().getServices()) {
                    try {
                        if (oldReplicas < replicas) {
                            // 注册逻辑卷 修改节点ip、port、状态
                            Map<String, String> nodeMessageMap = new HashMap<>();
                            for (String nodeName : redisCluster.getStatus().getBindings().keySet()) {
                                for (String newNodeId : changeNodeList) {
                                    StatefulNode newNode = componentOperationsDataBaseUtil.getStatefulNodeById(newNodeId);
                                    if (nodeName.equals(newNode.getNodeName())) {
                                        LOG.info("节点: " + nodeName + "修改状态为RUNNING");
                                        String nodeRole = redisCluster.getStatus().getBindings().get(nodeName)
                                                .getRole();
                                        String nodeIp = redisCluster.getStatus().getBindings().get(nodeName)
                                                .getBindIp();
                                        int nodePort = redisCluster.getStatus().getServices()
                                                .get(nodeRole.toLowerCase()).getNodePort();

                                        nodeMessageMap.put("ip", nodeIp);
                                        nodeMessageMap.put("port", String.valueOf(nodePort));
                                        componentOperationsDataBaseUtil.updateNodeState(newNodeId,
                                                CommonConst.STATE_NODE_RUNNING, nodeMessageMap);

                                        String storage = redisCluster.getSpec().getCapacity();
                                        componentOperationsClientUtil.registerLvm(tenantName, nodeName, nodeIp,
                                                storage);
                                    }
                                }
                            }
                        }
                        componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_RUNNING, null,
                                null);
                        return;

                    } catch (Exception e) {
                        LOG.error("检查集群增加节点结果异常，error: ", e);
                        // 修改waiting状态节点为Failed
                        componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId,
                                serviceName);

                    }
                }
                LOG.info("=============获取操作结果成功==========");
            }

        }

    }

    /**
     * 集群增加节点：构建redisCluster
     * 
     * @param tenantName
     * @param serviceName
     * @param data
     * @return
     */
    private RedisCluster buildRedisCluster(String tenantName, String serviceName, Map<String, String> data) {

        RedisCluster redisCluster = null;
        redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);
        if (null == redisCluster) {
            LOG.error("获取redisCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        int replicas = Integer.parseInt(data.get("replicas"));
        redisCluster.getSpec().setReplicas(replicas);
        LOG.info("集群增加节点：构建redisCluster成功, serviceName:" + serviceName);
        return redisCluster;
    }

    /**
     * 变化节点存表
     * 
     * @param changeNodeList
     * @param serviceId
     * @param cluster
     * @return
     */
    private List<String> buildNode(List<String> changeNodeList, String serviceId, RedisCluster cluster) {
        List<StatefulNode> statefulNodeList = null;
        statefulNodeList = componentOperationsDataBaseUtil.getStatefulNodeListById(serviceId);
        StatefulNode node = new StatefulNode();
        Map<String, RedisBindingNode> newNodes = cluster.getStatus().getBindings();

        for (StatefulNode statefulNode : statefulNodeList) {
            boolean isDeleted = true;
            for (Map.Entry<String, RedisBindingNode> entry : newNodes.entrySet()) {
                if (statefulNode.getNodeName().equals(entry.getValue().getName())) {
                    isDeleted = false;
                }
            }

            if (isDeleted) {
                changeNodeList.add(statefulNode.getId());
            }
        }

        for (Map.Entry<String, RedisBindingNode> entry : newNodes.entrySet()) {
            boolean isExist = false;
            for (StatefulNode statefulNode : statefulNodeList) {
                if (entry.getKey().equals(statefulNode.getNodeName())) {
                    isExist = true;
                    if (entry.getValue().getStatus().equals(CommonConst.STATE_NODE_DELETED)) {
                        changeNodeList.add(statefulNode.getId());
                    }
                }
            }
            if (!isExist) {
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
                try {
                    StatefulNode newnode = statefulNodeRepository.save(node);
                    if (null != newnode) {
                        LOG.info("保存节点记录信息成功，nodeName" + entry.getKey());
                        changeNodeList.add(newnode.getId());
                    } else {
                        LOG.error("保存节点记录信息失败");
                        return null;
                    }
                } catch (Exception e) {
                    LOG.error("保存节点记录信息失败,Exception: ", e);
                    return null;
                }
            }

        }

        return changeNodeList;
    }

}
