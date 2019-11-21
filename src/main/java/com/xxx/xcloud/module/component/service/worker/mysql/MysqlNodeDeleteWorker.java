package com.xxx.xcloud.module.component.service.worker.mysql;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlServer;

@Service
@Scope("prototype")
public class MysqlNodeDeleteWorker extends BaseMysqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MysqlNodeDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MysqlNodeDeleteWorker====================");
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

        // 4、获取mysql cluster
        MysqlCluster oldCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, service.getServiceName());
        if (null == oldCluster || null == oldCluster.getStatus()) {
            LOG.error("根据serviceName获取oldCluster为空，或oldCluster不包含该节点，serviceId:" + node.getServiceName()
                    + ",oldCluster:" + JSON.toJSONString(oldCluster));
            optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);
            return;
        }

        Map<String, MysqlServer> serverNodes = oldCluster.getStatus().getServerNodes();
        if (null == serverNodes || serverNodes.isEmpty() || !serverNodes.containsKey(node.getNodeName())) {
            LOG.info("节点已经被删除，nodeId：" + nodeId + ",nodeName:" + node.getNodeName());
            optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);
            return;
        }

        MysqlCluster newCluster = updateYamlForMysqlCluster(tenantName, service.getServiceName(),
                MysqlClusterConst.OPERATOR_NODE_DELETE, node.getNodeName(), null);

        // 5、调用k8sclient删除节点并循环获取删除结果
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("mysql节点" + node.getNodeName() + "删除失败！");
            componentOptNodeBase(CommonConst.APPTYPE_MYSQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        if (!checkNodeDeleteResult(tenantName, service, node.getNodeName())) {
            LOG.error("mysql节点删除失败，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
            componentOptNodeBase(CommonConst.APPTYPE_MYSQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        LOG.info("mysql节点删除成功，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);

    }

    /**
     * 处理节点删除
     * 
     * @param tenantName
     * @param service
     * @param node
     */
    private void optNodeDelete(String tenantName, String serviceId, String serviceName, StatefulNode node) {
        mysqlOptNodeDelete(tenantName, node.getId(), CommonConst.STATE_NODE_DELETED);
        mysqlOptNodeDeleteSyncYaml(tenantName, serviceId, serviceName);
        componentOperationsClientUtil.deleteLvm(tenantName, node.getLvmName());
        if (node.getRole().contains(CommonConst.ROLE_MASTER)) {
            deleteBackUp(tenantName, node.getId());
        }

    }

    /**
     * 处理节点删除，同步yaml状态及扩展字段
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void mysqlOptNodeDeleteSyncYaml(String tenantName, String serviceId, String serviceName) {
        // 获取mysqlCluster
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);
        if (null == mysqlCluster || null == mysqlCluster.getStatus()
                || null == mysqlCluster.getStatus().getServerNodes()) {
            LOG.info("mysqlCluster为null");
            return;
        }

        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(mysqlCluster);

        componentOperationsClientUtil.changeMysqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, null);
    }

    /**
     * 根据nodeId修改集群和节点状态
     * 
     * @param nodeId
     * @param nodeState
     */
    private void mysqlOptNodeDelete(String tenantName, String nodeId, String nodeState) {
        try {
            StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
            if (null != node && !CommonConst.STATE_NODE_DELETED.equals(node.getNodeState())) {
                node.setNodeState(nodeState);
                statefulNodeRepository.save(node);
                // 修改tenant表
//                boolean result = tenantService.updateUsedResource(tenantName, node.getCpu() * (-1),
//                        node.getMemory() * (-1), node.getStorage() * (-1));
//                LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + node.getCpu() * (-1) + ",updateMemory:"
//                        + node.getMemory() * (-1) + ",updateCapacity:" + node.getStorage() * (-1));
            }

            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
            if (null != service) {
                service.setServiceState(componentOperationsClientUtil.getClusterStateByNodes(node.getServiceId(),
                        service.getLastopt()));
                service.setNodeNum(service.getNodeNum() - 1);
                if (null != node) {
                    service.setCpu(service.getCpu() - node.getCpu());
                    service.setMemory(service.getMemory() - node.getMemory());
                    service.setStorage(service.getStorage() - node.getStorage());
                }

                statefulServiceRepository.save(service);
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

            MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName,
                    service.getServiceName());
            if (null == mysqlCluster) {
                LOG.info("获取的集群为空!");
                return true;
            }

            MysqlServer server = null;
            if (null != mysqlCluster.getStatus() && null != mysqlCluster.getStatus().getServerNodes()) {
                server = mysqlCluster.getStatus().getServerNodes().get(nodeName);
            }
            if (null == server) {
                LOG.info("===========删除pod成功===========");
                return true;
            }
        }
    }
}
