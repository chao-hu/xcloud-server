package com.xxx.xcloud.module.component.service.worker.storm;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.storm.StormCluster;
import com.xxx.xcloud.module.component.model.storm.StormNode;

@Service
@Scope("prototype")
public class StormNodeDeleteWorker extends BaseStormClusterWorker {
    private static Logger LOG = LoggerFactory.getLogger(StormNodeDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============StormNodeDeleteWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空，serviceId：" + node.getServiceId());
            return;
        }

        // 4、获取storm cluster
        StormCluster oldCluster = componentOperationsClientUtil.getStormCluster(tenantName, service.getServiceName());
        if (null == oldCluster || null == oldCluster.getStatus()) {
            LOG.error("根据serviceName获取oldCluster为空，或oldCluster不包含该节点，serviceId:" + node.getServiceName()
                    + ",oldCluster:" + JSON.toJSONString(oldCluster));
            optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);
            return;
        }

        Map<String, StormNode> serverNodes = oldCluster.getStatus().getServerNodes();
        if (null == serverNodes || serverNodes.isEmpty() || !serverNodes.containsKey(node.getNodeName())) {
            LOG.info("节点已经被删除，nodeId：" + nodeId + ",nodeName:" + node.getNodeName());
            optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);
            return;
        }

        StormCluster newCluster = updateYamlForStormCluster(tenantName, service.getServiceName(),
                StormClusterConst.OPERATOR_NODE_DELETE, node.getNodeName(), 0);

        // 5、调用k8sclient删除节点并循环获取删除结果
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("storm节点" + node.getNodeName() + "删除失败！");
            componentOptNodeBase(CommonConst.APPTYPE_STORM, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        if (!checkNodeDeleteResult(tenantName, service, node.getNodeName())) {
            LOG.error("storm节点删除失败，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
            componentOptNodeBase(CommonConst.APPTYPE_STORM, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        LOG.info("storm节点删除成功，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);

    }

    /**
     * 删除节点
     * 
     * @param tenantName
     * @param node
     */
    private void optNodeDelete(String tenantName, String serviceId, String serviceName, StatefulNode node) {
        stormOptNodeDelete(tenantName, node.getId(), CommonConst.STATE_NODE_DELETED);
        componentOperationsClientUtil.deleteLvm(tenantName, node.getLvmName());
        stormOptNodeDeleteSyncYaml(tenantName, serviceId, serviceName);
    }

    /**
     * 处理节点删除，同步yaml状态及扩展字段
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void stormOptNodeDeleteSyncYaml(String tenantName, String serviceId, String serviceName) {
        // 获取stormCluster
        StormCluster stormCluster = componentOperationsClientUtil.getStormCluster(tenantName, serviceName);
        if (null == stormCluster || null == stormCluster.getStatus()
                || null == stormCluster.getStatus().getServerNodes()) {
            LOG.info("stormCluster为null");
            return;
        }

        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(stormCluster);

        componentOperationsClientUtil.changeStormClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, null);
    }

    /**
     * 根据nodeId修改集群和节点状态
     * 
     * @param nodeId
     * @param nodeState
     */
    private void stormOptNodeDelete(String tenantName, String nodeId, String nodeState) {
        try {
            StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
            if (null != node && !CommonConst.STATE_NODE_DELETED.equals(node.getNodeState())) {
                node.setNodeState(nodeState);
                statefulNodeRepository.save(node);
                // 修改tenant表
//                boolean result = tenantService.updateUsedResource(tenantName, node.getCpu() * (-1),
//                        node.getMemory() * (-1), 0.0);
//                LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + node.getCpu() * (-1) + ",updateMemory:"
//                        + node.getMemory() * (-1) + ",updateCapacity:0.0");
            }

            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
            if (null != service) {
                String status = componentOperationsClientUtil.getClusterStateByNodes(node.getServiceId(),
                        service.getLastopt());
                service.setServiceState(status);
                service.setNodeNum(service.getNodeNum() - 1);
                if (null != node) {
                    service.setCpu(service.getCpu() - node.getCpu());
                    service.setMemory(service.getMemory() - node.getMemory());
                }
                statefulServiceRepository.save(service);
                if (CommonConst.STATE_CLUSTER_DELETED.equals(service.getServiceState())) {
                    // 删除依赖表
                    componentOperationsDataBaseUtil.deleteStatefulServiceDependency(service.getId());
                }
            }
        } catch (Exception e) {
            LOG.error("根据nodeId修改集群和节点状态失败，nodeId:" + nodeId + ",error:", e);
        }
    }

    /**
     * 节点删除结果查询
     * 
     * @param tenantName
     * @param serviceName
     * @param nodeName
     * @return
     */
    private boolean checkNodeDeleteResult(String tenantName, StatefulService service, String nodeName) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时：" + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                return false;
            }

            if ((System.currentTimeMillis() - start) > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("删除节点超时，service：" + JSON.toJSONString(service.getServiceName()));
                return false;
            }

            StormCluster stormCluster = componentOperationsClientUtil.getStormCluster(tenantName,
                    service.getServiceName());
            if (null == stormCluster) {
                LOG.info("获取的集群为空!");
                return true;
            }

            StormNode server = null;
            if (null != stormCluster.getStatus() && null != stormCluster.getStatus().getServerNodes()) {
                server = stormCluster.getStatus().getServerNodes().get(nodeName);
            }
            if (null == server) {
                LOG.info("===========删除pod成功===========");
                return true;
            }
        }
    }

}
