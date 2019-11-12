package com.xxx.xcloud.module.configmap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.configmap.entity.ServiceConfig;

/**
 * 
 * <p>
 * Description: 服务与配置文件模板关联表持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Repository
public interface ServiceConfigRepository extends JpaRepository<ServiceConfig, String> {

    /**
     * 根据配置文件模板id查找服务和配置文件关联关系
     * @Title: findByConfigTemplateId
     * @Description: 根据配置文件模板id查找服务和配置文件关联关系
     * @param configTemplateId 配置文件模板id
     * @return List<ServiceConfig> 
     * @throws
     */
    List<ServiceConfig> findByConfigTemplateId(String configTemplateId);

    /**
     * 根据服务id查找服务和配置文件关联关系
     * @Title: findByServiceId
     * @Description: 根据服务id查找服务和配置文件关联关系
     * @param serviceId 服务id
     * @return List<ServiceConfig> 
     * @throws
     */
    List<ServiceConfig> findByServiceId(String serviceId);

    /**
     * 根据服务id和配置文件模板id删除服务和配置文件关联关系
     * @Title: deleteByServiceIdAndConfigTemplateId
     * @Description: 根据服务id和配置文件模板id删除服务和配置文件关联关系
     * @param serviceId 服务id
     * @param configTemplateId 配置文件模板id 
     * @throws
     */
    @Query("delete from ServiceConfig s where s.serviceId = ?1 and s.configTemplateId = ?2")
    @Modifying
    void deleteByServiceIdAndConfigTemplateId(String serviceId, String configTemplateId);

    /**
     * 根据服务id删除服务和配置文件关联关系
     * @Title: deleteByServiceId
     * @Description: 根据服务id删除服务和配置文件关联关系
     * @param serviceId 服务id
     * @throws
     */
    @Query("delete from ServiceConfig s where s.serviceId = ?1")
    @Modifying
    void deleteByServiceId(String serviceId);

    /**
     * 根据配置文件模板id删除服务和配置文件关联关系
     * @Title: deleteByConfigTemplateId
     * @Description: 根据配置文件模板id删除服务和配置文件关联关系
     * @param configTemplateId 配置文件模板id
     * @throws
     */
    @Query("delete from ServiceConfig s where s.configTemplateId = ?1")
    @Modifying
    void deleteByConfigTemplateId(String configTemplateId);
}
