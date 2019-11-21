package com.xxx.xcloud.module.component.service.worker.storm;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.storm.StormCluster;

@Service
@Scope("prototype")
public class StormNodeStartWorker extends BaseStormClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(StormNodeStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============StormNodeStartWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            componentOptNodeBase(CommonConst.APPTYPE_STORM, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空,serviceId:" + node.getServiceId());
            return;
        }

        // 4、拼接storm cluster
        StormCluster newCluster = updateYamlForStormCluster(tenantName, service.getServiceName(),
                StormClusterConst.OPERATOR_NODE_START, node.getNodeName(), 0);

        // 5、调用k8sclient启动节点并循环获取启动结果
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("storm节点" + node.getNodeName() + "启动失败！");
            componentOptNodeBase(CommonConst.APPTYPE_STORM, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        if (checkNodeStartOrStopResult(tenantName, service.getServiceName(), StormClusterConst.OPERATOR_NODE_START,
                CommonConst.STATE_NODE_RUNNING, node.getNodeName())) {
            LOG.info("storm节点启动成功，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        } else {
            LOG.error("storm节点启动失败，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        }

        optNodeStart(tenantName, service.getId(), service.getServiceName());

    }

    /**
     * 处理节点启动
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    private void optNodeStart(String tenantName, String serviceId, String serviceName) {
        // 获取stormCluster
        StormCluster stormCluster = componentOperationsClientUtil.getStormCluster(tenantName, serviceName);

        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(stormCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(stormCluster);

        componentOperationsClientUtil.changeStormClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId,
                serviceName, serviceExtendedField, nodesExtendedField);
    }

}
