package com.xxx.xcloud.module.component.service.worker.postgresql;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlInstance;

@Service
@Scope("prototype")
public class PostgresqlNodeDeleteWorker extends BasePostgresqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(PostgresqlNodeDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============PostgresqlNodeDeleteWorker====================");
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

        // 4、获取postgresqlCluster
        PostgresqlCluster oldCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                service.getServiceName());
        if (null == oldCluster || null == oldCluster.getStatus()) {
            LOG.error("根据serviceName获取oldCluster为空，或oldCluster不包含该节点，serviceId:" + node.getServiceName()
                    + ",oldCluster:" + JSON.toJSONString(oldCluster));
            optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);
            return;
        }

        Map<String, PostgresqlInstance> serverNodes = oldCluster.getStatus().getInstances();
        if (null == serverNodes || serverNodes.isEmpty() || !serverNodes.containsKey(node.getNodeName())) {
            LOG.info("节点已经被删除，nodeId：" + nodeId + ",nodeName:" + node.getNodeName());
            optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);
            return;
        }

        PostgresqlCluster newCluster = updateYamlForPostgresqlCluster(tenantName, service.getServiceName(),
                PostgresqlClusterConst.OPERATOR_NODE_DELETE, node.getNodeName(), 0);

        // 5、调用k8sclient删除节点并循环获取删除结果
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("postgresql节点" + node.getNodeName() + "删除失败！");
            componentOptNodeBase(CommonConst.APPTYPE_POSTGRESQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        if (!checkNodeDeleteResult(tenantName, service, node.getNodeName())) {
            LOG.error("postgresql节点删除失败，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
            componentOptNodeBase(CommonConst.APPTYPE_POSTGRESQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        LOG.info("postgresql节点删除成功，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        optNodeDelete(tenantName, service.getId(), service.getServiceName(), node);

    }

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

            PostgresqlCluster postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                    service.getServiceName());
            if (null == postgresqlCluster) {
                LOG.info("获取的集群为空!");
                return true;
            }

            PostgresqlInstance instance = null;
            if (null != postgresqlCluster.getStatus() && null != postgresqlCluster.getStatus().getInstances()) {
                instance = postgresqlCluster.getStatus().getInstances().get(nodeName);
            }
            if (null == instance) {
                LOG.info("===========删除pod成功===========");
                return true;
            }
        }
    }

    private void optNodeDelete(String tenantName, String serviceId, String serviceName, StatefulNode node) {
        postgresqlOptNodeDelete(tenantName, node.getId(), CommonConst.STATE_NODE_DELETED);
        postgresqlOptNodeDeleteSyncYaml(tenantName, serviceId, serviceName);
        componentOperationsClientUtil.deleteLvm(tenantName, node.getLvmName());
    }

    /**
     * 处理节点删除，同步yaml状态及扩展字段
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void postgresqlOptNodeDeleteSyncYaml(String tenantName, String serviceId, String serviceName) {
        // 获取postgresqlCluster
        PostgresqlCluster postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                serviceName);
        if (null == postgresqlCluster || null == postgresqlCluster.getStatus()
                || null == postgresqlCluster.getStatus().getInstances()) {
            LOG.info("postgresqlCluster为null");
            return;
        }
        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(postgresqlCluster);

        componentOperationsClientUtil.changePostgresqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, null);
    }

    private void postgresqlOptNodeDelete(String tenantName, String nodeId, String nodeState) {
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
                String status = componentOperationsClientUtil.getClusterStateByNodes(node.getServiceId(),
                        service.getLastopt());
                service.setServiceState(status);
                service.setNodeNum(service.getNodeNum() - 1);
                if (null != node) {
                    service.setCpu(service.getCpu() - node.getCpu());
                    service.setMemory(service.getMemory() - node.getMemory());
                    service.setStorage(service.getStorage() - node.getStorage());
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

}
