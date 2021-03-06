package com.xxx.xcloud.module.component.service.worker.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;

@Service
@Scope("prototype")
public class FtpClusterStopWorker extends BaseFtpClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(FtpClusterStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============FtpClusterStopWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群停止时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群停止时获取的service：" + JSON.toJSONString(service));

        // 3、拼接ftpCluster
        FtpCluster ftpCluster = updateYamlForFtpCluster(tenantName, service.getServiceName(),
                FtpClusterConst.OPERATOR_CLUSTER_STOP, null);

        // 4、调用k8sclient停止集群
        if (!updateAndRetry(tenantName, ftpCluster)) {
            LOG.error("ftp集群" + service.getServiceName() + "停止失败！");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            return;
        }

        // 5、循环获取集群停止结果
        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(), FtpClusterConst.OPERATOR_CLUSTER_STOP,
                CommonConst.STATE_CLUSTER_STOPPED)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_STOPPED,
                    CommonConst.STATE_NODE_STOPPED, null);
            return;
        }
        componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());

    }

}
