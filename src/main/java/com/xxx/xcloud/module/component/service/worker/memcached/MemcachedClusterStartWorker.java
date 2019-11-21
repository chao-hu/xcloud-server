package com.xxx.xcloud.module.component.service.worker.memcached;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.memcached.MemcachedCluster;

@Service
@Scope("prototype")
public class MemcachedClusterStartWorker extends BaseMemcachedClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MemcachedClusterStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MemcachedClusterStartWorker====================");
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

        // 3、拼接memcachedCluster
        MemcachedCluster memcachedCluster = buildMemcachedCluster(tenantName, service.getServiceName());

        // 4、调用k8sclient启动集群
        if (!updateAndRetry(tenantName, memcachedCluster)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.info("memcached集群启动失败！");
            return;
        }

        // 5、循环获取启动结果
        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(), CommonConst.OPT_CLUSTER_START,
                CommonConst.STATE_CLUSTER_RUNNING)) {
            Map<String, String> serviceExtendedField = buildServiceExtendedField(tenantName, service.getServiceName());

            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_RUNNING,
                    CommonConst.STATE_NODE_RUNNING, serviceExtendedField, null);
            return;
        }
        componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(tenantName, serviceId,
                service.getServiceName());
    }

    /**
     * 启动集群：构建memcachedCluster
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    private MemcachedCluster buildMemcachedCluster(String tenantName, String serviceName) {

        MemcachedCluster memcachedCluster = null;
        memcachedCluster = componentOperationsClientUtil.getMemcachedCluster(tenantName, serviceName);
        if (null == memcachedCluster) {
            LOG.error("获取memcachedCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        memcachedCluster.getSpec().setStopped(false);
        LOG.info("启动集群：构建memcachedCluster成功, serviceName:" + serviceName);
        return memcachedCluster;
    }

}
