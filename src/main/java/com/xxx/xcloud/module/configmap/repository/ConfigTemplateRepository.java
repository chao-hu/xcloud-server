package com.xxx.xcloud.module.configmap.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.configmap.entity.ConfigTemplate;

/**
 * 
 * <p>
 * Description: 配置文件模板持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Repository
public interface ConfigTemplateRepository extends JpaRepository<ConfigTemplate, String> {

    /**
     * 根据租户名称和模板名称查找配置文件模板
     * @Title: findByTenantNameAndTemplateName
     * @Description: 根据租户名称和模板名称查找配置文件模板
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @return ConfigTemplate 
     * @throws
     */
    ConfigTemplate findByTenantNameAndTemplateName(String tenantName, String templateName);

    /**
     * 根据租户名称和模板名称模糊查找配置文件模板
     * @Title: findByTenantNameAndTemplateNameLike
     * @Description: 根据租户名称和模板名称模糊查找配置文件模板
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @return List<ConfigTemplate> 
     * @throws
     */
    @Query("select s from ConfigTemplate s where s.templateName like ?2 and s.tenantName = ?1")
    List<ConfigTemplate> findByTenantNameAndTemplateNameLike(String tenantName, String templateName);

    /**
     * 根据租户名称、模板名称和项目信息模糊查找配置文件模板
     * @Title: findByTenantNameAndTemplateNameLikeAndProjectId
     * @Description: 根据租户名称、模板名称和项目信息模糊查找配置文件模板
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @param projectId 项目信息
     * @return List<ConfigTemplate> 
     * @throws
     */
    @Query("select s from ConfigTemplate s where s.templateName like ?2 and s.tenantName = ?1 and s.projectId = ?3")
    List<ConfigTemplate> findByTenantNameAndTemplateNameLikeAndProjectId(String tenantName, String templateName,
            String projectId);

    /**
     * TODO 根据租户名称、模板名称和项目信息模糊查找配置文件模板
     * @Title: findByTemplateNameLikeAndTenantNameOrderByCreateTimeDesc
     * @Description: 根据租户名称、模板名称和项目信息模糊查找配置文件模板（按时间降序排列）
     * @param templateName 模板名称
     * @param tenantName 租户名称
     * @param pageable
     * @return Page<ConfigTemplate> 
     * @throws
     */
    Page<ConfigTemplate> findByTemplateNameLikeAndTenantNameOrderByCreateTimeDesc(String templateName,
            String tenantName, Pageable pageable);

    /**
     * 根据租户名称、模板名称和项目信息模糊查找配置文件模板
     * @Title: findByTemplateNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc
     * @Description: 根据租户名称、模板名称和项目信息模糊查找配置文件模板（按时间降序排列）
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<ConfigTemplate> 
     * @throws
     */
    Page<ConfigTemplate> findByTemplateNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc(String templateName,
            String tenantName, String projectId, Pageable pageable);

    /**
     * 根据租户名称查找配置文件模板
     * @Title: findByTenantNameOrderByCreateTimeDesc
     * @Description: 根据租户名称查找配置文件模板（按时间降序排列）
     * @param tenantName 租户名称
     * @param pageable
     * @return Page<ConfigTemplate> 
     * @throws
     */
    Page<ConfigTemplate> findByTenantNameOrderByCreateTimeDesc(String tenantName, Pageable pageable);

    /**
     * 根据租户名称和项目信息查找配置文件模板
     * @Title: findByTenantNameAndProjectIdOrderByCreateTimeDesc
     * @Description: 根据租户名称和项目信息查找配置文件模板（按时间降序排列）
     * @param tenantName 租户名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<ConfigTemplate> 
     * @throws
     */
    Page<ConfigTemplate> findByTenantNameAndProjectIdOrderByCreateTimeDesc(String tenantName, String projectId,
            Pageable pageable);

}
