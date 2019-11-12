package com.xxx.xcloud.module.configmap.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.configmap.entity.ConfigTemplate;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;
import com.xxx.xcloud.module.configmap.repository.ConfigTemplateRepository;
import com.xxx.xcloud.module.configmap.repository.ServiceConfigRepository;
import com.xxx.xcloud.module.configmap.service.ConfigService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * 
 * <p>
 * Description: 配置文件模板功能实现类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigServiceImpl.class);

    private static final String OPERATE_UPDATE = "update";
    private static final String OPERATE_DELETE = "delete";

    @Autowired
    private ServiceConfigRepository serviceConfigRepository;

    @Autowired
    private ConfigTemplateRepository configTemplateRepository;


    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    private ConfigMap generateConfigMap(String name, Map<String, String> data) {
        ConfigMap configMap = new ConfigMap();
        configMap.setData(data);
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(name);
        configMap.setMetadata(objectMeta);

        return configMap;
    }

    @Override
    public ConfigTemplate add(ConfigTemplate configTemplate) {
        Tenant tenant = tenantService.findTenantByTenantName(configTemplate.getTenantName());
        if (tenant == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "指定的租户不存在");
        }
        if (!configTemplate.getTemplateName().matches(Global.CHECK_CONFIGMAP_NAME)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "模板名称不符合正则规范");
        }
        if (null != configTemplateRepository.findByTenantNameAndTemplateName(configTemplate.getTenantName(),
                configTemplate.getTemplateName())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "指定的模板已经存在");
        }

        Map<String, String> map = new HashMap<String, String>(16);
        try {
            map = JSON.parseObject(configTemplate.getConfigData(), new TypeReference<Map<String, String>>() {});
            configTemplate.setFileNumber(map.size());
        } catch (Exception e) {
            LOG.error("模板数据转换map失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "模板数据不符合{key:value,key2:value2}格式");
        }

        try {
            KubernetesClientFactory.getClient().configMaps().inNamespace(configTemplate.getTenantName())
                    .create(generateConfigMap(configTemplate.getTemplateName(), map));
        } catch (Exception e) {
            LOG.error("k8s创建configmap失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_CONFIGMAP_FAILED, "k8s创建configmap失败");
        }

        configTemplate.setCreateTime(new Date());
        configTemplate = configTemplateRepository.save(configTemplate);

        return configTemplate;
    }

    @Override
    @Transactional(rollbackFor = ErrorMessageException.class)
    public void delete(String id) {
        checkMountedServiceStatus(id, OPERATE_DELETE);
        ConfigTemplate configTemplate = configTemplateRepository.findById(id).get();
        if (configTemplate != null) {
            try {
                KubernetesClientFactory.getClient().configMaps().inNamespace(configTemplate.getTenantName())
                        .withName(configTemplate.getTemplateName()).cascading(true).delete();
            } catch (Exception e) {
                throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_CONFIGMAP_FAILED, "k8s删除configmap失败");
            }
            configTemplateRepository.delete(configTemplate);
            serviceConfigRepository.deleteByConfigTemplateId(id);
        }
    }

    @Override
    public void update(String id, Map<String, String> configData) {

        checkMountedServiceStatus(id, OPERATE_UPDATE);
        ConfigTemplate configTemplate = configTemplateRepository.findById(id).get();
        if (configTemplate == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "指定的模板不存在");
        }

        try {
            KubernetesClientFactory.getClient().configMaps().inNamespace(configTemplate.getTenantName())
                    .createOrReplace(generateConfigMap(configTemplate.getTemplateName(), configData));
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_CONFIGMAP_FAILED, "k8s修改configmap失败");
        }

        configTemplate.setConfigData(JSON.toJSONString(configData));
        configTemplate.setFileNumber(configData.size());
        configTemplateRepository.save(configTemplate);
    }

    @Override
    public ConfigTemplate get(String id) {
        return configTemplateRepository.findById(id).get();
    }

    @Override
    public List<ServiceConfig> listMount(String serviceId) {
        List<ServiceConfig> serviceConfigs = serviceConfigRepository.findByServiceId(serviceId);
        for (ServiceConfig serviceConfig : serviceConfigs) {
            ConfigTemplate configTemplate = configTemplateRepository.findById(serviceConfig.getConfigTemplateId())
                    .get();
            serviceConfig.setConfigTemplate(configTemplate);
        }

        return serviceConfigs;
    }

    @Override
    @Transactional(rollbackFor = ErrorMessageException.class)
    public void mountSave(String serviceId, Map<String, String> map) {
        serviceConfigRepository.deleteByServiceId(serviceId);
        if (map != null) {
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String configTemplateId = iterator.next();
                String path = map.get(configTemplateId);
                if (configTemplateRepository.findById(configTemplateId).get() == null) {
                    throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "configmap模板不存在");
                }
                if (StringUtils.isEmpty(path)) {
                    throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "configmap挂载路径为空");
                }
                ServiceConfig serviceConfig = new ServiceConfig();
                serviceConfig.setServiceId(serviceId);
                serviceConfig.setConfigTemplateId(configTemplateId);
                serviceConfig.setPath(path);

                serviceConfigRepository.save(serviceConfig);
            }
        }
    }

    @Override
    public void mountSaveSingleTemplate(ServiceConfig serviceConfig) {
        serviceConfigRepository.save(serviceConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mountCancel(String serviceId, String configTemplateId) {
        serviceConfigRepository.deleteByServiceIdAndConfigTemplateId(serviceId, configTemplateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mountCancelById(String serviceConfigId) {
        serviceConfigRepository.deleteById(serviceConfigId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mountClear(String serviceId) {
        serviceConfigRepository.deleteByServiceId(serviceId);
    }

    @Override
    public List<ConfigTemplate> list(String tenantName, String templateName, String projectId) {
        if (StringUtils.isEmpty(templateName)) {
            templateName = "";
        }
        templateName = "%" + templateName + "%";
        List<ConfigTemplate> configTemplates = new ArrayList<ConfigTemplate>();
        if (StringUtils.isEmpty(projectId)) {
            configTemplates = configTemplateRepository.findByTenantNameAndTemplateNameLike(tenantName, templateName);
        } else {
            configTemplates = configTemplateRepository.findByTenantNameAndTemplateNameLikeAndProjectId(tenantName,
                    templateName, projectId);
        }
        if (configTemplates.size() > 0) {
            for (ConfigTemplate configTemplate : configTemplates) {
                /*
                 * List<ServiceConfig> serviceConfigs = serviceConfigRepository
                 * .findByConfigTemplateId(configTemplate.getId());
                 * List<com.xxx.xcloud.module.application.entity.Service>
                 * services = new
                 * ArrayList<com.xxx.xcloud.module.application.entity.Service>()
                 * ; if (serviceConfigs.size() > 0) { for (ServiceConfig
                 * serviceConfig : serviceConfigs) {
                 * Optional<com.xxx.xcloud.module.application.entity.Service>
                 * serviceOptional = serviceRepository
                 * .findById(serviceConfig.getServiceId()); if
                 * (serviceOptional.isPresent()) {
                 * services.add(serviceOptional.get()); } } }
                 * configTemplate.setServices(services);
                 */
                Map<String, String> configData = null;
                try {
                    configData = JSON.parseObject(configTemplate.getConfigData(),
                            new TypeReference<Map<String, String>>() {});
                } catch (Exception e) {
                    LOG.error("configData 数据格式有误，不是map格式！configData:" + configTemplate.getConfigData(), e);
                }

                if (null != configData) {
                    configTemplate.setFileNumber(configData.size());
                } else {
                    configTemplate.setFileNumber(0);
                }
            }
        }
        return configTemplates;
    }

    /**
     *
     * <p>
     * Description: 检查是否有服务使用当前配置文件模板，有运行中的则不允许修改，且不允许删除
     * </p>
     *
     * @param configTemplateId
     *            模板id
     * @param type
     *            操作配置文件模板的行为 @
     */
    private void checkMountedServiceStatus(String configTemplateId, String type) {
        /*
         * List<ServiceConfig> serviceConfigs =
         * serviceConfigRepository.findByConfigTemplateId(configTemplateId); for
         * (ServiceConfig serviceConfig : serviceConfigs) {
         * Optional<com.xxx.xcloud.module.application.entity.Service>
         * serviceOptional = serviceRepository
         * .findById(serviceConfig.getServiceId()); if
         * (serviceOptional.isPresent()) { if (OPERATE_DELETE.equals(type)) {
         * throw new
         * ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_DELETE_FAILED,
         * "当前有服务 " + serviceOptional.get().getServiceName() + " 在使用此模板，禁止删除！");
         * } else if (OPERATE_UPDATE.equals(type)) { byte status =
         * serviceOptional.get().getStatus(); if (status ==
         * Global.OPERATION_RUNNING || status == Global.OPERATION_UPDATING ||
         * status == Global.OPERATION_UPDATE_FAILED) { throw new
         * ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE,
         * "当前有运行中的服务 " + serviceOptional.get().getServiceName() +
         * " 在使用此模板，禁止修改！"); } } } }
         */
    }

    @Override
    public ConfigTemplate getConfigTemplateByNameAndTenantName(String configTemplateName, String tenantname) {
        ConfigTemplate configTemplate = null;
        try {
            configTemplate = configTemplateRepository.findByTenantNameAndTemplateName(tenantname, configTemplateName);
        } catch (Exception e) {
            LOG.error("查询配置文件模板失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询配置文件模板失败");
        }
        return configTemplate;
    }

    @Override
    public Page<ConfigTemplate> getConfigTemplateList(String tenantName, String templateName, String projectId,
            PageRequest pageable) {
        Page<ConfigTemplate> configTemplatePage = null;

        try {
            if (StringUtils.isNotEmpty(templateName)) {
                if (StringUtils.isEmpty(projectId)) {
                    configTemplatePage = configTemplateRepository
                            .findByTemplateNameLikeAndTenantNameOrderByCreateTimeDesc("%" + templateName + "%",
                                    tenantName, pageable);
                } else {
                    configTemplatePage = configTemplateRepository
                            .findByTemplateNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc(
                                    "%" + templateName + "%", tenantName, projectId, pageable);
                }
            } else {
                if (StringUtils.isEmpty(projectId)) {
                    configTemplatePage = configTemplateRepository.findByTenantNameOrderByCreateTimeDesc(tenantName,
                            pageable);
                } else {
                    configTemplatePage = configTemplateRepository
                            .findByTenantNameAndProjectIdOrderByCreateTimeDesc(tenantName, projectId, pageable);
                }
            }
        } catch (Exception e) {
            LOG.error("查询配置文件模板列表失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询配置文件模板列表失败");
        }

        return configTemplatePage;
    }

}
