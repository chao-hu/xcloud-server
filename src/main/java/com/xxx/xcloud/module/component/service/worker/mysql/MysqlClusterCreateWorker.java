package com.xxx.xcloud.module.component.service.worker.mysql;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.mysql.MysqlBackup;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlClusterOp;
import com.xxx.xcloud.module.component.model.mysql.MysqlConfig;
import com.xxx.xcloud.module.component.model.mysql.MysqlServer;
import com.xxx.xcloud.module.component.model.mysql.MysqlSpec;
import com.xxx.xcloud.utils.StringUtils;

@Service
@Scope("prototype")
public class MysqlClusterCreateWorker extends BaseMysqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MysqlClusterCreateWorker.class);

    @Override
    public void execute() {

        LOG.info("===============MysqlClusterCreateWorker====================");
        // 1、获取数据
        String tenantName = data.get("tenantName");
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        String version = data.get("version");
        String type = data.get("type");
        String password = data.get("password");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));
        String configUpdated = data.get("configuration");
        String performance = data.get("performance");
        String healthCheck = data.get("healthCheck");
        String healthCheckConfiguration = data.get("healthCheckConfiguration");

        // 2、 获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (Exception e) {
            LOG.error("集群创建时获取service失败，error：" + e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群创建时获取的service：" + JSON.toJSONString(service));

        // 3、拼接mysqlCluster
        MysqlCluster mysqlCluster = buildMysqlCluster(tenantName, projectId, orderId, version, type, serviceName,
                password, cpu, memory, capacity, replicas, configUpdated, performance, healthCheck, healthCheckConfiguration);

        // 4、调用k8s client创建
        if (!createAndRetry(tenantName, mysqlCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.error("mysql集群" + serviceName + "创建失败！");
            return;
        }

        // 5、循环获取创建结果
        checkCreateResult(tenantName, service, cpu, memory, capacity);
    }

    /**
     * 创建集群：构建mysqlCluster
     * 
     * @param tenantName
     * @param projectId
     * @param version
     * @param type
     * @param serviceName
     * @param password
     * @param cpu
     * @param memory
     * @param capacity
     * @param replicas
     * @param jsonObject
     * @return
     */
    @SuppressWarnings("unchecked")
    private MysqlCluster buildMysqlCluster(String tenantName, String projectId, String orderId, String version,
            String type, String serviceName, String password, Double cpu, Double memory, Double capacity, int replicas,
            String configUpdated, String performance, String healthCheck, String healthCheckConfiguration) {

        MysqlCluster mysqlCluster = new MysqlCluster();
        mysqlCluster.getMetadata().setName(serviceName);
        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            mysqlCluster.getMetadata().setLabels(labels);
        }

        MysqlSpec spec = new MysqlSpec();
        MysqlClusterOp clusterOp = new MysqlClusterOp();
        clusterOp.setOperator(MysqlClusterConst.OPERATOR_CLUSTER_CREATE);
        spec.setClusterop(clusterOp);
        spec.setVersion(version);
        spec.setType(type);
        spec.setImage(getRepoPath(CommonConst.APPTYPE_MYSQL, null, version));
        spec.setExporterimage(getRepoPath(CommonConst.APPTYPE_MYSQL, CommonConst.EXPORTER, version));

        Resources backResources = componentOperationsClientUtil.getResources(
                MysqlClusterConst.MYSQL_BACKUP_CONTAINER_DEFAULT_CPU,
                MysqlClusterConst.MYSQL_BACKUP_CONTAINER_DEFAULT_MEMORY, CommonConst.UNIT_GI);
        LOG.info("构建backup Resources");
        MysqlBackup mysqlBackup = new MysqlBackup();
        mysqlBackup.setResources(backResources);
        mysqlBackup.setBackupimage(getRepoPath(CommonConst.APPTYPE_MYSQL, "backup", version));
        LOG.info("mysqlBackup：" + JSON.toJSONString(mysqlBackup));
        spec.setMysqlbackup(mysqlBackup);
        LOG.info("构建backup Resources完毕");
        if (StringUtils.isNotEmpty(healthCheck)) {
            spec.setHealthcheck(Boolean.parseBoolean(healthCheck));
        }else {
            spec.setHealthcheck(false);
        }

        LOG.info("spec:" + JSON.toJSONString(spec));
        MysqlConfig config = new MysqlConfig();
        config.setPassword(password);
        if (StringUtils.isNotEmpty(healthCheckConfiguration)) {
            Map<String, String> healthCheckConfigurationMap = JSON.parseObject(healthCheckConfiguration, Map.class);
            if (healthCheckConfigurationMap.containsKey(MysqlClusterConst.LIVENESS_DELAY_TIMEOUT)){
                config.setLivenessDelayTimeout(Integer.parseInt(healthCheckConfigurationMap.get(MysqlClusterConst.LIVENESS_DELAY_TIMEOUT)));
            }else {
                config.setLivenessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT);
            }
            
            if (healthCheckConfigurationMap.containsKey(MysqlClusterConst.LIVENESS_FAILURE_THRESHOLD)){
                config.setLivenessFailureThreshold(Integer.parseInt(healthCheckConfigurationMap.get(MysqlClusterConst.LIVENESS_FAILURE_THRESHOLD)));
            }else {
                config.setLivenessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD);
            }
            
            if (healthCheckConfigurationMap.containsKey(MysqlClusterConst.READINESS_DELAY_TIMEOUT)){
                config.setReadinessDelayTimeout(Integer.parseInt(healthCheckConfigurationMap.get(MysqlClusterConst.READINESS_DELAY_TIMEOUT)));
            }else {
                config.setReadinessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_READINESS_DELAY_TIMEOUT);
            }
            
            if (healthCheckConfigurationMap.containsKey(MysqlClusterConst.READINESS_FAILURE_THRESHOLD)){
                config.setReadinessFailureThreshold(Integer.parseInt(healthCheckConfigurationMap.get(MysqlClusterConst.READINESS_FAILURE_THRESHOLD)));
            }else {
                config.setReadinessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_READINESS_FAILURE_THRESHOLD);
            }
        } else {
            config.setLivenessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT);
            config.setLivenessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD);
            config.setReadinessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_READINESS_DELAY_TIMEOUT);
            config.setReadinessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_READINESS_FAILURE_THRESHOLD);
        }
        LOG.info("config:" + JSON.toJSONString(config));

        MysqlConfig buildConfig = buildConfig(configUpdated, config, version);
        spec.setConfig(buildConfig);

        LOG.info("buildConfig完成,spec：" + JSON.toJSONString(spec));
        spec.setReplicas(replicas);
        spec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));

        spec.setCapacity(capacity + CommonConst.UNIT_GI);
        LOG.info("创建mysql配置文件获取到的vgname:" + XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        spec.setVolume(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));

        String phpMyadminImage = getRepoPath(CommonConst.APPTYPE_MYSQL, "phpmyadmin", version);
        LOG.info("创建mysql时的phpMyAdminimage为:" + phpMyadminImage);
        spec.setPhpMyAdminimage(phpMyadminImage);
        LOG.info("build的mysqlcluster spec:" + JSON.toJSONString(spec));

        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_MYSQL);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        boolean nodeSelectorPerformance = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.NODESELECTOR_PERFORMANCE));
        if (StringUtils.isNotEmpty(performance) && nodeSelectorPerformance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        
        if (!nodeSelector.isEmpty()) {
            spec.setNodeSelector(nodeSelector);
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            spec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        mysqlCluster.setSpec(spec);

        LOG.info("创建拼接的mysqlCluster：" + JSON.toJSONString(mysqlCluster));
        return mysqlCluster;
    }

    /**
     * 
     * 
     * @param jsonObject
     * @param config
     * @return
     */
    public MysqlConfig buildConfig(String configUpdated, MysqlConfig config, String version) {
        if (null == configUpdated || StringUtils.isEmpty(configUpdated)) {
            LOG.info("创建时没有修改参数");
            return config;
        }
        LOG.info("configUpdated" + configUpdated);
        JSONObject configUpdatedJson = JSON.parseObject(configUpdated);
        if (null != configUpdatedJson && !configUpdatedJson.isEmpty()) {
            Map<String, String> changedUnitMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configUpdatedJson,
                    CommonConst.APPTYPE_MYSQL, version);
            config.setMycnf(changedUnitMap);
        }

        return config;
    }

    /**
     * 
     * 
     * @param tenantName
     * @param mysqlCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, MysqlCluster mysqlCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建mysqlCluster：" + JSON.toJSONString(mysqlCluster));
        if (null != mysqlCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = createMysqlCluster(tenantName, mysqlCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("创建mysql集群" + mysqlCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检查集群创建结果
     * 
     * @param nameSpace
     * @param serviceName
     * @param service
     * @return
     */
    private void checkCreateResult(String tenantName, StatefulService service, Double cpu, Double memory,
            Double capacity) {
        long start = System.currentTimeMillis();
        boolean hasNodes = false;
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时：" + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                break;
            }
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("创建超时，service：" + JSON.toJSONString(service));
                break;
            }
            MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName,
                    service.getServiceName());
            LOG.info("判断mysqlCluster中数据是否存在");
            LOG.info("mysqlCluster:" + JSON.toJSONString(mysqlCluster));
            LOG.info("status:" + mysqlCluster.getStatus());
            if (null != mysqlCluster && null != mysqlCluster.getStatus()
                    && null != mysqlCluster.getStatus().getServerNodes()
                    && null != mysqlCluster.getStatus().getServerNodes().keySet() && !hasNodes
                    && service.getNodeNum() <= mysqlCluster.getStatus().getServerNodes().keySet().size()) {
                if (buildNodes(mysqlCluster, service, cpu, memory, capacity)) {
                    LOG.info("节点表插入数据成功,集群名称：" + mysqlCluster.getMetadata().getName());
                    hasNodes = true;
                }
            }
            if (null != mysqlCluster && null != mysqlCluster.getStatus()) {
                LOG.info("cluster此时的状态：" + mysqlCluster.getStatus().getPhase());
            }
            if (hasNodes && null != mysqlCluster && null != mysqlCluster.getStatus()
                    && null != mysqlCluster.getStatus().getServerNodes()
                    && null != mysqlCluster.getStatus().getServerNodes().keySet()
                    && service.getNodeNum() <= mysqlCluster.getStatus().getServerNodes().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(mysqlCluster.getStatus().getPhase())) {
                if (StringUtils.isNotEmpty(mysqlCluster.getSpec().getPhpMyAdminimage())) {
                    if (null != mysqlCluster.getStatus().getPhpMyAdmin() && CommonConst.STATE_NODE_RUNNING
                            .equals(mysqlCluster.getStatus().getPhpMyAdmin().getServerStatus())) {
                        LOG.info("含phpmyadmin的mysql集群创建成功，tenantName:" + tenantName + ",serviceName:"
                                + service.getServiceName());
                        break;
                    }
                } else {
                    LOG.info("mysql集群创建成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                    break;
                }
            }
        }

        // 检查结束
        optClusterCreate(tenantName, service.getId(), service.getServiceName(), hasNodes);

    }

    /**
     * 创建waiting的节点
     * 
     * @param cluster
     * @param service
     * @return
     */
    private boolean buildNodes(MysqlCluster cluster, StatefulService service, Double cpu, Double memory,
            Double storage) {

        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");

        StatefulNode node = null;
        for (Map.Entry<String, MysqlServer> entry : cluster.getStatus().getServerNodes().entrySet()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(service.getId(),
                        entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                node.setCpu(cpu);
                node.setMemory(memory);
                node.setStorage(storage);

                node.setAppType(CommonConst.APPTYPE_MYSQL);
                node.setServiceId(service.getId());
                node.setServiceName(service.getServiceName());

                node.setNodeName(entry.getKey());
                node.setRole(entry.getValue().getRole());
                node.setLvmName(entry.getValue().getVolumeid());

                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());

                statefulNodeRepository.save(node);
            } catch (Exception e) {
                LOG.error("保存节点记录信息失败,data:" + JSON.toJSONString(node) + ",error:", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 处理集群创建
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optClusterCreate(String tenantName, String serviceId, String serviceName, boolean hasNodes) {
        // 获取mysqlCluster
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);

        if (hasNodes) {
            // 注册逻辑卷
            registeClusterLvm(tenantName, mysqlCluster);
        }
        // 获取集群running时集群点的扩展字段，如复制用户名，复制用户密码
        Map<String, String> serviceExtendedField = buildServiceExtendedField(mysqlCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(mysqlCluster);

        componentOperationsClientUtil.changeMysqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, nodesExtendedField);

    }
}
