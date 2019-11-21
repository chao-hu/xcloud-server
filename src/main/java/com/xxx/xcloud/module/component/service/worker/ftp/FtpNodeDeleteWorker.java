package com.xxx.xcloud.module.component.service.worker.ftp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;
import com.xxx.xcloud.module.component.model.ftp.FtpNode;

@Service
@Scope("prototype")
public class FtpNodeDeleteWorker extends BaseFtpClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(FtpNodeDeleteWorker.class);

    @Override
    public void execute() {
        LOG.info("===============FtpNodeDeleteWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空，serviceId：" + node.getServiceId());
            return;
        }

        // 4、获取ftp cluster
        FtpCluster oldCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
        if (null == oldCluster || null == oldCluster.getStatus()) {
            LOG.error("根据serviceName获取oldCluster为空，或oldCluster不包含该节点，serviceId:" + node.getServiceName()
                    + ",oldCluster:" + JSON.toJSONString(oldCluster));
            optNodeDelete(tenantName, node);
            return;
        }

        Map<String, FtpNode> serverNodes = oldCluster.getStatus().getServerNodes();
        if (null == serverNodes || serverNodes.isEmpty() || !serverNodes.containsKey(node.getNodeName())) {
            LOG.info("节点已经被删除，nodeId：" + nodeId + ",nodeName:" + node.getNodeName());
            optNodeDelete(tenantName, node);
            return;
        }

        FtpCluster newCluster = updateYamlForFtpCluster(tenantName, service.getServiceName(),
                FtpClusterConst.OPERATOR_NODE_DELETE, node.getNodeName());

        // 5、调用k8sclient删除节点并循环获取删除结果
        if (updateAndRetry(tenantName, newCluster) && checkNodeDeleteResult(tenantName, service, node.getNodeName())) {
            optNodeDelete(tenantName, node);
            return;
        }

        LOG.error("ftp节点" + node.getNodeName() + "删除失败！");
        componentOptNodeBase(CommonConst.APPTYPE_FTP, nodeId, CommonConst.STATE_NODE_FAILED);
    }

    /**
     * 检查节点删除结果
     * 
     * @param tenantName
     * @param service
     * @param nodeName
     * @return
     */
    private boolean checkNodeDeleteResult(String tenantName, StatefulService service, String nodeName) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时：" + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                return false;
            }

            if ((System.currentTimeMillis() - start) > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("删除节点超时，service：" + JSON.toJSONString(service.getServiceName()));
                return false;
            }

            FtpCluster ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
            if (null == ftpCluster) {
                LOG.info("获取的集群为空!");
                return true;
            }

            FtpNode server = null;
            if (null != ftpCluster.getStatus() && null != ftpCluster.getStatus().getServerNodes()) {
                server = ftpCluster.getStatus().getServerNodes().get(nodeName);
            }
            if (null == server) {
                LOG.info("===========删除pod成功===========");
                return true;
            }
        }
    }

    /**
     * 处理节点删除结果
     * 
     * @param tenantName
     * @param nodeId
     * @param nodeState
     */
    private void ftpOptNodeDelete(String tenantName, String nodeId, String nodeState) {
        try {
            StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
            if (null != node && !CommonConst.STATE_NODE_DELETED.equals(node.getNodeState())) {
                node.setNodeState(nodeState);
                statefulNodeRepository.save(node);
                // 修改tenant表
//                boolean result = tenantService.updateUsedResource(tenantName, node.getCpu() * (-1),
//                        node.getMemory() * (-1), node.getStorage() * (-1));
//                LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + node.getCpu() * (-1) + ",updateMemory:"
//                        + node.getMemory() * (-1) + ",updateCapacity:" + node.getStorage() * (-1));
            }

            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
            if (null != service) {
                service.setServiceState(componentOperationsClientUtil.getZkClusterStateByNodes(node.getServiceId(),
                        service.getLastopt()));
                service.setNodeNum(service.getNodeNum() - 1);
                if (null != node) {
                    service.setCpu(service.getCpu() - node.getCpu());
                    service.setMemory(service.getMemory() - node.getMemory());
                    service.setStorage(service.getStorage() - node.getStorage());
                }
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("根据nodeId修改集群和节点状态失败，nodeId:" + nodeId + ",error:", e);
        }
    }

    /**
     * 处理节点删除结果
     * 
     * @param tenantName
     * @param service
     * @param node
     */
    private void optNodeDelete(String tenantName, StatefulNode node) {
        ftpOptNodeDelete(tenantName, node.getId(), CommonConst.STATE_NODE_DELETED);
        componentOperationsClientUtil.deleteLvm(tenantName, node.getLvmName());
    }
}
