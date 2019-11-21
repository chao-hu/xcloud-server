package com.xxx.xcloud.module.component.service.worker.zookeeper;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;
import com.xxx.xcloud.module.component.model.zookeeper.ZkInstance;

@Service
@Scope("prototype")
public class ZkClusterExpandNodeWorker extends BaseZkClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(ZkClusterExpandNodeWorker.class);

    @Override
    public void execute() {
        LOG.info("===============ZkClusterExpandNodeWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        Integer addNum = Integer.parseInt(data.get("addNum"));

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群扩展节点时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群扩展节点时获取的service：" + JSON.toJSONString(service));

        // 3、修改zkCluster
        ZkCluster oldCluster = componentOperationsClientUtil.getZkCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("集群扩展节点时获取的cluster为null");
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            return;
        }

        ZkCluster newCluster = updateYamlForZkCluster(tenantName, service.getServiceName(),
                ZkClusterConst.OPERATOR_CLUSTER_EXPAND, null, addNum);

        // 4、调用k8sclient扩展节点
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("zk集群" + service.getServiceName() + "扩展节点失败！");
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            return;
        }

        // 5、循环获取扩展节点结果
        checkExpandNodeResult(tenantName, service, oldCluster, addNum);
    }

    /**
     * 
     * @param tenantName
     * @param service
     * @param oldCluster
     * @param addNum
     */
    private void checkExpandNodeResult(String tenantName, StatefulService service, ZkCluster oldCluster,
            Integer addNum) {
        long start = System.currentTimeMillis();
        boolean hasNode = false;
        boolean tenantUpdateResult = false;
        Double cpuOld = Double.parseDouble(oldCluster.getSpec().getResources().getLimits().getCpu());
        String memoryOldStr = oldCluster.getSpec().getResources().getLimits().getMemory();
        Double memoryOld = Double.parseDouble(memoryOldStr.substring(0, memoryOldStr.length() - 2));

        while (true) {

            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常");
                break;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.info("=============循环获取操作结果超时==========");
                break;
            }

            ZkCluster newCluster = componentOperationsClientUtil.getZkCluster(tenantName, service.getServiceName());
            if (null == newCluster) {
                LOG.error("获取的newCluster为null");
                break;
            }
            LOG.info("newCluster:" + JSON.toJSONString(newCluster));

            StatefulService newService = null;
            try {
                newService = componentOperationsDataBaseUtil.getStatefulServiceById(service.getId());
            } catch (Exception e) {
                LOG.error("修改zk集群循环获取操作结果时查找service异常！", e);
                continue;
            }
            if (null == newService) {
                LOG.error("修改zk集群循环获取操作结果时查找service为空！");
                break;
            }

            if (!hasNode
                    && newCluster.getSpec().getReplicas() <= newCluster.getStatus().getInstances().keySet().size()) {
                if (buildNode(newService, newCluster, cpuOld, memoryOld)) {
                    // 节点个数加1
                    LOG.info("查询到的zk service：" + JSON.toJSONString(newService));
                    newService.setNodeNum(newService.getNodeNum() + addNum);
                    newService.setCpu(newService.getNodeNum() * cpuOld);
                    newService.setMemory(newService.getNodeNum() * memoryOld);
                    try {
                        if (!tenantUpdateResult) {
                            // 修改tenant表
//                            boolean result = tenantService.updateUsedResource(tenantName, cpuOld * addNum,
//                                    memoryOld * addNum, 0.0);
//                            LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuOld * addNum + ",updateMemory:"
//                                    + memoryOld * addNum + ",updateCapacity:0.0");
                            tenantUpdateResult = true;
                        }
                        statefulServiceRepository.save(newService);
                        hasNode = true;
                    } catch (ErrorMessageException e) {
                        LOG.error("修改tenant表资源失败！", e);
                    } catch (Exception e) {
                        LOG.error("修改StatefulService集群信息失败！", e);
                    }
                }
            }

            if (hasNode && newCluster.getSpec().getReplicas() <= newCluster.getStatus().getInstances().keySet().size()
                    && CommonConst.STATE_CLUSTER_STOPPED.equals(newCluster.getStatus().getPhase())) {
                LOG.info("Zk集群扩展节点成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;
            }
        }

        // 检查结束
        optClusterExpand(tenantName, service.getId(), service.getServiceName());

    }

    /**
     * 保存节点信息
     * 
     * @param service
     * @param oldZkCluster
     * @param newZkCluster
     * @param cpu
     * @param memory
     * @return
     */
    private boolean buildNode(StatefulService service, ZkCluster newZkCluster, Double cpu, Double memory) {
        StatefulNode node = new StatefulNode();
        List<StatefulNode> oldNodesList = componentOperationsDataBaseUtil.getStatefulNodeListById(service.getId());
        Set<String> oldNodes = new HashSet<>();
        Map<String, ZkInstance> newNodes = newZkCluster.getStatus().getInstances();

        if (null != oldNodesList && !oldNodesList.isEmpty()) {
            for (StatefulNode statefulNode : oldNodesList) {
                oldNodes.add(statefulNode.getNodeName());
            }
        }

        LOG.info("旧的statefulNodes：" + JSON.toJSONString(oldNodesList));
        try {
            for (Map.Entry<String, ZkInstance> entry : newNodes.entrySet()) {
                if (!oldNodes.contains(entry.getKey())) {
                    StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(
                            service.getId(), entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
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
                }
            }
            return true;
        } catch (Exception e) {
            LOG.error("保存节点记录信息失败！", e);
            return false;
        }
    }

    /**
     * 处理新增节点
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optClusterExpand(String tenantName, String serviceId, String serviceName) {
        // 获取zkCluster
        ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, serviceName);

        // 新增节点不需要注册lvm，节点或集群启动再注册
        // 获取集群扩展节点时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(zkCluster);

        // 修改数据库中集群、节点状态
        componentOperationsClientUtil.changeZkClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName,
                serviceExtendedField, null);

    }
}
