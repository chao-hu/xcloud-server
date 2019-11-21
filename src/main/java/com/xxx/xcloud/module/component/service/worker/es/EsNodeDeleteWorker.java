package com.xxx.xcloud.module.component.service.worker.es;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.model.es.EsInstance;

@Service
@Scope("prototype")
public class EsNodeDeleteWorker extends BaseEsClusterWorker {
    private static Logger LOG = LoggerFactory.getLogger(EsNodeDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============EsNodeDeleteWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            esOptNodeDelete(tenantName, nodeId);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空，serviceId：" + node.getServiceId());
            return;
        }

        // 4、获取es cluster
        EsCluster oldCluster = componentOperationsClientUtil.getEsCluster(tenantName, service.getServiceName());
        if (null == oldCluster || null == oldCluster.getStatus()) {
            LOG.error("根据serviceName获取oldCluster为空，或oldCluster不包含该节点，serviceId:" + node.getServiceName()
                    + ",oldCluster:" + JSON.toJSONString(oldCluster));
            esOptNodeDelete(tenantName, nodeId);
            return;
        }

        Map<String, EsInstance> serverNodes = oldCluster.getStatus().getInstances();
        if (null == serverNodes || serverNodes.isEmpty() || !serverNodes.containsKey(node.getNodeName())) {
            LOG.info("节点已经被删除，nodeId：" + nodeId + ",nodeName:" + node.getNodeName());
            esOptNodeDelete(tenantName, nodeId);
            return;
        }

        // 5、拼接es cluster
        EsCluster esCluster = buildEsCluster(tenantName, service.getServiceName(), node.getNodeName());

        // 6、调用k8sclient删除节点并循环获取删除结果
        if (!updateAndRetry(tenantName, esCluster)) {
            LOG.info("删除节点：" + node.getNodeName() + "失败");
            updateEsNodeStateAndClusterState(nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        if (checkNodeDeleteResult(tenantName, service.getServiceName(), node.getNodeName())) {
            esOptNodeDelete(tenantName, nodeId);
            updateServiceExtendedField(tenantName, service.getId(), service.getServiceName());
        } else {
            updateEsNodeStateAndClusterState(nodeId, CommonConst.STATE_NODE_FAILED);
            LOG.error("es节点" + node.getNodeName() + "删除失败！");
        }

    }

    /**
     * 检查节点删除结果
     * @Title: checkNodeDeleteResult
     * @Description: 检查节点删除结果
     * @param tenantName
     * @param serviceName
     * @param nodeName
     * @return boolean 
     * @throws
     */
    private boolean checkNodeDeleteResult(String tenantName, String serviceName, String nodeName) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(2000);
                LOG.info("删除es已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.info("============es循环获取删除结果超时==========");
                return false;
            }
            EsCluster escluster = null;
            escluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
            if (null == escluster) {
                LOG.info("获取的集群为null");
                return true;
            }

            if (null != escluster && null == escluster.getStatus().getInstances()) {
                return true;
            }

            if (null != escluster && null != escluster.getStatus().getInstances()) {
                Map<String, EsInstance> nodes = escluster.getStatus().getInstances();
                if (!nodes.containsKey(nodeName)) {
                    return true;
                }
            }
        }

    }

    /**
     * 根据nodeId修改集群和节点状态
     * 
     * @param nodeId
     * @param nodeState
     */
    private void esOptNodeDelete(String tenantName, String nodeId) {
        try {
            StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
            if (null != node && !CommonConst.STATE_NODE_DELETED.equals(node.getNodeState())) {
                node.setNodeState(CommonConst.STATE_NODE_DELETED);
                statefulNodeRepository.save(node);
                // 修改tenant表
                // boolean result = tenantService.updateUsedResource(tenantName,
                // node.getCpu() * (-1),
                // node.getMemory() * (-1), node.getStorage() * (-1));
                // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" +
                // node.getCpu() * (-1) + ",updateMemory:"
                // + node.getMemory() * (-1) + ",updateCapacity" + node.getStorage());
            }

            componentOperationsClientUtil.deleteLvm(tenantName, node.getLvmName());

            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
            if (null != service) {
                service.setServiceState(componentOperationsClientUtil.getEsClusterStateByNodes(node.getServiceId(),
                        service.getLastopt()));
                int newNodeNum = service.getNodeNum() - 1;
                service.setNodeNum(newNodeNum);
                Map<String, String> extendedField = componentOperationsDataBaseUtil
                        .getServiceExtendedField(service.getExtendedField());
                if (EsClusterConst.ES_MASTER_SEPARATE_FLAG_TRUE.equals(extendedField.get("masterSeparateFlag"))) {
                    int oldDataReplicas = Integer.parseInt(extendedField.get("dataReplicas"));
                    extendedField.put("dataReplicas", String.valueOf(oldDataReplicas - 1));
                }
                if (null != node) {
                    service.setCpu(service.getCpu() - node.getCpu());
                    service.setMemory(service.getMemory() - node.getMemory());
                    service.setStorage(service.getStorage() - node.getStorage());
                    service.setExtendedField(JSON.toJSONString(extendedField));
                }

                statefulServiceRepository.save(service);

                if (newNodeNum <= 0) {
                    boolean deleteResult = deleteAndRetry(tenantName, service.getServiceName());
                    if (!deleteResult) {
                        LOG.error("es集群:" + service.getServiceName() + "删除失败");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("根据nodeId修改集群和节点状态失败，nodeId:" + nodeId + ",error:", e);
        }
    }

    /**
     * 删除节点：构建esCluster
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    private EsCluster buildEsCluster(String tenantName, String serviceName, String nodeName) {

        EsCluster esCluster = null;
        esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        if (null == esCluster) {
            LOG.error("获取esCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        esCluster.getSpec().setOpt(EsClusterConst.ES_NODE_OPT_DELETE);
        esCluster.getSpec().setOptNodename(nodeName);
        LOG.info("删除节点：构建esCluster成功, serviceName:" + serviceName);
        return esCluster;
    }

}
