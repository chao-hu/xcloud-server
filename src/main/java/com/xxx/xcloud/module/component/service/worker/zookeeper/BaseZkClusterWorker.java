package com.xxx.xcloud.module.component.service.worker.zookeeper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;
import com.xxx.xcloud.module.component.model.zookeeper.ZkInstance;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BaseZkClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseZkClusterWorker.class);

    /**
     * 创建zk集群
     * 
     * @param nameSpace
     * @param zkCluster
     * @return
     */
    protected boolean createZkCluster(String nameSpace, ZkCluster zkCluster) {
        if (StringUtils.isEmpty(nameSpace)) {
            LOG.error("创建zkCluster时tenantName为空");
            return false;
        }
        if (null == zkCluster) {
            LOG.error("创建zkCluster时zkCluster为空");
            return false;
        }
        try {
            ZkCluster oldCluster = componentOperationsClientUtil.getZkCluster(nameSpace,
                    zkCluster.getMetadata().getName());
            if (null == oldCluster) {
                ZkCluster newCluster = ComponentClientFactory.getZkClient().inNamespace(nameSpace).create(zkCluster);
                LOG.info("zk集群创建后返回的cluster：" + JSON.toJSONString(newCluster));
                if (null != newCluster) {
                    return true;
                }
                return false;
            } else {
                LOG.error("zk集群已经存在,租户:" + zkCluster.getMetadata().getNamespace() + ",集群名称:"
                        + zkCluster.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("zk集群创建失败", e);
            return false;
        }
    }

    /**
     * 删除zk集群
     * 
     * @param nameSpace
     * @param clusterName
     * @return
     */
    protected boolean deleteZkCluster(String tenantName, String clusterName) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return false;
        }
        boolean result = false;
        try {
            ZkCluster zkCluster = ComponentClientFactory.getZkClient().inNamespace(tenantName).withName(clusterName)
                    .get();
            if (null == zkCluster) {
                LOG.info("zk集群不存在或已经被删除");
                return true;
            }
            result = ComponentClientFactory.getZkClient().inNamespace(tenantName).withName(clusterName).delete();
            LOG.info("ZkCluster删除result:" + result + "，clusterName：" + clusterName);
        } catch (Exception e) {
            LOG.error("zk删除失败，clusterName：" + clusterName, e);
            return false;
        }
        return result;
    }

    /**
     * 
     * @param tenantName
     * @param zkCluster
     * @return
     */
    protected boolean updateAndRetry(String tenantName, ZkCluster zkCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的zkCluster：" + JSON.toJSONString(zkCluster));
        if (null != zkCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = componentOperationsClientUtil.updateZkCluster(tenantName, zkCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("修改zk集群" + zkCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        } else {
            LOG.error("zkCluster为null");
        }
        return false;
    }

    /**
     * 删除集群
     * 
     * @param tenantName
     * @param zkCluster
     * @return
     */
    protected boolean deleteAndRetry(String tenantName, String serviceName) {
        LOG.info("==========开始删除集群==========");
        LOG.info("删除集群的tenantName：" + tenantName + ",serviceName:" + JSON.toJSONString(serviceName));
        ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, serviceName);
        if (null == zkCluster) {
            LOG.info("zkCluster已经不存在");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteZkCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除zk集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    /**
     * 修改zkCluster的yaml 集群启动，停止 节点启动，停止
     * 
     * @param tenantName
     * @param serviceId
     * @return
     */
    protected ZkCluster updateYamlForZkCluster(String tenantName, String serviceName, String opt, String nodeName,
            int addNum) {
        ZkCluster zkCluster = null;
        zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, serviceName);
        if (null == zkCluster) {
            LOG.error("获取zkCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }

        switch (opt) {
        case ZkClusterConst.OPERATOR_CLUSTER_START:
        case ZkClusterConst.OPERATOR_CLUSTER_STOP:
            zkCluster.getSpec().setOpt(opt);
            break;
        case ZkClusterConst.OPERATOR_CLUSTER_EXPAND:
            if (addNum != 0) {
                zkCluster.getSpec().setOpt(opt);
                zkCluster.getSpec().setReplicas(zkCluster.getSpec().getReplicas() + addNum);
            }
            break;
        case ZkClusterConst.OPERATOR_NODE_START:
        case ZkClusterConst.OPERATOR_NODE_STOP:
        case ZkClusterConst.OPERATOR_NODE_DELETE:
            zkCluster.getSpec().setOpt(opt);
            zkCluster.getSpec().setOptNodename(nodeName);
            break;
        default:
            break;
        }
        return zkCluster;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        String returnStatus = null;
        ZkCluster cluster = componentOperationsClientUtil.getZkCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getInstances() && !cluster.getStatus().getInstances().isEmpty()) {
                for (Map.Entry<String, ZkInstance> entry : cluster.getStatus().getInstances().entrySet()) {
                    if (nodeName.equals(entry.getKey())) {
                        return entry.getValue().getInstancePhase();
                    }
                }
            }
        }
        return returnStatus;
    }

    /**
     * 获取集群扩展字段
     * 
     * @param id
     * @param zkCluster
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(ZkCluster zkCluster) {
        LOG.info("zkCluster:" + JSON.toJSONString(zkCluster));
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != zkCluster && null != zkCluster.getStatus()) {
            String interAddress = zkCluster.getStatus().getInterAddress();
            String exterAddress = zkCluster.getStatus().getExterAddress();
            if (StringUtils.isNotEmpty(interAddress)) {
                serviceExtendedField.put("interAddress", interAddress);
            }

            if (StringUtils.isNotEmpty(exterAddress)) {
                serviceExtendedField.put("exterAddress", exterAddress);
            }

            String resourceEffectiveGet = zkCluster.getStatus().getResourceupdateneedrestart();
            String configEffectiveGet = zkCluster.getStatus().getParameterupdateneedrestart();
            String resourceEffective = "false".equals(resourceEffectiveGet) ? "true" : "false";
            String configEffective = "false".equals(configEffectiveGet) ? "true" : "false";
            serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, resourceEffective);
            serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, configEffective);
        }
        return serviceExtendedField;
    }

    /**
     * 构造节点扩展字段
     * 
     * @param zkCluster
     * @return
     */
    protected Map<String, Map<String, String>> buildNodesExtendedField(ZkCluster zkCluster) {
        LOG.info("zkCluster:" + JSON.toJSONString(zkCluster));
        Map<String, Map<String, String>> nodesExtendedField = new HashMap<>();
        if (null != zkCluster && null != zkCluster.getStatus()) {
            Map<String, ZkInstance> serverNodes = zkCluster.getStatus().getInstances();
            for (Map.Entry<String, ZkInstance> entry : serverNodes.entrySet()) {
                Map<String, String> nodeMap = new HashMap<>();
                if (StringUtils.isNotEmpty(entry.getValue().getExterHost())) {
                    nodeMap.put("ip", entry.getValue().getExterHost());
                }

                if (0 != entry.getValue().getExterport()) {
                    nodeMap.put("port", String.valueOf(entry.getValue().getExterport()));
                }

                if (!nodeMap.isEmpty()) {
                    nodesExtendedField.put(entry.getKey(), nodeMap);
                }
            }
        }
        return nodesExtendedField;
    }

    /**
     * 给集群注册lvm卷
     * 
     * @param nameSpace
     * @param mysqlCluster
     */
    protected void registeClusterLvm(String tenantName, ZkCluster zkCluster) {
        LOG.info("创建lvm,接收的参数=>tenantName:" + tenantName + ",zkCluster:" + zkCluster);
        if (null != zkCluster && null != zkCluster.getStatus() && null != zkCluster.getStatus().getInstances()) {
            for (Map.Entry<String, ZkInstance> entry : zkCluster.getStatus().getInstances().entrySet()) {
                if (CommonConst.STATE_NODE_RUNNING.equals(entry.getValue().getInstancePhase())) {
                    componentOperationsClientUtil.registerLvm(tenantName, entry.getValue().getLvName(),
                            entry.getValue().getExterHost(), ZkClusterConst.ZK_CAPACITY_DEFAULT, CommonConst.UNIT_MI);
                }
            }
        }
    }
}
