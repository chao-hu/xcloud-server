package com.xxx.xcloud.module.component.service.worker.ftp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.entity.SelectorLabel;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;
import com.xxx.xcloud.module.component.model.ftp.FtpConfig;
import com.xxx.xcloud.module.component.model.ftp.FtpNode;
import com.xxx.xcloud.module.component.model.ftp.FtpOp;
import com.xxx.xcloud.module.component.model.ftp.FtpSpec;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;
import com.xxx.xcloud.utils.StringUtils;

@Service
@Scope("prototype")
public class FtpClusterCreateWorker extends BaseFtpClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(FtpClusterCreateWorker.class);

    @Override
    public void execute() {
        LOG.info("===============FtpClusterCreateWorker====================");
        // 1、获取数据
        String tenantName = data.get("tenantName");
        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String serviceId = data.get("serviceId");
        String serviceName = data.get("serviceName");
        String version = data.get("version");
        String userName = data.get("userName");
        String password = data.get("password");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = Integer.parseInt(data.get("replicas"));
        String performance = data.get("performance");

        // 2、 获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (Exception e) {
            LOG.error("集群创建时获取service失败，error：" + e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群创建时获取的service：" + JSON.toJSONString(service));

        // 3、拼接ftpCluster
        FtpCluster ftpCluster = buildFtpCluster(tenantName, projectId, orderId, version, serviceName, userName,
                password, cpu, memory, capacity, replicas, performance);

        // 4、调用k8s client创建
        if (!createAndRetry(tenantName, ftpCluster)) {
            componentOperationsDataBaseUtil.updateClusterState(serviceId, CommonConst.STATE_CLUSTER_FAILED, null, null);
            LOG.error("ftp集群" + serviceName + "创建失败！");
            return;
        }

        // 5、循环获取创建结果
        checkCreateResult(tenantName, service, cpu, memory, capacity);
    }

    /**
     * 拼接ftp cluster
     *
     * @param tenantName
     * @param projectId
     * @param orderId
     * @param version
     * @param serviceName
     * @param userName
     * @param password
     * @param cpu
     * @param memory
     * @param capacity
     * @param replicas
     * @param performance
     * @return
     */
    private FtpCluster buildFtpCluster(String tenantName, String projectId, String orderId, String version,
            String serviceName, String userName, String password, Double cpu, Double memory, Double capacity,
            int replicas, String performance) {
        FtpCluster ftpCluster = new FtpCluster();
        ftpCluster.getMetadata().setName(serviceName);
        Map<String, String> labels = componentOperationsClientUtil.getMetadataLabels(projectId, orderId);
        if (null != labels && !labels.isEmpty()) {
            ftpCluster.getMetadata().setLabels(labels);
        }

        FtpSpec spec = new FtpSpec();
        String ftpImage = getRepoPath(CommonConst.APPTYPE_FTP, null, version);

        /* FtpOp */
        FtpOp ftpOp = new FtpOp();
        ftpOp.setOperator(FtpClusterConst.OPERATOR_CLUSTER_CREATE);
        ftpOp.setName(userName);
        spec.setFtpop(ftpOp);

        spec.setVersion(version);
        spec.setImage(ftpImage);
        spec.setHealthcheck(false);

        String ftpUserStr = data.get("ftpUser");
        FtpUser ftpUser = JSON.parseObject(ftpUserStr, FtpUser.class);
        Map<String, String> userConfig = componentOperationsClientUtil.buildUserConfig(ftpUser, serviceName);
        FtpConfig config = new FtpConfig();
        if (null != userConfig && !userConfig.isEmpty()) {
            config.setUsercnf(userConfig);
            spec.setConfig(config);
        }

        spec.setReplicas(replicas);

        /* resources */
        Resources resources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);

        spec.setResources(resources);
        spec.setCapacity(capacity + CommonConst.UNIT_GI);
        spec.setVolume(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
        spec.setUpdatetime(String.valueOf(new Date()));

        /* nodeSelector */
        Map<String, String> nodeSelector = new HashMap<>();
        List<SelectorLabel> labelList = selectorLabelRepository.findByTypeAndEnableTrue(CommonConst.APPTYPE_FTP);
        if (labelList != null && labelList.size() > 0) {
            for (SelectorLabel label : labelList) {
                nodeSelector.put(label.getLabelKey(), label.getLabelValue());
            }
        }
        boolean nodeSelectorPerformance = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.NODESELECTOR_PERFORMANCE));
        if (StringUtils.isNotEmpty(performance) && nodeSelectorPerformance) {
            nodeSelector.put(CommonConst.NODESELECTOR_PERFORMANCE, performance);
        }
        if (!nodeSelector.isEmpty()) {
            spec.setNodeSelector(nodeSelector);
        }

        boolean componentSchedulerLvm = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_SCHEDULER_LVM));
        if (componentSchedulerLvm) {
            spec.setSchedulerName(CommonConst.LVM_SCHEDULER);
        }

        // 4、spec
        ftpCluster.setSpec(spec);
        return ftpCluster;
    }

    /**
     * 调k8s创建集群
     *
     * @param tenantName
     * @param ftpCluster
     * @return
     */
    private boolean createAndRetry(String tenantName, FtpCluster ftpCluster) {
        LOG.info("==========开始创建集群==========");
        LOG.info("创建ftpCluster：" + JSON.toJSONString(ftpCluster));
        if (null != ftpCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = createFtpCluster(tenantName, ftpCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("创建ftp集群" + ftpCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检查集群创建结果
     *
     * @param tenantName
     * @param service
     * @param cpu
     * @param memory
     * @param capacity
     */
    private void checkCreateResult(String tenantName, StatefulService service, Double cpu, Double memory,
            Double capacity) {
        long start = System.currentTimeMillis();
        boolean hasNodes = false;
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时：" + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                break;
            }
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("创建超时，service：" + JSON.toJSONString(service));
                break;
            }
            FtpCluster ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
            LOG.info("判断ftpCluster中数据是否存在");
            LOG.info("ftpCluster:" + JSON.toJSONString(ftpCluster));
            LOG.info("status:" + ftpCluster.getStatus());
            if (null != ftpCluster && null != ftpCluster.getStatus() && null != ftpCluster.getStatus().getServerNodes()
                    && null != ftpCluster.getStatus().getServerNodes().keySet() && !hasNodes
                    && service.getNodeNum() <= ftpCluster.getStatus().getServerNodes().keySet().size()) {
                if (buildNodes(ftpCluster, service, cpu, memory, capacity)) {
                    LOG.info("节点表插入数据成功,集群名称：" + ftpCluster.getMetadata().getName());
                    hasNodes = true;
                }
            }
            if (null != ftpCluster && null != ftpCluster.getStatus()) {
                LOG.info("cluster此时的状态：" + ftpCluster.getStatus().getPhase());
            }
            if (hasNodes && null != ftpCluster && null != ftpCluster.getStatus()
                    && null != ftpCluster.getStatus().getServerNodes()
                    && null != ftpCluster.getStatus().getServerNodes().keySet()
                    && service.getNodeNum() <= ftpCluster.getStatus().getServerNodes().keySet().size()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(ftpCluster.getStatus().getPhase())) {

                LOG.info("ftp集群创建成功，tenantName:" + tenantName + ",serviceName:" + service.getServiceName());
                break;
            }
        }

        // 检查结束
        optClusterCreate(tenantName, service.getId(), service.getServiceName(), hasNodes);

    }

    private void optClusterCreate(String tenantName, String serviceId, String serviceName, boolean hasNodes) {
        // 获取ftpCluster
        FtpCluster ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, serviceName);

        if (hasNodes) {
            // 注册逻辑卷
            registeClusterLvm(tenantName, ftpCluster);
        }
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(ftpCluster);

        // 修改数据库中集群、节点状态
        componentOperationsClientUtil.changeFtpClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName,
                null, nodesExtendedField);
    }

    /**
     * 保存节点信息
     *
     * @param cluster
     * @param service
     * @param cpu
     * @param memory
     * @param capacity
     * @return
     */
    private boolean buildNodes(FtpCluster cluster, StatefulService service, Double cpu, Double memory,
            Double capacity) {
        if (null == cluster.getStatus()) {
            LOG.info("节点表插入时从cluster中获取status为空");
            return false;
        }
        LOG.info("==========开始插入节点表数据==========");

        StatefulNode node = null;
        for (Map.Entry<String, FtpNode> entry : cluster.getStatus().getServerNodes().entrySet()) {
            try {
                StatefulNode oldNode = statefulNodeRepository.findByServiceIdAndNodeNameAndNodeState(service.getId(),
                        entry.getValue().getName(), CommonConst.STATE_NODE_WAITING);
                if (null != oldNode) {
                    LOG.info("node已经存在，StatefulNode:" + JSON.toJSONString(oldNode));
                    continue;
                }
                node = new StatefulNode();
                node.setCpu(cpu);
                node.setMemory(memory);
                node.setStorage(capacity);

                node.setAppType(CommonConst.APPTYPE_FTP);
                node.setServiceId(service.getId());
                node.setServiceName(service.getServiceName());

                node.setNodeName(entry.getKey());
                node.setRole(entry.getValue().getRole());
                node.setLvmName(entry.getValue().getVolumeid());

                node.setNodeState(CommonConst.STATE_NODE_WAITING);
                node.setCreateTime(new Date());

                statefulNodeRepository.save(node);
            } catch (Exception e) {
                LOG.error("保存节点记录信息失败,data:" + JSON.toJSONString(node) + ",error:", e);
                return false;
            }
        }
        return true;
    }

    /**
     * ftp集群注册lvm
     *
     * @param tenantName
     * @param ftpCluster
     */
    private void registeClusterLvm(String tenantName, FtpCluster ftpCluster) {
        LOG.info("创建lvm,接收的参数=>tenantName:" + tenantName + ",ftpCluster:" + JSON.toJSONString(ftpCluster));
        for (Map.Entry<String, FtpNode> entry : ftpCluster.getStatus().getServerNodes().entrySet()) {
            if (CommonConst.STATE_NODE_RUNNING.equals(entry.getValue().getStatus())) {
                componentOperationsClientUtil.registerLvm(tenantName, entry.getValue().getVolumeid(),
                        entry.getValue().getHostip(), ftpCluster.getSpec().getCapacity());
            }
        }
    }

    /**
     * 获取节点扩展字段
     *
     * @param ftpCluster
     * @return
     */
    private Map<String, Map<String, String>> buildNodesExtendedField(FtpCluster ftpCluster) {
        Map<String, Map<String, String>> nodesExtendedField = new HashMap<>();
        Map<String, FtpNode> serverNodes = ftpCluster.getStatus().getServerNodes();
        for (Map.Entry<String, FtpNode> entry : serverNodes.entrySet()) {
            Map<String, String> nodeMap = new HashMap<>();
            if (StringUtils.isNotEmpty(entry.getValue().getHostip())) {
                nodeMap.put("ip", entry.getValue().getHostip());
            }

            Map<String, String> nodePorts = entry.getValue().getNodeport();
            if (StringUtils.isNotEmpty(nodePorts.get("ftp-server-access-port"))) {
                nodeMap.put(FtpClusterConst.ACCESS_PORT, nodePorts.get("ftp-server-access-port"));
            }
            if (StringUtils.isNotEmpty(nodePorts.get("ftp-server-passive-port"))) {
                nodeMap.put(FtpClusterConst.PASSIVE_PORT, nodePorts.get("ftp-server-passive-port"));
            }

            if (!nodeMap.isEmpty()) {
                nodesExtendedField.put(entry.getKey(), nodeMap);
            }
        }
        return nodesExtendedField;
    }

}
