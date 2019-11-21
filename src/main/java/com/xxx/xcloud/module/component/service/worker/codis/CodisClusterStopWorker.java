package com.xxx.xcloud.module.component.service.worker.codis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;

@Service
@Scope("prototype")
public class CodisClusterStopWorker extends BaseCodisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(CodisClusterStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============CodisClusterStopWorker====================");
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
        String serviceName = service.getServiceName();

        // 3、拼接codisCluster
        CodisCluster codisCluster = buildCodisCluster(tenantName, serviceName);

        // 4、调用k8sclient停止集群
        if (!updateAndRetry(tenantName, codisCluster)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.info("codis集群停止失败！");
            return;
        }

        // 5、循环获取停止结果

        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(), CommonConst.OPT_CLUSTER_STOP,
                CommonConst.STATE_CLUSTER_STOPPED)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_STOPPED,
                    CommonConst.STATE_NODE_STOPPED, null);
            return;
        }
        componentOperationsClientUtil.changeCodisClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
    }

    /**
     * 停止集群：构建codisCluster
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    private CodisCluster buildCodisCluster(String tenantName, String serviceName) {

        CodisCluster codisCluster = null;
        codisCluster = componentOperationsClientUtil.getCodisCluster(tenantName, serviceName);
        if (null == codisCluster) {
            LOG.error("获取codisCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        codisCluster.getSpec().setStopped(true);
        LOG.info("停止集群：构建codisCluster成功, serviceName:" + serviceName);
        return codisCluster;
    }

}
