package com.xxx.xcloud.module.component.service.worker.mysql;

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
public class MysqlClusterStopWorker extends BaseMysqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MysqlClusterStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MysqlClusterStopWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群停止时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群停止时获取的service：" + JSON.toJSONString(service));

        // 3、拼接mysqlCluster
        MysqlCluster mysqlCluster = updateYamlForMysqlCluster(tenantName, service.getServiceName(),
                MysqlClusterConst.OPERATOR_CLUSTER_STOP, null, null);

        // 4、调用k8sclient停止集群
        if (!updateAndRetry(tenantName, mysqlCluster)) {
            LOG.error("mysql集群" + service.getServiceName() + "停止失败！");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            return;
        }

        // 5、循环获取集群停止结果
        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(), MysqlClusterConst.OPERATOR_CLUSTER_STOP,
                CommonConst.STATE_CLUSTER_STOPPED)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_STOPPED,
                    CommonConst.STATE_NODE_STOPPED, null);
            return;
        }
        componentOperationsClientUtil.changeMysqlClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());
    }

}
