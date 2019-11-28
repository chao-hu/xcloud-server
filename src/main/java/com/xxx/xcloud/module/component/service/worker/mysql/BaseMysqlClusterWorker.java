package com.xxx.xcloud.module.component.service.worker.mysql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.backup.entity.Job;
import com.xxx.xcloud.module.backup.entity.JobHistory;
import com.xxx.xcloud.module.backup.repository.JobHistoryRepository;
import com.xxx.xcloud.module.backup.repository.JobRepository;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlBackupConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlServer;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.module.component.util.EtcdUtil;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BaseMysqlClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseMysqlClusterWorker.class);

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobHistoryRepository jobHistoryRepository;

    /**
     * 创建mysql集群
     * 
     * @param nameSpace
     * @param mysqlCluster
     * @return
     */
    protected boolean createMysqlCluster(String nameSpace, MysqlCluster mysqlCluster) {
        if (StringUtils.isEmpty(nameSpace)) {
            LOG.error("创建mysqlCluster时tenantName为空");
            return false;
        }
        if (null == mysqlCluster) {
            LOG.error("创建mysqlCluster时mysqlCluster为空");
            return false;
        }
        try {
            MysqlCluster oldCluster = componentOperationsClientUtil.getMysqlCluster(nameSpace,
                    mysqlCluster.getMetadata().getName());
            if (null == oldCluster) {
                MysqlCluster newCluster = ComponentClientFactory.getMysqlClient().inNamespace(nameSpace)
                        .create(mysqlCluster);
                LOG.info("mysql集群创建后返回的cluster：" + JSON.toJSONString(newCluster));
                if (null != newCluster) {
                    return true;
                }
                return false;
            } else {
                LOG.error("mysql集群已经存在,租户:" + mysqlCluster.getMetadata().getNamespace() + ",集群名称:"
                        + mysqlCluster.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("mysql集群创建失败", e);
            return false;
        }
    }

    /**
     * 删除mysql集群
     * 
     * @param nameSpace
     * @param clusterName
     * @return
     */
    protected boolean deleteMysqlCluster(String tenantName, String clusterName) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return false;
        }
        boolean result = false;
        try {
            MysqlCluster mysqlCluster = ComponentClientFactory.getMysqlClient().inNamespace(tenantName)
                    .withName(clusterName).get();
            if (null == mysqlCluster) {
                return true;
            }
            result = ComponentClientFactory.getMysqlClient().inNamespace(tenantName).withName(clusterName).delete();
            LOG.info("MysqlCluster删除result:" + result + "，clusterName：" + clusterName);
        } catch (Exception e) {
            LOG.error("mysql删除失败，clusterName：" + clusterName, e);
            return false;
        }
        return result;
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
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);
        if (null == mysqlCluster) {
            LOG.info("mysqlcluster已经不存在");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteMysqlCluster(tenantName, serviceName);
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
     * 
     * @param tenantName
     * @param mysqlCluster
     * @return
     */
    protected boolean updateAndRetry(String tenantName, MysqlCluster mysqlCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的mysqlCluster：" + JSON.toJSONString(mysqlCluster));
        if (null != mysqlCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = componentOperationsClientUtil.updateMysqlCluster(tenantName, mysqlCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("修改mysql集群" + mysqlCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        } else {
            LOG.error("mysqlcluster为null");
        }
        return false;
    }

    /**
     * 删除集群时删除备份数据
     * 
     * @param service
     */
    protected void deleteBackUp(StatefulService service) {
        // 删除job
        List<Job> jobList = null;
        try {
            jobList = jobRepository.findByServiceId(service.getId());
            if (null != jobList && !jobList.isEmpty()) {
                for (Job job : jobList) {
                    job.setStatus(MysqlBackupConst.JOB_STATUS_DETETED);
                    jobRepository.save(job);
                }
            }
        } catch (Exception e) {
            LOG.error("删除集群时删除job异常！", e);
        }

        // 删除jobHistory
        List<JobHistory> jobHistoryList = null;
        try {
            jobHistoryList = jobHistoryRepository.findByServiceId(service.getId());
            if (null != jobHistoryList && !jobHistoryList.isEmpty()) {
                for (JobHistory jobHistory : jobHistoryList) {
                    jobHistory.setStatus(MysqlBackupConst.JOB_HISTORY_STATUS_DETETED);
                    jobHistoryRepository.save(jobHistory);
                }
            }
        } catch (Exception e) {
            LOG.error("删除集群时删除job异常！", e);
        }

        // 删除etcd数据目录
        if (null != jobList && !jobList.isEmpty()) {
            for (Job job : jobList) {
                if (null != job) {
                    String key = "/backup/" + service.getNamespace() + "/" + job.getNodeName() + "/" + job.getId();
                    EtcdUtil.deleteEtcdValueByKey(key);
                }
            }
        }
    }

    /**
     * 删除节点时删除备份数据
     * 
     * @param service
     * @param nodeId
     */
    protected void deleteBackUp(String tenantName, String nodeId) {
        List<Job> jobList = null;
        try {
            jobList = jobRepository.findByNodeId(nodeId);
            if (null != jobList && !jobList.isEmpty()) {
                for (Job job : jobList) {
                    job.setStatus(MysqlBackupConst.JOB_STATUS_DETETED);
                    jobRepository.save(job);
                }
            }
        } catch (Exception e) {
            LOG.error("删除节点时删除job异常！", e);
        }

        // 2、删除jobHistory
        List<JobHistory> jobHistoryList = null;
        try {
            jobHistoryList = jobHistoryRepository.findByNodeId(nodeId);
            if (null != jobHistoryList && !jobHistoryList.isEmpty()) {
                for (JobHistory jobHistory : jobHistoryList) {
                    jobHistory.setStatus(MysqlBackupConst.JOB_HISTORY_STATUS_DETETED);
                    jobHistoryRepository.save(jobHistory);
                }
            }
        } catch (Exception e) {
            LOG.error("删除节点时删除job异常！", e);
        }

        // 3、删除etcd数据
        if (null != jobList && !jobList.isEmpty()) {
            for (Job job : jobList) {
                if (null != job) {
                    String key = "/backup/" + tenantName + "/" + job.getNodeName() + "/" + job.getId();
                    EtcdUtil.deleteEtcdValueByKey(key);
                }
            }
        }

    }

    /**
     * 修改mysqlCluster的yaml 集群启动，停止 节点启动，停止
     * 
     * @param tenantName
     * @param serviceId
     * @return
     */
    protected MysqlCluster updateYamlForMysqlCluster(String tenantName, String serviceName, String opt, String nodeName,
            JSONObject jsonObject) {
        LOG.info("修改mysql集群，接收的参数=>tenantName:" + tenantName + ",serviceName:" + serviceName + ",opt:" + opt
                + ",nodeName:" + nodeName + ",jsonObject:" + jsonObject);
        MysqlCluster mysqlCluster = null;
        mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);
        if (null == mysqlCluster) {
            LOG.error("获取mysqlCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        switch (opt) {
        case MysqlClusterConst.OPERATOR_CLUSTER_START:
            // 节点启动或集群启动
            if (StringUtils.isNotEmpty(nodeName)) {
                mysqlCluster.getSpec().getNodeop().setOperator(MysqlClusterConst.OPERATOR_NODE_START);
                mysqlCluster.getSpec().getNodeop().setNodename(nodeName);
            } else {
                mysqlCluster.getSpec().getClusterop().setOperator(MysqlClusterConst.OPERATOR_CLUSTER_START);
            }
            break;
        case MysqlClusterConst.OPERATOR_CLUSTER_STOP:
            // 节点停止或集群停止
            if (StringUtils.isNotEmpty(nodeName)) {
                mysqlCluster.getSpec().getNodeop().setOperator(MysqlClusterConst.OPERATOR_NODE_STOP);
                mysqlCluster.getSpec().getNodeop().setNodename(nodeName);
            } else {
                mysqlCluster.getSpec().getClusterop().setOperator(MysqlClusterConst.OPERATOR_CLUSTER_STOP);
            }
            break;
        case MysqlClusterConst.OPERATOR_CLUSTER_EXPAND:
            Integer addNum = jsonObject.getInteger("addNum");
            if (addNum != 0) {
                String masterName = null;
                LOG.info("增加节点时的参数jsonObject:" + JSON.toJSONString(jsonObject));
                if (null != mysqlCluster.getSpec()
                        && MysqlClusterConst.TYPE_MM.equals(mysqlCluster.getSpec().getType())) {
                    masterName = jsonObject.getString("masterName");
                    LOG.info("增加节点指定的masterName：" + masterName);
                    if (StringUtils.isNotEmpty(masterName)) {
                        mysqlCluster.getSpec().getClusterop().setMaster(masterName);
                    }
                }
                mysqlCluster.getSpec().getClusterop().setOperator(MysqlClusterConst.OPERATOR_CLUSTER_EXPAND);
                mysqlCluster.getSpec().setReplicas(addNum + mysqlCluster.getSpec().getReplicas());
            }
            break;
        case MysqlClusterConst.OPERATOR_NODE_DELETE:
            if (StringUtils.isNotEmpty(nodeName)) {
                mysqlCluster.getSpec().getNodeop().setOperator(MysqlClusterConst.OPERATOR_NODE_DELETE);
                mysqlCluster.getSpec().getNodeop().setNodename(nodeName);
                mysqlCluster.getSpec().setReplicas(mysqlCluster.getSpec().getReplicas() - 1);
            }
            break;
        default:
            break;
        }

        return mysqlCluster;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        String returnStatus = null;
        MysqlCluster cluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getServerNodes() && !cluster.getStatus().getServerNodes().isEmpty()) {
                for (Map.Entry<String, MysqlServer> entry : cluster.getStatus().getServerNodes().entrySet()) {
                    if (nodeName.equals(entry.getKey())) {
                        return entry.getValue().getStatus();
                    }
                }
            }
        }
        return returnStatus;
    }

    /**
     * 获取集群扩展字段
     * 
     * @param mysqlCluster
     * @return
     */
    protected Map<String, String> buildServiceExtendedField(MysqlCluster mysqlCluster) {
        Map<String, String> serviceExtendedField = new HashMap<>();
        if (null != mysqlCluster) {
            if (null != mysqlCluster.getStatus()) {
                boolean resourceEffective = !mysqlCluster.getStatus().isResourceupdateneedrestart();
                boolean configEffective = !mysqlCluster.getStatus().isParameterupdateneedrestart();
                serviceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, String.valueOf(resourceEffective));
                serviceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, String.valueOf(configEffective));
                if (null != mysqlCluster.getStatus().getPhpMyAdmin()) {
                    String phpMyadminHost = mysqlCluster.getStatus().getPhpMyAdmin().getPhpMyAdminExterHost();
                    int phpMyadminPort = mysqlCluster.getStatus().getPhpMyAdmin().getPhpMyAdminExterPort();
                    if (StringUtils.isNotEmpty(phpMyadminHost)) {
                        serviceExtendedField.put("phpMyadminHost", phpMyadminHost);
                    }
                    if (StringUtils.isNotEmpty(String.valueOf(phpMyadminPort)) && 0 != phpMyadminPort) {
                        serviceExtendedField.put("phpMyadminPort", String.valueOf(phpMyadminPort));
                    }
                }
            }
            if (null != mysqlCluster.getSpec() && null != mysqlCluster.getSpec().getConfig()) {
                String replUser = mysqlCluster.getSpec().getConfig().getRepluser();
                String replPassword = mysqlCluster.getSpec().getConfig().getReplpassword();
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
     * 获取节点ip，端口
     * 
     * @param mysqlCluster
     */
    protected Map<String, Map<String, String>> buildNodesExtendedField(MysqlCluster mysqlCluster) {
        Map<String, Map<String, String>> nodesExtendedField = new HashMap<>();
        Map<String, MysqlServer> serverNodes = mysqlCluster.getStatus().getServerNodes();
        if (null != mysqlCluster && null != mysqlCluster.getStatus()
                && null != mysqlCluster.getStatus().getServerNodes()) {
            for (Map.Entry<String, MysqlServer> entry : serverNodes.entrySet()) {
                Map<String, String> nodeMap = new HashMap<>();
                if (StringUtils.isNotEmpty(entry.getValue().getNodeIP())) {
                    nodeMap.put("ip", entry.getValue().getNodeIP());
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
    protected void registeClusterLvm(String tenantName, MysqlCluster mysqlCluster) {
        LOG.info("创建lvm,接收的参数=>tenantName:" + tenantName + ",mysqlCluster:" + JSON.toJSONString(mysqlCluster));
        if (null != mysqlCluster && null != mysqlCluster.getStatus()
                && null != mysqlCluster.getStatus().getServerNodes()) {
            for (Map.Entry<String, MysqlServer> entry : mysqlCluster.getStatus().getServerNodes().entrySet()) {
                if (CommonConst.STATE_NODE_RUNNING.equals(entry.getValue().getStatus())) {
                    componentOperationsClientUtil.registerLvm(tenantName, entry.getValue().getVolumeid(),
                            entry.getValue().getNodeIP(), mysqlCluster.getSpec().getCapacity());
                }
            }
        }
    }
}
