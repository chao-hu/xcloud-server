package com.xxx.xcloud.module.ceph.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ceph.entity.CephFile;

/**
 * 
 * <p>
 * Description: 文件存储持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Repository
public interface CephFileRepository extends CrudRepository<CephFile, String> {

    
    /**根据文件名称和租户名称查找文件存储
     * @Title: findByNameAndTenantName
     * @Description: 根据文件名称和租户名称查找文件存储
     * @param name 文件名称
     * @param tenantName 租户名称
     * @return CephFile 
     * @throws
     */
    CephFile findByNameAndTenantName(String name, String tenantName);

    
    /**根据文件名称和租户名称分页查找文件存储
     * @Title: findByTenantNameAndName
     * @Description: 根据文件名称和租户名称分页查找文件存储
     * @param tenantName 租户名称
     * @param name 文件名称
     * @param pageable
     * @return Page<CephFile> 
     * @throws
     */
    @Query("select s from CephFile s where s.name like ?2% and s.tenantName =?1")
    Page<CephFile> findByTenantNameAndName(String tenantName, String name, Pageable pageable);

    
    /**根据文件名称、租户名称和项目信息分页查找文件存储
     * @Title: findByTenantNameAndNameAndProjectId
     * @Description: 根据文件名称、租户名称和项目信息分页查找文件存储
     * @param tenantName 租户名称
     * @param name 文件名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<CephFile> 
     * @throws
     */
    @Query("select s from CephFile s where s.name like ?2% and s.tenantName =?1 and s.projectId=?3")
    Page<CephFile> findByTenantNameAndNameAndProjectId(String tenantName, String name, String projectId,
            Pageable pageable);

    /**根据租户名称分页查找文件存储
     * @Title: findByTenantName
     * @Description:  根据租户名称分页查找文件存储
     * @param tenantName 租户名称
     * @param pageable
     * @return Page<CephFile> 
     * @throws
     */
    Page<CephFile> findByTenantName(String tenantName, Pageable pageable);

    
    /**根据租户名称和项目信息分页查找文件存储
     * @Title: findByTenantNameAndProjectId
     * @Description: 根据租户名称和项目信息分页查找文件存储
     * @param tenantName 租户名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<CephFile> 
     * @throws
     */
    Page<CephFile> findByTenantNameAndProjectId(String tenantName, String projectId, Pageable pageable);

    
    /**根据租户名称查找文件存储
     * @Title: findByTenantName
     * @Description: 根据租户名称查找文件存储
     * @param tenantName 租户名称
     * @return List<CephFile> 
     * @throws
     */
    List<CephFile> findByTenantName(String tenantName);

    
    /**根据租户名称删除文件存储
     * @Title: deleteByTenantName
     * @Description: 根据租户名称删除文件存储
     * @param tenantName void 
     * @throws
     */
    void deleteByTenantName(String tenantName);

}
