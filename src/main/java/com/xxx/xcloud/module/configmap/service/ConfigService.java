package com.xxx.xcloud.module.configmap.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.xxx.xcloud.module.configmap.entity.ConfigTemplate;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;

/**
 * 
 * <p>
 * Description: 配置文件模板操作接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
public interface ConfigService {

    /**
     * 新增配置文件模板
     * @Title: add
     * @Description: 新增配置文件模板
     * @param configTemplate 配置文件模板
     * @return ConfigTemplate 
     * @throws
     */
    ConfigTemplate add(ConfigTemplate configTemplate);;

    /**
     * 删除配置文件模板
     * @Title: delete
     * @Description: 删除配置文件模板
     * @param id 主键id
     * @throws
     */
    void delete(String id);

    /**
     * 修改配置文件模板内容
     * @Title: update
     * @Description: 修改配置文件模板内容
     * @param id 主键id
     * @param configData data
     * @throws
     */
    void update(String id, Map<String, String> configData);

    /**
     * 根据id获取配置文件模板
     * @Title: get
     * @Description: 根据id获取配置文件模板
     * @param id 主键id
     * @return ConfigTemplate 
     * @throws
     */
    ConfigTemplate get(String id);

    /**
     * 获取配置文件模板列表
     * @Title: list
     * @Description: 获取配置文件模板列表
     * @param tenantName 租户名称
     * @param templateName ConfigTemplate
     * @param projectId 项目id
     * @return List<ConfigTemplate> 
     * @throws
     */
    List<ConfigTemplate> list(String tenantName, String templateName, String projectId);

    /**
     * 返回服务挂载的configmap
     * @Title: listMount
     * @Description: 返回服务挂载的configmap
     * @param serviceId 服务id
     * @return List<ServiceConfig> 
     * @throws
     */
    List<ServiceConfig> listMount(String serviceId);

    /**
     * 挂载多个模板
     * @Title: mountSave
     * @Description: 挂载多个模板
     * @param serviceId 服务id
     * @param map data
     * @throws
     */
    void mountSave(String serviceId, Map<String, String> map);

    /**
     * 挂载单个模板
     * @Title: mountSaveSingleTemplate
     * @Description: 挂载单个模板
     * @param serviceConfig 模板
     * @throws
     */
    void mountSaveSingleTemplate(ServiceConfig serviceConfig);

    /**
     * 取消挂载
     * @Title: mountCancel
     * @Description: 取消挂载
     * @param serviceId 服务id
     * @param configTemplateId 模板id
     * @throws
     */
    void mountCancel(String serviceId, String configTemplateId);

    /**
     * 取消挂载
     * @Title: mountCancelById
     * @Description: 取消挂载
     * @param serviceConfigId 主键id
     * @throws
     */
    void mountCancelById(String serviceConfigId);

    /**
     * 清空服务挂载
     * @Title: mountClear
     * @Description: 清空服务挂载
     * @param serviceId 服务id
     * @throws
     */
    void mountClear(String serviceId);

    /**
     * 根据配置文件模板名称和租户名称获取配置文件模板详情
     * @Title: getConfigTemplateByNameAndTenantName
     * @Description: 根据配置文件模板名称和租户名称获取配置文件模板详情
     * @param configTemplateName 配置文件模板名称
     * @param tenantname 租户名称
     * @return ConfigTemplate 
     * @throws
     */
    ConfigTemplate getConfigTemplateByNameAndTenantName(String configTemplateName, String tenantname);

    /**
     * 获取配置文件模板列表
     * @Title: getConfigTemplateList
     * @Description: 获取配置文件模板列表
     * @param tenantName 租户名称
     * @param templateName 租户名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<ConfigTemplate> 
     * @throws
     */
    Page<ConfigTemplate> getConfigTemplateList(String tenantName, String templateName, String projectId,
            PageRequest pageable);

}
