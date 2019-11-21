package com.xxx.xcloud.module.component.service.worker.mysql;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;

@Service
@Scope("prototype")
public class MysqlNodeStartWorker extends BaseMysqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MysqlNodeStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MysqlNodeStartWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            componentOptNodeBase(CommonConst.APPTYPE_MYSQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空,serviceId:" + node.getServiceId());
            return;
        }

        // 4、拼接mysql cluster
        MysqlCluster newCluster = updateYamlForMysqlCluster(tenantName, service.getServiceName(),
                MysqlClusterConst.OPERATOR_NODE_START, node.getNodeName(), null);

        // 5、调用k8sclient启动节点并循环获取启动结果
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("mysql节点" + node.getNodeName() + "启动失败！");
            componentOptNodeBase(CommonConst.APPTYPE_MYSQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        if (checkNodeStartOrStopResult(tenantName, service.getServiceName(), MysqlClusterConst.OPERATOR_NODE_START,
                CommonConst.STATE_NODE_RUNNING, node.getNodeName())) {
            LOG.info("mysql节点启动成功，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        } else {
            LOG.error("mysql节点启动失败，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        }

        optNodeStart(tenantName, service.getId(), service.getServiceName());

    }

    /**
     * 处理节点启动
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optNodeStart(String tenantName, String serviceId, String serviceName) {
        // 获取mysqlCluster
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);

        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(mysqlCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(mysqlCluster);

        componentOperationsClientUtil.changeMysqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, nodesExtendedField);
    }
}
