package com.xxx.xcloud.module.component.service.worker.es;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.es.EsCluster;

@Service
@Scope("prototype")
public class EsClusterStartWorker extends BaseEsClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(EsClusterStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============EsClusterStopWorker====================");
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

        // 3、拼接esCluster
        EsCluster esCluster = buildEsCluster(tenantName, serviceName);

        // 4、调用k8sclient启动集群
        if (!updateAndRetry(tenantName, esCluster)) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.info("es集群启动失败！");
            return;
        }

        // 5、循环获取启动结果

        if (checkClusterStartOrStopResult(tenantName, service.getServiceName(), EsClusterConst.ES_CLUSTER_OPT_START,
                CommonConst.STATE_CLUSTER_RUNNING)) {
            Map<String, String> serviceExtendedField = buildServiceExtendedField(tenantName, service.getServiceName());

            componentOperationsDataBaseUtil.updateClusterAndNodesState(service.getId(), CommonConst.STATE_CLUSTER_RUNNING,
                    CommonConst.STATE_NODE_RUNNING, serviceExtendedField, null);
            return;
        }
        componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);
    }

    /**
     * 停止集群：构建esCluster
     * 
     * @param tenantName
     * @param serviceName
     * @return
     */
    private EsCluster buildEsCluster(String tenantName, String serviceName) {

        EsCluster esCluster = null;
        esCluster = componentOperationsClientUtil.getEsCluster(tenantName, serviceName);
        if (null == esCluster) {
            LOG.error("获取esCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        esCluster.getSpec().setOpt(EsClusterConst.ES_CLUSTER_OPT_START);
        LOG.info("启动集群：构建esCluster成功, serviceName:" + serviceName);
        return esCluster;
    }

}
