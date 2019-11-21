package com.xxx.xcloud.module.component.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CodisClusterConst;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.consts.PrometheusClusterConst;
import com.xxx.xcloud.module.component.consts.RedisClusterConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.MemoryCpu;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;
import com.xxx.xcloud.module.component.model.codis.CodisGroupBindingNode;
import com.xxx.xcloud.module.component.model.codis.CodisGroupStatus;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.model.es.EsInstance;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;
import com.xxx.xcloud.module.component.model.ftp.FtpNode;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;
import com.xxx.xcloud.module.component.model.kafka.KafkaCluster;
import com.xxx.xcloud.module.component.model.kafka.KafkaNode;
import com.xxx.xcloud.module.component.model.lvm.LvmSpec;
import com.xxx.xcloud.module.component.model.lvm.Lvm;
import com.xxx.xcloud.module.component.model.memcached.MemcachedCluster;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterGroupInfo;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterServer;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlPhpMyAdmin;
import com.xxx.xcloud.module.component.model.mysql.MysqlServer;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlInstance;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusCluster;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusInstances;
import com.xxx.xcloud.module.component.model.redis.RedisBindingNode;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;
import com.xxx.xcloud.module.component.model.storm.StormCluster;
import com.xxx.xcloud.module.component.model.storm.StormNode;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;
import com.xxx.xcloud.module.component.model.zookeeper.ZkInstance;
import com.xxx.xcloud.module.component.repository.StatefulNodeRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceRepository;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
 * @ClassName: ComponentOperationsClientUtil
 * @Description: ComponentOperationsClientUtil
 * @author lnn
 * @date 2019年11月18日
 *
 */
@Service
public class ComponentOperationsClientUtil {

    private static Logger LOG = LoggerFactory.getLogger(ComponentOperationsClientUtil.class);

    @Autowired
    private StatefulServiceRepository statefulServiceRepository;

    @Autowired
    private StatefulNodeRepository statefulNodeRepository;

    @Autowired
    private ComponentOperationsDataBaseUtil componentOperationsDataBaseUtil;

