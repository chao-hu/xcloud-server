package com.xxx.xcloud.module.component.service.worker.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;
@Service
@Scope("prototype")
public class EsNodeStopWorker extends BaseEsClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(EsNodeStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============EsNodeStopWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            updateEsNodeStateAndClusterState(nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空,serviceId:" + node.getServiceId());
            return;
        }

        // 4、拼接es cluster
        EsCluster esCluster = buildEsCluster(tenantName, service.getServiceName(), node.getNodeName());

        // 5、调用k8sclient停止节点并循环获取停止结果
        if (updateAndRetry(tenantName, esCluster) && checkNodeStartOrStopResult(tenantName, service.getServiceName(),
                EsClusterConst.ES_NODE_OPT_STOP, CommonConst.STATE_NODE_STOPPED, node.getNodeName())) {
            updateEsNodeStateAndClusterState(nodeId, CommonConst.STATE_NODE_STOPPED);
            return;
        }

        LOG.error("es节点" + node.getNodeName() + "停止失败！");
        updateEsNodeStateAndClusterState(nodeId, CommonConst.STATE_NODE_FAILED);
    }

    /**
     * 停止节点：构建esCluster
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
        esCluster.getSpec().setOpt(EsClusterConst.ES_NODE_OPT_STOP);
        esCluster.getSpec().setOptNodename(nodeName);
        LOG.info("停止节点：构建esCluster成功, serviceName:" + serviceName);
        return esCluster;
    }

}
