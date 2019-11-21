package com.xxx.xcloud.module.component.service.worker.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;

@Service
@Scope("prototype")
public class PostgresqlNodeStopWorker extends BasePostgresqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(PostgresqlNodeStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============PostgresqlNodeStopWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            componentOptNodeBase(CommonConst.APPTYPE_POSTGRESQL, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空,serviceId:" + node.getServiceId());
            return;
        }

        // 4、拼接postgresqlCluster
        PostgresqlCluster postgresqlCluster = updateYamlForPostgresqlCluster(tenantName, service.getServiceName(),
                PostgresqlClusterConst.OPERATOR_NODE_STOP, node.getNodeName(), 0);

        // 5、调用k8sclient停止节点并循环获取停止结果
        if (updateAndRetry(tenantName, postgresqlCluster) && checkNodeStartOrStopResult(tenantName,
                service.getServiceName(), PostgresqlClusterConst.OPERATOR_NODE_STOP, CommonConst.STATE_NODE_STOPPED,
                node.getNodeName())) {
            componentOptNodeBase(CommonConst.APPTYPE_POSTGRESQL, nodeId, CommonConst.STATE_NODE_STOPPED);
            return;
        }

        LOG.error("postgresql节点" + node.getNodeName() + "停止失败！");
        componentOptNodeBase(CommonConst.APPTYPE_POSTGRESQL, nodeId, CommonConst.STATE_NODE_FAILED);

    }

}
