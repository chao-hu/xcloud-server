package com.xxx.xcloud.module.component.service.worker.mysql;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlServer;

@Service
@Scope("prototype")
public class MysqlClusterExpandNodeWorker extends BaseMysqlClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(MysqlClusterExpandNodeWorker.class);

    @Override
    public void execute() {
        LOG.info("===============MysqlClusterExpandNodeWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        JSONObject jsonObject = JSON.parseObject(data.get("json"));
        Integer addNum = jsonObject.getInteger("addNum");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群扩展节点时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群扩展节点时获取的service：" + JSON.toJSONString(service));

        // 3、修改mysqlCluster
        MysqlCluster oldCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("集群扩展节点时获取的cluster为null");
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            return;
        }

        MysqlCluster newCluster = updateYamlForMysqlCluster(tenantName, service.getServiceName(),
                MysqlClusterConst.OPERATOR_CLUSTER_EXPAND, null, jsonObject);

        // 4、调用k8sclient扩展节点
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("mysql集群" + service.getServiceName() + "扩展节点失败！");
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            return;
        }

        // 5、循环获取扩展节点结果
        checkExpandNodeResult(tenantName, service, oldCluster, addNum);

    }

    /**
     * 检查集群扩展节点是否成功
     * 
     * @param tenantName
     * @param service
     * @param oldCluster
     * @param addNum
     * @return
     */
    private void checkExpandNodeResult(String tenantName, StatefulService service, MysqlCluster oldCluster,
            Integer addNum) {
        long start = System.currentTimeMillis();
        boolean hasNode = false;
        boolean tenantUpdateResult = false;
        Double cpuOld = Double.parseDouble(oldCluster.getSpec().getResources().getLimits().getCpu());
        String memoryOldStr = oldCluster.getSpec().getResources().getLimits().getMemory();
        Double memoryOld = Double.parseDouble(memoryOldStr.substring(0, memoryOldStr.length() - 2));
        String capacityOldStr = oldCluster.getSpec().getCapacity();
        Double capacityOld = Double.parseDouble(capacityOldStr.substring(0, capacityOldStr.length() - 2));

        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常");
                break;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.info("=============循环获取操作结果超时==========");
                break;
            }

            MysqlCluster newCluster = componentOperationsClientUtil.getMysqlCluster(tenantName,
                    service.getServiceName());
            if (null == newCluster) {
                LOG.error("获取的newCluster为null");
                break;
            }
            LOG.info("newCluster:" + JSON.toJSONString(newCluster));

            StatefulService newService = null;
            try {
                newService = componentOperationsDataBaseUtil.getStatefulServiceById(service.getId());
            } catch (Exception e) {
                LOG.error("修改mysql集群循环获取操作结果时查找service异常！", e);
                continue;
            }
            if (null == newService) {
                LOG.error("修改mysql集群循环获取操作结果时查找service为空！");
                break;
            }

            if (!hasNode
                    && newService.getNodeNum() + addNum <= newCluster.getStatus().getServerNodes().keySet().size()) {
                if (buildNode(newService, newCluster, cpuOld, memoryOld, capacityOld)) {
                    // 节点个数加1
                    LOG.info("查询到的mysql service：" + JSON.toJSONString(newService));
                    newService.setNodeNum(newService.getNodeNum() + addNum);
                    newService.setCpu(newService.getNodeNum() * cpuOld);
                    newService.setMemory(newService.getNodeNum() * memoryOld);
                    newService.setStorage(newService.getNodeNum() * capacityOld);
                    try {
                        if (!tenantUpdateResult) {
                            // 修改tenant表
//                            boolean result = tenantService.updateUsedResource(tenantName, cpuOld * addNum,
//                                    memoryOld * addNum, capacityOld * addNum);
//                            LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuOld * addNum + ",updateMemory:"
//                                    + memoryOld * addNum + ",updateCapacity:" + capacityOld * addNum);
                            tenantUpdateResult = true;
                        }
                        statefulServiceRepository.save(newService);
                        hasNode = true;
                    } catch (ErrorMessageException e) {
                        LOG.error("修改tenant表资源失败！", e);
                    } catch (Exception e) {
                        LOG.error("修改StatefulService集群信息失败！", e);
                    }
                }
            }

            if (hasNode && newService.getNodeNum() <= newCluster.getStatus().getServerNodes().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(newCluster.getStatus().getPhase())) {
                LOG.info("mysql集群扩展节点成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;
            }
        }

        // 检查结束
        optClusterExpand(tenantName, service.getId(), service.getServiceName(), hasNode);

    }

    /**
     * 为新增的节点增加statefulNode记录
     * 
     * @param service
     * @param newMysqlCluster
     * @param cpu
     * @param memory
     * @param storage
     * @return
     */
    private boolean buildNode(StatefulService service, MysqlCluster newMysqlCluster, Double cpu, Double memory,
            Double storage) {
        StatefulNode node = new StatefulNode();
        List<StatefulNode> oldNodesList = componentOperationsDataBaseUtil.getStatefulNodeListById(service.getId());
        Set<String> oldNodes = new HashSet<>();
        Map<String, MysqlServer> newNodes = newMysqlCluster.getStatus().getServerNodes();

        if (null != oldNodesList && !oldNodesList.isEmpty()) {
            for (StatefulNode statefulNode : oldNodesList) {
                oldNodes.add(statefulNode.getNodeName());
            }
        }

        LOG.info("旧的statefulNodes：" + JSON.toJSONString(oldNodesList));
        try {
            for (Map.Entry<String, MysqlServer> entry : newNodes.entrySet()) {
                if (!oldNodes.contains(entry.getKey())) {
                    StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(
                            service.getId(), entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
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
                }
            }
            return true;
        } catch (Exception e) {
            LOG.error("保存节点记录信息失败！", e);
            return false;
        }
    }

    /**
     * 处理新增节点
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optClusterExpand(String tenantName, String serviceId, String serviceName, boolean hasNode) {
        // 获取mysqlCluster
        MysqlCluster mysqlCluster = componentOperationsClientUtil.getMysqlCluster(tenantName, serviceName);

        if (hasNode) {
            // 注册lvm
            registeClusterLvm(tenantName, mysqlCluster);
        }
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(mysqlCluster);

        // 修改数据库中集群、节点状态
        componentOperationsClientUtil.changeMysqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, null, nodesExtendedField);
    }
}