    /**
     * 构造resources对象
     *
     * @param cpu
     * @param memory
     * @param memoryUnit
     * @return
     */
    public Resources getResources(Double cpu, Double memory, String memoryUnit) {
        Resources resources = new Resources();
        MemoryCpu requests = new MemoryCpu();
        MemoryCpu limits = new MemoryCpu();
        requests.setCpu(String
                .valueOf(cpu / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTCPU))));
        requests.setMemory(
                memory / Integer.parseInt(XcloudProperties.getConfigMap().get(Global.RATIO_LIMITTOREQUESTMEMORY))
                        + memoryUnit);
        limits.setCpu(String.valueOf(cpu));
        limits.setMemory(memory + memoryUnit);
        resources.setRequests(requests);
        resources.setLimits(limits);

        return resources;
    }

    /**
     * 组件labels中添加monitor_project和monitor_order标签
     *
     * @param projectId
     * @param orderId
     * @return
     */
    public Map<String, String> getMetadataLabels(String projectId, String orderId) {
        Map<String, String> labels = new HashMap<>();
        boolean labelsProjectid = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_LABELS_PROJECTID));
        boolean labelsOrderid = Boolean
                .parseBoolean(XcloudProperties.getConfigMap().get(Global.COMPONENT_LABELS_ORDERID));

        if (labelsProjectid && StringUtils.isNotEmpty(projectId)) {
            labels.put(CommonConst.LABELS_MONITOR_PROJCETID, projectId);
        }

        if (labelsOrderid && StringUtils.isNotEmpty(orderId)) {
            labels.put(CommonConst.LABELS_MONITOR_ORDER, orderId);
        }
        return labels;
    }

    /**
     * 查询lvm
     *
     * @param namespace
     * @param lvmname
     * @return
     */
    public Lvm getLvm(String tenantName, String lvmName) {
        LOG.info("获取lvm，tanantName:" + tenantName + ",lvmName:" + lvmName);
        // 1、参数校验
        if (StringUtils.isEmpty(lvmName)) {
            LOG.error("lvmname is empty!");
            return null;
        }
        try {
            return ComponentClientFactory.getLvmClient().inNamespace(tenantName).withName(lvmName).get();
        } catch (Exception e) {
            LOG.error("获取lvm失败，lvmName:" + lvmName + ",error:", e);
            return null;
        }
    }

    /**
     * 修改lvm
     *
     * @param tenantName
     * @param lvm
     * @return
     */
    public boolean replaceLvm(String tenantName, Lvm lvm) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }
        if (null == lvm) {
            LOG.error("Lvm is empty!");
            return false;
        }
        try {
            Lvm updatedLvm = ComponentClientFactory.getLvmClient().inNamespace(tenantName).createOrReplace(lvm);
            if (null != updatedLvm) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("修改lvm失败：", e);
            return false;
        }
    }

    /**
     * 为node注册lvm
     *
     * @param namespaces
     * @param lvName
     * @param exterHost
     * @param storage
     */
    public void registerLvm(String tenantName, String lvName, String exterHost, String storage) {
        registerLvm(tenantName, lvName, exterHost, storage, CommonConst.UNIT_GI);
    }

    /**
     * 为node注册lvm
     *
     * @param namespaces
     * @param lvName
     * @param exterHost
     * @param storage
     */
    public void registerLvm(String tenantName, String lvName, String exterHost, String storage, String storageUnit) {
        Lvm oldLvm = getLvm(tenantName, lvName);
        LOG.info("获取旧的lvm：" + JSON.toJSONString(oldLvm));
        LOG.info("storageUnit:" + storageUnit);
        if (null == oldLvm) {
            // 1、拼lvm参数
            Lvm lvm = new Lvm();
            ObjectMeta metadata = new ObjectMeta();
            LvmSpec spec = new LvmSpec();
            spec.setHost(exterHost);
            spec.setLvName(lvName);
            spec.setMessage("");
            spec.setPath("");
            if (StringUtils.isNotEmpty(storageUnit) && CommonConst.UNIT_MI.equals(storageUnit)) {
                spec.setSize(storage);
            } else {
                spec.setSize(StringUtils.unitExchange(storage));
            }
            spec.setVgName(XcloudProperties.getConfigMap().get(Global.LVM_VGNAME));
            metadata.setName(lvName);
            lvm.setMetadata(metadata);
            lvm.setSpec(spec);
            lvm.setApiVersion(CommonConst.LVM_API_VERSION);
            lvm.setKind(CommonConst.LVM_KIND);

            createLvm(tenantName, lvm);
        }

    }

    /**
     * 创建lvm
     *
     * @param tenantName
     * @param lvm
     */
    public void createLvm(String tenantName, Lvm lvm) {
        try {
            Lvm newLvm = ComponentClientFactory.getLvmClient().inNamespace(tenantName).create(lvm);
            if (null != newLvm) {
                LOG.info("创建lvm成功，tenantName:" + tenantName + ",lvmName:" + lvm.getMetadata().getName());
                return;
            }
            LOG.error("创建lvm失败，返回为null，tenantName:" + tenantName + ",lvmName:" + lvm.getMetadata().getName());
        } catch (Exception e) {
            LOG.error("创建lvm失败，tenantName:" + tenantName + ",lvmName:" + lvm.getMetadata().getName() + ",error:", e);
        }
    }

    /**
     * 删除lvm
     *
     * @param tenantName
     * @param lvmname
     * @return
     */
    public boolean deleteLvm(String tenantName, String lvmname) {
        // 1、参数校验
        if (StringUtils.isEmpty(lvmname)) {
            LOG.error("lvmname is empty!");
            return false;
        }
        try {
            Lvm oldLvm = ComponentClientFactory.getLvmClient().inNamespace(tenantName).withName(lvmname).get();
            if (null == oldLvm) {
                LOG.info("lvm不存在或已经被删除，tenantName:" + tenantName + ",lvmName：" + lvmname);
                return true;
            }
            boolean result = ComponentClientFactory.getLvmClient().inNamespace(tenantName).withName(lvmname)
                    .cascading(true).delete();
            LOG.info("删除lvm结果：" + result + "，lvmName：" + lvmname);
            return result;
        } catch (KubernetesClientException e) {
            LOG.error("删除lvm失败，lvmName：" + lvmname, e);
            return false;
        }
    }

    /**
     * 删除集群中所有节点的lvm
     *
     * @param tenantName
     * @param serviceId
     */
    public void deleteNodesLvm(String tenantName, String serviceId) {
        List<StatefulNode> nodes = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                CommonConst.STATE_NODE_DELETED);
        LOG.info("serviceId" + serviceId + ",修改节点状态时查询到的nodes：" + JSON.toJSONString(nodes));
        if (!nodes.isEmpty()) {
            for (StatefulNode statefulNode : nodes) {
                if (StringUtils.isNotEmpty(statefulNode.getLvmName())) {
                    if (deleteLvm(tenantName, statefulNode.getLvmName())) {
                        LOG.info("删除集群时删除lvm成功, lvmName:" + statefulNode.getLvmName());
                    }
                }
            }
        }
    }

    /**
     * Mysql、Storm、kafka、ftp、postgresql根据节点状态修正集群状态
     *
     * @param id
     * @return
     */
    public String getClusterStateByNodes(String serviceId, String lastOpt) {
        LOG.info("根据所有节点状态获取集群状态...");
        List<StatefulNode> nodes = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                CommonConst.STATE_CLUSTER_DELETED);
        if (null == nodes || nodes.isEmpty()) {
            LOG.info("根据所有节点状态获取集群状态失败！集群上次操作为：" + lastOpt);
            if (CommonConst.ACTION_CLUSTER_DELETE.equals(lastOpt) || CommonConst.ACTION_NODE_DELETE.equals(lastOpt)) {
                return CommonConst.STATE_CLUSTER_DELETED;
            }
            return CommonConst.STATE_CLUSTER_FAILED;
        }

        int runNum = 0;
        int stoppedNum = 0;
        int failedNum = 0;
        for (StatefulNode node : nodes) {
            if (CommonConst.STATE_NODE_WAITING.equals(node.getNodeState())) {
                return CommonConst.STATE_CLUSTER_WAITING;
            }
            if (CommonConst.STATE_NODE_RUNNING.equals(node.getNodeState())) {
                runNum++;
            }
            if (CommonConst.STATE_NODE_STOPPED.equals(node.getNodeState())) {
                stoppedNum++;
            }
            if (CommonConst.STATE_NODE_FAILED.equals(node.getNodeState())) {
                failedNum++;
            }
        }
        LOG.info("runNum:" + runNum + "   " + "stoppedNum:" + stoppedNum + "   " + "failedNum:" + failedNum);
        if (runNum == nodes.size()) {
            LOG.info("返回的集群状态为running");
            return CommonConst.STATE_CLUSTER_RUNNING;
        }
        if (stoppedNum == nodes.size()) {
            LOG.info("返回的集群状态是stopped");
            return CommonConst.STATE_CLUSTER_STOPPED;
        }
        if (failedNum == nodes.size()) {
            LOG.info("返回的集群状态是failed");
            return CommonConst.STATE_CLUSTER_FAILED;
        }
        LOG.info("返回的集群状态是warning");
        return CommonConst.STATE_CLUSTER_WARNING;
    }

    /**
     * 查询mysql集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public MysqlCluster getMysqlCluster(String tenantName, String clusterName) {
        LOG.info("获取mysqlCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        MysqlCluster mysqlCluster = null;
        try {
            mysqlCluster = ComponentClientFactory.getMysqlClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取mysqlCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return mysqlCluster;
    }

    /**
     * 修改mysql集群
     *
     * @param nameSpace
     * @param mysqlCluster
     * @return
     */
    public boolean updateMysqlCluster(String tenantName, MysqlCluster mysqlCluster) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("nameSpace is empty!");
            return false;
        }
        if (null == mysqlCluster) {
            LOG.error("修改 mysqlCluster时mysqlCluster为空");
            return false;
        }
        try {
            MysqlCluster updatedCluster = ComponentClientFactory.getMysqlClient().inNamespace(tenantName)
                    .createOrReplace(mysqlCluster);
            LOG.info("修改mysqlCluster后返回的结果：" + JSON.toJSONString(updatedCluster));
            if (null != updatedCluster) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("mysql修改失败", e);
            return false;
        }
    }

    /**
     * 给集群修改lvm资源
     *
     * @param nameSpace
     * @param mysqlCluster
     */
    public void repleaceMysqlClusterLvm(String tenantName, MysqlCluster mysqlCluster) {
        LOG.info("修改lvm,接收的参数=>tenantName:" + tenantName + ",mysqlCluster:" + JSON.toJSONString(mysqlCluster));
        try {
            for (Map.Entry<String, MysqlServer> entry : mysqlCluster.getStatus().getServerNodes().entrySet()) {
                Lvm lvm = getLvm(tenantName, entry.getValue().getVolumeid());
                if (null == lvm) {
                    lvm = new Lvm();
                    registerLvm(tenantName, entry.getValue().getVolumeid(), entry.getValue().getNodeIP(),
                            StringUtils.unitExchange(mysqlCluster.getSpec().getCapacity()));
                    LOG.info("lvm不存在，创建lvm，lvm：" + JSON.toJSONString(lvm));
                } else {
                    lvm.getSpec().setSize(StringUtils.unitExchange(mysqlCluster.getSpec().getCapacity()));
                    boolean result = replaceLvm(tenantName, lvm);
                    LOG.info("修改lvm结果，result:" + result + ",lvm：" + JSON.toJSONString(lvm));
                }
            }
        } catch (Exception e) {
            LOG.error("修改lvm失败,mysqlCluster:" + JSON.toJSONString(mysqlCluster) + ",error:", e);
        }
    }

    /**
     * 修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     * @throws ErrorMessageException
     */
    public void changeMysqlClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) throws ErrorMessageException {
        MysqlCluster mysqlCluster = getMysqlCluster(tenantName, serviceName);
        if (null == mysqlCluster || null == mysqlCluster.getStatus()
                || null == mysqlCluster.getStatus().getServerNodes()) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("修改集群和节点状态时，获取的mysqlcluster为空，tenantName:" + tenantName + ",serviceName:" + serviceName
                    + ",修改集群和节点状态为failed");
            return;
        }
        Map<String, MysqlServer> serverNodes = mysqlCluster.getStatus().getServerNodes();
        try {
            List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            if (null != nodeList && !nodeList.isEmpty()) {
                for (StatefulNode node : nodeList) {
                    if (serverNodes.containsKey(node.getNodeName())) {
                        MysqlServer serverNode = serverNodes.get(node.getNodeName());
                        if (CommonConst.STATE_NODE_WAITING.equals(serverNode.getStatus())
                                || CommonConst.STATE_NODE_UNKNOWN.equals(serverNode.getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (CommonConst.STATE_NODE_RUNNING.equals(serverNode.getStatus())) {
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    newNodeExtendedField.put("ip", serverNode.getNodeIP());
                                    newNodeExtendedField.put("port", String.valueOf(serverNode.getNodeport()));
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(serverNodes.get(node.getNodeName()).getStatus());
                        }
                        if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                && nodesExtendedField.containsKey(node.getNodeName())) {
                            componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                    nodesExtendedField.get(node.getNodeName()));
                        }
                        statefulNodeRepository.save(node);

                    }

                }
            }
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                service.setServiceState(getClusterStateByNodes(serviceId, service.getLastopt()));
                if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                } else {
                    Map<String, String> oldServiceExtendedField = componentOperationsDataBaseUtil
                            .getServiceExtendedField(service.getExtendedField());
                    if (null != mysqlCluster.getStatus().getPhpMyAdmin() && CommonConst.STATE_NODE_RUNNING
                            .equals(mysqlCluster.getStatus().getPhpMyAdmin().getServerStatus())) {
                        MysqlPhpMyAdmin mysqlPhpMyadmin = mysqlCluster.getStatus().getPhpMyAdmin();
                        if (!oldServiceExtendedField.containsKey("phpMyadminHost")) {
                            String phpMyadminHost = mysqlPhpMyadmin.getPhpMyAdminExterHost();

                            if (StringUtils.isNotEmpty(phpMyadminHost)) {
                                oldServiceExtendedField.put("phpMyadminHost", phpMyadminHost);
                            }
                        }
                        if (!oldServiceExtendedField.containsKey("phpMyadminPort")) {
                            int phpMyadminPort = mysqlPhpMyadmin.getPhpMyAdminExterPort();
                            if (StringUtils.isNotEmpty(String.valueOf(phpMyadminPort)) && 0 != phpMyadminPort) {
                                oldServiceExtendedField.put("phpMyadminPort", String.valueOf(phpMyadminPort));
                            }
                        }
                    }
                    boolean parameterUpdateNeedRestart = mysqlCluster.getStatus().isParameterupdateneedrestart();
                    boolean resourceUpdateNeedRestart = mysqlCluster.getStatus().isResourceupdateneedrestart();
                    if (parameterUpdateNeedRestart) {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    if (resourceUpdateNeedRestart) {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, oldServiceExtendedField);
                }
                statefulServiceRepository.save(service);
            }
        } catch (

        Exception e) {
            LOG.error("修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "修改集群和节点状态失败，serviceId:" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeMysqlClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeMysqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 查询redis集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public RedisCluster getRedisCluster(String tenantName, String clusterName) {
        LOG.info("获取redisCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        RedisCluster redisCluster = null;
        try {
            redisCluster = ComponentClientFactory.getRedisClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取redisCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return redisCluster;
    }

    /**
     * 更新单个redis集群
     *
     * @param tenantName
     * @param redisCluster
     * @return
     */
    public Boolean updateRedisCluster(String tenantName, RedisCluster redisCluster) {
        LOG.info("--------更新redisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        }

        try {
            RedisCluster newRedisCluster = ComponentClientFactory.getRedisClient().inNamespace(tenantName)
                    .createOrReplace(redisCluster);
            if (null == newRedisCluster) {
                LOG.error("redisCluster更新后返回的redisCluster 为空");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("redisCluster更新失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * Redis根据yaml修改集群及节点状态
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void changeRedisClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) {
        RedisCluster redisCluster = null;
        redisCluster = getRedisCluster(tenantName, serviceName);
        if (null == redisCluster || null == redisCluster.getStatus()
                || null == redisCluster.getStatus().getBindings()) {
            LOG.error("修改集群节点状态时，获取的redisCluster为null");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
        } else {
            Map<String, RedisBindingNode> bindingNodes = redisCluster.getStatus().getBindings();
            try {
                List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                        CommonConst.STATE_NODE_DELETED);
                if (null != nodeList && !nodeList.isEmpty()) {
                    for (StatefulNode node : nodeList) {
                        if (bindingNodes.containsKey(node.getNodeName())) {
                            RedisBindingNode bindingNode = bindingNodes.get(node.getNodeName());
                            if (RedisClusterConst.getRedisNodeWaitingStateList()
                                    .contains(bindingNodes.get(node.getNodeName()).getStatus())) {
                                node.setNodeState(CommonConst.STATE_NODE_FAILED);
                            } else {
                                if (CommonConst.STATE_NODE_RUNNING.equals(bindingNode.getStatus())) {
                                    if (StringUtils.isEmpty(node.getIp())
                                            || StringUtils.isEmpty(String.valueOf(node.getPort()))
                                            || node.getPort() == 0) {
                                        Map<String, String> newNodeExtendedField = new HashMap<>();
                                        newNodeExtendedField.put("ip", bindingNode.getBindIp());
                                        newNodeExtendedField.put("port", String.valueOf(redisCluster.getStatus()
                                                .getServices().get(bindingNode.getRole().toLowerCase()).getNodePort()));
                                        componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                    }
                                }
                                node.setNodeState(bindingNodes.get(node.getNodeName()).getStatus());
                            }
                            if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                    && nodesExtendedField.containsKey(node.getNodeName())) {
                                componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                        nodesExtendedField.get(node.getNodeName()));
                            }
                            statefulNodeRepository.save(node);
                        }
                    }
                }
                StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
                if (null != service) {
                    if (RedisClusterConst.getRedisClusterWaitingStateList()
                            .contains(redisCluster.getStatus().getPhase())) {
                        service.setServiceState(changeClusterStateByNodes(serviceId, service.getLastopt()));
                    } else {
                        service.setServiceState(redisCluster.getStatus().getPhase());
                    }

                    if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                        componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                    } else {
                        if (CommonConst.STATE_CLUSTER_RUNNING.equals(redisCluster.getStatus().getPhase())) {
                            Map<String, String> newServiceExtendedField = componentOperationsDataBaseUtil
                                    .getServiceExtendedField(service.getExtendedField());
                            newServiceExtendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE,
                                    String.valueOf(!redisCluster.getStatus().isNeedRestart()));
                            service.setExtendedField(JSON.toJSONString(newServiceExtendedField));
                        }
                    }
                    statefulServiceRepository.save(service);
                }
            } catch (Exception e) {
                LOG.error("操作超时修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            }
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeRedisClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeRedisClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * Redis或Codis或Memcached或Prometheus根据节点状态修正集群状态
     *
     * @param id
     * @return
     */
    public String changeClusterStateByNodes(String serviceId, String lastOpt) {
        LOG.info("根据所有节点状态获取集群状态...");
        List<StatefulNode> nodes = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                CommonConst.STATE_NODE_DELETED);
        if (null == nodes || nodes.isEmpty()) {
            LOG.info("根据所有节点状态获取集群状态失败！集群上次操作为：" + lastOpt);
            if (CommonConst.ACTION_CLUSTER_DELETE.equals(lastOpt) || CommonConst.ACTION_NODE_DELETE.equals(lastOpt)) {
                return CommonConst.STATE_CLUSTER_DELETED;
            }
            return CommonConst.STATE_CLUSTER_FAILED;
        }

        int runNum = 0;
        int stoppedNum = 0;
        for (StatefulNode node : nodes) {
            if (CommonConst.STATE_NODE_RUNNING.equals(node.getNodeState())) {
                runNum++;
            }
            if (CommonConst.STATE_NODE_STOPPED.equals(node.getNodeState())) {
                stoppedNum++;
            }
        }
        LOG.info("runNum:" + runNum + "   " + "stoppedNum:" + stoppedNum);
        if (runNum == nodes.size()) {
            LOG.info("返回的集群状态为running");
            return CommonConst.STATE_CLUSTER_RUNNING;
        }
        if (stoppedNum == nodes.size()) {
            LOG.info("返回的集群状态是stopped");
            return CommonConst.STATE_CLUSTER_STOPPED;
        }
        LOG.info("返回的集群状态是failed");
        return CommonConst.STATE_CLUSTER_FAILED;
    }

    /**
     * 查询codis集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public CodisCluster getCodisCluster(String tenantName, String clusterName) {
        LOG.info("获取codisCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        CodisCluster codisCluster = null;
        try {
            codisCluster = ComponentClientFactory.getCodisClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取codisCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return codisCluster;
    }

    /**
     * 更新codis集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public boolean updateCodisCluster(String tenantName, CodisCluster codisCluster) {
        LOG.info("--------更新codisCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            CodisCluster newCodisCluster = ComponentClientFactory.getCodisClient().inNamespace(tenantName)
                    .createOrReplace(codisCluster);
            if (null == newCodisCluster) {
                LOG.error("codisCluster更新后返回的codisCluster 为空");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("codisCluster更新失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }
    }

    /**
     * Codis根据yaml修改集群及节点状态
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void changeCodisClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) {
        CodisCluster codisCluster = null;
        codisCluster = getCodisCluster(tenantName, serviceName);
        String globalIp = "";
        if (null == codisCluster || null == codisCluster.getStatus() || null == codisCluster.getStatus().getGroup()) {
            LOG.error("修改集群节点状态时，获取的codisCluster为null");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
        } else {
            try {
                List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                        CommonConst.STATE_NODE_DELETED);
                for (CodisGroupStatus codisGroupStatus : codisCluster.getStatus().getGroup().values()) {
                    for (CodisGroupBindingNode codisGroupBindingNode : codisGroupStatus.getBindings().values()) {
                        if (null != nodeList && !nodeList.isEmpty()) {
                            for (StatefulNode node : nodeList) {
                                if (node.getNodeName().equals(codisGroupBindingNode.getName())) {
                                    if (CodisClusterConst.getCodisNodeWaitingStateList()
                                            .contains(codisGroupBindingNode.getStatus())) {
                                        node.setNodeState(CommonConst.STATE_NODE_FAILED);
                                    } else {
                                        if (CommonConst.STATE_NODE_RUNNING.equals(codisGroupBindingNode.getStatus())) {
                                            if (StringUtils.isEmpty(node.getIp())
                                                    || StringUtils.isEmpty(String.valueOf(node.getPort()))
                                                    || node.getPort() == 0) {
                                                Map<String, String> newNodeExtendedField = new HashMap<>();
                                                globalIp = codisGroupBindingNode.getBindIp();
                                                newNodeExtendedField.put("ip", globalIp);
                                                componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                                        newNodeExtendedField);
                                            }
                                        }
                                        node.setNodeState(codisGroupBindingNode.getStatus());
                                    }
                                    if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                            && nodesExtendedField.containsKey(node.getNodeName())) {
                                        componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                                nodesExtendedField.get(node.getNodeName()));
                                    }
                                    statefulNodeRepository.save(node);
                                }
                            }
                        }

                    }
                }
                for (StatefulNode node : nodeList) {
                    if (CodisClusterConst.CODIS_ROLE_DASHBOARD.equals(node.getNodeName())) {
                        if (null != codisCluster.getStatus().getDashboard()
                                && CodisClusterConst.getCodisNodeWaitingStateList()
                                        .contains(codisCluster.getStatus().getDashboard().getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (null != codisCluster.getStatus().getDashboard() && CommonConst.STATE_NODE_RUNNING
                                    .equals(codisCluster.getStatus().getDashboard().getStatus())) {
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    newNodeExtendedField.put("ip", globalIp);
                                    newNodeExtendedField.put("port",
                                            String.valueOf(codisCluster.getStatus().getDashboard().getNodePort()));
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(codisCluster.getStatus().getDashboard().getStatus());
                        }
                    } else if (CodisClusterConst.CODIS_ROLE_SENTINEL.equals(node.getNodeName())) {
                        if (null != codisCluster.getStatus().getSentinel()
                                && CodisClusterConst.getCodisNodeWaitingStateList()
                                        .contains(codisCluster.getStatus().getSentinel().getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            node.setNodeState(codisCluster.getStatus().getSentinel().getStatus());
                        }
                    } else if (CodisClusterConst.CODIS_ROLE_PROXY.equals(node.getNodeName())) {
                        if (null != codisCluster.getStatus().getProxy()
                                && CodisClusterConst.getCodisNodeWaitingStateList()
                                        .contains(codisCluster.getStatus().getProxy().getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (null != codisCluster.getStatus().getProxy() && CommonConst.STATE_NODE_RUNNING
                                    .equals(codisCluster.getStatus().getProxy().getStatus())) {
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    newNodeExtendedField.put("ip", globalIp);
                                    newNodeExtendedField.put("port",
                                            String.valueOf(codisCluster.getStatus().getProxy().getNodePort()));
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(codisCluster.getStatus().getProxy().getStatus());
                        }
                    }
                    statefulNodeRepository.save(node);
                }

                StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
                if (null != service) {
                    service.setServiceState(changeClusterStateByNodes(serviceId, service.getLastopt()));
                    if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                        componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                    }
                    statefulServiceRepository.save(service);
                }

            } catch (Exception e) {
                LOG.error("操作超时修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            }
        }

    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeCodisClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeCodisClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 查询storm集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public StormCluster getStormCluster(String tenantName, String clusterName) {
        LOG.info("获取stormCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        StormCluster stormCluster = null;
        try {
            stormCluster = ComponentClientFactory.getStormClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取stormCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return stormCluster;
    }

    /**
     * 修改storm集群
     *
     * @param nameSpace
     * @param stormCluster
     * @return
     */
    public boolean updateStormCluster(String tenantName, StormCluster stormCluster) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("nameSpace is empty!");
            return false;
        }
        if (null == stormCluster) {
            LOG.error("修改 stormCluster时stormCluster为空");
            return false;
        }
        try {
            StormCluster updatedCluster = ComponentClientFactory.getStormClient().inNamespace(tenantName)
                    .createOrReplace(stormCluster);
            LOG.info("修改stormCluster后返回的结果：" + JSON.toJSONString(updatedCluster));
            if (null != updatedCluster) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("storm修改失败", e);
            return false;
        }
    }

    /**
     * 修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     * @throws ErrorMessageException
     */
    public void changeStormClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) throws ErrorMessageException {
        StormCluster stormCluster = getStormCluster(tenantName, serviceName);
        if (null == stormCluster || null == stormCluster.getStatus()
                || null == stormCluster.getStatus().getServerNodes()) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("修改集群和节点状态时，获取的stormcluster为空，tenantName:" + tenantName + ",serviceName:" + serviceName
                    + ",修改集群和节点状态为failed");
            return;
        }
        Map<String, StormNode> serverNodes = stormCluster.getStatus().getServerNodes();
        try {
            List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            String tcpPort = "";
            int uiPort = 0;
            String nimbusIp = "";
            if (null != nodeList && !nodeList.isEmpty()) {
                for (StatefulNode node : nodeList) {
                    if (serverNodes.containsKey(node.getNodeName())) {
                        if (CommonConst.STATE_NODE_WAITING.equals(serverNodes.get(node.getNodeName()).getStatus())
                                || CommonConst.STATE_NODE_UNKNOWN
                                        .equals(serverNodes.get(node.getNodeName()).getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (CommonConst.STATE_NODE_RUNNING
                                    .equals(serverNodes.get(node.getNodeName()).getStatus())) {
                                if (StormClusterConst.ROLE_NIMBUS.equals(node.getRole())) {
                                    uiPort = serverNodes.get(node.getNodeName()).getNodePort();
                                    nimbusIp = serverNodes.get(node.getNodeName()).getHostip();
                                }
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    tcpPort = String.valueOf(serverNodes.get(node.getNodeName()).getNodeExterport());
                                    newNodeExtendedField.put("ip", nimbusIp);
                                    newNodeExtendedField.put("port", tcpPort);
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(serverNodes.get(node.getNodeName()).getStatus());
                        }
                        if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                && nodesExtendedField.containsKey(node.getNodeName())) {
                            componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                    nodesExtendedField.get(node.getNodeName()));
                        }
                        statefulNodeRepository.save(node);
                    }
                }
            }
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                service.setServiceState(getClusterStateByNodes(serviceId, service.getLastopt()));
                if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                } else {
                    Map<String, String> oldServiceExtendedField = componentOperationsDataBaseUtil
                            .getServiceExtendedField(service.getExtendedField());
                    boolean parameterUpdateNeedRestart = stormCluster.getStatus().isParameterUpdateNeedRestart();
                    boolean resourceUpdateNeedRestart = stormCluster.getStatus().isResourceUpdateNeedRestart();
                    if (parameterUpdateNeedRestart) {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    if (resourceUpdateNeedRestart) {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    if (StringUtils.isEmpty(oldServiceExtendedField.get(StormClusterConst.NIMBUS_UI_URL))) {
                        if (StringUtils.isNotEmpty(nimbusIp) && 0 != uiPort) {
                            oldServiceExtendedField.put(StormClusterConst.NIMBUS_UI_URL,
                                    "http://" + nimbusIp + ":" + uiPort);
                        }
                    }

                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, oldServiceExtendedField);
                }
                statefulServiceRepository.save(service);
            }
        } catch (

        Exception e) {
            LOG.error("修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "修改集群和节点状态失败，serviceId:" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeStormClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeStormClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 查询es集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public EsCluster getEsCluster(String tenantName, String clusterName) {
        LOG.info("获取esCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        EsCluster esCluster = null;
        try {
            esCluster = ComponentClientFactory.getEsClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取esCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return esCluster;
    }

    /**
     * 更新单个es集群
     *
     * @param tenantName
     * @param esCluster
     * @return
     */
    public Boolean updateEsCluster(String tenantName, EsCluster esCluster) {
        LOG.info("--------更新esCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        }

        try {
            EsCluster newEsCluster = ComponentClientFactory.getEsClient().inNamespace(tenantName)
                    .createOrReplace(esCluster);
            if (null == newEsCluster) {
                LOG.error("esCluster更新后返回的esCluster 为空");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("esCluster更新失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }

    }

    /**
     * Es根据yaml修改集群及节点状态
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void changeEsClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId, String serviceName,
            Map<String, String> serviceExtendedField, Map<String, Map<String, String>> nodesExtendedField) {
        EsCluster esCluster = null;
        esCluster = getEsCluster(tenantName, serviceName);
        if (null == esCluster || null == esCluster.getStatus() || null == esCluster.getStatus().getInstances()) {
            LOG.error("修改集群节点状态时，获取的esCluster为null");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
        } else {
            Map<String, EsInstance> esInstances = esCluster.getStatus().getInstances();
            try {
                List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                        CommonConst.STATE_NODE_DELETED);
                if (null != nodeList && !nodeList.isEmpty()) {
                    for (StatefulNode node : nodeList) {
                        if (esInstances.containsKey(node.getNodeName())) {
                            EsInstance esInstance = esInstances.get(node.getNodeName());
                            if (EsClusterConst.getEsNodeStateWaitingList().contains(esInstance.getInstancePhase())) {
                                node.setNodeState(CommonConst.STATE_NODE_FAILED);
                            } else {
                                if (CommonConst.STATE_NODE_RUNNING.equals(esInstance.getInstancePhase())) {
                                    if (StringUtils.isEmpty(node.getIp())
                                            || StringUtils.isEmpty(String.valueOf(node.getPort()))
                                            || node.getPort() == 0) {
                                        Map<String, String> newNodeExtendedField = new HashMap<>();
                                        newNodeExtendedField.put("ip", esInstance.getExterHost());
                                        newNodeExtendedField.put("port", String.valueOf(esInstance.getExterHttpport()));
                                        componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                    }
                                }
                                node.setNodeState(esInstances.get(node.getNodeName()).getInstancePhase());
                            }
                            if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                    && nodesExtendedField.containsKey(node.getNodeName())) {
                                componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                        nodesExtendedField.get(node.getNodeName()));
                            }
                            statefulNodeRepository.save(node);
                        }
                    }
                }
                StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
                if (null != service) {
                    service.setServiceState(getEsClusterStateByNodes(serviceId, service.getLastopt()));
                    if (null != esCluster.getStatus().getKibana() && CommonConst.STATE_NODE_RUNNING
                            .equals(esCluster.getStatus().getKibana().getInstancePhase())) {
                        Map<String, String> oldServiceExtendedField = componentOperationsDataBaseUtil
                                .getServiceExtendedField(service.getExtendedField());
                        if (null == oldServiceExtendedField || oldServiceExtendedField.isEmpty()
                                || !oldServiceExtendedField.containsKey("kibanaIp")) {
                            Map<String, String> newServiceExtendedField = new HashMap<>();
                            String kibanaExterHost = esCluster.getStatus().getKibana().getKibanaExterHost();
                            String kibanaExterPort = String
                                    .valueOf(esCluster.getStatus().getKibana().getKibanaExterPort());
                            newServiceExtendedField.put("kibanaIp", kibanaExterHost);
                            newServiceExtendedField.put("kibanaPort", kibanaExterPort);
                            componentOperationsDataBaseUtil.updateClusterExtendedField(service, newServiceExtendedField);
                        }
                    }
                    if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                        componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                    }

                    statefulServiceRepository.save(service);
                }
            } catch (Exception e) {
                LOG.error("操作超时修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            }
        }
    }

    /**
     * statecheckEs根据yaml修改集群及节点状态
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    public void changeEsClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName) {
        changeEsClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * Es根据节点状态修正集群状态
     *
     * @param id
     * @return
     */
    public String getEsClusterStateByNodes(String serviceId, String lastOpt) {
        LOG.info("根据所有节点状态获取集群状态...");
        List<StatefulNode> nodes = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                CommonConst.STATE_NODE_DELETED);
        if (null == nodes || nodes.isEmpty()) {
            LOG.info("根据所有节点状态获取集群状态失败！集群上次操作为：" + lastOpt);
            if (CommonConst.ACTION_CLUSTER_DELETE.equals(lastOpt) || CommonConst.ACTION_NODE_DELETE.equals(lastOpt)) {
                return CommonConst.STATE_CLUSTER_DELETED;
            }
            return CommonConst.STATE_CLUSTER_FAILED;
        }

        int running = 0, runningW = 0, runningM = 0, runningD = 0, stopped = 0, failed = 0, replicas;
        replicas = nodes.size();

        for (StatefulNode node : nodes) {
            String state = node.getNodeState();
            if (CommonConst.STATE_NODE_RUNNING.equals(state)) {
                if (EsClusterConst.ES_ROLE_WORKER.equals(node.getRole())) {
                    runningW = runningW + 1;
                } else if (EsClusterConst.ES_ROLE_MASTER.equals(node.getRole())) {
                    runningM = runningM + 1;
                } else if (EsClusterConst.ES_ROLE_DATA.equals(node.getRole())) {
                    runningD = runningD + 1;
                }
                running = running + 1;
            } else if (CommonConst.STATE_NODE_STOPPED.equals(state)) {
                stopped = stopped + 1;
            } else if (CommonConst.STATE_NODE_FAILED.equals(state)) {
                failed = failed + 1;
            }
        }
        if (running == replicas) {
            LOG.info("返回的集群状态是running");
            return CommonConst.STATE_CLUSTER_RUNNING;
        } else if (stopped == replicas) {
            LOG.info("返回的集群状态是stopped");
            return CommonConst.STATE_CLUSTER_STOPPED;
        } else if (runningW > 0 || (runningM > 0 && runningD > 0)) {
            LOG.info("返回的集群状态是warning");
            return CommonConst.STATE_CLUSTER_WARNING;
        }
        LOG.info("返回的集群状态是failed");
        return CommonConst.STATE_CLUSTER_FAILED;
    }

    /**
     * 查询kafka集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public KafkaCluster getKafkaCluster(String tenantName, String clusterName) {
        LOG.info("获取kafkaCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        KafkaCluster kafkaCluster = null;
        try {
            kafkaCluster = ComponentClientFactory.getKafkaClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取kafkaCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return kafkaCluster;
    }

    /**
     * 修改kafka集群
     *
     * @param nameSpace
     * @param kafkaCluster
     * @return
     */
    public boolean updateKafkaCluster(String tenantName, KafkaCluster kafkaCluster) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("nameSpace is empty!");
            return false;
        }
        if (null == kafkaCluster) {
            LOG.error("修改 kafkaCluster时kafkaCluster为空");
            return false;
        }
        try {
            KafkaCluster updatedCluster = ComponentClientFactory.getKafkaClient().inNamespace(tenantName)
                    .createOrReplace(kafkaCluster);
            LOG.info("修改kafkaCluster后返回的结果：" + JSON.toJSONString(updatedCluster));
            if (null != updatedCluster) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("kafka修改失败", e);
            return false;
        }
    }

    /**
     * 修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     * @throws ErrorMessageException
     */
    public void changeKafkaClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) throws ErrorMessageException {
        KafkaCluster kafkaCluster = getKafkaCluster(tenantName, serviceName);
        if (null == kafkaCluster || null == kafkaCluster.getStatus()
                || null == kafkaCluster.getStatus().getServerNodes()) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("修改集群和节点状态时，获取的kafkacluster为空，tenantName:" + tenantName + ",serviceName:" + serviceName
                    + ",修改集群和节点状态为failed");
            return;
        }

        Map<String, KafkaNode> serverNodes = kafkaCluster.getStatus().getServerNodes();
        try {
            List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            if (null != nodeList && !nodeList.isEmpty()) {
                for (StatefulNode node : nodeList) {
                    if (serverNodes.containsKey(node.getNodeName())) {
                        KafkaNode kafkaNode = serverNodes.get(node.getNodeName());
                        if (CommonConst.STATE_NODE_WAITING.equals(serverNodes.get(node.getNodeName()).getStatus())
                                || CommonConst.STATE_NODE_UNKNOWN
                                        .equals(serverNodes.get(node.getNodeName()).getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (CommonConst.STATE_NODE_RUNNING.equals(kafkaNode.getStatus())) {
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    newNodeExtendedField.put("ip", kafkaNode.getHostip());
                                    newNodeExtendedField.put("port", String.valueOf(kafkaNode.getNodeport()));
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(serverNodes.get(node.getNodeName()).getStatus());
                        }
                        if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                && nodesExtendedField.containsKey(node.getNodeName())) {
                            componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                    nodesExtendedField.get(node.getNodeName()));
                        }
                        statefulNodeRepository.save(node);
                    }
                }
            }
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                service.setServiceState(getClusterStateByNodes(serviceId, service.getLastopt()));
                if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                } else {
                    Map<String, String> oldServiceExtendedField = componentOperationsDataBaseUtil
                            .getServiceExtendedField(service.getExtendedField());
                    boolean parameterUpdateNeedRestart = kafkaCluster.getStatus().isParameterupdateneedrestart();
                    boolean resourceUpdateNeedRestart = kafkaCluster.getStatus().isResourceupdateneedrestart();
                    if (parameterUpdateNeedRestart) {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    if (resourceUpdateNeedRestart) {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, oldServiceExtendedField);
                }
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "修改集群和节点状态失败，serviceId:" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeKafkaClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeKafkaClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 给集群修改lvm资源
     *
     * @param nameSpace
     * @param kafkaCluster
     */
    public void repleaceKafkaClusterLvm(String tenantName, KafkaCluster kafkaCluster) {
        LOG.info("修改lvm,接收的参数=>tenantName:" + tenantName + ",kafkaCluster:" + JSON.toJSONString(kafkaCluster));
        try {
            for (Map.Entry<String, KafkaNode> entry : kafkaCluster.getStatus().getServerNodes().entrySet()) {
                Lvm lvm = getLvm(tenantName, entry.getValue().getVolumeid());
                if (null == lvm) {
                    lvm = new Lvm();
                    registerLvm(tenantName, entry.getValue().getVolumeid(), entry.getValue().getHostip(),
                            StringUtils.unitExchange(kafkaCluster.getSpec().getCapacity()));
                    LOG.info("lvm不存在，创建lvm，lvm：" + JSON.toJSONString(lvm));
                } else {
                    lvm.getSpec().setSize(StringUtils.unitExchange(kafkaCluster.getSpec().getCapacity()));
                    boolean result = replaceLvm(tenantName, lvm);
                    LOG.info("修改lvm结果，result:" + result + ",lvm：" + JSON.toJSONString(lvm));
                }
            }
        } catch (Exception e) {
            LOG.error("修改lvm失败,kafkaCluster:" + JSON.toJSONString(kafkaCluster) + ",error:", e);
        }
    }

    /**
     * 查询ftp集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public FtpCluster getFtpCluster(String tenantName, String clusterName) {
        LOG.info("获取ftpCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        FtpCluster ftpCluster = null;
        try {
            ftpCluster = ComponentClientFactory.getFtpClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取ftpCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return ftpCluster;
    }

    /**
     * 修改ftp集群
     *
     * @param nameSpace
     * @param ftpCluster
     * @return
     */
    public boolean updateFtpCluster(String tenantName, FtpCluster ftpCluster) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("nameSpace is empty!");
            return false;
        }
        if (null == ftpCluster) {
            LOG.error("修改 ftpCluster时ftpCluster为空");
            return false;
        }
        try {
            FtpCluster updatedCluster = ComponentClientFactory.getFtpClient().inNamespace(tenantName)
                    .createOrReplace(ftpCluster);
            LOG.info("修改ftpCluster后返回的结果：" + JSON.toJSONString(updatedCluster));
            if (null != updatedCluster) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("ftp修改失败", e);
            return false;
        }
    }

    /**
     * 修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeFtpClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId, String serviceName,
            Map<String, String> serviceExtendedField, Map<String, Map<String, String>> nodesExtendedField)
            throws ErrorMessageException {
        FtpCluster ftpCluster = getFtpCluster(tenantName, serviceName);
        if (null == ftpCluster || null == ftpCluster.getStatus() || null == ftpCluster.getStatus().getServerNodes()) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("修改集群和节点状态时，获取的ftpcluster为空，tenantName:" + tenantName + ",serviceName:" + serviceName
                    + ",修改集群和节点状态为failed");
            return;
        }

        Map<String, FtpNode> serverNodes = ftpCluster.getStatus().getServerNodes();
        try {
            List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            if (null != nodeList && !nodeList.isEmpty()) {
                for (StatefulNode node : nodeList) {
                    if (serverNodes.containsKey(node.getNodeName())) {
                        FtpNode ftpNode = serverNodes.get(node.getNodeName());
                        if (CommonConst.STATE_NODE_WAITING.equals(serverNodes.get(node.getNodeName()).getStatus())
                                || CommonConst.STATE_NODE_UNKNOWN
                                        .equals(serverNodes.get(node.getNodeName()).getStatus())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (CommonConst.STATE_NODE_RUNNING.equals(ftpNode.getStatus())) {
                                Map<String, String> newNodeExtendedField = new HashMap<>();
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    newNodeExtendedField.put("ip", ftpNode.getHostip());
                                }
                                String dbExtendedField = node.getExtendedField();
                                if (StringUtils.isEmpty(dbExtendedField)) {
                                    Map<String, String> nodePorts = ftpNode.getNodeport();
                                    if (StringUtils.isNotEmpty(nodePorts.get("ftp-server-access-port"))) {
                                        newNodeExtendedField.put(FtpClusterConst.ACCESS_PORT,
                                                nodePorts.get("ftp-server-access-port"));
                                    }
                                    if (StringUtils.isNotEmpty(nodePorts.get("ftp-server-passive-port"))) {
                                        newNodeExtendedField.put(FtpClusterConst.PASSIVE_PORT,
                                                nodePorts.get("ftp-server-passive-port"));
                                    }
                                } else {
                                    JSONObject dbExtendedFieldJson = JSON.parseObject(dbExtendedField);
                                    Map<String, String> nodePorts = ftpNode.getNodeport();
                                    if (!dbExtendedFieldJson.containsKey("ftp-server-access-port")
                                            && StringUtils.isNotEmpty(nodePorts.get("ftp-server-access-port"))) {
                                        newNodeExtendedField.put(FtpClusterConst.ACCESS_PORT,
                                                nodePorts.get("ftp-server-access-port"));
                                    }
                                    if (!dbExtendedFieldJson.containsKey("ftp-server-passive-port")
                                            && StringUtils.isNotEmpty(nodePorts.get("ftp-server-passive-port"))) {
                                        newNodeExtendedField.put(FtpClusterConst.PASSIVE_PORT,
                                                nodePorts.get("ftp-server-passive-port"));
                                    }
                                }
                                if (!newNodeExtendedField.isEmpty()) {
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(serverNodes.get(node.getNodeName()).getStatus());
                        }
                        if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                && nodesExtendedField.containsKey(node.getNodeName())) {
                            componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                    nodesExtendedField.get(node.getNodeName()));
                        }
                        statefulNodeRepository.save(node);
                    }
                }
            }
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                service.setServiceState(getClusterStateByNodes(serviceId, service.getLastopt()));
                if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                }
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "修改集群和节点状态失败，serviceId:" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeFtpClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeFtpClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 给集群修改lvm资源
     *
     * @param nameSpace
     * @param ftpCluster
     */
    public void repleaceFtpClusterLvm(String tenantName, FtpCluster ftpCluster) {
        LOG.info("修改lvm,接收的参数=>tenantName:" + tenantName + ",ftpCluster:" + JSON.toJSONString(ftpCluster));
        try {
            for (Map.Entry<String, FtpNode> entry : ftpCluster.getStatus().getServerNodes().entrySet()) {
                Lvm lvm = getLvm(tenantName, entry.getValue().getVolumeid());
                if (null == lvm) {
                    lvm = new Lvm();
                    registerLvm(tenantName, entry.getValue().getVolumeid(), entry.getValue().getHostip(),
                            StringUtils.unitExchange(ftpCluster.getSpec().getCapacity()));
                    LOG.info("lvm不存在，创建lvm，lvm：" + JSON.toJSONString(lvm));
                } else {
                    lvm.getSpec().setSize(StringUtils.unitExchange(ftpCluster.getSpec().getCapacity()));
                    boolean result = replaceLvm(tenantName, lvm);
                    LOG.info("修改lvm结果，result:" + result + ",lvm：" + JSON.toJSONString(lvm));
                }
            }
        } catch (Exception e) {
            LOG.error("修改lvm失败,ftpCluster:" + JSON.toJSONString(ftpCluster) + ",error:", e);
        }
    }

    /**
     * 构建user config
     *
     * @param userName
     * @param password
     * @param permission
     * @param status
     * @param serviceName
     * @return
     */
    public Map<String, String> buildUserConfig(FtpUser ftpUser, String serviceName) {

        Map<String, String> map = new HashMap<>(10);

        // 1、用户名称
        String userName = ftpUser.getUserName();
        if (StringUtils.isNotEmpty(userName)) {
            map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_USR), userName);
        } else {
            LOG.error("FTP用户没有获取到用户名称！");
        }

        // 2、用户密码
        String password = ftpUser.getPassword();
        if (StringUtils.isNotEmpty(password)) {
            map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_PWD), password);
        } else {
            LOG.error("FTP用户没有获取到密码！");
        }

        // 3、用户是否可用
        String enable = "false";
        if (ftpUser.getStatus() == 1) {
            enable = "true";
        }
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_ENABLE), enable);

        // 4、用户权限(读、写)
        String isWrite = "false";
        String permission = ftpUser.getPermission();
        if (StringUtils.isNotEmpty(permission) && permission.toLowerCase().contains("w")) {
            isWrite = "true";
        }
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_ISWRITE), isWrite);

        // 5、通用配置：下面的用户配置不允许更改
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_DIR), ftpUser.getDirectory());
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_MAXLOGINNUMBER), "0");
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_MAXLOGINPERIP), "0");
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_IDLETIME), "0");
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_UPLOADRATE), "0");
        map.put(userConfigWrapper(userName, FtpClusterConst.USER_CONFIG_SUFFIX_DOWNLOADRATE), "0");

        return map;
    }

    /**
     * @param userName
     *            用户名称
     * @param configName
     *            配置名称
     * @return 包装后的用户配置
     */
    private String userConfigWrapper(String userName, String configName) {
        return FtpClusterConst.USER_CONFIG_PREFIX + FtpClusterConst.DOT_STRING + userName + FtpClusterConst.DOT_STRING
                + configName;
    }

    /**
     * 查询memcached集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public MemcachedCluster getMemcachedCluster(String tenantName, String clusterName) {
        LOG.info("获取MemcachedCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        MemcachedCluster memcachedCluster = null;
        try {
            memcachedCluster = ComponentClientFactory.getMemcachedClient().inNamespace(tenantName).withName(clusterName)
                    .get();
        } catch (Exception e) {
            LOG.error("获取memcachedCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return memcachedCluster;
    }

    /**
     * 更新memcached集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public boolean updateMemcachedCluster(String tenantName, MemcachedCluster memcachedCluster) {
        LOG.info("--------更新memcachedCluster--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            MemcachedCluster newMemcachedCluster = ComponentClientFactory.getMemcachedClient().inNamespace(tenantName)
                    .createOrReplace(memcachedCluster);
            if (null == newMemcachedCluster) {
                LOG.error("memcachedCluster更新后返回的memcachedCluster 为空");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("memcachedCluster更新失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }
    }

    /**
     * memcached根据yaml修改集群及节点状态
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    public void changeMemcachedClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) {
        MemcachedCluster memcachedCluster = null;
        memcachedCluster = getMemcachedCluster(tenantName, serviceName);
        if (null == memcachedCluster || null == memcachedCluster.getStatus()
                || null == memcachedCluster.getStatus().getGroups()) {
            LOG.error("修改集群节点状态时，获取的memcachedCluster为null");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
        } else {
            try {
                List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                        CommonConst.STATE_NODE_DELETED);
                if (null != nodeList && !nodeList.isEmpty()) {
                    for (StatefulNode node : nodeList) {
                        for (MemcachedClusterGroupInfo memcachedClusterGroupInfo : memcachedCluster.getStatus()
                                .getGroups().values()) {
                            for (MemcachedClusterServer memcachedClusterServer : memcachedClusterGroupInfo.getServer()
                                    .values()) {
                                if (node.getNodeName().equals(memcachedClusterServer.getName())) {
                                    if (MemcachedClusterConst.getMemcachedNodeWaitingStateList()
                                            .contains(memcachedClusterServer.getStatus())) {
                                        node.setNodeState(CommonConst.STATE_NODE_FAILED);
                                    } else {
                                        if (CommonConst.STATE_NODE_RUNNING.equals(memcachedClusterServer.getStatus())) {
                                            if (StringUtils.isEmpty(node.getIp())
                                                    || StringUtils.isEmpty(String.valueOf(node.getPort()))
                                                    || node.getPort() == 0) {
                                                Map<String, String> newNodeExtendedField = new HashMap<>();
                                                newNodeExtendedField.put("ip", memcachedClusterServer.getNodeIp());
                                                newNodeExtendedField.put("port", String
                                                        .valueOf(memcachedClusterServer.getService().getNodePort()));
                                                componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                                        newNodeExtendedField);
                                            }
                                        }
                                        node.setNodeState(memcachedClusterServer.getStatus());
                                    }
                                    if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                            && nodesExtendedField.containsKey(node.getNodeName())) {
                                        componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                                nodesExtendedField.get(node.getNodeName()));
                                    }
                                    statefulNodeRepository.save(node);
                                }

                            }
                        }
                    }
                }
                StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
                if (null != service) {
                    service.setServiceState(changeClusterStateByNodes(serviceId, service.getLastopt()));
                    if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                        componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                    }
                    statefulServiceRepository.save(service);
                }
            } catch (Exception e) {
                LOG.error("操作超时修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            }
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeMemcachedClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeMemcachedClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 查询prometheus集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public PrometheusCluster getPrometheusCluster(String tenantName, String clusterName) {
        LOG.info("获取prometheus，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        PrometheusCluster prometheus = null;
        try {
            prometheus = ComponentClientFactory.getPrometheusClient().inNamespace(tenantName).withName(clusterName)
                    .get();
        } catch (Exception e) {
            LOG.error("获取prometheus失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return prometheus;
    }

    /**
     * 更新prometheus集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public boolean updatePrometheusCluster(String tenantName, PrometheusCluster prometheus) {
        LOG.info("--------更新prometheus--------");

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        }

        try {
            PrometheusCluster newPrometheus = ComponentClientFactory.getPrometheusClient().inNamespace(tenantName)
                    .createOrReplace(prometheus);
            if (null == newPrometheus) {
                LOG.error("newPrometheus更新后返回的prometheus 为空");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("prometheus更新失败");
            LOG.error(JSON.toJSONString(e));
            return false;
        }
    }

    /**
     * prometheus根据yaml修改集群及节点状态
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     */
    public void changePrometheusClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) {
        PrometheusCluster prometheus = null;
        prometheus = getPrometheusCluster(tenantName, serviceName);
        if (null == prometheus || null == prometheus.getStatus() || null == prometheus.getStatus().getInstances()) {
            LOG.error("修改集群节点状态时，获取的prometheus为null");
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
        } else {
            try {
                List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                        CommonConst.STATE_NODE_DELETED);
                if (null != nodeList && !nodeList.isEmpty()) {
                    for (StatefulNode node : nodeList) {
                        for (PrometheusInstances prometheusInstances : prometheus.getStatus().getInstances().values()) {
                            if (PrometheusClusterConst.getPrometheusNodeStateWaitingList()
                                    .contains(prometheusInstances.getInstancePhase())) {
                                node.setNodeState(CommonConst.STATE_NODE_FAILED);
                            } else {
                                if (CommonConst.STATE_NODE_RUNNING.equals(prometheusInstances.getInstancePhase())) {
                                    if (StringUtils.isEmpty(node.getIp())
                                            || StringUtils.isEmpty(String.valueOf(node.getPort()))
                                            || node.getPort() == 0) {
                                        Map<String, String> newNodeExtendedField = new HashMap<>();
                                        newNodeExtendedField.put("ip", prometheusInstances.getExterHost());
                                        newNodeExtendedField.put("port",
                                                String.valueOf(prometheusInstances.getExterHttpport()));
                                        componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                    }
                                }
                                node.setNodeState(prometheusInstances.getInstancePhase());
                            }
                            if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                    && nodesExtendedField.containsKey(node.getNodeName())) {
                                componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                        nodesExtendedField.get(node.getNodeName()));
                            }
                            statefulNodeRepository.save(node);
                        }
                    }
                }
                StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
                if (null != service) {
                    service.setServiceState(changeClusterStateByNodes(serviceId, service.getLastopt()));
                    if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                        componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                    }
                    statefulServiceRepository.save(service);
                }
            } catch (Exception e) {
                LOG.error("操作超时修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            }
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changePrometheusClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changePrometheusClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * 查询zk集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public ZkCluster getZkCluster(String tenantName, String clusterName) {
        LOG.info("获取zkCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        ZkCluster zkCluster = null;
        try {
            zkCluster = ComponentClientFactory.getZkClient().inNamespace(tenantName).withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取zkCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return zkCluster;
    }

    /**
     * 修改zk集群
     *
     * @param nameSpace
     * @param zkCluster
     * @return
     */
    public boolean updateZkCluster(String tenantName, ZkCluster zkCluster) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("nameSpace is empty!");
            return false;
        }
        if (null == zkCluster) {
            LOG.error("修改 zkCluster时zkCluster为空");
            return false;
        }
        try {
            ZkCluster updatedCluster = ComponentClientFactory.getZkClient().inNamespace(tenantName)
                    .createOrReplace(zkCluster);
            LOG.info("修改zkCluster后返回的结果：" + JSON.toJSONString(updatedCluster));
            if (null != updatedCluster) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("zk修改失败", e);
            return false;
        }
    }

    /**
     * 修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     * @throws ErrorMessageException
     */
    public void changeZkClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId, String serviceName,
            Map<String, String> serviceExtendedField, Map<String, Map<String, String>> nodesExtendedField)
            throws ErrorMessageException {
        ZkCluster zkCluster = getZkCluster(tenantName, serviceName);
        if (null == zkCluster || null == zkCluster.getStatus() || null == zkCluster.getStatus().getInstances()) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("修改集群和节点状态时，获取的zkcluster为空，tenantName:" + tenantName + ",serviceName:" + serviceName
                    + ",修改集群和节点状态为failed");
            return;
        }
        Map<String, ZkInstance> serverNodes = zkCluster.getStatus().getInstances();
        try {
            List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            if (null != nodeList && !nodeList.isEmpty()) {
                for (StatefulNode node : nodeList) {
                    if (serverNodes.containsKey(node.getNodeName())) {
                        ZkInstance zkInstance = serverNodes.get(node.getNodeName());
                        if (ZkClusterConst.getZKNodeStateWaitingList().contains(zkInstance.getInstancePhase())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (CommonConst.STATE_NODE_RUNNING.equals(zkInstance.getInstancePhase())) {
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    newNodeExtendedField.put("ip", zkInstance.getExterHost());
                                    newNodeExtendedField.put("port", String.valueOf(zkInstance.getExterport()));
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(serverNodes.get(node.getNodeName()).getInstancePhase());
                        }
                        if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                && nodesExtendedField.containsKey(node.getNodeName())) {
                            componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                    nodesExtendedField.get(node.getNodeName()));
                        }
                        statefulNodeRepository.save(node);
                    }
                }
            }
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                service.setServiceState(getZkClusterStateByNodes(serviceId, service.getLastopt()));
                if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                } else {
                    Map<String, String> oldServiceExtendedField = componentOperationsDataBaseUtil
                            .getServiceExtendedField(service.getExtendedField());
                    String parameterUpdateNeedRestart = zkCluster.getStatus().getParameterupdateneedrestart();
                    String resourceUpdateNeedRestart = zkCluster.getStatus().getResourceupdateneedrestart();
                    if (CommonConst.EFFECTIVE_TRUE.equals(parameterUpdateNeedRestart)) {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    if (CommonConst.EFFECTIVE_TRUE.equals(resourceUpdateNeedRestart)) {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                    } else {
                        oldServiceExtendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);
                    }
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, oldServiceExtendedField);
                }
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "修改集群和节点状态失败，serviceId:" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changeZkClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changeZkClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }

    /**
     * zk根据节点状态修正集群状态
     *
     * @param id
     * @return
     */
    public String getZkClusterStateByNodes(String serviceId, String lastOpt) {
        LOG.info("根据所有节点状态获取集群状态...");
        List<StatefulNode> nodes = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                CommonConst.STATE_CLUSTER_DELETED);
        if (null == nodes || nodes.isEmpty()) {
            LOG.info("根据所有节点状态获取集群状态失败！集群上次操作为：" + lastOpt);
            if (CommonConst.ACTION_CLUSTER_DELETE.equals(lastOpt) || CommonConst.ACTION_NODE_DELETE.equals(lastOpt)) {
                return CommonConst.STATE_CLUSTER_DELETED;
            }
            return CommonConst.STATE_CLUSTER_FAILED;
        }

        int runNum = 0, stoppedNum = 0, failedNum = 0;
        for (StatefulNode node : nodes) {
            if (CommonConst.STATE_NODE_WAITING.equals(node.getNodeState())) {
                return CommonConst.STATE_CLUSTER_WAITING;
            }
            if (CommonConst.STATE_NODE_RUNNING.equals(node.getNodeState())) {
                runNum++;
            }
            if (CommonConst.STATE_NODE_STOPPED.equals(node.getNodeState())) {
                stoppedNum++;
            }
            if (CommonConst.STATE_NODE_FAILED.equals(node.getNodeState())) {
                failedNum++;
            }
        }
        LOG.info("runNum:" + runNum + "   " + "stoppedNum:" + stoppedNum + "   " + "failedNum:" + failedNum);
        if (runNum == nodes.size()) {
            LOG.info("返回的集群状态为running");
            return CommonConst.STATE_CLUSTER_RUNNING;
        }
        if (stoppedNum == nodes.size()) {
            LOG.info("返回的集群状态是stopped");
            return CommonConst.STATE_CLUSTER_STOPPED;
        }
        if (runNum > nodes.size() / 2) {
            LOG.info("返回的集群状态是warning");
            return CommonConst.STATE_CLUSTER_WARNING;
        }

        LOG.info("返回的集群状态是failed");
        return CommonConst.STATE_CLUSTER_FAILED;
    }

    /**
     * 查询postgresql集群
     *
     * @param nameSpace
     * @param name
     * @return
     */
    public PostgresqlCluster getPostgresqlCluster(String tenantName, String clusterName) {
        LOG.info("获取postgresqlCluster，tanantName:" + tenantName + ",clusterName:" + clusterName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return null;
        }
        PostgresqlCluster postgresqlCluster = null;
        try {
            postgresqlCluster = ComponentClientFactory.getPostgresqlClient().inNamespace(tenantName)
                    .withName(clusterName).get();
        } catch (Exception e) {
            LOG.error("获取postgresqlCluster失败,nameSpace" + tenantName + ",clusterName:" + clusterName + ",error:", e);
            return null;
        }
        return postgresqlCluster;
    }

    /**
     * 修改postgresql集群
     *
     * @param nameSpace
     * @param postgresqlCluster
     * @return
     */
    public boolean updatePostgresqlCluster(String tenantName, PostgresqlCluster postgresqlCluster) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("nameSpace is empty!");
            return false;
        }
        if (null == postgresqlCluster) {
            LOG.error("修改 postgresqlCluster时postgresqlCluster为空");
            return false;
        }
        try {
            PostgresqlCluster updatedCluster = ComponentClientFactory.getPostgresqlClient().inNamespace(tenantName)
                    .createOrReplace(postgresqlCluster);
            LOG.info("修改postgresqlCluster后返回的结果：" + JSON.toJSONString(updatedCluster));
            if (null != updatedCluster) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("postgresql修改失败", e);
            return false;
        }
    }

    /**
     * 给集群修改lvm资源
     *
     * @param nameSpace
     * @param ftpCluster
     */
    public void repleacePostgresqlClusterLvm(String tenantName, PostgresqlCluster postgresqlCluster) {
        LOG.info(
                "修改lvm,接收的参数=>tenantName:" + tenantName + ",postgresqlCluster:" + JSON.toJSONString(postgresqlCluster));
        try {
            for (Map.Entry<String, PostgresqlInstance> entry : postgresqlCluster.getStatus().getInstances()
                    .entrySet()) {
                Lvm lvm = getLvm(tenantName, entry.getValue().getLvName());
                if (null == lvm) {
                    lvm = new Lvm();
                    registerLvm(tenantName, entry.getValue().getLvName(), entry.getValue().getExterHost(),
                            StringUtils.unitExchange(postgresqlCluster.getSpec().getStorage()));
                    LOG.info("lvm不存在，创建lvm，lvm：" + JSON.toJSONString(lvm));
                } else {
                    lvm.getSpec().setSize(StringUtils.unitExchange(postgresqlCluster.getSpec().getStorage()));
                    boolean result = replaceLvm(tenantName, lvm);
                    LOG.info("修改lvm结果，result:" + result + ",lvm：" + JSON.toJSONString(lvm));
                }
            }
        } catch (Exception e) {
            LOG.error("修改lvm失败,postgresqlCluster:" + JSON.toJSONString(postgresqlCluster) + ",error:", e);
        }
    }

    /**
     * 修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param serviceExtendedField
     * @param nodesExtendedField
     * @throws ErrorMessageException
     */
    public void changePostgresqlClusterAndNodesStateAndExtendedByYaml(String tenantName, String serviceId,
            String serviceName, Map<String, String> serviceExtendedField,
            Map<String, Map<String, String>> nodesExtendedField) throws ErrorMessageException {
        PostgresqlCluster postgresqlCluster = getPostgresqlCluster(tenantName, serviceName);
        if (null == postgresqlCluster || null == postgresqlCluster.getStatus()
                || null == postgresqlCluster.getStatus().getInstances()) {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_FAILED,
                    CommonConst.STATE_NODE_FAILED, null);
            LOG.error("修改集群和节点状态时，获取的postgresqlCluster为空，tenantName:" + tenantName + ",serviceName:" + serviceName
                    + ",修改集群和节点状态为failed");
            return;
        }
        Map<String, PostgresqlInstance> serverNodes = postgresqlCluster.getStatus().getInstances();
        try {
            List<StatefulNode> nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                    CommonConst.STATE_NODE_DELETED);
            if (null != nodeList && !nodeList.isEmpty()) {
                for (StatefulNode node : nodeList) {
                    if (serverNodes.containsKey(node.getNodeName())) {
                        PostgresqlInstance postgresqlInstance = serverNodes.get(node.getNodeName());
                        if (PostgresqlClusterConst.getNodeStateWaitingList()
                                .contains(postgresqlInstance.getInstancePhase())) {
                            node.setNodeState(CommonConst.STATE_NODE_FAILED);
                        } else {
                            if (CommonConst.STATE_NODE_RUNNING.equals(postgresqlInstance.getInstancePhase())) {
                                if (StringUtils.isEmpty(node.getIp())
                                        || StringUtils.isEmpty(String.valueOf(node.getPort())) || node.getPort() == 0) {
                                    Map<String, String> newNodeExtendedField = new HashMap<>();
                                    newNodeExtendedField.put("ip", postgresqlInstance.getExterHost());
                                    newNodeExtendedField.put("port", String.valueOf(postgresqlInstance.getExterPort()));
                                    componentOperationsDataBaseUtil.updateNodeExtendedField(node, newNodeExtendedField);
                                }
                            }
                            node.setNodeState(serverNodes.get(node.getNodeName()).getInstancePhase());
                        }
                        if (null != nodesExtendedField && !nodesExtendedField.isEmpty()
                                && nodesExtendedField.containsKey(node.getNodeName())) {
                            componentOperationsDataBaseUtil.updateNodeExtendedField(node,
                                    nodesExtendedField.get(node.getNodeName()));
                        }
                        statefulNodeRepository.save(node);
                    }
                }
            }
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                service.setServiceState(getClusterStateByNodes(serviceId, service.getLastopt()));
                if (null != serviceExtendedField && !serviceExtendedField.isEmpty()) {
                    componentOperationsDataBaseUtil.updateClusterExtendedField(service, serviceExtendedField);
                }
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("修改集群和节点状态失败，serviceId:" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "修改集群和节点状态失败，serviceId:" + serviceId + ",error:" + e.getMessage());
        }
    }

    /**
     * statecheck修改waiting节点为failed
     *
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @throws ErrorMessageException
     */
    public void changePostgresqlClusterAndNodesStateByYaml(String tenantName, String serviceId, String serviceName)
            throws ErrorMessageException {
        changePostgresqlClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName, null, null);
    }
}
