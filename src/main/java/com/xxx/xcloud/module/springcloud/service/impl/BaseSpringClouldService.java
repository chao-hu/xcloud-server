package com.xxx.xcloud.module.springcloud.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.entity.StatefulServiceUnitVersion;
import com.xxx.xcloud.module.component.repository.StatefulServiceUnitVersionRepository;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudServiceRepository;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudService;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;

/**
 * SpringCloud Base Impl
 *
 * @author LiuNanNan
 * @date 2019年4月25日
 */

public abstract class BaseSpringClouldService implements ISpringCloudService {

    private static Logger LOG = LoggerFactory.getLogger(BaseSpringClouldService.class);

    @Autowired
    protected StatefulServiceUnitVersionRepository statefulServiceUnitVersionRepository;

    @Autowired
    protected SpringCloudServiceRepository springCloudServiceRepository;

    /**
     * 获取服务版本对应的镜像
     *
     * @param version
     * @return
     */
    public String getRepoPath(String appType, String extendedField, String version) {
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
     * 根据serviceId获取SpringClouldService
     *
     * @param serviceId
     * @return
     */
    public SpringCloudService getSpringClouldServiceById(String serviceId) throws ErrorMessageException {
        LOG.info("根据serviceId获取SpringClouldService，serviceId：" + serviceId);
        SpringCloudService service = null;
        try {
            service = springCloudServiceRepository.findByIdAndServiceStateNot(serviceId,
                    SpringCloudCommonConst.STATE_APP_DELETED);
        } catch (Exception e) {
            LOG.error("根据serviceId获取StatefulService失败,serviceId：" + serviceId + ",error:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "根据serviceId获取StatefulService失败,serviceId：" + serviceId + ",error:" + e.getMessage());
        }
        return service;
    }

    /**
     * 修改SpringClouldService状态
     *
     * @param serviceId
     * @return
     */
    public Boolean updateServiceState(SpringCloudService springCloudService, String serviceState) {
        try {
            springCloudService.setServiceState(serviceState);
            springCloudServiceRepository.save(springCloudService);
        } catch (Exception e) {
            LOG.error("数据库连接异常，无法更新服务: " + springCloudService.getServiceName() + "的状态, error: ", e);
            return false;
        }
        return true;
    }

    /**
     * 创建deployment
     *
     * @param nameSpace
     * @param deployment
     * @return
     */
    protected boolean createDeployment(String tenantName, Deployment deployment) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("创建deployment时tenantName为空");
            return false;
        }
        if (null == deployment) {
            LOG.error("创建deployment时deployment为空");
            return false;
        }
        try {
            Deployment oldDeployment = getDeployment(tenantName, deployment.getMetadata().getName());
            if (null == oldDeployment) {
                Deployment newDeployment = KubernetesClientFactory.getClient().apps().deployments()
                        .inNamespace(tenantName).create(deployment);
                LOG.info("deployment创建后返回的信息：" + JSON.toJSONString(newDeployment));
                if (null != newDeployment) {
                    return true;
                }
                return false;
            } else {
                LOG.error("deployment已经存在,租户:" + tenantName + ",集群名称:" + deployment.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("deployment创建失败", e);
            return false;
        }
    }

    /**
     * 查询 deployment
     *
     * @param nameSpace
     * @param deploymentName
     * @return
     */
    public Deployment getDeployment(String tenantName, String deploymentName) {
        LOG.info("获取deployment，tanantName:" + tenantName + ",deploymentName:" + deploymentName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(deploymentName)) {
            LOG.error("deploymentName is empty!");
            return null;
        }
        Deployment deployment = null;
        try {
            deployment = KubernetesClientFactory.getClient().apps().deployments().inNamespace(tenantName)
                    .withName(deploymentName).get();
        } catch (Exception e) {
            LOG.error("获取deployment失败,nameSpace" + tenantName + ",deploymentName:" + deploymentName + ",error:", e);
            return null;
        }
        return deployment;
    }

    /**
     * 删除 deployment
     *
     * @param nameSpace
     * @param svcName
     * @return
     */
    public boolean deleteDeployment(String tenantName, String deploymentName) {
        LOG.info("删除deployment，tanantName:" + tenantName + ",deploymentName:" + deploymentName);
        boolean result = false;
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return result;
        } else if (StringUtils.isEmpty(deploymentName)) {
            LOG.error("deploymentName is empty!");
            return result;
        }
        Deployment deployment = getDeployment(tenantName, deploymentName);
        if (null == deployment) {
            return true;
        }
        try {
            result = KubernetesClientFactory.getClient().apps().deployments().inNamespace(tenantName)
                    .withName(deploymentName).cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除deployment失败,nameSpace" + tenantName + ",deploymentName:" + deploymentName + ",error:", e);
            return result;
        }
        return result;
    }

    /**
     * 创建svc
     *
     * @param nameSpace
     * @param svc
     * @return
     */
    protected boolean createSvc(String tenantName, Service svc) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("创建svc时tenantName为空");
            return false;
        }
        if (null == svc) {
            LOG.error("创建svc时svc为空");
            return false;
        }
        try {
            Service oldSvc = getSvc(tenantName, svc.getMetadata().getName());
            if (null == oldSvc) {
                Service newSvc = KubernetesClientFactory.getClient().services().inNamespace(tenantName).create(svc);
                LOG.info("svc创建后返回的信息：" + JSON.toJSONString(newSvc));
                if (null != newSvc) {
                    return true;
                }
                return false;
            } else {
                LOG.error("svc已经存在,租户:" + tenantName + ",svc名称:" + svc.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("svc创建失败", e);
            return false;
        }
    }

    /**
     * 查询 svc
     *
     * @param nameSpace
     * @param svcName
     * @return
     */
    public Service getSvc(String tenantName, String svcName) {
        LOG.info("获取svc，tanantName:" + tenantName + ",svcName:" + svcName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(svcName)) {
            LOG.error("svcName is empty!");
            return null;
        }
        Service svc = null;
        try {
            svc = KubernetesClientFactory.getClient().services().inNamespace(tenantName).withName(svcName).get();
        } catch (Exception e) {
            LOG.error("获取svc失败,nameSpace" + tenantName + ",svcName:" + svcName + ",error:", e);
            return null;
        }
        return svc;
    }

    /**
     * 删除 svc
     *
     * @param nameSpace
     * @param svcName
     * @return
     */
    public boolean deleteSvc(String tenantName, String svcName) {
        LOG.info("删除svc，tanantName:" + tenantName + ",svcName:" + svcName);
        boolean result = false;
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return result;
        } else if (StringUtils.isEmpty(svcName)) {
            LOG.error("svcName is empty!");
            return result;
        }

        Service svc = getSvc(tenantName, svcName);
        if (null == svc) {
            return true;
        }

        try {
            result = KubernetesClientFactory.getClient().services().inNamespace(tenantName).withName(svcName)
                    .cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除svc失败,nameSpace" + tenantName + ",svcName:" + svcName + ",error:", e);
            return result;
        }
        return result;
    }

