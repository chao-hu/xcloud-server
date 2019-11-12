package com.xxx.xcloud.module.env.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.xxx.xcloud.module.env.entity.EnvTemplate;

/**
 * 
 * <p>
 * Description: 环境变量模板操作接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
public interface EnvService {

    /**
     * 
     * <p>
     * Description: 新增环境变量模板
     * </p>
     *
     * @param envTemplate
     * @return
     * @throws EnvException
     */
    EnvTemplate add(EnvTemplate envTemplate);

    /**
     * 
     * <p>Description: </p>
     *
     * @param id
     */
    void delete(String id);

    
    /**
     * 更新环境变量模板
     * @Title: update
     * @Description: 更新变量数据
     * @param id
     * @param envData 
     * @throws
     */
    void update(String id, Map<String, Object> envData);

    
    /**
     * 通过id获取环境变量模板
     * @Title: get
     * @Description: 通过id获取环境变量模板
     * @param id 主键id
     * @return EnvTemplate 
     * @throws
     */
    EnvTemplate get(String id);

    /**
     * 
     * <p>
     * Description: 获取环境变量模板列表
     * </p>
     *
     * @param tenantName
     *            租户名称
     * @param templateName
     *            模板名称(模糊查询)
     * @param projectId
     *            项目id
     * @return List<EnvTemplate>
     */
    List<EnvTemplate> list(String tenantName, String templateName, String projectId);

    /**
     * Description: 根据 环境变量模板名称和租户名称 获取环境变量模板详情
     *
     * @param envTemplateName
     *            环境变量模板名称
     * @param tenantname
     *            租户名称
     * @return EnvTemplate
     */
    EnvTemplate getEnvTemplateByNameAndTenantName(String envTemplateName, String tenantname);

    /**
     * 
     * <p>
     * Description: 根据 环境变量模板名称、租户名称和项目id 获取环境变量模板列表
     * </p>
     *
     * @param tenantName
     *            租户名称
     * @param templateName
     *            模板名称(模糊查询)
     * @param projectId
     *            项目id
     * @param pageable
     * @return
     */
    Page<EnvTemplate> getEnvTemplateList(String tenantName, String templateName, String projectId,
            PageRequest pageable);

}
