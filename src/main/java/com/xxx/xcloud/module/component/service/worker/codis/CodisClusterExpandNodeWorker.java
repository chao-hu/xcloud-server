package com.xxx.xcloud.module.component.service.worker.codis;

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
import com.xxx.xcloud.module.component.consts.CodisClusterConst;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;
import com.xxx.xcloud.module.component.model.codis.CodisGroupBindingNode;
import com.xxx.xcloud.module.component.model.codis.CodisGroupStatus;

@Service
@Scope("prototype")
public class CodisClusterExpandNodeWorker extends BaseCodisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(CodisClusterExpandNodeWorker.class);

    @Override
    public void execute() {
        LOG.info("===============CodisClusterExpandNodeWorker====================");
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

        // 3、拼接codisCluster
        CodisCluster codisCluster = buildCodisCluster(tenantName, service.getServiceName(), data);

        // 4、调用k8sclient集群增加节点
        if (!updateAndRetry(tenantName, codisCluster)) {
            // 修改waiting状态节点为Failed
            componentOperationsClientUtil.changeCodisClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
            LOG.info("codis集群增加节点失败！");
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
        int replicas = Integer.parseInt(data.get("replicas"));
        int proxyReplicas = Integer.parseInt(data.get("proxyReplicas"));

        statefulService = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == statefulService) {
            LOG.error("获取集群表信息失败，集群名称：" + serviceName);
            return;
        }
        int oldReplicas = statefulService.getNodeNum();
        Map<String, String> extendedField = componentOperationsDataBaseUtil
                .getServiceExtendedField(statefulService.getExtendedField());
        int oldProxyReplicas = Integer.parseInt(extendedField.get("proxyReplicas"));
        // double oldTotalCpu = statefulService.getCpu();
        // double oldTotalMemory = statefulService.getMemory();
        // double oldTotalcapacity = statefulService.getStorage();

        List<String> changeNodeList = new ArrayList<String>();

        while (true) {

            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeCodisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
                return;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("集群增加节点超时，serviceName：" + serviceName);

                // 修改waiting状态节点为Failed
                componentOperationsClientUtil.changeCodisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
                return;

            }
            CodisCluster codisCluster = componentOperationsClientUtil.getCodisCluster(tenantName, serviceName);
            LOG.info("codisCluster:" + JSON.toJSONString(codisCluster));
            if (null == codisCluster) {
                LOG.info("codisCluster:" + JSON.toJSONString(codisCluster));
                componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                        CommonConst.STATE_NODE_FAILED, null);
                return;
            }

            if (!hasNode && codisCluster.getSpec().getServerGroups().getReplicas() == codisCluster.getStatus()
                    .getGroup().keySet().size()) {
                if (oldReplicas != replicas) {
                    changeNodeList = buildNode(changeNodeList, serviceId, codisCluster);
                    if (null == changeNodeList || changeNodeList.isEmpty()) {
                        continue;
                    }
                }
                LOG.info("changeNodeList:" + JSON.toJSONString(changeNodeList));
                double newTotalCpu, newTotalMemory, newTotalcapacity;

                Double cpu = Double
                        .parseDouble(codisCluster.getSpec().getServerGroups().getResources().getLimits().getCpu());
                String memoryStr = codisCluster.getSpec().getServerGroups().getResources().getLimits().getMemory();
                Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                String capacityStr = codisCluster.getSpec().getServerGroups().getCapacity();
                Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                Double proxyCpu = Double
                        .parseDouble(codisCluster.getSpec().getProxy().getResources().getLimits().getCpu());
                String proxyMemoryStr = codisCluster.getSpec().getProxy().getResources().getLimits().getMemory();
                Double proxyMemory = Double.parseDouble(proxyMemoryStr.substring(0, proxyMemoryStr.length() - 2));

                newTotalCpu = cpu * replicas + proxyCpu * proxyReplicas;
                newTotalMemory = memory * replicas + proxyMemory * proxyReplicas;
                newTotalcapacity = capacity * replicas;

                // 修改tenant表资源
//                boolean result = tenantService.updateUsedResource(tenantName, newTotalCpu - oldTotalCpu,
//                        newTotalMemory - oldTotalMemory, newTotalcapacity - oldTotalcapacity);
//                LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + String.valueOf(newTotalCpu - oldTotalCpu)
//                        + ",updateMemory:" + String.valueOf(newTotalMemory - oldTotalMemory) + ",updateCapacity:"
//                        + String.valueOf(newTotalcapacity - oldTotalcapacity));

                // 修改节点表

                if (oldReplicas > replicas) {
                    Map<Integer, CodisGroupStatus> newGroups = codisCluster.getStatus().getGroup();
                    for (String nodeId : changeNodeList) {
                        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
                        Map<String, String> nodeExtendedField = componentOperationsDataBaseUtil
                                .getServiceExtendedField(node.getExtendedField());
                        int groupId = Integer.parseInt(nodeExtendedField.get("groupId"));
                        if (!newGroups.keySet().contains(groupId)) {
                            node.setNodeState(CommonConst.STATE_NODE_DELETED);
                            statefulNodeRepository.save(node);
                        }
                        componentOperationsClientUtil.deleteLvm(tenantName, node.getNodeName());
                    }
                }

                // 修改集群表
                statefulService.setCpu(newTotalCpu);
                statefulService.setMemory(newTotalMemory);
                statefulService.setNodeNum(replicas);
                if (proxyReplicas != oldProxyReplicas) {
                    extendedField.put("proxyReplicas", String.valueOf(proxyReplicas));
                    statefulService.setExtendedField(JSON.toJSONString(extendedField));
                }
                statefulService.setStorage(newTotalcapacity);
                try {
                    statefulServiceRepository.save(statefulService);
                } catch (Exception e) {
                    LOG.error("修改StatefulService集群节点个数失败！");
                }
                hasNode = true;
            }

            if (hasNode
                    && codisCluster.getSpec().getServerGroups().getReplicas() == codisCluster.getStatus().getGroup()
                            .keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(codisCluster.getStatus().getPhase())) {

                LOG.info("集群增加节点，修改数据库");
                if (CommonConst.STATE_CLUSTER_RUNNING.equals(codisCluster.getStatus().getPhase())
                        && null != codisCluster.getStatus().getGroup()) {
                    try {
                        if (oldReplicas != replicas) {
                            Map<Integer, CodisGroupStatus> newGroups = codisCluster.getStatus().getGroup();

                            if (oldReplicas < replicas) {
                                // 注册逻辑卷 修改节点ip、port、状态
                                Map<String, String> nodeMessageMap = new HashMap<>();

                                for (CodisGroupStatus newGroup : newGroups.values()) {
                                    for (CodisGroupBindingNode newBinding : newGroup.getBindings().values()) {
                                        for (String newNodeId : changeNodeList) {
                                            StatefulNode newNode = componentOperationsDataBaseUtil
                                                    .getStatefulNodeById(newNodeId);
                                            if (newBinding.getName().equals(newNode.getNodeName())) {
                                                LOG.info("节点: " + newBinding.getName() + "修改状态为RUNNING");
                                                String nodeIp = newBinding.getBindIp();
                                                nodeMessageMap.put("ip", nodeIp);
                                                componentOperationsDataBaseUtil.updateNodeState(newNodeId,
                                                        CommonConst.STATE_NODE_RUNNING, nodeMessageMap);

                                                String storage = codisCluster.getSpec().getServerGroups().getCapacity();
                                                componentOperationsClientUtil.registerLvm(tenantName,
                                                        newBinding.getName(), nodeIp, storage);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (null != codisCluster.getStatus().getProxy() && CommonConst.STATE_CLUSTER_RUNNING
                                .equals(codisCluster.getStatus().getProxy().getStatus())) {
                            StatefulNode proxyNode = componentOperationsDataBaseUtil.getStatefulNodeByServiceIdAndNodeName(
                                    serviceId, CodisClusterConst.CODIS_ROLE_PROXY);
                            proxyNode.setNodeState(CommonConst.STATE_NODE_RUNNING);
                            statefulNodeRepository.save(proxyNode);
                        }
                        componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_RUNNING, null,
                                null);
                        return;

                    } catch (Exception e) {
                        LOG.error("检查集群增加节点结果异常，error: ", e);
                        // 修改waiting状态节点为Failed
                        componentOperationsClientUtil.changeCodisClusterAndNodesStateByYaml(tenantName, serviceId,
                                serviceName);
                    }
                }
                LOG.info("=============获取操作结果成功==========");
            }
        }
    }

    /**
     * 集群增加节点：构建codisCluster
     * 
     * @param tenantName
     * @param serviceName
     * @param data
     * @return
     */
    private CodisCluster buildCodisCluster(String tenantName, String serviceName, Map<String, String> data) {

        CodisCluster codisCluster = null;
        codisCluster = componentOperationsClientUtil.getCodisCluster(tenantName, serviceName);
        if (null == codisCluster) {
            LOG.error("获取codisCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        int replicas = Integer.parseInt(data.get("replicas"));
        int proxyReplicas = Integer.parseInt(data.get("proxyReplicas"));

        codisCluster.getSpec().getServerGroups()
                .setReplicas(replicas / (CodisClusterConst.CODIS_SERVER_GROUPS_SLAVE_REPLICAS + 1));
        codisCluster.getSpec().getProxy().setReplicas(proxyReplicas);
        LOG.info("集群增加节点：构建codisCluster成功, serviceName:" + serviceName);
        return codisCluster;
    }

    /**
     * 变化节点存表
     * 
     * @param changeNodeList
     * @param serviceId
     * @param cluster
     * @return
     */
    private List<String> buildNode(List<String> changeNodeList, String serviceId, CodisCluster cluster) {
        List<StatefulNode> statefulNodeList = null;
        statefulNodeList = componentOperationsDataBaseUtil.getStatefulNodeListById(serviceId);
        Map<Integer, CodisGroupStatus> newGroups = cluster.getStatus().getGroup();
        try {
            for (StatefulNode statefulNode : statefulNodeList) {
                if (!CodisClusterConst.getCodisBaseNodeNameList().contains(statefulNode.getNodeName())) {
                    boolean isDeleted = true;
                    Map<String, String> nodeExtendedField = componentOperationsDataBaseUtil
                            .getServiceExtendedField(statefulNode.getExtendedField());
                    int groupId = Integer.parseInt(nodeExtendedField.get("groupId"));
                    if (newGroups.keySet().contains(groupId)) {
                        CodisGroupStatus groups = newGroups.get(groupId);
                        for (CodisGroupBindingNode newBinding : groups.getBindings().values()) {
                            if (statefulNode.getNodeName().equals(newBinding.getName())) {
                                isDeleted = false;
                            }
                        }
                    }

                    if (isDeleted) {
                        changeNodeList.add(statefulNode.getId());
                    }
                }

            }
            for (CodisGroupStatus newGroup : newGroups.values()) {
                for (CodisGroupBindingNode newBinding : newGroup.getBindings().values()) {
                    boolean isExist = false;
                    for (StatefulNode statefulNode : statefulNodeList) {
                        if (newBinding.getName().equals(statefulNode.getNodeName())) {
                            isExist = true;
                            if (newBinding.getStatus().equals(CommonConst.STATE_NODE_DELETED)) {
                                changeNodeList.add(statefulNode.getId());
                            }
                        }
                    }
                    if (!isExist) {
                        StatefulNode node = new StatefulNode();

                        Double cpu = Double
                                .parseDouble(cluster.getSpec().getServerGroups().getResources().getLimits().getCpu());
                        String memoryStr = cluster.getSpec().getServerGroups().getResources().getLimits().getMemory();
                        Double memory = Double.parseDouble(memoryStr.substring(0, memoryStr.length() - 2));
                        String capacityStr = cluster.getSpec().getServerGroups().getCapacity();
                        Double capacity = Double.parseDouble(capacityStr.substring(0, capacityStr.length() - 2));

                        node.setCpu(cpu);
                        node.setMemory(memory);
                        node.setStorage(capacity);

                        node.setAppType(CommonConst.APPTYPE_CODIS);
                        node.setServiceId(serviceId);
                        node.setServiceName(cluster.getMetadata().getName());

                        node.setNodeName(newBinding.getName());
                        node.setRole(CodisClusterConst.CODIS_ROLE_SERVER);
                        node.setLvmName(newBinding.getName());

                        node.setNodeState(CommonConst.STATE_NODE_WAITING);
                        node.setCreateTime(new Date());
                        String extendedField = "";
                        Map<String, String> extendedFieldMap = new HashMap<>();
                        extendedFieldMap.put("groupId", String.valueOf(newGroup.getId()));
                        extendedField = JSON.toJSONString(extendedFieldMap);
                        node.setExtendedField(extendedField);
                        StatefulNode newnode = statefulNodeRepository.save(node);
                        if (null != newnode) {
                            LOG.info("保存节点记录信息成功，nodeName" + newBinding.getName());
                            changeNodeList.add(newnode.getId());
                        } else {
                            LOG.error("保存节点记录信息失败");
                            return null;
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("保存节点记录信息失败,Exception: ", e);
            return null;
        }

        return changeNodeList;
    }
}
