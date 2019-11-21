package com.xxx.xcloud.module.component.service.worker.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;

@Service
@Scope("prototype")
public class FtpNodeStartWorker extends BaseFtpClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(FtpNodeStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============FtpNodeStartWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            componentOptNodeBase(CommonConst.APPTYPE_FTP, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空,serviceId:" + node.getServiceId());
            return;
        }

        // 4、拼接ftp cluster
        FtpCluster ftpCluster = updateYamlForFtpCluster(tenantName, service.getServiceName(),
                FtpClusterConst.OPERATOR_NODE_START, node.getNodeName());

        // 5、调用k8sclient启动节点并循环获取启动结果
        if (updateAndRetry(tenantName, ftpCluster) && checkNodeStartOrStopResult(tenantName, service.getServiceName(),
                FtpClusterConst.OPERATOR_NODE_START, CommonConst.STATE_NODE_RUNNING, node.getNodeName())) {
            componentOptNodeBase(CommonConst.APPTYPE_FTP, nodeId, CommonConst.STATE_NODE_RUNNING);
            updateServiceExtendedField(tenantName, service.getId(), service.getServiceName());
            return;
        }

        LOG.error("ftp节点" + node.getNodeName() + "启动失败！");
        componentOptNodeBase(CommonConst.APPTYPE_FTP, nodeId, CommonConst.STATE_NODE_FAILED);
    }

}
