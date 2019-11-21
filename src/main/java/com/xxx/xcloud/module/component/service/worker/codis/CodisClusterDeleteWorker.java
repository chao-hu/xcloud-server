package com.xxx.xcloud.module.component.service.worker.codis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;

@Service
@Scope("prototype")
public class CodisClusterDeleteWorker extends BaseCodisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(CodisClusterDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============CodisClusterDeleteWorker====================");
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

        // 3、获取codisCluster
        CodisCluster codisCluster = componentOperationsClientUtil.getCodisCluster(tenantName, service.getServiceName());
        if (null == codisCluster) {
            componentOperationsClientUtil.deleteNodesLvm(tenantName, serviceId);
            LOG.info("删除集群时codis集群不存在，service" + JSON.toJSONString(service));
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_DELETED,
                    CommonConst.STATE_NODE_DELETED, null);
            // 删除依赖表
            componentOperationsDataBaseUtil.deleteStatefulServiceDependency(service.getId());
            // 修改tenant表
            // boolean result = tenantService.updateUsedResource(tenantName,
            // service.getCpu() * (-1),
            // service.getMemory() * (-1), service.getStorage() * (-1));
            // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
            // service.getCpu() * (-1) + ",updateMemory:"
            // + service.getMemory() * (-1) + ",updateCapacity:" +
            // service.getStorage() * (-1));
            return;
        }

        // 4、调用k8sclient删除集群

        boolean deleteResult = deleteAndRetry(tenantName, service.getServiceName());
        if (!deleteResult) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("codis集群:" + service.getServiceName() + "删除失败");
        } else {
            componentOperationsClientUtil.deleteNodesLvm(tenantName, serviceId);
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_DELETED,
                    CommonConst.STATE_NODE_DELETED, null);

            // 删除依赖表
            componentOperationsDataBaseUtil.deleteStatefulServiceDependency(service.getId());

            // 5、修改tenant表
            // boolean result = tenantService.updateUsedResource(tenantName,
            // service.getCpu() * (-1),
            // service.getMemory() * (-1), service.getStorage() * (-1));
            // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
            // service.getCpu() * (-1) + ",updateMemory:"
            // + service.getMemory() * (-1) + ",updateCapacity:" +
            // service.getStorage() * (-1));

        }
    }

    private boolean deleteAndRetry(String tenantName, String serviceName) {
        LOG.info("==========开始删除集群==========");
        LOG.info("删除集群的tenantName：" + tenantName + ",serviceName:" + JSON.toJSONString(serviceName));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteCodisCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除codis集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }
}
