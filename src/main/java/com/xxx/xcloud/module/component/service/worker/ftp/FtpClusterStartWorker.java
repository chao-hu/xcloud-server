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
public class FtpClusterStartWorker extends BaseFtpClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(FtpClusterStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============FtpClusterStartWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群启动时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群启动时获取的service：" + JSON.toJSONString(service));

        // 3、拼接ftpCluster
        FtpCluster ftpCluster = updateYamlForFtpCluster(tenantName, service.getServiceName(),
                FtpClusterConst.OPERATOR_CLUSTER_START, null);

        // 4、调用k8sclient启动集群
        if (!updateAndRetry(tenantName, ftpCluster)) {
            LOG.error("ftp集群" + service.getServiceName() + "启动失败！");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            return;
        }

        // 5、循环获取集群启动结果
        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(), FtpClusterConst.OPERATOR_CLUSTER_START,
                CommonConst.STATE_CLUSTER_RUNNING)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_RUNNING,
                    CommonConst.STATE_NODE_RUNNING, null);
            updateServiceExtendedField(tenantName, serviceId, service.getServiceName());
            return;
        }
        componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());
    }

}
