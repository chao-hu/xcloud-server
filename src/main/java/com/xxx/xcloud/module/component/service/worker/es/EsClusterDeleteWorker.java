package com.xxx.xcloud.module.component.service.worker.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;

@Service
@Scope("prototype")
public class EsClusterDeleteWorker extends BaseEsClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(EsClusterDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============EsClusterDeleteWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

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

        // 3、获取esCluster
        EsCluster esCluster = componentOperationsClientUtil.getEsCluster(tenantName, service.getServiceName());
        if (null == esCluster) {
            LOG.info("删除集群时es集群不存在，service" + JSON.toJSONString(service));
            componentOperationsClientUtil.deleteNodesLvm(tenantName, serviceId);
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_DELETED,
                    CommonConst.STATE_NODE_DELETED, null);
            // 修改tenant表
//            boolean result = tenantService.updateUsedResource(tenantName, service.getCpu() * (-1),
//                    service.getMemory() * (-1), service.getStorage() * (-1));
//            LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + service.getCpu() * (-1) + ",updateMemory:"
//                    + service.getMemory() * (-1) + ",updateCapacity:" + service.getStorage() * (-1));
            return;
        }

        // 4、调用k8sclient删除集群
        boolean deleteResult = deleteAndRetry(tenantName, service.getServiceName());
        if (!deleteResult) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("es集群:" + service.getServiceName() + "删除失败");
        } else {
            componentOperationsClientUtil.deleteNodesLvm(tenantName, serviceId);
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_DELETED,
                    CommonConst.STATE_NODE_DELETED, null);
            // 5、修改tenant表
            // boolean result = tenantService.updateUsedResource(tenantName,
            // service.getCpu() * (-1),
            // service.getMemory() * (-1), service.getStorage() * (-1));
            // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
            // service.getCpu() * (-1) + ",updateMemory:"
            // + service.getMemory() * (-1) + ",updateCapacity:" + service.getStorage() * (-1));

        }
    }

    
}
