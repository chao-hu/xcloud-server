package com.xxx.xcloud.module.component.service.worker.mysql;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;

@Service
@Scope("prototype")
public class MysqlClusterStartWorker extends BaseMysqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MysqlClusterStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MysqlClusterStartWorker====================");
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

        // 3、拼接mysqlCluster
        MysqlCluster nwqCluster = updateYamlForMysqlCluster(tenantName, service.getServiceName(),
                MysqlClusterConst.OPERATOR_CLUSTER_START, null, null);

        // 4、调用k8sclient启动集群
        if (!updateAndRetry(tenantName, nwqCluster)) {
            LOG.error("mysql集群" + service.getServiceName() + "启动失败！");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            return;
        }

        // 5、循环获取集群启动结果
        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(),
                MysqlClusterConst.OPERATOR_CLUSTER_START, CommonConst.STATE_CLUSTER_RUNNING)) {
            LOG.info("mysql集群启动成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
        } else {
            LOG.error("mysql集群启动失败，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
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
        // 获取mysqlCluster
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);

        // 获取集群running是集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(mysqlCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(mysqlCluster);
        // 修改集群、节点状态
        componentOperationsClientUtil.changeMysqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, nodesExtendedField);

    }
}
