package com.xxx.xcloud.module.component.service.worker.memcached;

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
import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.memcached.MemcachedCluster;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterGroupInfo;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterServer;

@Service
@Scope("prototype")
public class MemcachedClusterExpandNodeWorker extends BaseMemcachedClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MemcachedClusterExpandNodeWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MemcachedClusterExpandNodeWorker====================");
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

        // 3、拼接memcachedCluster
        MemcachedCluster memcachedCluster = buildMemcachedCluster(tenantName, service.getServiceName(), data);

        // 4、调用k8sclient集群增加节点
        if (!updateAndRetry(tenantName, memcachedCluster)) {
            // 修改waiting状态节点为Failed
            componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
            LOG.info("memcached集群增加节点失败！");
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
        int replicas = Integer.parseInt(data.get("replicas"));
        List<String> changeNodeList = new ArrayList<String>();
        while (true) {

            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(tenantName, serviceId,
                        serviceName);
                return;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("启动集群超时，serviceName：" + serviceName);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(tenantName, serviceId,
                        serviceName);
                return;
            }
            MemcachedCluster memcachedCluster = componentOperationsClientUtil.getMemcachedCluster(tenantName,
                    serviceName);
            LOG.info("cluster:" + JSON.toJSONString(memcachedCluster));
            if (null == memcachedCluster) {
                LOG.info("memcachedCluster:" + JSON.toJSONString(memcachedCluster));
                componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                        CommonConst.STATE_NODE_FAILED, null);
                return;
            }

            double newTotalCpu, newTotalMemory;
            Double cpu = Double.parseDouble(memcachedCluster.getSpec().getResources().getLimits().getCpu());
            String memoryStr = memcachedCluster.getSpec().getResources().getLimits().getMemory();
            Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));

            statefulService = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);

//            double oldTotalCpu = statefulService.getCpu();
//            double oldTotalMemory = statefulService.getMemory();

            newTotalCpu = cpu * replicas;
            newTotalMemory = memory * replicas;

            if (isNodeNumEqualsReplicas(memcachedCluster)) {
                if (!hasNode) {
                    changeNodeList = buildNode(serviceId, memcachedCluster);
                    if (null == changeNodeList || changeNodeList.isEmpty() || changeNodeList.size() == 0) {
                        continue;
                    }

                    // 修改tenant表资源
//                    boolean result = tenantService.updateUsedResource(tenantName, newTotalCpu - oldTotalCpu,
//                            newTotalMemory - oldTotalMemory, 0.0);
//                    LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + String.valueOf(newTotalCpu - oldTotalCpu)
//                            + ",updateMemory:" + String.valueOf(newTotalMemory - oldTotalMemory));

                    statefulService.setCpu(newTotalCpu);
                    statefulService.setMemory(newTotalMemory);
                    statefulService.setNodeNum(replicas);
                    try {
                        statefulServiceRepository.save(statefulService);
                    } catch (Exception e) {
                        LOG.error("修改StatefulService集群节点个数失败！");
                    }
                    hasNode = true;
                }
                if (hasNode && CommonConst.STATE_CLUSTER_RUNNING.equals(memcachedCluster.getStatus().getPhase())) {

                    LOG.info("集群增加节点，修改数据库");
                    if (null != memcachedCluster.getStatus().getGroups()) {
                        try {
                            // 修改节点ip、port、状态
                            Map<String, String> nodeMessageMap = new HashMap<>();
                            for (String newNodeId : changeNodeList) {
                                StatefulNode newNode = componentOperationsDataBaseUtil.getStatefulNodeById(newNodeId);
                                for (MemcachedClusterGroupInfo memcachedClusterGroupInfo : memcachedCluster.getStatus()
                                        .getGroups().values()) {
                                    for (MemcachedClusterServer memcachedClusterServer : memcachedClusterGroupInfo
                                            .getServer().values()) {
                                        if (memcachedClusterServer.getName().equals(newNode.getNodeName())) {
                                            LOG.info("节点: " + memcachedClusterServer.getName() + "修改状态为RUNNING");
                                            String nodeIp = memcachedClusterServer.getNodeIp();
                                            int nodePort = memcachedClusterServer.getService().getNodePort();

                                            nodeMessageMap.put("ip", nodeIp);
                                            nodeMessageMap.put("port", String.valueOf(nodePort));
                                            componentOperationsDataBaseUtil.updateNodeState(newNodeId,
                                                    CommonConst.STATE_NODE_RUNNING, nodeMessageMap);
                                        }
                                    }
                                }
                            }

                            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_RUNNING,
                                    null, null);
                            LOG.info("=============获取操作结果成功==========");
                            return;
                        } catch (Exception e) {
                            LOG.error("检查集群增加节点结果异常，error: ", e);
                            // 修改waiting状态节点为Failed
                            componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(tenantName,
                                    serviceId, serviceName);
                        }
                    }

                }

            }

        }

    }

    /**
     * 集群增加节点：构建memcachedCluster
     *
     * @param tenantName
     * @param serviceName
     * @param data
     * @return
     */
    private MemcachedCluster buildMemcachedCluster(String tenantName, String serviceName, Map<String, String> data) {

        MemcachedCluster memcachedCluster = null;
        memcachedCluster = componentOperationsClientUtil.getMemcachedCluster(tenantName, serviceName);
        if (null == memcachedCluster) {
            LOG.error("获取memcachedCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        int replicas = Integer.parseInt(data.get("replicas"));
        memcachedCluster.getSpec().setReplicas(replicas);
        LOG.info("集群增加节点：构建memcachedCluster成功, serviceName:" + serviceName);
        return memcachedCluster;
    }

    /**
     * 变化节点存表
     *
     * @param serviceId
     * @param cluster
     * @return
     */
    private List<String> buildNode(String serviceId, MemcachedCluster cluster) {
        List<String> changeNodeList = new ArrayList<String>();
        List<StatefulNode> statefulNodeList = null;
        statefulNodeList = componentOperationsDataBaseUtil.getStatefulNodeListById(serviceId);
        StatefulNode node = new StatefulNode();
        for (MemcachedClusterGroupInfo memcachedClusterGroupInfo : cluster.getStatus().getGroups().values()) {
            for (MemcachedClusterServer memcachedClusterServer : memcachedClusterGroupInfo.getServer().values()) {
                boolean isExist = false;
                for (StatefulNode statefulNode : statefulNodeList) {
                    if (memcachedClusterServer.getName().equals(statefulNode.getNodeName())) {
                        isExist = true;
                    }
                }
                if (!isExist) {
                    node = new StatefulNode();

                    Double cpu = Double.parseDouble(cluster.getSpec().getResources().getLimits().getCpu());
                    String memoryStr = cluster.getSpec().getResources().getLimits().getMemory();
                    Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));

                    node.setCpu(cpu);
                    node.setMemory(memory);

                    node.setAppType(CommonConst.APPTYPE_MEMCACHED);
                    node.setServiceId(serviceId);
                    node.setServiceName(cluster.getMetadata().getName());

                    node.setNodeName(memcachedClusterServer.getName());
                    node.setRole(MemcachedClusterConst.MEMCACHED_ROLE_SERVER);

                    node.setNodeState(CommonConst.STATE_NODE_WAITING);
                    node.setCreateTime(new Date());
                    try {
                        StatefulNode newnode = statefulNodeRepository.save(node);
                        if (null != newnode) {
                            LOG.info("保存节点记录信息成功，nodeName" + memcachedClusterServer.getName());
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
        }

        return changeNodeList;
    }
}
