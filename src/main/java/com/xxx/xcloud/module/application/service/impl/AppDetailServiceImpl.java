package com.xxx.xcloud.module.application.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.application.service.IAppDetailService;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.rest.v1.service.model.Event;
import com.xxx.xcloud.threadpool.BdosSyncService;
import com.xxx.xcloud.utils.DateUtil;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.RollingUpdateDeployment;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月4日 下午5:58:15
 */
@org.springframework.stereotype.Service
public class AppDetailServiceImpl implements IAppDetailService {
    private static final Logger LOG = LoggerFactory.getLogger(AppDetailServiceImpl.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private BdosSyncService bdosSyncService;

    @Override
    public Service getServiceById(String serviceId) {
        Service service = null;
        Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
        if (!serviceOptional.isPresent()) {
            LOG.info("服务不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        } else {
            service = serviceOptional.get();
            String registryImageName = imageService.getRegistryImageName(service.getImageVersionId());
            if (!StringUtils.isEmpty(registryImageName)) {
                service.setRegistryImageName(registryImageName.substring(registryImageName.indexOf("/") + 1)
                        .replace(getPublicLibRepositoryName(), ""));
            } else {
                service.setRegistryImageName("");
            }
            Image image = imageService.getDetailByImageVersionId(service.getImageVersionId()).getImage();
            service.setImageName(image.getImageName());
        }
        return service;
    }
    
    @Override
    public Map<String, Object> getServiceStartLogs(String serviceId, String tenantName) {
        Service service = getServiceById(serviceId);
        Map<String, Object> serviceEvents = new HashMap<String, Object>(16);
        Map<String, String> label = new HashMap<String, String>(16);
        label.put("app", service.getServiceName());
        Map<String, String> field = new HashMap<String, String>(16);
        field.put("involvedObject.kind", "Pod");

        try {
            PodList podList = KubernetesClientFactory.getClient().inNamespace(service.getTenantName()).pods()
                    .withLabels(label).list();
            for (Pod pod : podList.getItems()) {
                field.put("involvedObject.name", pod.getMetadata().getName());
                EventList eventList = KubernetesClientFactory.getClient().inNamespace(tenantName).events()
                        .withFields(field).list();
                List<Event> events = new ArrayList<Event>();
                for (io.fabric8.kubernetes.api.model.Event k8sEvent : eventList.getItems()) {
                    Event event = new Event();
                    event.setType(k8sEvent.getType());
                    event.setMessage(k8sEvent.getMessage());
                    event.setTimeStamp(DateUtil.parseStandardDate(k8sEvent.getFirstTimestamp()));
                    events.add(event);
                }
                if (!events.isEmpty()) {
                    serviceEvents.put(pod.getMetadata().getName(), events);
                }
            }
        } catch (Exception e) {
            LOG.error("获取服务:" + service.getServiceName() + "事件日志失败", e);
        }

        return serviceEvents;
    }

    @Override
    @Transactional(rollbackFor = ErrorMessageException.class)
    public boolean updateServiceQuota(String serviceId, Double cpu, Double memory, Integer gpu) {
        Service service = getServiceById(serviceId);
        setRestartFlagTrue(service);
        try {
            service.setCpu(cpu);
            service.setMemory(memory);
            service.setGpu(gpu);
            service.setUpdateTime(new Date());
            serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "配额数据保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "配额数据保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "配额修改成功");
        return true;
    }

    @Override
    public Boolean updateServicePartialInfo(String serviceId, String cmd, String description, String hostAliases,
            String initContainer, Boolean isUsedApm) {
        Service service = getServiceById(serviceId);

        try {
            if (cmd != null) {
                service.setCmd(cmd);
                setRestartFlagTrue(service);
            }
            if (description != null) {
                service.setDescription(description);
            }
            if (isUsedApm != null) {
                service.setIsUsedApm(isUsedApm);
                setRestartFlagTrue(service);
            }
            if (hostAliases != null) {
                service.setHostAliases(hostAliases);
                setRestartFlagTrue(service);
            }
            if (hostAliases != null) {
                service.setInitContainer(initContainer);
                setRestartFlagTrue(service);
            }
            service.setUpdateTime(new Date());
            serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "自定义启动命令|描述 修改失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "信息修改失败");
        }
        LOG.info("服务:" + service.getServiceName() + "自定义启动命令|描述 修改成功");
        return true;
    }

    @Override
    public boolean serviceElasticScale(String serviceId, Integer instanceNum) {
        Service service = getServiceById(serviceId);
        serviceIsRuningState(service);
        if (null != service.getHpa()) {
            Map<String, Object> hpa = JSON.parseObject(service.getHpa(), new TypeReference<Map<String, Object>>() {
            });
            Boolean isTurnOn = (Boolean) hpa.get("isTurnOn");
            if (isTurnOn) {
                LOG.info("已经使用自动伸缩不允许再使用弹性伸缩");
                throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED, "已经使用自动伸缩不允许再使用弹性伸缩");
            }
        }
        if (service.getInstance() == instanceNum) {
            LOG.info("实例个数未发生改变");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED, "实例个数未发生改变");
        }
        Deployment k8sDeployment = null;
        try {
            Deployment deployment = null;
            deployment = KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                    .withName(service.getServiceName()).get();
            if (null == deployment) {
                LOG.info("Deployment:" + service.getServiceName() + "不存在");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                        "服务:" + service.getServiceName() + "弹性伸缩配置失败");
            }
            deployment.getSpec().setReplicas(instanceNum);
            k8sDeployment = KubernetesClientFactory.getClient().apps().deployments()
                    .inNamespace(service.getTenantName()).withName(service.getServiceName()).replace(deployment);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "弹性伸缩设置失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED,
                    "服务:" + service.getServiceName() + "弹性伸缩配置失败");
        }
        if (null != k8sDeployment) {
            try {
                service.setInstance(instanceNum);
                service.setUpdateTime(new Date());
                serviceRepository.save(service);
            } catch (Exception e) {
                LOG.error("服务:" + service.getServiceName() + "弹性伸缩配置保存失败", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                        "服务:" + service.getServiceName() + "弹性伸缩配置保存失败");
            }
        } else {
            LOG.info("服务:" + service.getServiceName() + "弹性伸缩配置保存失败");
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED,
                    "服务:" + service.getServiceName() + "弹性伸缩配置失败");
        }
        LOG.info("服务:" + service.getServiceName() + "弹性伸缩配置成功");
        return true;
    }

    @Override
    public boolean upgradeImageVersion(String serviceId, String imageVersionId, IntOrString maxUnavailable) {
        Service service = getServiceById(serviceId);
        serviceIsRuningState(service);
        String registryImageName = imageService.getRegistryImageName(imageVersionId);
        Deployment deployment = null;
        Deployment updateDeployment = null;
        try {
            deployment = KubernetesClientFactory.getClient().apps().deployments().inNamespace(service.getTenantName())
                    .withName(service.getServiceName()).get();
            if (null == deployment) {
                LOG.info("Deployment:" + service.getServiceName() + "不存在");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                        "服务:" + service.getServiceName() + "版本升级失败");
            }
            deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(registryImageName);
            RollingUpdateDeployment rollingUpdate = deployment.getSpec().getStrategy().getRollingUpdate();
            // 升级时不可用pod的上限(若pod期望值为10,maxUnavailable值设置为3,则升级时至少有10-3=7个pod实例可用,maxSurge=0时maxUnavailable必须大于0)
            // 是否是按照步长升级待验证
            rollingUpdate.setMaxUnavailable(maxUnavailable);
            // 升级时生存pod实例的最大值(若pod期望值为10,maxSurge值设置为3,则升级过程中pod实例的峰值为10+3=13)
            rollingUpdate.setMaxSurge(new IntOrString(0));
            deployment.getSpec().getStrategy().setRollingUpdate(rollingUpdate);
            updateDeployment = KubernetesClientFactory.getClient().apps().deployments()
                    .inNamespace(service.getTenantName()).withName(service.getServiceName())
                    .createOrReplace(deployment);
        } catch (Exception e) {
            LOG.error("Deployment:" + service.getServiceName() + "修改失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED,
                    "服务:" + service.getServiceName() + "版本升级失败");
        }
        if (null == updateDeployment) {
            LOG.info("Deployment:" + service.getServiceName() + "修改失败");
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED,
                    "服务:" + service.getServiceName() + "版本升级失败");
        }
        bdosSyncService.checkUpgradeImageVersion(service, imageVersionId);
        try {
            service.setStatus(Global.OPERATION_UPDATING);
            service.setUpdateTime(new Date());
            serviceRepository.save(service);
        } catch (Exception e) {
            LOG.error("服务:" + service.getServiceName() + "版本升级数据保存失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "服务:" + service.getServiceName() + "版本升级数据保存失败");
        }
        LOG.info("服务:" + service.getServiceName() + "版本升级成功");
        return true;
    }

    /**
     * Description:判断服务是否在运行状态下,如果不是则抛异常
     *
     * @param service
     *            void
     */
    private void serviceIsRuningState(Service service) {
        if (Global.OPERATION_RUNNING != service.getStatus()) {
            LOG.info("服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
        }
    }

    /**
     *
     * <p>
     * Description: 服务在启动中或运行中，设置服务重新生效字段为true
     * </p>
     *
     * @param service
     */
    private void setRestartFlagTrue(Service service) {
        if (Global.OPERATION_RUNNING == service.getStatus() || Global.OPERATION_STARTING == service.getStatus()
                || Global.OPERATION_UPDATING == service.getStatus()
                || Global.OPERATION_UPDATE_FAILED == service.getStatus()) {
            service.setIsRestartEffect(true);
        }
    }

    /**
     * 获取公共仓库名称
     *
     * @return String
     * @date: 2019年4月24日 上午10:06:12
     */
    private String getPublicLibRepositoryName() {
        return XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/";
    }

}
