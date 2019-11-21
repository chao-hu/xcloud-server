package com.xxx.xcloud.module.component.service.worker.postgresql;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlInstance;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BasePostgresqlClusterWorker extends BaseComponentServiceWorker {

    private static final Logger LOG = LoggerFactory.getLogger(BasePostgresqlClusterWorker.class);

    /**
     * 创建postgresql集群
     * 
     * @param nameSpace
     * @param postgresqlCluster
     * @return
     */
    protected boolean createPostgresqlCluster(String nameSpace, PostgresqlCluster postgresqlCluster) {
        if (StringUtils.isEmpty(nameSpace)) {
            LOG.error("创建postgresqlCluster时tenantName为空");
            return false;
        }
        if (null == postgresqlCluster) {
            LOG.error("创建postgresqlCluster时postgresqlCluster为空");
            return false;
        }
        try {
            PostgresqlCluster oldCluster = componentOperationsClientUtil.getPostgresqlCluster(nameSpace,
                    postgresqlCluster.getMetadata().getName());
            if (null == oldCluster) {
                PostgresqlCluster newCluster = ComponentClientFactory.getPostgresqlClient().inNamespace(nameSpace)
                        .create(postgresqlCluster);
                LOG.info("postgresql集群创建后返回的cluster：" + JSON.toJSONString(newCluster));
                if (null != newCluster) {
                    return true;
                }
                return false;
            } else {
                LOG.error("postgresql集群已经存在,租户:" + postgresqlCluster.getMetadata().getNamespace() + ",集群名称:"
                        + postgresqlCluster.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("postgresql集群创建失败", e);
            return false;
        }
    }

    /**
     * 删除postgresql集群
     * 
     * @param nameSpace
     * @param clusterName
     * @return
     */
    protected boolean deletePostgresqlCluster(String tenantName, String clusterName) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return false;
        }
        boolean result = false;
        try {
            PostgresqlCluster postgresqlCluster = ComponentClientFactory.getPostgresqlClient().inNamespace(tenantName)
                    .withName(clusterName).get();
            if (null == postgresqlCluster) {
                return true;
            }
            result = ComponentClientFactory.getPostgresqlClient().inNamespace(tenantName).withName(clusterName)
                    .delete();
            LOG.info("postgresqlCluster删除result:" + result + "，clusterName：" + clusterName);
        } catch (Exception e) {
            LOG.error("postgresqlCluster删除失败，clusterName：" + clusterName, e);
            return false;
        }
        return result;
    }

    /**
     * 修改集群并重试
     * 
     * @param tenantName
     * @param postgresqlCluster
     * @return
     */
    protected boolean updateAndRetry(String tenantName, PostgresqlCluster postgresqlCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的postgresqlCluster：" + JSON.toJSONString(postgresqlCluster));
        if (null != postgresqlCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = componentOperationsClientUtil.updatePostgresqlCluster(tenantName, postgresqlCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("修改postgresql集群" + postgresqlCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        } else {
            LOG.error("postgresqlCluster为null");
        }
        return false;
    }

    /**
     * 删除集群并重试
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    protected boolean deleteAndRetry(String tenantName, String serviceName) {
        LOG.info("==========开始删除集群==========");
        LOG.info("删除集群的tenantName：" + tenantName + ",serviceName:" + JSON.toJSONString(serviceName));
        PostgresqlCluster postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                serviceName);
        if (null == postgresqlCluster) {
            LOG.info("postgresqlCluster已经不存在");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deletePostgresqlCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除postgresql集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    /**
     * 修改集群yaml
     * 
     * @param tenantName
     * @param serviceName
     * @param opt
     * @param nodeName
     * @param jsonObject
     * @return
     */
    protected PostgresqlCluster updateYamlForPostgresqlCluster(String tenantName, String serviceName, String opt,
            String nodeName, int addNum) {
        LOG.info("修改postgresql集群，接收的参数=>tenantName:" + tenantName + ",serviceName:" + serviceName + ",opt:" + opt
                + ",nodeName:" + nodeName + ",addNum:" + addNum);
        PostgresqlCluster postgresqlCluster = null;
        postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName, serviceName);
        if (null == postgresqlCluster) {
            LOG.error("获取postgresqlCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        switch (opt) {
        case PostgresqlClusterConst.OPERATOR_CLUSTER_START:
        case PostgresqlClusterConst.OPERATOR_CLUSTER_STOP:
            postgresqlCluster.getSpec().setOpt(opt);
            break;
        case PostgresqlClusterConst.OPERATOR_NODE_START:
        case PostgresqlClusterConst.OPERATOR_NODE_STOP:
        case PostgresqlClusterConst.OPERATOR_NODE_DELETE:
            postgresqlCluster.getSpec().setOpt(opt);
            postgresqlCluster.getSpec().setOptNodename(nodeName);
            break;
        case PostgresqlClusterConst.OPERATOR_CLUSTER_EXPAND:
            postgresqlCluster.getSpec().setOpt(opt);
            postgresqlCluster.getSpec().setReplicas(postgresqlCluster.getSpec().getReplicas() + addNum);
            break;
        default:
            break;
        }
        return postgresqlCluster;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        String returnStatus = null;
        PostgresqlCluster cluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getInstances() && !cluster.getStatus().getInstances().isEmpty()) {
                for (Map.Entry<String, PostgresqlInstance> entry : cluster.getStatus().getInstances().entrySet()) {
                    if (nodeName.equals(entry.getKey())) {
                        return entry.getValue().getInstancePhase();
                    }
                }
            }
        }
        return returnStatus;
    }

    /**
     * 构造集群扩展字段
     * 
     * @param postgresqlCluster
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(PostgresqlCluster postgresqlCluster) {
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != postgresqlCluster) {
            if (null != postgresqlCluster.getStatus()) {
                String resourceEffectiveGet = postgresqlCluster.getStatus().getResourceupdateneedrestart();
                String configEffectiveGet = postgresqlCluster.getStatus().getParameterupdateneedrestart();
                String resourceEffective = "false".equals(resourceEffectiveGet) ? "true" : "false";
                String configEffective = "false".equals(configEffectiveGet) ? "true" : "false";
                serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, resourceEffective);
                serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, configEffective);
            }
            if (null != postgresqlCluster.getSpec()) {
                String replUser = postgresqlCluster.getSpec().getReplUser();
                String replPassword = postgresqlCluster.getSpec().getReplPassword();
                if (StringUtils.isNotEmpty(replUser)) {
                    serviceExtendedField.put("replUser", replUser);
                }
                if (StringUtils.isNotEmpty(replPassword)) {
                    serviceExtendedField.put("replPassword", replPassword);
                }
            }
        }
        return serviceExtendedField;
    }

    /**
     * 获取节点扩展字段
     * 
     * @param id
     * @param postgresqlCluster
     * @return
     */
    protected Map<String, Map<String, String>> buildNodesExtendedField(PostgresqlCluster postgresqlCluster) {
        Map<String, Map<String, String>> nodesExtendedField = new HashMap<>();
        if (null != postgresqlCluster && null != postgresqlCluster.getStatus()
                && null != postgresqlCluster.getStatus().getInstances()) {
            Map<String, PostgresqlInstance> serverNodes = postgresqlCluster.getStatus().getInstances();
            for (Map.Entry<String, PostgresqlInstance> entry : serverNodes.entrySet()) {
                Map<String, String> nodeMap = new HashMap<>();
                if (StringUtils.isNotEmpty(entry.getValue().getExterHost())) {
                    nodeMap.put("ip", entry.getValue().getExterHost());
                }

                if (0 != entry.getValue().getExterPort()) {
                    nodeMap.put("port", String.valueOf(entry.getValue().getExterPort()));
                }

                if (!nodeMap.isEmpty()) {
                    nodesExtendedField.put(entry.getKey(), nodeMap);
                }
            }
        }
        return nodesExtendedField;
    }

    /**
     * 集群注册lvm
     * 
     * @param tenantName
     * @param postgresqlCluster
     */
    protected void registeClusterLvm(String tenantName, PostgresqlCluster postgresqlCluster) {
        LOG.info(
                "创建lvm,接收的参数=>tenantName:" + tenantName + ",postgresqlCluster:" + JSON.toJSONString(postgresqlCluster));
        if (null != postgresqlCluster && null != postgresqlCluster.getStatus()
                && null != postgresqlCluster.getStatus().getInstances()) {
            for (Map.Entry<String, PostgresqlInstance> entry : postgresqlCluster.getStatus().getInstances()
                    .entrySet()) {
                if (CommonConst.STATE_NODE_RUNNING.equals(entry.getValue().getInstancePhase())) {
                    componentOperationsClientUtil.registerLvm(tenantName, entry.getValue().getLvName(),
                            entry.getValue().getExterHost(), postgresqlCluster.getSpec().getStorage());
                }
            }
        }
    }
}
