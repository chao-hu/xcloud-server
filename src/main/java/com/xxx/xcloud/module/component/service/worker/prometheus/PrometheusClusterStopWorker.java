package com.xxx.xcloud.module.component.service.worker.prometheus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.PrometheusClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusCluster;

@Service
@Scope("prototype")
public class PrometheusClusterStopWorker extends BasePrometheusClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(PrometheusClusterStopWorker.class);

    @Override
    public void execute() {
        LOG.info("===============PrometheusClusterStopWorker====================");
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
        String serviceName = service.getServiceName();

        // 3、拼接prometheusCluster
        PrometheusCluster prometheusCluster = buildPrometheusCluster(tenantName, serviceName);

        // 4、调用k8sclient停止集群
        if (!updateAndRetry(tenantName, prometheusCluster)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.info("prometheus集群启动失败！");
            return;
        }

        // 5、循环获取停止结果

        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(),
                PrometheusClusterConst.PROMETHEUS_CLUSTER_OPT_STOP, CommonConst.STATE_CLUSTER_STOPPED)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_STOPPED,
                    CommonConst.STATE_CLUSTER_STOPPED, null);
            return;
        }
        componentOperationsClientUtil.changePrometheusClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
    }

    /**
     * 停止集群：构建prometheusCluster
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    private PrometheusCluster buildPrometheusCluster(String tenantName, String serviceName) {

        PrometheusCluster prometheusCluster = null;
        prometheusCluster = componentOperationsClientUtil.getPrometheusCluster(tenantName, serviceName);
        if (null == prometheusCluster) {
            LOG.error("获取prometheusCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        prometheusCluster.getSpec().setOpt(PrometheusClusterConst.PROMETHEUS_CLUSTER_OPT_STOP);
        LOG.info("停止集群：构建prometheusCluster成功, serviceName:" + serviceName);
        return prometheusCluster;
    }

}
