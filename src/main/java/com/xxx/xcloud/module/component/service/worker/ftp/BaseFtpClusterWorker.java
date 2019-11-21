package com.xxx.xcloud.module.component.service.worker.ftp;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.component.ComponentClientFactory;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;
import com.xxx.xcloud.module.component.model.ftp.FtpNode;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;
import com.xxx.xcloud.module.component.service.worker.base.BaseComponentServiceWorker;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BaseFtpClusterWorker extends BaseComponentServiceWorker {

    private static Logger LOG = LoggerFactory.getLogger(BaseFtpClusterWorker.class);

    /**
     * 创建ftp集群
     * 
     * @param nameSpace
     * @param ftpCluster
     * @return
     */
    protected boolean createFtpCluster(String nameSpace, FtpCluster ftpCluster) {
        if (StringUtils.isEmpty(nameSpace)) {
            LOG.error("创建ftpCluster时tenantName为空");
            return false;
        }
        if (null == ftpCluster) {
            LOG.error("创建ftpCluster时ftpCluster为空");
            return false;
        }
        try {
            FtpCluster oldCluster = componentOperationsClientUtil.getFtpCluster(nameSpace,
                    ftpCluster.getMetadata().getName());
            if (null == oldCluster) {
                FtpCluster newCluster = ComponentClientFactory.getFtpClient().inNamespace(nameSpace).create(ftpCluster);
                LOG.info("ftp集群创建后返回的cluster：" + JSON.toJSONString(newCluster));
                if (null != newCluster) {
                    return true;
                }
                return false;
            } else {
                LOG.error("ftp集群已经存在,租户:" + ftpCluster.getMetadata().getNamespace() + ",集群名称:"
                        + ftpCluster.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("ftp集群创建失败", e);
            return false;
        }
    }

    /**
     * 删除ftp集群
     * 
     * @param nameSpace
     * @param clusterName
     * @return
     */
    protected boolean deleteFtpCluster(String tenantName, String clusterName) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return false;
        } else if (StringUtils.isEmpty(clusterName)) {
            LOG.error("clusterName is empty!");
            return false;
        }
        boolean result = false;
        try {
            FtpCluster ftpCluster = ComponentClientFactory.getFtpClient().inNamespace(tenantName).withName(clusterName)
                    .get();
            if (null == ftpCluster) {
                return true;
            }
            result = ComponentClientFactory.getFtpClient().inNamespace(tenantName).withName(clusterName).delete();
            LOG.info("FtpCluster删除result:" + result + "，clusterName：" + clusterName);
        } catch (Exception e) {
            LOG.error("ftp删除失败，clusterName：" + clusterName, e);
            return false;
        }
        return result;
    }

    /**
     * 删除集群
     * 
     * @param tenantName
     * @param ftpCluster
     * @return
     */
    protected boolean deleteAndRetry(String tenantName, String serviceName) {
        LOG.info("==========开始删除集群==========");
        LOG.info("删除集群的tenantName：" + tenantName + ",serviceName:" + JSON.toJSONString(serviceName));
        FtpCluster ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, serviceName);
        if (null == ftpCluster) {
            LOG.info("ftpcluster已经不存在");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            LOG.info("当前重试次数为：" + Integer.toString(i + 1));
            boolean result = deleteFtpCluster(tenantName, serviceName);
            if (result) {
                return true;
            }
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                LOG.error("线程休眠异常！", e);
            }
            if (i == 2) {
                LOG.error("删除ftp集群" + serviceName + "超过最大限制次数！");
                return false;
            }
        }
        return false;
    }

    /**
     * 
     * @param tenantName
     * @param ftpCluster
     * @return
     */
    protected boolean updateAndRetry(String tenantName, FtpCluster ftpCluster) {
        LOG.info("==========开始修改集群==========");
        LOG.info("修改集群的ftpCluster：" + JSON.toJSONString(ftpCluster));
        if (null != ftpCluster) {
            for (int i = 0; i < 3; i++) {
                LOG.info("当前重试次数为：" + Integer.toString(i + 1));
                boolean result = componentOperationsClientUtil.updateFtpCluster(tenantName, ftpCluster);
                if (result) {
                    return true;
                }
                try {
                    Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                } catch (Exception e) {
                    LOG.error("线程休眠异常！", e);
                }
                if (i == 2) {
                    LOG.error("修改ftp集群" + ftpCluster.getMetadata().getName() + "超过最大限制次数！");
                    return false;
                }
            }
        } else {
            LOG.error("ftpcluster为null");
        }
        return false;
    }

    /**
     * 修改ftpCluster的yaml 集群启动，停止 节点启动，停止
     * 
     * @param tenantName
     * @param serviceId
     * @return
     */
    protected FtpCluster updateYamlForFtpCluster(String tenantName, String serviceName, String opt, String nodeName) {
        LOG.info("修改ftp集群，接收的参数=>tenantName:" + tenantName + ",serviceName:" + serviceName + ",opt:" + opt
                + ",nodeName:" + nodeName);
        FtpCluster ftpCluster = null;
        ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, serviceName);
        if (null == ftpCluster) {
            LOG.error("获取ftpCluster失败，tenantName：" + tenantName + ",serviceName:" + serviceName);
            return null;
        }
        switch (opt) {
        case FtpClusterConst.OPERATOR_CLUSTER_START:
        case FtpClusterConst.OPERATOR_CLUSTER_STOP:
            ftpCluster.getSpec().getFtpop().setOperator(opt);
            ftpCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            break;
        case FtpClusterConst.OPERATOR_NODE_START:
        case FtpClusterConst.OPERATOR_NODE_STOP:
            ftpCluster.getSpec().getFtpop().setOperator(opt);
            ftpCluster.getSpec().getFtpop().setName(nodeName);
            ftpCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            break;
        case FtpClusterConst.OPERATOR_NODE_DELETE:
            ftpCluster.getSpec().getFtpop().setOperator(opt);
            ftpCluster.getSpec().getFtpop().setName(nodeName);
            int oldReplicas = ftpCluster.getSpec().getReplicas();
            ftpCluster.getSpec().setReplicas(oldReplicas - 1);
            ftpCluster.getSpec().setUpdatetime(String.valueOf(new Date()));
            break;
        default:
            break;
        }

        return ftpCluster;
    }

    @Override
    protected String getStatusFromYaml(String tenantName, String serviceName, String nodeName) {
        String returnStatus = null;
        FtpCluster cluster = componentOperationsClientUtil.getFtpCluster(tenantName, serviceName);
        LOG.info("cluster:" + JSON.toJSONString(cluster));
        if (null == cluster) {
            return null;
        }
        if (StringUtils.isEmpty(nodeName)) {
            return cluster.getStatus().getPhase();
        } else {
            if (null != cluster.getStatus().getServerNodes() && !cluster.getStatus().getServerNodes().isEmpty()) {
                for (Map.Entry<String, FtpNode> entry : cluster.getStatus().getServerNodes().entrySet()) {
                    if (nodeName.equals(entry.getKey())) {
                        return entry.getValue().getStatus();
                    }
                }
            }
        }
        return returnStatus;
    }

    /**
     * 集群或节点操作时修改ftp集群的用户是否生效字段
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     */
    protected void updateServiceExtendedField(String tenantName, String serviceId, String serviceName) {
        LOG.info("修改ftpCluster用户的扩展字段");
        try {
            StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                JSONObject extendedFieldJson = new JSONObject();
                if (StringUtils.isNotEmpty(service.getExtendedField())) {
                    extendedFieldJson = JSON.parseObject(service.getExtendedField());
                }
                LOG.info("service原有的扩展字段为：" + JSON.toJSONString(extendedFieldJson));
                if (!extendedFieldJson.isEmpty()) {
                    if (extendedFieldJson.containsKey(FtpClusterConst.EXTENDED_FIELD_FTPUSER)) {
                        JSONObject extendedFieldUserMap = extendedFieldJson
                                .getJSONObject(FtpClusterConst.EXTENDED_FIELD_FTPUSER);
                        if (null != extendedFieldUserMap && !extendedFieldUserMap.isEmpty()) {

                            for (Map.Entry<String, Object> entry : extendedFieldUserMap.entrySet()) {
                                FtpUser ftpUser = JSON.parseObject(JSON.toJSONString(entry.getValue()), FtpUser.class);
                                ftpUser.setEffective(CommonConst.EFFECTIVE_TRUE);
                                extendedFieldUserMap.put(entry.getKey(), ftpUser);
                            }
                            extendedFieldJson.put(FtpClusterConst.EXTENDED_FIELD_FTPUSER, extendedFieldUserMap);
                            LOG.info("ftp集群合并后的扩展字段的用户部分为：" + extendedFieldJson);
                        }
                    }

                    FtpCluster ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, serviceName);
                    if (null == ftpCluster) {
                        LOG.error("通过k8s获取ftpCluster为null");
                        return;
                    }
                    if (null == ftpCluster.getStatus()) {
                        LOG.error("通过k8s获取ftpCluster的Status为null");
                        return;
                    }
                    boolean resourceEffective = !ftpCluster.getStatus().isResourceupdateneedrestart();
                    extendedFieldJson.put(CommonConst.RESOURCE_EFFECTIVE, String.valueOf(resourceEffective));

                    service.setExtendedField(JSON.toJSONString(extendedFieldJson));
                    LOG.info("ftp集群合并后的扩展字段为：" + service.getExtendedField());
                    statefulServiceRepository.save(service);
                }
            }
        } catch (Exception e) {
            LOG.error("根据serviceId修改集群的扩展字段失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",serviceName:"
                    + serviceName + ", e: " + e.getMessage());
        }

    }

}
