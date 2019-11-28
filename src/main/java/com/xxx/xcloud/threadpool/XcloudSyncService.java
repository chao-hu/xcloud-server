package com.xxx.xcloud.threadpool;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.application.service.CheckPodStatusScheduledThreadPool;
import com.xxx.xcloud.module.application.service.IAppDetailService;
import com.xxx.xcloud.utils.SpringUtils;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;

/**
 * Description:使用线程的方法(方法上加上@Async注解即可)
 *
 * @author LYJ </br>
 *         create time：2018年12月19日 下午2:54:39 </br>
 * @version 1.0
 * @since
 */
@Component
public class XcloudSyncService {

    private static final Logger LOG = LoggerFactory.getLogger(XcloudSyncService.class);

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Description:检查是否版本升级完成
     *
     * @author LYJ
     * @param service
     * @param imageId
     *            void
     */
    @Async
    public void checkUpgradeImageVersion(Service service, String imageVersionId) {
        long start = System.currentTimeMillis();
        String type = "Progressing";
        String status = "False";
        String reason = "ProgressDeadlineExceeded";
        while (true) {
            try {
                Thread.sleep(5000);
                Deployment deployment = KubernetesClientFactory.getClient().apps().deployments()
                        .inNamespace(service.getTenantName()).withName(service.getServiceName()).get();
                Integer replicas = deployment.getStatus().getReplicas();
                Integer updatedReplicas = deployment.getStatus().getUpdatedReplicas();
                if (replicas.equals(updatedReplicas)) {
                    service.setStatus(Global.OPERATION_RUNNING);
                    service.setUpdateTime(new Date());
                    service.setImageVersionId(imageVersionId);
                    serviceRepository.save(service);
                    break;
                } else {
                    List<DeploymentCondition> conditions = deployment.getStatus().getConditions();
                    if (conditions.size() > 0) {
                        for (DeploymentCondition deploymentCondition : conditions) {
                            if (type.equals(deploymentCondition.getType())
                                    && status.equals(deploymentCondition.getStatus())
                                    && reason.equals(deploymentCondition.getReason())) {
                                service.setStatus(Global.OPERATION_UPDATE_FAILED);
                                service.setUpdateTime(new Date());
                                serviceRepository.save(service);
                                break;
                            }
                        }
                    }
                }
                System.out.println(
                        "服务:" + service.getServiceName() + "版本升级已经用时： " + (System.currentTimeMillis() - start));
            } catch (Exception e) {
                LOG.error("异步线程->版本升级出错", e);
            }
        }
    }

    @Async
    public void checkServiceRunning(Service service) {
        if (!checkRunning(service)) {
            LOG.error("无实例处于可用状态，服务启动失败");
            Map<String, Object> serviceEvents = SpringUtils.getBean(IAppDetailService.class)
                    .getServiceStartLogs(service.getId(), service.getTenantName());
            try {
                if (!serviceEvents.isEmpty()) {
                    service.setEventLogs(JSON.toJSONString(serviceEvents));
                }
                service.setStatus(Global.OPERATION_START_FAILED);
                service = serviceRepository.save(service);
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ", e1);
            }
            try {
               /* if (createServiceFlag) {
                    KubernetesClientFactory.getClient().services().inNamespace(service.getTenantName())
                            .withName(service.getServiceName()).cascading(true).delete();
                }*/
                KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                        .withName(service.getServiceName()).cascading(true).delete();
            } catch (Exception e) {
                LOG.error("删除svc或deployment失败", e);
            }
        } else {
            try {
                service.setStatus(Global.OPERATION_RUNNING);
                service.setIsRestartEffect(false);
                service = serviceRepository.save(service);
                CheckPodStatusScheduledThreadPool.getInstance()
                        .add(service.getTenantName() + Global.CONCATENATE + service.getServiceName());
                LOG.info("服务 " + service.getServiceName() + " 启动成功 ");
            } catch (Exception e1) {
                LOG.error("数据库连接异常 ", e1);
            }
        }
    }

    private boolean checkRunning(Service service) {
        Map<String, String> labelSelector = new HashMap<String, String>(16);
        labelSelector.put("app", service.getServiceName());
        PodList podList = null;
        long start = System.currentTimeMillis();

        while (true) {
            try {
                boolean flag = false;
                Thread.sleep(3000);
                podList = KubernetesClientFactory.getClient().pods().inNamespace(service.getTenantName())
                        .withLabels(labelSelector).list();
                if (podList == null || podList.getItems().size() == 0) {
                    return false;
                }

                for (Pod pod : podList.getItems()) {
                    if (pod.getStatus().getPhase().equals("Running")) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    return true;
                }

                LOG.info("服务" + service.getServiceName() + "启动用时： " + (System.currentTimeMillis() - start));
                // 设置超时时间为5min
                if (System.currentTimeMillis() - start > 5 * 60 * 1000) {
                    return false;
                }
            } catch (InterruptedException e) {
                return false;
            }
        }
    }
}
