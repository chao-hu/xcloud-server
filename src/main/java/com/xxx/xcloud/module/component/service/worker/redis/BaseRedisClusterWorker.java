package com.xxx.xcloud.module.component.service.worker.redis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.client.KubernetesClientException;

public abstract class BaseRedisClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseRedisClusterWorker.class);

    /**
     * 创建单个redis集群
     * 
     * @param tenantName
     * @param redisCluster
     * @return
     */
    public boolean createRedisCluster(String tenantName, RedisCluster redisCluster) {
        LOG.info("--------创建redisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            RedisCluster oldRedisCluster = componentOperationsClientUtil.getRedisCluster(tenantName,
                    redisCluster.getMetadata().getName());
            if (null == oldRedisCluster) {
                RedisCluster newRedisCluster = ComponentClientFactory.getRedisClient().inNamespace(tenantName)
                        .create(redisCluster);
                LOG.info("redis创建后返回的redisCluster：" + JSON.toJSONString(newRedisCluster));
                return true;
            } else {
                LOG.error("redisCluster:" + redisCluster.getMetadata().getName() + "已经存在");
                return false;
            }
        } catch (Exception e) {
            LOG.error("redisCluster创建失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 删除单个redis集群
     * 
     * @param tenantName
     * @param clusterName
     * @return
     */
    public Boolean deleteRedisCluster(String tenantName, String clusterName) {
        LOG.info("--------删除redisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        try {
            Boolean result = ComponentClientFactory.getRedisClient().inNamespace(tenantName).withName(clusterName)
                    .delete();
            LOG.info("redisCluster" + clusterName + "删除是否成功：" + result);
            return result;
        } catch (KubernetesClientException e) {
            LOG.error("delete redisCluster" + clusterName + "失败！");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 
     * @param tenantName
     * @param redisCluster
     * @return
     */
    public boolean updateAndRetry(String tenantName, RedisCluster redisCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的redisCluster：" + JSON.toJSONString(redisCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = componentOperationsClientUtil.updateRedisCluster(tenantName, redisCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("修改redis集群" + redisCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        RedisCluster cluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            LOG.info("cluster:" + JSON.toJSONString(cluster));
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getBindings() && !cluster.getStatus().getBindings().isEmpty()) {
                if (null != cluster.getStatus().getBindings().get(nodeName)) {
                    return cluster.getStatus().getBindings().get(nodeName).getStatus();
                }
            }
        }
        return null;
    }

    /**
     * 获取集群扩展字段
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(String tenantName, String serviceName) {
        RedisCluster redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName, serviceName);
        LOG.info("redisCluster:" + JSON.toJSONString(redisCluster));
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != redisCluster && null != redisCluster.getStatus()) {
            boolean resourceOrConfigEffective = !redisCluster.getStatus().isNeedRestart();
            serviceExtendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE,
                    String.valueOf(resourceOrConfigEffective));
        }
        return serviceExtendedField;
    }

}
