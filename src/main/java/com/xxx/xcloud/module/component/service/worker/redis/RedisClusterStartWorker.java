package com.xxx.xcloud.module.component.service.worker.redis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;

@Service
@Scope("prototype")
public class RedisClusterStartWorker extends BaseRedisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(RedisClusterStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============RedisClusterStartWorker====================");
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

        // 3、拼接redisCluster
        RedisCluster redisCluster = buildRedisCluster(tenantName, service.getServiceName());

        // 4、调用k8sclient启动集群
        if (!updateAndRetry(tenantName, redisCluster)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.info("redis集群启动失败！");
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
        componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, serviceId,
                service.getServiceName());
    }

    /**
     * 启动集群：构建redisCluster
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    private RedisCluster buildRedisCluster(String tenantName, String serviceName) {

        RedisCluster redisCluster = null;
        redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);
        if (null == redisCluster) {
            LOG.error("获取redisCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        redisCluster.getSpec().setStopped(false);
        LOG.info("启动集群：构建redisCluster成功, serviceName:" + serviceName);
        return redisCluster;
    }

}
