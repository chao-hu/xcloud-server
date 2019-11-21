package com.xxx.xcloud.module.component.service.worker.storm;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.model.storm.StormCluster;
import com.xxx.xcloud.module.component.model.storm.StormNode;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BaseStormClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseStormClusterWorker.class);

    /**
     * 创建storm集群
     * 
     * @param nameSpace
     * @param stormCluster
     * @return
     */
    protected boolean createStormCluster(String nameSpace, StormCluster stormCluster) {
        if (StringUtils.isEmpty(nameSpace)) {
            LOG.error("创建stormCluster时tenantName为空");
            return false;
        }
        if (null == stormCluster) {
            LOG.error("创建stormCluster时stormCluster为空");
            return false;
        }
        try {
            StormCluster oldCluster = componentOperationsClientUtil.getStormCluster(nameSpace,
                    stormCluster.getMetadata().getName());
            if (null == oldCluster) {
                StormCluster newCluster = ComponentClientFactory.getStormClient().inNamespace(nameSpace)
                        .create(stormCluster);
                LOG.info("storm集群创建后返回的cluster：" + JSON.toJSONString(newCluster));
                if (null != newCluster) {
                    return true;
                }
                return false;
            } else {
                LOG.error("storm集群已经存在,租户:" + stormCluster.getMetadata().getNamespace() + ",集群名称:"
                        + stormCluster.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("storm集群创建失败", e);
            return false;
        }
    }

    /**
     * 删除storm集群
     * 
     * @param nameSpace
     * @param clusterName
     * @return
     */
    protected boolean deleteStormCluster(String tenantName, String clusterName) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return false;
        }
        boolean result = false;
        try {
            StormCluster stormCluster = ComponentClientFactory.getStormClient().inNamespace(tenantName)
                    .withName(clusterName).get();
            if (null == stormCluster) {
                return true;
            }
            result = ComponentClientFactory.getStormClient().inNamespace(tenantName).withName(clusterName).delete();
            LOG.info("StormCluster删除result:" + result + "，clusterName：" + clusterName);
        } catch (Exception e) {
            LOG.error("storm删除失败，clusterName：" + clusterName, e);
            return false;
        }
        return result;
    }

    /**
     * 
     * @param tenantName
     * @param stormCluster
     * @return
     */
    protected boolean updateAndRetry(String tenantName, StormCluster stormCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的stormCluster：" + JSON.toJSONString(stormCluster));
        if (null != stormCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = componentOperationsClientUtil.updateStormCluster(tenantName, stormCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("修改storm集群" + stormCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        } else {
            LOG.error("stormCluster为null");
        }
        return false;
    }

    /**
     * 修改stormCluster的yaml 集群启动，停止 节点启动，停止
     * 
     * @param tenantName
     * @param serviceId
     * @return
     */
    protected StormCluster updateYamlForStormCluster(String tenantName, String serviceName, String opt, String nodeName,
            int supervisorAddNum) {
        StormCluster stormCluster = null;
        stormCluster = componentOperationsClientUtil.getStormCluster(tenantName, serviceName);
        if (null == stormCluster) {
            LOG.error("获取stormCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }

        boolean clusterIsFailed = false;
        if (CommonConst.STATE_CLUSTER_FAILED.equalsIgnoreCase(stormCluster.getStatus().getPhase())) {
            clusterIsFailed = true;
        }

        boolean nodeIsFailed = false;
        boolean hasNode = false;
        if (StringUtils.isNotEmpty(nodeName)) {
            Map<String, StormNode> serverNodes = stormCluster.getStatus().getServerNodes();
            if (serverNodes.containsKey(nodeName)) {
                hasNode = true;
                nodeIsFailed = CommonConst.STATE_NODE_FAILED.equalsIgnoreCase(serverNodes.get(nodeName).getStatus());
            }
        }

        switch (opt) {
        case StormClusterConst.OPERATOR_CLUSTER_START:
        case StormClusterConst.OPERATOR_CLUSTER_STOP:
            stormCluster.getSpec().getStormOp().setOperator(opt);
            stormCluster.getSpec().setOpFailed(clusterIsFailed);
            break;
        case StormClusterConst.OPERATOR_CLUSTER_EXPAND:
            if (supervisorAddNum != 0) {
                Map<String, Integer> roleReplicas = stormCluster.getSpec().getReplicas();
                int oldSupervisorNum = 0;
                if (roleReplicas.containsKey(StormClusterConst.ROLE_SUPERVISOR)) {
                    oldSupervisorNum = roleReplicas.get(StormClusterConst.ROLE_SUPERVISOR);
                    roleReplicas.put(StormClusterConst.ROLE_SUPERVISOR, oldSupervisorNum + supervisorAddNum);
                } else {
                    roleReplicas.put(StormClusterConst.ROLE_SUPERVISOR, supervisorAddNum);
                }
                stormCluster.getSpec().setReplicas(roleReplicas);
                stormCluster.getSpec().getStormOp().setOperator(StormClusterConst.OPERATOR_CLUSTER_EXPAND);
                stormCluster.getSpec().setOpFailed(clusterIsFailed);
            }
            break;
        case StormClusterConst.OPERATOR_NODE_START:
        case StormClusterConst.OPERATOR_NODE_STOP:
            if (hasNode) {
                stormCluster.getSpec().getStormOp().setOperator(opt);
                stormCluster.getSpec().getStormOp().setNodeName(nodeName);
                stormCluster.getSpec().setOpFailed(nodeIsFailed);
            }
            break;
        case StormClusterConst.OPERATOR_NODE_DELETE:
            if (hasNode) {
                stormCluster.getSpec().getStormOp().setOperator(StormClusterConst.OPERATOR_NODE_DELETE);
                stormCluster.getSpec().getStormOp().setNodeName(nodeName);
                // 只删除supervisor节点
                Map<String, Integer> roleReplicas = stormCluster.getSpec().getReplicas();
                int supervisorNumOld = roleReplicas.get(StormClusterConst.ROLE_SUPERVISOR);
                int supervisorNumNew = supervisorNumOld - 1 >= 0 ? supervisorNumOld - 1 : 0;
                roleReplicas.put(StormClusterConst.ROLE_SUPERVISOR, supervisorNumNew);
                stormCluster.getSpec().setReplicas(roleReplicas);
                stormCluster.getSpec().setOpFailed(nodeIsFailed);
            }
            break;
        default:
            break;
        }
        return stormCluster;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        String returnStatus = null;
        StormCluster cluster = componentOperationsClientUtil.getStormCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getServerNodes() && !cluster.getStatus().getServerNodes().isEmpty()) {
                for (Map.Entry<String, StormNode> entry : cluster.getStatus().getServerNodes().entrySet()) {
                    if (nodeName.equals(entry.getKey())) {
                        return entry.getValue().getStatus();
                    }
                }
            }
        }
        return returnStatus;
    }

    /**
     * 删除集群
     * 
     * @param tenantName
     * @param mysqlCluster
     * @return
     */
    protected boolean deleteAndRetry(String tenantName, String serviceName) {
        LOG.info("==========开始删除集群==========");
        LOG.info("删除集群的tenantName：" + tenantName + ",serviceName:" + JSON.toJSONString(serviceName));
        StormCluster stormCluster = componentOperationsClientUtil.getStormCluster(tenantName, serviceName);
        if (null == stormCluster) {
            LOG.info("stormCluster已经不存在");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteStormCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除storm集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    /**
     * 获取集群扩展字段
     * 
     * @param stormCluster
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(StormCluster newStormCluster) {
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != newStormCluster && null != newStormCluster.getStatus()) {
            boolean resourceEffective = !newStormCluster.getStatus().isResourceUpdateNeedRestart();
            boolean configEffective = !newStormCluster.getStatus().isParameterUpdateNeedRestart();
            serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, String.valueOf(resourceEffective));
            serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, String.valueOf(configEffective));
        }
        return serviceExtendedField;
    }

    /**
     * storm节点其他信息
     * 
     * @param stormCluster
     * @return
     */
    protected Map<String, Map<String, String>> buildNodesExtendedField(StormCluster stormCluster) {
        Map<String, Map<String, String>> nodesExtendedField = new HashMap<>();
        if (null != stormCluster && null != stormCluster.getStatus()
                && null != stormCluster.getStatus().getServerNodes()) {
            Map<String, StormNode> serverNodes = stormCluster.getStatus().getServerNodes();
            for (Map.Entry<String, StormNode> entry : serverNodes.entrySet()) {
                Map<String, String> nodeMap = new HashMap<>();
                if (StringUtils.isNotEmpty(entry.getValue().getHostip())) {
                    nodeMap.put("ip", entry.getValue().getHostip());
                }

                if (StormClusterConst.ROLE_NIMBUS.equals(entry.getValue().getRole())
                        && 0 != entry.getValue().getNodeExterport()) {
                    nodeMap.put("port", String.valueOf(entry.getValue().getNodeExterport()));
                }

                if (!nodeMap.isEmpty()) {
                    nodesExtendedField.put(entry.getKey(), nodeMap);
                }
            }
        }
        return nodesExtendedField;
    }

    /**
     * 注册lvm信息
     * 
     * @param tenantName
     * @param stormCluster
     */
    protected void registeClusterLvm(String tenantName, StormCluster stormCluster) {
        LOG.info("创建lvm,接收的参数=>tenantName:" + tenantName + ",stormCluster:" + JSON.toJSONString(stormCluster));
        for (Map.Entry<String, StormNode> entry : stormCluster.getStatus().getServerNodes().entrySet()) {
            if (CommonConst.STATE_NODE_RUNNING.equals(entry.getValue().getStatus())
                    && (StormClusterConst.ROLE_NIMBUS).equals(entry.getValue().getRole())) {
                componentOperationsClientUtil.registerLvm(tenantName, entry.getValue().getVolumeid(),
                        entry.getValue().getHostip(), stormCluster.getSpec().getCapacity());
            }
        }
    }

}
