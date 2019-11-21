package com.xxx.xcloud.module.component.service.worker.postgresql;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;

@Service
@Scope("prototype")
public class PostgresqlClusterStartWorker extends BasePostgresqlClusterWorker {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresqlClusterStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============PostgresqlClusterStartWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群启动时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群启动时获取的service：" + JSON.toJSONString(service));

        // 3、拼接postgresqlCluster
        PostgresqlCluster newCluster = updateYamlForPostgresqlCluster(tenantName, service.getServiceName(),
                PostgresqlClusterConst.OPERATOR_CLUSTER_START, null, 0);

        // 4、调用k8sclient启动集群
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("postgresql集群" + service.getServiceName() + "启动失败！");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            return;
        }

        // 5、循环获取集群启动结果
        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(),
                PostgresqlClusterConst.OPERATOR_CLUSTER_START, CommonConst.STATE_CLUSTER_RUNNING)) {
            LOG.info("postgresql集群启动成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
        } else {
            LOG.error("postgresql集群启动失败，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
        }

        optClusterStart(tenantName, serviceId, service.getServiceName());

    }

    /**
     * 处理集群启动
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optClusterStart(String tenantName, String serviceId, String serviceName) {
        // 获取postgresqlCluster
        PostgresqlCluster postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                serviceName);

        // stopped的集群新增节点后，启动集群注册lvm
        registeClusterLvm(tenantName, postgresqlCluster);
        // 获取集群running是集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(postgresqlCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(postgresqlCluster);
        // 修改集群、节点状态
        componentOperationsClientUtil.changePostgresqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, nodesExtendedField);
    }

}
