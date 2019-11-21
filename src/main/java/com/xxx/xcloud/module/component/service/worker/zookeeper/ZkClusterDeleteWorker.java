package com.xxx.xcloud.module.component.service.worker.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;

@Service
@Scope("prototype")
public class ZkClusterDeleteWorker extends BaseZkClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(ZkClusterDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============ZkClusterDeleteWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群删除时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群删除时获取的service：" + JSON.toJSONString(service));

        // 3、获取zkCluster
        ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, service.getServiceName());
        if (null == zkCluster) {
            LOG.info("删除集群时zk集群不存在，service" + JSON.toJSONString(service));
            optClusterDelete(tenantName, service);
            return;
        }

        // 3、删除zkCluster
        if (!deleteAndRetry(tenantName, service.getServiceName())) {
            LOG.error("zk集群:" + service.getServiceName() + "删除失败!");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            return;
        }

        if (checkDeleteResult(tenantName, service)) {
            optClusterDelete(tenantName, service);
            return;
        }
        componentOperationsClientUtil.changeZkClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());

    }

    /**
     * 检查集群删除结果
     * 
     * @param tenantName
     * @param service
     * @return
     */
    private boolean checkDeleteResult(String tenantName, StatefulService service) {
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
                LOG.error("删除集群超时，service：" + JSON.toJSONString(service));
                return false;
            }

            ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, service.getServiceName());
            if (null == zkCluster) {
                LOG.info("==========获取操作结果成功==========");
                return true;
            }
        }
    }

    /**
     * 处理集群删除
     * 
     * @param tenantName
     * @param service
     */
    private void optClusterDelete(String tenantName, StatefulService service) {
        componentOperationsClientUtil.deleteNodesLvm(tenantName, service.getId());
        componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_DELETED,
                CommonConst.STATE_NODE_DELETED, null);
        // 修改tenant表
//        boolean result = tenantService.updateUsedResource(tenantName, service.getCpu() * (-1),
//                service.getMemory() * (-1), service.getStorage() * (-1));
//        LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + service.getCpu() * (-1) + ",updateMemory:"
//                + service.getMemory() * (-1) + ",updateCapacity:" + service.getStorage() * (-1));

    }

}