    /**
     * 查询 PodList
     *
     * @param nameSpace
     * @param serviceName
     * @return
     */
    public PodList getPodList(String tenantName, String serviceName) {
        LOG.info("获取PodList，tanantName:" + tenantName + ",serviceName:" + serviceName);
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(serviceName)) {
            LOG.error("serviceName is empty!");
            return null;
        }
        PodList podList = null;
        Service service = getSvc(tenantName, serviceName);
        if (null != service) {
            try {
                podList = KubernetesClientFactory.getClient().inNamespace(tenantName).pods()
                        .withLabels(service.getSpec().getSelector()).list();
            } catch (Exception e) {
                LOG.error("获取PodList失败,tenantName" + tenantName + ",serviceName:" + serviceName + ",error:", e);
                return null;
            }
        }
        return podList;
    }

    @Override
    public ApiResult stop(String serviceId) {
        // 1. 获取数据库信息
        LOG.info("停止操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        LOG.info("接收SpringCloudService 参数 : " + springCloudService.toString());

        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String nodeNames = springCloudService.getNodeNames();

        boolean result = false;

        List<String> nodeNameList = Arrays.asList(nodeNames.split(","));
        if (null != nodeNameList && nodeNameList.size() > 0) {

            for (String nodeName : nodeNameList) {

                try {
                    // 删除deployment资源
                    result = deleteDeployment(tenantName, nodeName);
                    // 修改状态
                    if (result) {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_STOPPED);
                    } else {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                    }
                    springCloudService = springCloudServiceRepository.save(springCloudService);

                } catch (Exception e) {
                    LOG.error("停止deployment服务对象失败", e);
                    try {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                        springCloudService = springCloudServiceRepository.save(springCloudService);
                    } catch (Exception e1) {
                        LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                        return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, null,
                                "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                    }
                    return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, null, "停止deployment服务对象失败");
                }

            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "停止服务成功");
    }

    @Override
    public ApiResult delete(String serviceId) {
        // 1. 获取数据库信息
        LOG.info("删除操作，接收SpringCloudService ID : " + serviceId);
        SpringCloudService springCloudService = getSpringClouldServiceById(serviceId);
        if (null == springCloudService) {
            LOG.error("根据serviceId： " + serviceId + "获取的SpringCloudService为空");
        }
        LOG.info("接收SpringCloudService 参数 : " + springCloudService.toString());

        String serviceName = springCloudService.getServiceName();
        String tenantName = springCloudService.getTenantName();
        String nodeNames = springCloudService.getNodeNames();

        boolean result = false;

        List<String> nodeNameList = Arrays.asList(nodeNames.split(","));
        if (null != nodeNameList && nodeNameList.size() > 0) {

            for (String nodeName : nodeNameList) {

                try {
                    // 检查deployment资源是否存在，如果存在，就删除deployment
                    result = deleteDeployment(tenantName, nodeName);

                    // 删除svc资源
                    result = deleteSvc(tenantName, nodeName);

                    // 修改状态
                    if (result) {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_DELETED);
                    } else {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                    }

                    springCloudService = springCloudServiceRepository.save(springCloudService);

                } catch (Exception e) {
                    LOG.error("删除deployment或svc对象失败", e);
                    try {
                        springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                        springCloudService = springCloudServiceRepository.save(springCloudService);
                    } catch (Exception e1) {
                        LOG.error("数据库连接异常，无法更新服务: " + serviceName + "的状态, error: ", e1);
                        return new ApiResult(ReturnCode.CODE_SQL_CONNECT_FAILED, null,
                                "数据库连接异常 ，无法更新服务: " + serviceName + "的状态");
                    }
                    return new ApiResult(ReturnCode.CODE_K8S_DEPLOYMENT_FAILED, null, "停止deployment或svc对象失败");
                }

            }
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, springCloudService, "删除服务成功");
    }

    /**
     * 创建configMap
     *
     * @param nameSpace
     * @param configMap
     * @return
     */
    protected boolean createConfigMap(String tenantName, ConfigMap configMap) {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("创建configMap时tenantName为空");
            return false;
        }
        if (null == configMap) {
            LOG.error("创建configMap时configMap为空");
            return false;
        }
        try {
            ConfigMap oldConfigMap = getconfigMap(tenantName, configMap.getMetadata().getName());
            if (null == oldConfigMap) {
                ConfigMap newConfigMap = KubernetesClientFactory.getClient().configMaps().inNamespace(tenantName)
                        .create(configMap);
                LOG.info("configMap创建后返回的信息：" + JSON.toJSONString(newConfigMap));
                if (null != newConfigMap) {
                    return true;
                }
                return false;
            } else {
                LOG.error("configMap已经存在,租户:" + tenantName + ",集群名称:" + configMap.getMetadata().getName());
                return false;
            }
        } catch (Exception e) {
            LOG.error("configMap创建失败", e);
            return false;
        }
    }

    /**
     * 查询 configMap
     *
     * @param nameSpace
     * @param configMapName
     * @return
     */
    public ConfigMap getconfigMap(String tenantName, String configMapName) {
        LOG.info("获取configMap，tanantName:" + tenantName + ",configMapName:" + configMapName);

        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return null;
        } else if (StringUtils.isEmpty(configMapName)) {
            LOG.error("configMapName is empty!");
            return null;
        }
        ConfigMap configMap = null;
        try {
            configMap = KubernetesClientFactory.getClient().configMaps().inNamespace(tenantName).withName(configMapName)
                    .get();
        } catch (Exception e) {
            LOG.error("获取configMap失败,nameSpace" + tenantName + ",configMapName:" + configMapName + ",error:", e);
            return null;
        }
        return configMap;
    }

    /**
     * 删除 configMap
     *
     * @param nameSpace
     * @param configMapName
     * @return
     */
    public boolean deleteConfigMap(String tenantName, String configMapName) {
        LOG.info("删除configMap，tanantName:" + tenantName + ",configMapName:" + configMapName);
        boolean result = false;
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName is empty!");
            return result;
        } else if (StringUtils.isEmpty(configMapName)) {
            LOG.error("configMapName is empty!");
            return result;
        }

        ConfigMap configMap = getconfigMap(tenantName, configMapName);
        if (null == configMap) {
            return true;
        }

        try {
            result = KubernetesClientFactory.getClient().configMaps().inNamespace(tenantName).withName(configMapName)
                    .cascading(true).delete();
        } catch (Exception e) {
            LOG.error("删除configMap失败,nameSpace" + tenantName + ",configMapName:" + configMapName + ",error:", e);
            return result;
        }
        return result;
    }

    @Override
    public abstract ApiResult create(String serviceId);

    @Override
    public abstract ApiResult start(String serviceId);

    @Override
    public SpringCloudService findById(String serviceId) throws Exception {
        SpringCloudService springCloudService;
        Optional<SpringCloudService> springCloudServiceOptional = springCloudServiceRepository.findById(serviceId);
        if (springCloudServiceOptional.isPresent()) {
            springCloudService = springCloudServiceOptional.get();
            return springCloudService;
        } else {
            LOG.error("根据serviceId" + serviceId + "获取service失败");
            return null;
        }
    }

}
