package com.xxx.xcloud.module.component.service.worker.es;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.model.es.EsInstance;
import com.xxx.xcloud.module.component.model.es.EsInstanceGroup;

@Service
@Scope("prototype")
public class EsClusterExpandNodeWorker extends BaseEsClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(EsClusterExpandNodeWorker.class);

    @Override
    public void execute() {
        LOG.info("===============EsClusterExpandNodeWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群增加节点时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群增加节点时获取的service：" + JSON.toJSONString(service));

        // 3、拼接esCluster
        EsCluster esCluster = buildEsCluster(tenantName, service.getServiceName(), data);

        // 4、调用k8sclient集群增加节点
        if (!updateAndRetry(tenantName, esCluster)) {
            // 修改waiting状态节点为Failed
            componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());

            LOG.info("es集群增加节点失败！");
            return;
        }

        // 5、循环获取增加节点结果
        checkExpandNodeResult(tenantName, serviceId, service.getServiceName(), data);
    }

    private void checkExpandNodeResult(String tenantName, String serviceId, String serviceName,
            Map<String, String> data) {

        long start = System.currentTimeMillis();
        boolean hasNode = false;
        StatefulService statefulService;
        int newReplicas = 0;
        List<String> changeNodeList = new ArrayList<String>();

        while (true) {

            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
                componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
                return;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("启动集群超时，serviceName：" + serviceName);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
                return;
            }
            EsCluster esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
            LOG.info("cluster:" + JSON.toJSONString(esCluster));
            if (null == esCluster) {
                LOG.info("esCluster:" + JSON.toJSONString(esCluster));
                componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                        CommonConst.STATE_NODE_FAILED, null);
                return;
            }

            statefulService = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            Map<String, String> extendedField = componentOperationsDataBaseUtil
                    .getServiceExtendedField(statefulService.getExtendedField());
            // double oldTotalCpu = statefulService.getCpu();
            // double oldTotalMemory = statefulService.getMemory();
            // double oldTotalcapacity = statefulService.getStorage();

            double newTotalCpu = 0, newTotalMemory = 0, newTotalcapacity = 0;
            EsInstanceGroup instanceGroup = new EsInstanceGroup();
            if (esCluster.getSpec().getInstanceGroup().size() == 1) {
                instanceGroup = esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_WORKER);
                Double workerCpu = Double.parseDouble(instanceGroup.getResources().getLimits().getCpu());
                String workerMemoryStr = instanceGroup.getResources().getLimits().getMemory();
                double workerMemory = Double.parseDouble(workerMemoryStr.substring(0, workerMemoryStr.length() - 2));
                String workerCapacityStr = instanceGroup.getStorage();
                double workerCapacity = Double
                        .parseDouble(workerCapacityStr.substring(0, workerCapacityStr.length() - 2));
                int workerReplicas = instanceGroup.getReplicas();

                newTotalCpu = workerCpu * workerReplicas;
                newTotalMemory = workerMemory * workerReplicas;
                newTotalcapacity = workerCapacity * workerReplicas;

                newReplicas = workerReplicas;

            } else if (esCluster.getSpec().getInstanceGroup().size() == 2) {
                instanceGroup = esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_MASTER);
                Double masterCpu = Double.parseDouble(instanceGroup.getResources().getLimits().getCpu());
                String masterMemoryStr = instanceGroup.getResources().getLimits().getMemory();
                double masterMemory = Double.parseDouble(masterMemoryStr.substring(0, masterMemoryStr.length() - 2));
                String masterCapacityStr = instanceGroup.getStorage();
                double masterCapacity = Double
                        .parseDouble(masterCapacityStr.substring(0, masterCapacityStr.length() - 2));
                int masterReplicas = instanceGroup.getReplicas();

                instanceGroup = esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_DATA);
                Double dataCpu = Double.parseDouble(instanceGroup.getResources().getLimits().getCpu());
                String dataMemoryStr = instanceGroup.getResources().getLimits().getMemory();
                double dataMemory = Double.parseDouble(dataMemoryStr.substring(0, dataMemoryStr.length() - 2));
                String dataCapacityStr = instanceGroup.getStorage();
                double dataCapacity = Double.parseDouble(dataCapacityStr.substring(0, dataCapacityStr.length() - 2));
                int dataReplicas = instanceGroup.getReplicas();
                extendedField.put("dataReplicas", String.valueOf(dataReplicas));

                newTotalCpu = masterCpu * masterReplicas + dataCpu * dataReplicas;
                newTotalMemory = masterMemory * masterReplicas + dataMemory * dataReplicas;
                newTotalcapacity = masterCapacity * masterReplicas + dataCapacity * dataReplicas;
                newReplicas = masterReplicas + dataReplicas;
            }

            if (!hasNode && newReplicas == esCluster.getStatus().getInstances().keySet().size()) {
                changeNodeList = buildNode(serviceId, esCluster);
                if (null == changeNodeList || changeNodeList.isEmpty()) {
                    continue;
                }

                // 修改tenant表资源
                // boolean result = tenantService.updateUsedResource(tenantName,
                // newTotalCpu - oldTotalCpu,
                // newTotalMemory - oldTotalMemory, newTotalcapacity -
                // oldTotalcapacity);
                // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
                // String.valueOf(newTotalCpu - oldTotalCpu)
                // + ",updateMemory:" + String.valueOf(newTotalMemory -
                // oldTotalMemory) + ",updateCapacity:"
                // + String.valueOf(newTotalcapacity - oldTotalcapacity));

                statefulService.setCpu(newTotalCpu);
                statefulService.setMemory(newTotalMemory);
                statefulService.setNodeNum(newReplicas);
                statefulService.setStorage(newTotalcapacity);
                statefulService.setExtendedField(JSON.toJSONString(extendedField));

                try {
                    statefulServiceRepository.save(statefulService);
                } catch (Exception e) {
                    LOG.error("修改StatefulService集群节点个数失败！");
                }
                hasNode = true;
            }

            if (hasNode && newReplicas == esCluster.getStatus().getInstances().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(esCluster.getStatus().getPhase())) {

                LOG.info("集群增加节点，修改数据库");
                if (CommonConst.STATE_CLUSTER_RUNNING.equals(esCluster.getStatus().getPhase())
                        && null != esCluster.getStatus().getInstances()) {
                    try {
                        // 注册逻辑卷 修改节点ip、port、状态
                        Map<String, String> nodeMessageMap = new HashMap<>();
                        for (String nodeName : esCluster.getStatus().getInstances().keySet()) {
                            for (String newNodeId : changeNodeList) {
                                StatefulNode newNode = componentOperationsDataBaseUtil.getStatefulNodeById(newNodeId);
                                if (nodeName.equals(newNode.getNodeName())) {
                                    LOG.info("节点: " + nodeName + "修改状态为RUNNING");
                                    String nodeRole = esCluster.getStatus().getInstances().get(nodeName).getRole();
                                    int nodePort = esCluster.getStatus().getInstances().get(nodeName)
                                            .getExterHttpport();
                                    String nodeIp = esCluster.getStatus().getInstances().get(nodeName).getExterHost();
                                    nodeMessageMap.put("ip", nodeIp);
                                    nodeMessageMap.put("port", String.valueOf(nodePort));
                                    componentOperationsDataBaseUtil.updateNodeState(newNodeId, CommonConst.STATE_NODE_RUNNING,
                                            nodeMessageMap);

                                    String storage = esCluster.getSpec().getInstanceGroup().get(nodeRole).getStorage();
                                    componentOperationsClientUtil.registerLvm(tenantName, newNode.getLvmName(), nodeIp, storage);
                                }
                            }
                        }
                        componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_RUNNING, null,
                                null);

                    } catch (Exception e) {
                        LOG.error("检查集群增加节点结果异常，error: ", e);
                        // 修改waiting状态节点为Failed
                        componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId,
                                serviceName);
                    }
                }
                LOG.info("=============获取操作结果成功==========");
                return;
            }

        }

    }

    /**
     * 集群增加节点：构建esCluster
     * 
     * @param tenantName
     * @param serviceName
     * @param data
     * @return
     */
    private EsCluster buildEsCluster(String tenantName, String serviceName, Map<String, String> data) {

        EsCluster esCluster = null;
        esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        if (null == esCluster) {
            LOG.error("获取esCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        int addReplicas = Integer.parseInt(data.get("addReplicas"));

        if (esCluster.getSpec().getInstanceGroup().size() == 1) {
            int oldWorkerReplicas = esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_WORKER)
                    .getReplicas();
            esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_WORKER)
                    .setReplicas(oldWorkerReplicas + addReplicas);

        } else if (esCluster.getSpec().getInstanceGroup().size() == 2) {
            int oldDataReplicas = esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_DATA).getReplicas();
            esCluster.getSpec().getInstanceGroup().get(EsClusterConst.ES_ROLE_DATA)
                    .setReplicas(oldDataReplicas + addReplicas);
        }
        esCluster.getSpec().setOpt(EsClusterConst.ES_CLUSTER_OPT_ADD_NODE);

        LOG.info("集群增加节点：构建esCluster成功, serviceName:" + serviceName);
        return esCluster;
    }

    /**
     * 变化节点存表
     * 
     * @param serviceId
     * @param esCluster
     * @return
     */
    private List<String> buildNode(String serviceId, EsCluster esCluster) {
        List<String> changeNodeList = new ArrayList<String>();
        List<StatefulNode> statefulNodeList = null;
        statefulNodeList = componentOperationsDataBaseUtil.getStatefulNodeListById(serviceId);
        StatefulNode node = new StatefulNode();
        Map<String, EsInstance> newNodes = esCluster.getStatus().getInstances();
        for (Map.Entry<String, EsInstance> entry : newNodes.entrySet()) {
            boolean isExist = false;
            for (StatefulNode statefulNode : statefulNodeList) {
                if (entry.getKey().equals(statefulNode.getNodeName())) {
                    isExist = true;
                }
            }
            if (!isExist) {
                String role = entry.getValue().getRole();
                node = new StatefulNode();

                Double cpu = Double.parseDouble(
                        esCluster.getSpec().getInstanceGroup().get(role).getResources().getLimits().getCpu());
                String memoryStr = esCluster.getSpec().getInstanceGroup().get(role).getResources().getLimits()
                        .getMemory();
                Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                String capacityStr = esCluster.getSpec().getInstanceGroup().get(role).getStorage();
                Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                node.setCpu(cpu);
                node.setMemory(memory);
                node.setStorage(capacity);

                node.setAppType(CommonConst.APPTYPE_ES);
                node.setServiceId(serviceId);
                node.setServiceName(esCluster.getMetadata().getName());

                node.setNodeName(entry.getKey());
                node.setRole(role);
                node.setLvmName(entry.getValue().getLvName());

                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());
                try {
                    StatefulNode newnode = statefulNodeRepository.save(node);
                    if (null != newnode) {
                        LOG.info("保存节点记录信息成功，nodeName" + entry.getKey());
                        changeNodeList.add(newnode.getId());
                    } else {
                        LOG.error("保存节点记录信息失败");
                        return null;
                    }
                } catch (Exception e) {
                    LOG.error("保存节点记录信息失败,Exception: ", e);
                    return null;
                }
            }

        }

        return changeNodeList;
    }

}
