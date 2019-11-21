package com.xxx.xcloud.module.component.service.worker.es;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.client.KubernetesClientException;

public abstract class BaseEsClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseEsClusterWorker.class);

    /**
     * 创建单个es集群
     * 
     * @param tenantName
     * @param esCluster
     * @return
     */
    public boolean createEsCluster(String tenantName, EsCluster esCluster) {
        LOG.info("--------创建esCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            EsCluster oldEsCluster = componentOperationsClientUtil.getEsCluster(tenantName,
                    esCluster.getMetadata().getName());
            if (null == oldEsCluster) {
                EsCluster newEsCluste = ComponentClientFactory.getEsClient().inNamespace(tenantName).create(esCluster);
                LOG.info("es创建后返回的esCluster：" + JSON.toJSONString(newEsCluste));
                return true;
            } else {
                LOG.error("esCluster:" + esCluster.getMetadata().getName() + "已经存在");
                return false;
            }
        } catch (Exception e) {
            LOG.error("esCluster创建失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 删除单个es集群
     * 
     * @param tenantName
     * @param clusterName
     * @return
     */
    public Boolean deleteEsCluster(String tenantName, String clusterName) {
        LOG.info("--------删除esCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        try {
            Boolean result = ComponentClientFactory.getEsClient().inNamespace(tenantName).withName(clusterName)
                    .delete();
            LOG.info("esCluster" + clusterName + "删除是否成功：" + result);
            return result;
        } catch (KubernetesClientException e) {
            LOG.error("delete esCluster" + clusterName + "失败！");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * 更改集群信息
     * 
     * @param tenantName
     * @param esCluster
     * @return
     */
    public boolean updateAndRetry(String tenantName, EsCluster esCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的esCluster：" + JSON.toJSONString(esCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = componentOperationsClientUtil.updateEsCluster(tenantName, esCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("修改es集群" + esCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        EsCluster cluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            LOG.info("cluster:" + JSON.toJSONString(cluster));
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getInstances() && !cluster.getStatus().getInstances().isEmpty()) {
                if (null != cluster.getStatus().getInstances().get(nodeName)) {
                    return cluster.getStatus().getInstances().get(nodeName).getInstancePhase();
                }
            }
        }
        return null;
    }

    /**
     * 修改节点状态，并根据所有节点状态修改集群状态
     * 
     * @param nodeId
     * @param nodeState
     */
    protected void updateEsNodeStateAndClusterState(String nodeId, String nodeState) {
        try {
            StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
            if (null != node && !CommonConst.STATE_NODE_DELETED.equals(node.getNodeState())) {
                node.setNodeState(nodeState);
                statefulNodeRepository.save(node);
            }

            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
            if (null != service) {
                service.setServiceState(componentOperationsClientUtil.getEsClusterStateByNodes(node.getServiceId(),
                        service.getLastopt()));
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("根据nodeId修改集群和节点状态失败，nodeId:" + nodeId + ",error:", e);
        }
    }

    /**
     * 获取集群扩展字段
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(String tenantName, String serviceName) {
        EsCluster esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        LOG.info("esCluster:" + JSON.toJSONString(esCluster));
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != esCluster && null != esCluster.getStatus()) {
            String resourceEffective = esCluster.getStatus().getResourceupdateneedrestart();
            String configEffective = esCluster.getStatus().getParameterupdateneedrestart();
            serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, resourceEffective);
            serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, configEffective);
        }
        return serviceExtendedField;
    }

    /**
     * 修改es扩展字段
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    protected void updateServiceExtendedField(String tenantName, String serviceId, String serviceName) {
        Map<String, String> extendedField = buildServiceExtendedField(tenantName, serviceName);
        if (null == extendedField || extendedField.isEmpty()) {
            LOG.error("根据esCluster获取集群扩展字段为空，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return;
        }
        LOG.info("根据esCluster获取集群扩展字段为extendedField：" + JSON.toJSONString(extendedField));
        try {
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                Map<String, String> extendedFieldMap = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
                LOG.info("service原有的扩展字段为：" + JSON.toJSONString(extendedFieldMap));
                extendedFieldMap.putAll(extendedField);
                service.setExtendedField(JSON.toJSONString(extendedFieldMap));
                LOG.info("es集群合并后的扩展字段为：" + service.getExtendedField());
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("根据serviceId修改集群的扩展字段失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",serviceName:"
                    + serviceName);
        }
    }
    
    /**
     * 删除es集群
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    protected boolean deleteAndRetry(String tenantName, String serviceName) {
        LOG.info("==========开始删除集群==========");
        LOG.info("删除集群的tenantName：" + tenantName + ",serviceName:" + JSON.toJSONString(serviceName));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteEsCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除es集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

}
