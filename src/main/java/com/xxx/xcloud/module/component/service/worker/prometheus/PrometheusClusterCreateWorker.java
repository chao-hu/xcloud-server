package com.xxx.xcloud.module.component.service.worker.prometheus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PrometheusClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusCluster;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusInstances;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusSpec;

import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
@Scope("prototype")
public class PrometheusClusterCreateWorker extends BasePrometheusClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(PrometheusClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============PrometheusClusterCreateWorker====================");
        // 1、获取数据
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");

        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        String performance = data.get("performance");

        String version = data.get("version");
        String serviceName = data.get("serviceName");

        double cpu = Double.parseDouble(data.get("cpu"));
        double memory = Double.parseDouble(data.get("memory"));
        double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));

        String apiServer = data.get("apiServer");
        String storageTsdbRetention = data.get("storageTsdbRetention");
        String kubeStateMetricsPort = data.get("kubeStateMetricsPort");
        String blackboxExporterPort = data.get("blackboxExporterPort");

        Map<String, String> prometheusConfigurationMap = new HashMap<>();
        prometheusConfigurationMap.put("api_server", apiServer);
        prometheusConfigurationMap.put("storage_tsdb_retention", storageTsdbRetention);
        prometheusConfigurationMap.put("kube_state_metrics_port", kubeStateMetricsPort);

        prometheusConfigurationMap.put("blackbox_exporter_port", blackboxExporterPort);

        // 2、拼接prometheusCluster
        PrometheusCluster prometheusCluster = null;
        prometheusCluster = buildPrometheusCluster(projectId, orderId, version, serviceName, cpu, memory, capacity,
                replicas, prometheusConfigurationMap, tenantName, performance);

        LOG.info("创建拼接后的prometheusCluster：" + JSON.toJSONString(prometheusCluster));
        // 3、调用k8sAPI创建
        if (!createAndRetry(tenantName, prometheusCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.info("prometheus集群创建失败！");
            return;
        }

        // 4、循环获取创建结果
        if (!checkClusterCreateResult(tenantName, serviceId, serviceName)) {
            componentOperationsClientUtil.changePrometheusClusterAndNodesStateByYaml(tenantName, serviceId,
                    serviceName);
        }
    }

    /**
     * buildPrometheusCluster yaml
     * 
     * @param version
     * @param serviceName
     * @param cpu
     * @param memory
     * @param capacity
     * @param replicas
     * @param configuration
     * @param tenantName
     * @param performance
     * @return
     */

    private PrometheusCluster buildPrometheusCluster(String projectId, String orderId, String version,
            String serviceName, Double cpu, Double memory, Double capacity, int replicas,
            Map<String, String> configuration, String tenantName, String performance) {
        LOG.info("===============开始构建prometheusCluster==================");
        PrometheusCluster prometheusCluster = new PrometheusCluster();
        ObjectMeta metaData = new ObjectMeta();
        prometheusCluster.setKind(PrometheusClusterConst.PROMETHEUS_KIND);
        prometheusCluster.setApiVersion(PrometheusClusterConst.PROMETHEUS_API_VERSION);
        metaData.setName(serviceName);
        metaData.setNamespace(tenantName);

        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            metaData.setLabels(labels);
        }

        prometheusCluster.setMetadata(metaData);

        PrometheusSpec prometheusSpec = new PrometheusSpec();
        prometheusSpec.setVersion(version);
        prometheusSpec.setOpt(PrometheusClusterConst.PROMETHEUS_CLUSTER_OPT_CREATE);
        prometheusSpec.setPrometheusImage(getRepoPath(CommonConst.APPTYPE_PROMETHEUS, null, version));
        prometheusSpec.setReplicas(replicas);
        prometheusSpec.setStorage(capacity + CommonConst.UNIT_GI);
        prometheusSpec.setVgName(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_PROMETHEUS);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        if (null != performance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        prometheusSpec.setNodeSelector(nodeSelector);

        prometheusSpec.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        if (null != configuration && !configuration.isEmpty()) {
            prometheusSpec.setPrometheusConfig(configuration);
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            prometheusSpec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        prometheusCluster.setSpec(prometheusSpec);
        return prometheusCluster;

    }

    /**
     * 检查集群创建结果
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @return
     */
    private Boolean checkClusterCreateResult(String tenantName, String serviceId, String serviceName) {
        long start = System.currentTimeMillis();
        boolean hasNodes = false;
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                return false;
            }
            LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.info("=================创建超时================");
                return false;
            }

            PrometheusCluster prometheusCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName,
                    serviceName);
            LOG.info("判断prometheusCluster中数据是否存在");
            LOG.info("prometheusCluster:" + JSON.toJSONString(prometheusCluster));
            LOG.info("status:" + prometheusCluster.getStatus());

            if (null != prometheusCluster && null != prometheusCluster.getStatus()
                    && null != prometheusCluster.getStatus().getInstances() && prometheusCluster.getSpec()
                            .getReplicas() == prometheusCluster.getStatus().getInstances().keySet().size()) {
                if (!hasNodes) {
                    LOG.info("节点表插入数据");
                    // 创建WAITING的节点
                    if (buildNodes(prometheusCluster, serviceId)) {
                        LOG.info("节点表插入数据成功！===" + prometheusCluster.getMetadata().getName());
                        hasNodes = true;
                    } else {
                        continue;
                    }
                } else {
                    if (CommonConst.STATE_CLUSTER_RUNNING.equals(prometheusCluster.getStatus().getPhase())
                            && null != prometheusCluster.getStatus().getInstances()) {
                        // 注册逻辑卷 获取节点ip、port
                        Map<String, Map<String, String>> nodesMessageMap = new HashMap<>();
                        for (PrometheusInstances prometheusInstances : prometheusCluster.getStatus().getInstances()
                                .values()) {
                            Map<String, String> nodeMessageMap = new HashMap<>();
                            String nodeIp = prometheusInstances.getExterHost();
                            int nodePort = prometheusInstances.getExterHttpport();

                            nodeMessageMap.put("ip", nodeIp);
                            nodeMessageMap.put("port", String.valueOf(nodePort));

                            String storage = prometheusCluster.getSpec().getStorage();
                            componentOperationsClientUtil.registerLvm(tenantName, prometheusInstances.getName(), nodeIp,
                                    storage);
                            nodesMessageMap.put(prometheusInstances.getName(), nodeMessageMap);

                        }

                        // 修改集群状态、扩展字段与节点状态、ip、port
                        Map<String, String> serviceExtendedFieldMap = new HashMap<>();
                        serviceExtendedFieldMap.put("configMapName", prometheusCluster.getStatus().getConfigName());
                        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId,
                                CommonConst.STATE_CLUSTER_RUNNING, CommonConst.STATE_NODE_RUNNING,
                                serviceExtendedFieldMap, nodesMessageMap);
                        return true;

                    }

                }
            }

        }
    }

    /**
     * 创建节点表数据
     * 
     * @param cluster
     * @param serviceId
     * @return
     */
    private boolean buildNodes(PrometheusCluster cluster, String serviceId) {

        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");
        StatefulNode node = null;
        for (Entry<String, PrometheusInstances> entry : cluster.getStatus().getInstances().entrySet()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(serviceId,
                        entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                Double cpu = Double.parseDouble(cluster.getSpec().getResources().getLimits().getCpu());
                String memoryStr = cluster.getSpec().getResources().getLimits().getMemory();
                Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                String capacityStr = cluster.getSpec().getStorage();
                Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                node.setCpu(cpu);
                node.setMemory(memory);
                node.setStorage(capacity);

                node.setAppType(CommonConst.APPTYPE_PROMETHEUS);
                node.setServiceId(serviceId);
                node.setServiceName(cluster.getMetadata().getName());

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

    /**
     * 
     * 
     * @param tenantName
     * @param prometheusCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, PrometheusCluster prometheusCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建prometheusCluster：" + JSON.toJSONString(prometheusCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = createPrometheusCluster(tenantName, prometheusCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("创建prometheus集群" + prometheusCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }
}
