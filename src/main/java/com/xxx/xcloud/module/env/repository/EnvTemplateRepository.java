package com.xxx.xcloud.module.env.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.env.entity.EnvTemplate;

/**
 * 
 * <p>
 * Description: 环境变量模板持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Repository
public interface EnvTemplateRepository extends JpaRepository<EnvTemplate, String> {

    /**
     * 根据租户名称和模板名称查找环境变量模板
     * @Title: findByTenantNameAndTemplateName
     * @Description: 根据租户名称和模板名称查找环境变量模板
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @return EnvTemplate 
     * @throws
     */
    EnvTemplate findByTenantNameAndTemplateName(String tenantName, String templateName);

    /**
     * 根据租户名称查找环境变量模板
     * @Title: findByTenantName
     * @Description: 根据租户名称查找环境变量模板
     * @param tenantName 租户名称
     * @return List<EnvTemplate> 
     * @throws
     */
    List<EnvTemplate> findByTenantName(String tenantName);

    /**
     * 根据租户名称和模板名称模糊查找环境变量模板
     * @Title: findByTenantNameAndTemplateNameLike
     * @Description: 根据租户名称和模板名称模糊查找环境变量模板
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @return List<EnvTemplate> 
     * @throws
     */
    @Query("select s from EnvTemplate s where s.templateName like ?2% and s.tenantName = ?1")
    List<EnvTemplate> findByTenantNameAndTemplateNameLike(String tenantName, String templateName);

    /**
     * 根据租户名称、模板名称和项目信息模糊查找环境变量模板
     * @Title: findByTenantNameAndTemplateNameLikeAndProjectId
     * @Description: 根据租户名称、模板名称和项目信息模糊查找环境变量模板
     * @param tenantName 租户名称
     * @param templateName 模板名称
     * @param projectId 项目信息
     * @return List<EnvTemplate> 
     * @throws
     */
    @Query("select s from EnvTemplate s where s.templateName like ?2% and s.tenantName = ?1 and s.projectId = ?3")
    List<EnvTemplate> findByTenantNameAndTemplateNameLikeAndProjectId(String tenantName, String templateName,
            String projectId);

    /**
     * 根据租户名称和模板名称模糊查找环境变量模板
     * @Title: findByTemplateNameLikeAndTenantNameOrderByCreateTimeDesc
     * @Description: 根据租户名称和模板名称模糊查找环境变量模板(按时间降序排列)
     * @param templateName 模板名称
     * @param tenantName 模板名称
     * @param pageable
     * @return Page<EnvTemplate> 
     * @throws
     */
    Page<EnvTemplate> findByTemplateNameLikeAndTenantNameOrderByCreateTimeDesc(String templateName, String tenantName,
            Pageable pageable);

    /**
     * 根据租户名称、模板名称和项目信息模糊查找环境变量模板
     * @Title: findByTemplateNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc
     * @Description: 根据租户名称、模板名称和项目信息模糊查找环境变量模板(按时间降序排列)
     * @param templateName 模板名称
     * @param tenantName 租户名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<EnvTemplate> 
     * @throws
     */
    Page<EnvTemplate> findByTemplateNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc(String templateName,
            String tenantName, String projectId, Pageable pageable);

    /**
     * 根据租户名称查找环境变量模板
     * @Title: findByTenantNameOrderByCreateTimeDesc
     * @Description: 根据租户名称查找环境变量模板(按时间降序排列)
     * @param tenantName 租户名称
     * @param pageable
     * @return Page<EnvTemplate> 
     * @throws
     */
    Page<EnvTemplate> findByTenantNameOrderByCreateTimeDesc(String tenantName, Pageable pageable);

    /**
     * 根据租户名称和项目id模糊查找环境变量模板
     * @Title: findByTenantNameAndProjectIdOrderByCreateTimeDesc
     * @Description: 根据租户名称和项目id模糊查找环境变量模板
     * @param tenantName 租户名称
     * @param projectId 项目id
     * @param pageable
     * @return Page<EnvTemplate> 
     * @throws
     */
    Page<EnvTemplate> findByTenantNameAndProjectIdOrderByCreateTimeDesc(String tenantName, String projectId,
            Pageable pageable);
}
