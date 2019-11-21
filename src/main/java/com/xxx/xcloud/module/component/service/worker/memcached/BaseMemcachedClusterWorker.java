package com.xxx.xcloud.module.component.service.worker.memcached;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;
import com.xxx.xcloud.module.component.model.memcached.MemcachedCluster;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterGroupInfo;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterServer;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.client.KubernetesClientException;

public abstract class BaseMemcachedClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseMemcachedClusterWorker.class);

    /**
     * 创建单个memcached集群
     * 
     * @param tenantName
     * @param memcachedCluster
     * @return
     */
    public boolean createMemcachedCluster(String tenantName, MemcachedCluster memcachedCluster) {
        LOG.info("--------创建memcachedCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            MemcachedCluster oldMemcachedCluster = componentOperationsClientUtil.getMemcachedCluster(tenantName,
                    memcachedCluster.getMetadata().getName());
            if (null == oldMemcachedCluster) {
                MemcachedCluster newMemcachedCluster = ComponentClientFactory.getMemcachedClient()
                        .inNamespace(tenantName).create(memcachedCluster);
                LOG.info("memcached创建后返回的memcachedCluster：" + JSON.toJSONString(newMemcachedCluster));
                return true;
            } else {
                LOG.error("memcachedCluster:" + memcachedCluster.getMetadata().getName() + "已经存在");
                return false;
            }
        } catch (Exception e) {
            LOG.error("memcachedCluster创建失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 删除单个memcached集群
     * 
     * @param tenantName
     * @param clusterName
     * @return
     */
    public Boolean deleteMemcachedCluster(String tenantName, String clusterName) {
        LOG.info("--------删除memcachedCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        try {
            Boolean result = ComponentClientFactory.getMemcachedClient().inNamespace(tenantName).withName(clusterName)
                    .delete();
            LOG.info("memcachedCluster" + clusterName + "删除是否成功：" + result);
            return result;
        } catch (KubernetesClientException e) {
            LOG.error("delete memcachedCluster" + clusterName + "失败！");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 
     * @param tenantName
     * @param memcachedCluster
     * @return
     */
    public boolean updateAndRetry(String tenantName, MemcachedCluster memcachedCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的memcachedCluster：" + JSON.toJSONString(memcachedCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = componentOperationsClientUtil.updateMemcachedCluster(tenantName, memcachedCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("修改memcached集群" + memcachedCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        MemcachedCluster cluster = componentOperationsClientUtil.getMemcachedCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            LOG.info("cluster:" + JSON.toJSONString(cluster));
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getGroups() && !cluster.getStatus().getGroups().isEmpty()) {
                for (MemcachedClusterGroupInfo group : cluster.getStatus().getGroups().values()) {
                    for (MemcachedClusterServer server : group.getServer().values()) {
                        if (server.getName().equals(nodeName)) {
                            return server.getStatus();
                        }
                    }
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
        MemcachedCluster memcachedCluster = componentOperationsClientUtil.getMemcachedCluster(tenantName, serviceName);
        LOG.info("memcachedCluster:" + JSON.toJSONString(memcachedCluster));
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != memcachedCluster && null != memcachedCluster.getStatus()) {
            boolean resourceOrConfigEffective = !memcachedCluster.getStatus().isNeedRestart();
            serviceExtendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE,
                    String.valueOf(resourceOrConfigEffective));
        }
        return serviceExtendedField;
    }

    /**
     * 判断集群节点数是否与副本数相同
     * 
     * @param memcachedCluster
     * @return
     */
    protected Boolean isNodeNumEqualsReplicas(MemcachedCluster memcachedCluster) {
        if (MemcachedClusterConst.MEMCACHED_TYPE_MM.equals(memcachedCluster.getSpec().getType())) {
            return memcachedCluster.getSpec().getReplicas() == memcachedCluster.getStatus().getGroups().keySet().size()
                    * 2;
        } else if (MemcachedClusterConst.MEMCACHED_TYPE_SINGLE.equals(memcachedCluster.getSpec().getType())) {
            return memcachedCluster.getSpec().getReplicas() == memcachedCluster.getStatus().getGroups().keySet().size();
        }
        return false;
    }
}
