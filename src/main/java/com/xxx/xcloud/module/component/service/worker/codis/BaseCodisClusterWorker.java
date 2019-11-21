package com.xxx.xcloud.module.component.service.worker.codis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;
import com.xxx.xcloud.module.component.model.codis.CodisGroupBindingNode;
import com.xxx.xcloud.module.component.model.codis.CodisGroupStatus;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.client.KubernetesClientException;

public abstract class BaseCodisClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseCodisClusterWorker.class);

    /**
     * 创建单个codis集群
     * 
     * @param tenantName
     * @param codisCluster
     * @return
     */
    public boolean createCodisCluster(String tenantName, CodisCluster codisCluster) {
        LOG.info("--------创建codisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            CodisCluster oldCodisCluster = componentOperationsClientUtil.getCodisCluster(tenantName,
                    codisCluster.getMetadata().getName());
            if (null == oldCodisCluster) {
                CodisCluster newCodisCluster = ComponentClientFactory.getCodisClient().inNamespace(tenantName)
                        .create(codisCluster);
                LOG.info("codis创建后返回的codisCluster：" + JSON.toJSONString(newCodisCluster));
                return true;
            } else {
                LOG.error("codisCluster:" + codisCluster.getMetadata().getName() + "已经存在");
                return false;
            }
        } catch (Exception e) {
            LOG.error("codisCluster创建失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 更新单个codis集群
     * 
     * @param tenantName
     * @param codisCluster
     * @return
     */
    public Boolean updateCodisCluster(String tenantName, CodisCluster codisCluster) {
        LOG.info("--------更新codisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        }

        try {
            CodisCluster newCodisCluster = ComponentClientFactory.getCodisClient().inNamespace(tenantName)
                    .createOrReplace(codisCluster);
            if (null == newCodisCluster) {
                LOG.error("codisCluster更新后返回的codisCluster 为空");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("codisCluster更新失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 删除单个codis集群
     * 
     * @param tenantName
     * @param clusterName
     * @return
     */
    public Boolean deleteCodisCluster(String tenantName, String clusterName) {
        LOG.info("--------删除codisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        try {
            Boolean result = ComponentClientFactory.getCodisClient().inNamespace(tenantName).withName(clusterName)
                    .delete();
            LOG.info("codisCluster" + clusterName + "删除是否成功：" + result);
            return result;
        } catch (KubernetesClientException e) {
            LOG.error("delete codisCluster" + clusterName + "失败！");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 
     * @param tenantName
     * @param codisCluster
     * @return
     */
    public boolean updateAndRetry(String tenantName, CodisCluster codisCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的codisCluster：" + JSON.toJSONString(codisCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = updateCodisCluster(tenantName, codisCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("修改codis集群" + codisCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        CodisCluster cluster = componentOperationsClientUtil.getCodisCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            LOG.info("cluster:" + JSON.toJSONString(cluster));
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getGroup() && !cluster.getStatus().getGroup().isEmpty()) {
                for (CodisGroupStatus codisGroupStatus : cluster.getStatus().getGroup().values()) {
                    for (CodisGroupBindingNode codisGroupBindingNode : codisGroupStatus.getBindings().values()) {
                        if (codisGroupBindingNode.getName().equals(nodeName)) {
                            return codisGroupBindingNode.getStatus();
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
        CodisCluster codisCluster = componentOperationsClientUtil.getCodisCluster(tenantName, serviceName);
        LOG.info("codisCluster:" + JSON.toJSONString(codisCluster));
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != codisCluster && null != codisCluster.getStatus()) {
            boolean resourceOrConfigEffective = !codisCluster.getStatus().isNeedRestart();
            serviceExtendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE,
                    String.valueOf(resourceOrConfigEffective));
        }
        return serviceExtendedField;
    }
}
