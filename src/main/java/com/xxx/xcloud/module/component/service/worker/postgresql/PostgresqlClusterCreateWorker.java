package com.xxx.xcloud.module.component.service.worker.postgresql;

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
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlInstance;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlSpec;
import com.xxx.xcloud.utils.StringUtils;

@Service
@Scope("prototype")
public class PostgresqlClusterCreateWorker extends BasePostgresqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(PostgresqlClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============PostgresqlClusterCreateWorker====================");
        // 1、获取数据
        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        String version = data.get("version");
        String type = data.get("type");
        String password = data.get("password");
        int replicas = Integer.parseInt(data.get("replicas"));
        String configUpdated = data.get("configuration");

        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String performance = data.get("performance");

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

        // 3、拼接postgresqlCluster
        PostgresqlCluster postgresqlCluster = buildPostgresqlCluster(tenantName, projectId, orderId, version, type,
                serviceName, password, cpu, memory, capacity, replicas, configUpdated, performance);

        // 4、调用k8s client创建
        if (!createAndRetry(tenantName, postgresqlCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.error("postgresql集群" + serviceName + "创建失败！");
            return;
        }

        // 5、循环获取创建结果
        checkCreateResult(tenantName, service, cpu, memory, capacity);
    }

    /**
     * 创建集群：构建postgresqlCluster
     * 
     * @param tenantName
     * @param projectId
     * @param orderId
     * @param version
     * @param type
     * @param serviceName
     * @param password
     * @param cpu
     * @param memory
     * @param capacity
     * @param replicas
     * @param configUpdated
     * @param performance
     * @return
     */
    private PostgresqlCluster buildPostgresqlCluster(String tenantName, String projectId, String orderId,
            String version, String type, String serviceName, String password, Double cpu, Double memory,
            Double capacity, int replicas, String configuration, String performance) {
        PostgresqlCluster postgresqlCluster = new PostgresqlCluster();

        postgresqlCluster.getMetadata().setName(serviceName);
        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            postgresqlCluster.getMetadata().setLabels(labels);
        }

        PostgresqlSpec spec = new PostgresqlSpec();
        spec.setPostgresqlClusterImage(getRepoPath(CommonConst.APPTYPE_POSTGRESQL, null, version));
        spec.setPostgresqlExporterImage(getRepoPath(CommonConst.APPTYPE_POSTGRESQL, CommonConst.EXPORTER, version));
        spec.setVersion(version);
        if (PostgresqlClusterConst.TYPE_SINGLE.equals(type)) {
            spec.setType(PostgresqlClusterConst.TYPE_ASYNC);
        } else {
            spec.setType(type);
        }
        spec.setPassword(password);
        spec.setOpt(PostgresqlClusterConst.OPERATOR_CLUSTER_CREATE);
        spec.setVgName(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        spec.setReplicas(replicas);
        spec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        spec.setStorage(capacity + CommonConst.UNIT_GI);
        spec.setIsHealthCheck(PostgresqlClusterConst.POSTGRESQL_ISHEALTHCHECK);

        if (StringUtils.isNotEmpty(configuration)) {
            JSONObject configurationJson = JSON.parseObject(configuration);
            Map<String, String> config = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configurationJson,
                    CommonConst.APPTYPE_POSTGRESQL, version);
            if (null != config && !config.isEmpty()) {
                spec.setPostgresqlClusterConfig(config);
            }
        }

        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_POSTGRESQL);
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

        postgresqlCluster.setSpec(spec);
        LOG.info("创建拼接的postgresqlCluster：" + JSON.toJSONString(postgresqlCluster));

        return postgresqlCluster;
    }

    /**
     * 创建集群并重试
     * 
     * @param tenantName
     * @param postgresqlCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, PostgresqlCluster postgresqlCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建postgresqlCluster：" + JSON.toJSONString(postgresqlCluster));
        if (null != postgresqlCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = createPostgresqlCluster(tenantName, postgresqlCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("创建postgresql集群" + postgresqlCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检查集群创建结果
     * 
     * @param tenantName
     * @param service
     * @param cpu
     * @param memory
     * @param capacity
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
            PostgresqlCluster postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                    service.getServiceName());
            LOG.info("判断postgresqlCluster中数据是否存在");
            LOG.info("postgresqlCluster:" + JSON.toJSONString(postgresqlCluster));
            LOG.info("status:" + postgresqlCluster.getStatus());
            if (null != postgresqlCluster && null != postgresqlCluster.getStatus()
                    && null != postgresqlCluster.getStatus().getInstances()
                    && null != postgresqlCluster.getStatus().getInstances().keySet() && !hasNodes
                    && service.getNodeNum() <= postgresqlCluster.getStatus().getInstances().keySet().size()) {
                if (buildNodes(postgresqlCluster, service, cpu, memory, capacity)) {
                    LOG.info("节点表插入数据成功,集群名称：" + service.getServiceName());
                    hasNodes = true;
                }
            }
            if (null != postgresqlCluster && null != postgresqlCluster.getStatus()) {
                LOG.info("cluster此时的状态：" + postgresqlCluster.getStatus().getPhase());
            }
            if (hasNodes && null != postgresqlCluster && null != postgresqlCluster.getStatus()
                    && null != postgresqlCluster.getStatus().getInstances()
                    && null != postgresqlCluster.getStatus().getInstances().keySet()
                    && service.getNodeNum() <= postgresqlCluster.getStatus().getInstances().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(postgresqlCluster.getStatus().getPhase())) {
                LOG.info("postgresql集群创建成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;
            }
        }

        // 检查结束
        optClusterCreate(tenantName, service.getId(), service.getServiceName(), hasNodes);

    }

    private void optClusterCreate(String tenantName, String serviceId, String serviceName, boolean hasNodes) {
        // 获取postresqlCluster
        PostgresqlCluster postgresqlCluster = componentOperationsClientUtil.getPostgresqlCluster(tenantName,
                serviceName);

        if (hasNodes) {
            // 注册逻辑卷
            registeClusterLvm(tenantName, postgresqlCluster);
        }

        // 获取集群running时集群点的扩展字段，如复制用户名，复制用户密码
        Map<String, String> serviceExtendedField = buildServiceExtendedField(postgresqlCluster);
        // 获取集群running时节点的扩展字段，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(postgresqlCluster);

        // 修改数据库中集群、节点状态
        componentOperationsClientUtil.changePostgresqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, nodesExtendedField);

    }

    /**
     * 构造节点
     * 
     * @param postgresqlCluster
     * @param service
     * @param cpu
     * @param memory
     * @param capacity
     * @return
     */
    private boolean buildNodes(PostgresqlCluster cluster, StatefulService service, Double cpu, Double memory,
            Double storage) {
        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");

        StatefulNode node = null;
        for (Map.Entry<String, PostgresqlInstance> entry : cluster.getStatus().getInstances().entrySet()) {
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

                node.setAppType(CommonConst.APPTYPE_POSTGRESQL);
                node.setServiceId(service.getId());
                node.setServiceName(service.getServiceName());

                node.setNodeName(entry.getKey());
                node.setRole(entry.getValue().getRole());
                node.setLvmName(entry.getValue().getLvName());

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
}
