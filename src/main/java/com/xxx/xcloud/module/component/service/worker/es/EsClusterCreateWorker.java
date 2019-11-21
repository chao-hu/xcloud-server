package com.xxx.xcloud.module.component.service.worker.es;

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
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.model.es.EsInstance;
import com.xxx.xcloud.module.component.model.es.EsInstanceGroup;
import com.xxx.xcloud.module.component.model.es.EsSpec;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;

@Service
@Scope("prototype")
public class EsClusterCreateWorker extends BaseEsClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(EsClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============EsClusterCreateWorker====================");
        // 1、获取数据
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");

        String tenantName = data.get("tenantName");
        String performance = data.get("performance");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        String version = data.get("version");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        String capacity = data.get("capacity");
        int replicas = Integer.parseInt(data.get("replicas"));
        String configuration = data.get("configuration");
        String masterSeparateFlag = data.get("masterSeparateFlag");

        Map<String, EsInstanceGroup> esInstanceGroups = new HashMap<>();
        EsInstanceGroup esInstanceGroup = new EsInstanceGroup();
        esInstanceGroup.setResources(componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI));
        esInstanceGroup.setReplicas(replicas);
        esInstanceGroup.setStorage(capacity);
        if (EsClusterConst.ES_MASTER_SEPARATE_FLAG_TRUE.equals(masterSeparateFlag)) {
            EsInstanceGroup esDataInstanceGroup = new EsInstanceGroup();
            Double dataCpu = Double.parseDouble(data.get("dataCpu"));
            Double dataMemory = Double.parseDouble(data.get("dataMemory"));
            String dataCapacity = data.get("dataCapacity");
            int dataReplicas = Integer.parseInt(data.get("dataReplicas"));
            esDataInstanceGroup.setReplicas(dataReplicas);
            esDataInstanceGroup
                    .setResources(componentOperationsClientUtil.getResources(dataCpu, dataMemory, CommonConst.UNIT_GI));
            esDataInstanceGroup.setStorage(dataCapacity + CommonConst.UNIT_GI);
            esDataInstanceGroup.setRole(EsClusterConst.ES_ROLE_DATA);
            esInstanceGroup.setRole(EsClusterConst.ES_ROLE_MASTER);
            esInstanceGroups.put(EsClusterConst.ES_ROLE_MASTER, esInstanceGroup);
            esInstanceGroups.put(EsClusterConst.ES_ROLE_DATA, esDataInstanceGroup);
        } else {
            esInstanceGroup.setRole(EsClusterConst.ES_ROLE_WORKER);
            esInstanceGroups.put(EsClusterConst.ES_ROLE_WORKER, esInstanceGroup);
        }

        // 2、拼接esCluster
        EsCluster esCluster = buildEsCluster(projectId, orderId, tenantName, serviceName, version, configuration,
                esInstanceGroups, performance);

        // 3、调用k8sAPI创建
        if (!createAndRetry(tenantName, esCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.info("es集群创建失败！");
            return;
        }
        // 4、循环获取创建结果
        if (!checkClusterCreateResult(tenantName, serviceId, serviceName)) {
            componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
        }
    }

    private EsCluster buildEsCluster(String projectId, String orderId, String tenantName, String serviceName,
            String version, String configuration, Map<String, EsInstanceGroup> esInstanceGroups, String performance) {
        EsCluster cluster = new EsCluster();

        cluster.setApiVersion(EsClusterConst.ES_API_VERSION);
        cluster.setKind(EsClusterConst.ES_KIND);

        ObjectMeta metaData = new ObjectMeta();
        metaData.setNamespace(tenantName);
        metaData.setName(serviceName);

        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            metaData.setLabels(labels);
        }

        cluster.setMetadata(metaData);

        EsSpec spec = new EsSpec();
        spec.setEsImage(getRepoPath(CommonConst.APPTYPE_ES, null, version));
        spec.setEsExporterImage(getRepoPath(CommonConst.APPTYPE_ES, "exporter", version));
        String kibanaImage = getRepoPath(CommonConst.APPTYPE_ES, "kibana", version);
        if (null != kibanaImage) {
            spec.setKibanaFlag(true);
            spec.setKibanaImage(kibanaImage);
        }
        spec.setVersion(version);
        spec.setVgName(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        spec.setOpt(EsClusterConst.ES_CLUSTER_OPT_CREATE);
        spec.setInstanceGroup(esInstanceGroups);

        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_ES);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        if (null != performance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        spec.setNodeSelector(nodeSelector);
        if (StringUtils.isNotEmpty(configuration)) {
            JSONObject configMapJson = JSON.parseObject(configuration);
            if (null != configMapJson && !configMapJson.isEmpty()) {
                Map<String, String> returnConfigMap = componentOperationsDataBaseUtil.parseConfigUpdatedForYaml(configMapJson,
                        CommonConst.APPTYPE_ES, version);
                spec.setConfig(returnConfigMap);
            }
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            spec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        cluster.setSpec(spec);
        LOG.info("创建拼接的esCluster：" + JSON.toJSONString(cluster));

        return cluster;
    }

    /**
     * 
     * 
     * @param tenantName
     * @param esCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, EsCluster esCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建esCluster：" + JSON.toJSONString(esCluster));
        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = createEsCluster(tenantName, esCluster);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("创建es集群" + esCluster.getMetadata().getName() + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

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

            EsCluster esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
            LOG.info("判断esCluster中数据是否存在");
            LOG.info("esCluster:" + JSON.toJSONString(esCluster));
            LOG.info("status:" + esCluster.getStatus());
            int clusterReplicas = 0;
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                clusterReplicas = service.getNodeNum();
            } else {
                continue;
            }
            if (null != esCluster && null != esCluster.getStatus() && null != esCluster.getStatus().getInstances()
                    && clusterReplicas == esCluster.getStatus().getInstances().keySet().size()) {
                if (!hasNodes) {
                    LOG.info("节点表插入数据");
                    // 创建WAITING的节点
                    if (buildNodes(esCluster, serviceId)) {
                        LOG.info("节点表插入数据成功！===" + esCluster.getMetadata().getName());
                        hasNodes = true;
                    }
                }
                boolean kibanaIsReady = true;
                if (EsClusterConst.ES_HAS_KIBANA_VERSION.equals(esCluster.getSpec().getVersion())
                        && !CommonConst.STATE_NODE_RUNNING.equals(esCluster.getStatus().getKibana().getInstancePhase())
                        && esCluster.getSpec().isKibanaFlag()) {
                    kibanaIsReady = false;
                }
                if (kibanaIsReady && hasNodes
                        && CommonConst.STATE_CLUSTER_RUNNING.equals(esCluster.getStatus().getPhase())) {
                    // 注册逻辑卷 获取节点ip、port
                    Map<String, Map<String, String>> nodesMessageMap = new HashMap<>();
                    for (EsInstance esInstance : esCluster.getStatus().getInstances().values()) {
                        Map<String, String> nodeMessageMap = new HashMap<>();
                        LOG.info("节点: " + esInstance.getName() + "修改状态为RUNNING");
                        String nodeRole = esInstance.getRole();
                        String nodeIp = esInstance.getExterHost();
                        int nodePort = esInstance.getExterHttpport();

                        nodeMessageMap.put("ip", nodeIp);
                        nodeMessageMap.put("port", String.valueOf(nodePort));

                        String storage = esCluster.getSpec().getInstanceGroup().get(nodeRole).getStorage();
                        componentOperationsClientUtil.registerLvm(tenantName, esInstance.getLvName(), nodeIp, storage);
                        nodesMessageMap.put(esInstance.getName(), nodeMessageMap);
                    }

                    Map<String, String> serviceExtendedField = new HashMap<>();
                    if (null != esCluster.getStatus().getKibana()) {
                        String kibanaExterHost = esCluster.getStatus().getKibana().getKibanaExterHost();
                        String kibanaExterPort = String.valueOf(esCluster.getStatus().getKibana().getKibanaExterPort());
                        serviceExtendedField.put("kibanaIp", kibanaExterHost);
                        serviceExtendedField.put("kibanaPort", kibanaExterPort);
                    }

                    componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_RUNNING,
                            CommonConst.STATE_NODE_RUNNING, serviceExtendedField, nodesMessageMap);
                    return true;

                }

            }
        }

    }

    private boolean buildNodes(EsCluster cluster, String serviceId) {

        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");
        StatefulNode node = null;
        for (EsInstance esInstance : cluster.getStatus().getInstances().values()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(serviceId,
                        esInstance.getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                String role = esInstance.getRole();
                Double cpu = Double.parseDouble(
                        cluster.getSpec().getInstanceGroup().get(role).getResources().getLimits().getCpu());
                String memoryStr = cluster.getSpec().getInstanceGroup().get(role).getResources().getLimits()
                        .getMemory();
                Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                String capacityStr = cluster.getSpec().getInstanceGroup().get(role).getStorage();
                Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                node.setCpu(cpu);
                node.setMemory(memory);
                node.setStorage(capacity);

                node.setAppType(CommonConst.APPTYPE_ES);
                node.setServiceId(serviceId);
                node.setServiceName(cluster.getMetadata().getName());

                node.setNodeName(esInstance.getName());
                node.setRole(role);
                node.setLvmName(esInstance.getLvName());

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
