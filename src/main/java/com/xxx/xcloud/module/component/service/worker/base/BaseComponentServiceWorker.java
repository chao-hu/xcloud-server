package com.xxx.xcloud.module.component.service.worker.base;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceUnitVersion;
import com.xxx.xcloud.module.component.repository.SelectorLabelRepository;
import com.xxx.xcloud.module.component.repository.StatefulNodeRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceUnitVersionRepository;
import com.xxx.xcloud.module.component.util.ComponentOperationsClientUtil;
import com.xxx.xcloud.module.component.util.ComponentOperationsDataBaseUtil;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.utils.StringUtils;

/**
 * @ClassName: BaseComponentServiceWorker
 * @Description: BaseComponentServiceWorker
 * @author lnn
 * @date 2019年11月19日
 *
 */
public abstract class BaseComponentServiceWorker extends BaseThreadWorker<Map<String, String>> {

    private static Logger LOG = LoggerFactory.getLogger(BaseComponentServiceWorker.class);

    // public static String VGNAME = BdosProperties.getLvm_vgname();

    // protected static boolean NODESELECTOR_COMPONENT =
    // Boolean.parseBoolean(BdosProperties.getNodeselector_component());

    // protected static boolean NODESELECTOR_PERFORMANCE = Boolean
    // .parseBoolean(BdosProperties.getNodeselector_performance());

    // protected static boolean COMPONENT_SCHEDULER_LVM = Boolean
    // .parseBoolean(BdosProperties.getComponent_scheduler_lvm());

    @Autowired
    protected ComponentOperationsDataBaseUtil componentOperationsDataBaseUtil;

    @Autowired
    protected ComponentOperationsClientUtil componentOperationsClientUtil;

    @Autowired
    @Qualifier("tenantServiceImplV1")
    protected ITenantService tenantService;
    
    @Autowired
    protected StatefulServiceRepository statefulServiceRepository;

    @Autowired
    protected StatefulNodeRepository statefulNodeRepository;

    @Autowired
    protected StatefulServiceUnitVersionRepository statefulServiceUnitVersionRepository;
    
    @Autowired
    protected SelectorLabelRepository selectorLabelRepository;

    /**
     * 获取服务版本对应的镜像
     * 
     * @param version
     * @return
     */
    protected String getRepoPath(String appType, String extendedField, String version) {
        LOG.info("获取服务版本对应的镜像，接收的参数=>appType:" + appType + ",extendedField:" + extendedField + ",version:" + version);

        StatefulServiceUnitVersion unitVersion = null;
        try {
            if (!StringUtils.isEmpty(extendedField)) {
                unitVersion = statefulServiceUnitVersionRepository.findByAppTypeAndExtendedFieldAndVersion(appType,
                        extendedField, version);

            } else {
                unitVersion = statefulServiceUnitVersionRepository
                        .findByAppTypeAndExtendedFieldIsNullAndVersion(appType, version);
            }
        } catch (Exception e) {
            LOG.error("获取仓库地址失败：", e);
            return null;
        }

        if (null == unitVersion) {
            LOG.error("获取到的unitVersion为null！");
            return null;
        }
        LOG.info("查询到的unitVersion：" + JSON.toJSONString(unitVersion));
        return unitVersion.getVersionPath();
    }

    /**
     * 判断是否超时
     * 
     * @param startTime
     * @param opt
     * @return
     */
    private boolean isTimeOut(long startTime, String opt) {
        try {
            Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
        } catch (InterruptedException e) {
            LOG.error("Thread sleep exception！", e);
        }

        long endTime = System.currentTimeMillis();
        LOG.info("操作：" + opt + "已经用时：" + (endTime - startTime));
        if (endTime - startTime > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
            LOG.error("---{" + opt + "}---operation timeout！---");
            return true;
        }
        return false;
    }

    /**
     * 检查K8s集群yaml状态是否符合预期
     *
     * @param nameSpace
     * @return
     */
    protected boolean checkClusterStartOrStopResult(String tenantName, String serviceName, String opt,
            String targetClusterStatus) {

        long startTime = System.currentTimeMillis();

        while (true) {

            if (isTimeOut(startTime, opt)) {
                LOG.error("获取集群操作结果超时，操作：" + opt + "，serviceName:" + serviceName + ",tenantName:" + tenantName);
                return false;
            }
            String clusterStatus = getStatusFromYaml(tenantName, serviceName, null);
            if (targetClusterStatus.equalsIgnoreCase(clusterStatus)) {
                return true;
            }
        }
    }

    /**
     * 检查K8s集群yaml状态是否符合预期
     *
     * @param nameSpace
     * @return
     */
    protected boolean checkNodeStartOrStopResult(String tenantName, String serviceName, String opt,
            String targetClusterStatus, String nodeName) {

        long startTime = System.currentTimeMillis();

        while (true) {

            if (isTimeOut(startTime, opt)) {
                LOG.error("获取节点操作结果超时，操作：" + opt + "，serviceName:" + serviceName + ",tenantName:" + tenantName
                        + ",nodeName:" + nodeName);
                return false;
            }
            String nodeStatus = getStatusFromYaml(tenantName, serviceName, nodeName);
            if (targetClusterStatus.equalsIgnoreCase(nodeStatus)) {
                return true;
            }
        }
    }

    /**
     * 获取状态
     * 
     * @param tenantName
     * @param serviceName
     * @param nodeName
     * @return
     */
    protected abstract String getStatusFromYaml(String tenantName, String serviceName, String nodeName);

    /**
     * 根据nodeId修改集群和节点状态 用于mysql、storm、kafka节点操作
     * 
     * @param nodeId
     * @param nodeState
     */
    protected void componentOptNodeBase(String appType, String nodeId, String nodeState) {
        try {
            StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
            if (null != node && !CommonConst.STATE_NODE_DELETED.equals(node.getNodeState())) {
                node.setNodeState(nodeState);
                statefulNodeRepository.save(node);
            }

            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
            if (null != service) {
                if (CommonConst.APPTYPE_ZK.equals(appType)) {
                    service.setServiceState(componentOperationsClientUtil.getZkClusterStateByNodes(node.getServiceId(),
                            service.getLastopt()));
                } else if (CommonConst.APPTYPE_MYSQL.equals(appType) || CommonConst.APPTYPE_STORM.equals(appType)
                        || CommonConst.APPTYPE_KAFKA.equals(appType) || CommonConst.APPTYPE_FTP.equals(appType)
                        || CommonConst.APPTYPE_POSTGRESQL.equals(appType)) {
                    service.setServiceState(componentOperationsClientUtil.getClusterStateByNodes(node.getServiceId(),
                            service.getLastopt()));
                }
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("根据nodeId修改集群和节点状态失败，nodeId:" + nodeId + ",error:", e);
        }
    }
}
