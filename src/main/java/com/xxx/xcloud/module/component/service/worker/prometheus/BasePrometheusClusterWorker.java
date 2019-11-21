package com.xxx.xcloud.module.component.service.worker.prometheus;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusCluster;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusInstances;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.client.KubernetesClientException;

public abstract class BasePrometheusClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BasePrometheusClusterWorker.class);

    /**
     * 创建单个prometheus集群
     * 
     * @param tenantName
     * @param prometheusCluster
     * @return
     */
    public boolean createPrometheusCluster(String tenantName, PrometheusCluster prometheusCluster) {
        LOG.info("--------创建prometheusCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            PrometheusCluster oldPrometheusCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName,
                    prometheusCluster.getMetadata().getName());
            if (null == oldPrometheusCluster) {
                PrometheusCluster newprometheusCluster = ComponentClientFactory.getPrometheusClient()
                        .inNamespace(tenantName).create(prometheusCluster);
                LOG.info("prometheus创建后返回的prometheusCluster：" + JSON.toJSONString(newprometheusCluster));
                return true;
            } else {
                LOG.error("prometheusCluster:" + prometheusCluster.getMetadata().getName() + "已经存在");
                return false;
            }
        } catch (Exception e) {
            LOG.error("prometheusCluster创建失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 删除单个prometheus集群
     * 
     * @param tenantName
     * @param clusterName
     * @return
     */
    public Boolean deletePrometheusCluster(String tenantName, String clusterName) {
        LOG.info("--------删除prometheusCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        try {
            Boolean result = ComponentClientFactory.getPrometheusClient().inNamespace(tenantName).withName(clusterName)
                    .delete();
            LOG.info("prometheusCluster" + clusterName + "删除是否成功：" + result);
            return result;
        } catch (KubernetesClientException e) {
            LOG.error("delete prometheusCluster" + clusterName + "失败！");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 
     * @param tenantName
     * @param prometheusCluster
     * @return
     */
    public boolean updateAndRetry(String tenantName, PrometheusCluster prometheusCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的prometheusCluster：" + JSON.toJSONString(prometheusCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = componentOperationsClientUtil.updatePrometheusCluster(tenantName, prometheusCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("修改prometheus集群" + prometheusCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        PrometheusCluster cluster = componentOperationsClientUtil.getPrometheusCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            LOG.info("cluster:" + JSON.toJSONString(cluster));
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getInstances() && !cluster.getStatus().getInstances().isEmpty()) {
                for (PrometheusInstances prometheusInstances : cluster.getStatus().getInstances().values()) {
                    if (prometheusInstances.getName().equals(nodeName)) {
                        return prometheusInstances.getInstancePhase();
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
        PrometheusCluster prometheusCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName,
                serviceName);
        LOG.info("prometheusCluster:" + JSON.toJSONString(prometheusCluster));
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != prometheusCluster && null != prometheusCluster.getStatus()) {
            String resourceEffective = prometheusCluster.getStatus().getResourceupdateneedrestart();
            String configEffective = prometheusCluster.getStatus().getParameterupdateneedrestart();
            serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, resourceEffective);
            serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, configEffective);
        }
        return serviceExtendedField;
    }
}
