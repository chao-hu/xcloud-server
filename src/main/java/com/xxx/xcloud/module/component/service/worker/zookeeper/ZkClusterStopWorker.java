package com.xxx.xcloud.module.component.service.worker.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;

@Service
@Scope("prototype")
public class ZkClusterStopWorker extends BaseZkClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(ZkClusterStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============ZkClusterStopWorker====================");
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

        // 3、拼接zkCluster
        ZkCluster zkCluster = updateYamlForZkCluster(tenantName, service.getServiceName(),
                ZkClusterConst.OPERATOR_CLUSTER_STOP, null, 0);

        // 4、调用k8sclient停止集群
        if (!updateAndRetry(tenantName, zkCluster)) {
            LOG.error("zk集群" + service.getServiceName() + "停止失败！");
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
        componentOperationsClientUtil.changeZkClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());
    }

}
