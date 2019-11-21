package com.xxx.xcloud.module.component.service.worker.kafka;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.KafkaClusterConst;
import com.xxx.xcloud.module.component.model.kafka.KafkaCluster;
import com.xxx.xcloud.module.component.model.kafka.KafkaNode;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BaseKafkaClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseKafkaClusterWorker.class);

    /**
     * 创建kafka集群
     * 
     * @param nameSpace
     * @param kafkaCluster
     * @return
     */
    protected boolean createKafkaCluster(String nameSpace, KafkaCluster kafkaCluster) {
        if (StringUtils.isEmpty(nameSpace)) {
            LOG.error("创建kafkaCluster时tenantName为空");
            return false;
        }
        if (null == kafkaCluster) {
            LOG.error("创建kafkaCluster时kafkaCluster为空");
            return false;
        }
        try {
            KafkaCluster oldCluster = componentOperationsClientUtil.getKafkaCluster(nameSpace,
                    kafkaCluster.getMetadata().getName());
            if (null == oldCluster) {
                KafkaCluster newCluster = ComponentClientFactory.getKafkaClient().inNamespace(nameSpace)
                        .create(kafkaCluster);
                LOG.info("kafka集群创建后返回的cluster：" + JSON.toJSONString(newCluster));
                if (null != newCluster) {
                    return true;
                }
                return false;
            } else {
                LOG.error("kafka集群已经存在,租户:" + kafkaCluster.getMetadata().getNamespace() + ",集群名称:"
                        + kafkaCluster.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("kafka集群创建失败", e);
            return false;
        }
    }

    /**
     * 删除kafka集群
     * 
     * @param nameSpace
     * @param clusterName
     * @return
     */
    protected boolean deleteKafkaCluster(String tenantName, String clusterName) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return false;
        }
        boolean result = false;
        try {
            KafkaCluster kafkaCluster = ComponentClientFactory.getKafkaClient().inNamespace(tenantName)
                    .withName(clusterName).get();
            if (null == kafkaCluster) {
                return true;
            }
            result = ComponentClientFactory.getKafkaClient().inNamespace(tenantName).withName(clusterName).delete();
            LOG.info("KafkaCluster删除result:" + result + "，clusterName：" + clusterName);
        } catch (Exception e) {
            LOG.error("kafka删除失败，clusterName：" + clusterName, e);
            return false;
        }
        return result;
    }

    /**
     * 
     * @param tenantName
     * @param kafkaCluster
     * @return
     */
    protected boolean updateAndRetry(String tenantName, KafkaCluster kafkaCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的kafkaCluster：" + JSON.toJSONString(kafkaCluster));
        if (null != kafkaCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = componentOperationsClientUtil.updateKafkaCluster(tenantName, kafkaCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("修改kafka集群" + kafkaCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        } else {
            LOG.error("kafkaCluster为null");
        }
        return false;
    }

    /**
     * 修改kafkaCluster的yaml 集群启动，停止 节点启动，停止
     * 
     * @param tenantName
     * @param serviceName
     * @param opt
     * @param nodeName
     * @param addNum
     * @return
     */
    protected KafkaCluster updateYamlForKafkaCluster(String tenantName, String serviceName, String opt, String nodeName,
            int addNum) {
        LOG.info("修改kafka集群，接收的参数=>tenantName:" + tenantName + ",serviceName:" + serviceName + ",opt:" + opt
                + ",nodeName:" + nodeName + ",addNum:" + addNum);
        KafkaCluster kafkaCluster = null;
        kafkaCluster = componentOperationsClientUtil.getKafkaCluster(tenantName, serviceName);
        if (null == kafkaCluster) {
            LOG.error("获取kafkaCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        switch (opt) {
        case KafkaClusterConst.OPERATOR_CLUSTER_START:
        case KafkaClusterConst.OPERATOR_CLUSTER_STOP:
            kafkaCluster.getSpec().getKafkaop().setOperator(opt);
            kafkaCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            break;
        case KafkaClusterConst.OPERATOR_CLUSTER_EXPAND:
            if (addNum != 0) {
                kafkaCluster.getSpec().setReplicas(addNum + kafkaCluster.getSpec().getReplicas());
                kafkaCluster.getSpec().getKafkaop().setOperator(KafkaClusterConst.OPERATOR_CLUSTER_EXPAND);
                kafkaCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            }
            break;
        case KafkaClusterConst.OPERATOR_NODE_START:
        case KafkaClusterConst.OPERATOR_NODE_STOP:
            kafkaCluster.getSpec().getKafkaop().setOperator(opt);
            kafkaCluster.getSpec().getKafkaop().setNodename(nodeName);
            kafkaCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            break;
        case KafkaClusterConst.OPERATOR_NODE_DELETE:
            kafkaCluster.getSpec().getKafkaop().setOperator(KafkaClusterConst.OPERATOR_NODE_DELETE);
            kafkaCluster.getSpec().getKafkaop().setNodename(nodeName);
            kafkaCluster.getSpec().setReplicas(kafkaCluster.getSpec().getReplicas() - 1);
            kafkaCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            break;
        default:
            break;
        }
        return kafkaCluster;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        String returnStatus = null;
        KafkaCluster cluster = componentOperationsClientUtil.getKafkaCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getServerNodes() && !cluster.getStatus().getServerNodes().isEmpty()) {
                for (Map.Entry<String, KafkaNode> entry : cluster.getStatus().getServerNodes().entrySet()) {
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
        KafkaCluster kafkaCluster = componentOperationsClientUtil.getKafkaCluster(tenantName, serviceName);
        if (null == kafkaCluster) {
            LOG.info("kafkaCluster已经不存在");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteKafkaCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除mysql集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    /**
     * 获取集群扩展字段
     * 
     * @param kafkaCluster
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(KafkaCluster kafkaCluster) {
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != kafkaCluster && null != kafkaCluster.getStatus()) {
            boolean resourceEffective = !kafkaCluster.getStatus().isResourceupdateneedrestart();
            boolean configEffective = !kafkaCluster.getStatus().isParameterupdateneedrestart();
            serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, String.valueOf(resourceEffective));
            serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, String.valueOf(configEffective));
        }
        return serviceExtendedField;
    }

    /**
     * 生成节点扩展字段
     * 
     * @param kafkaCluster
     * @return
     */
    protected Map<String, Map<String, String>> buildNodesExtendedField(KafkaCluster kafkaCluster) {
        Map<String, Map<String, String>> nodesExtendedField = new HashMap<>();
        if (null != kafkaCluster && null != kafkaCluster.getStatus()
                && null != kafkaCluster.getStatus().getServerNodes()) {
            Map<String, KafkaNode> serverNodes = kafkaCluster.getStatus().getServerNodes();
            for (Map.Entry<String, KafkaNode> entry : serverNodes.entrySet()) {
                Map<String, String> nodeMap = new HashMap<>();
                if (StringUtils.isNotEmpty(entry.getValue().getHostip())) {
                    nodeMap.put("ip", entry.getValue().getHostip());
                }
                if (0 != entry.getValue().getNodeport()) {
                    nodeMap.put("port", String.valueOf(entry.getValue().getNodeport()));
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
    protected void registeClusterLvm(String tenantName, KafkaCluster kafkaCluster) {
        LOG.info("创建lvm,接收的参数=>tenantName:" + tenantName + ",kafkaCluster:" + JSON.toJSONString(kafkaCluster));
        if (null != kafkaCluster && null != kafkaCluster.getStatus()
                && null != kafkaCluster.getStatus().getServerNodes()) {
            for (Map.Entry<String, KafkaNode> entry : kafkaCluster.getStatus().getServerNodes().entrySet()) {
                if (CommonConst.STATE_NODE_RUNNING.equals(entry.getValue().getStatus())) {
                    componentOperationsClientUtil.registerLvm(tenantName, entry.getValue().getVolumeid(),
                            entry.getValue().getHostip(), kafkaCluster.getSpec().getCapacity());
                }
            }
        }
    }
}
