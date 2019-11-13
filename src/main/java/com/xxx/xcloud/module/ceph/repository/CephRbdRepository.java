package com.xxx.xcloud.module.ceph.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ceph.entity.CephRbd;

/**
 * 
 * <p>
 * Description: 块存储持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Repository
public interface CephRbdRepository extends CrudRepository<CephRbd, String> {
    
    /**
     * 根据租户名称查找可用块存储
     * @Title: findAvaliable
     * @Description: 根据租户名称查找可用块存储
     * @param tenantName 租户名称
     * @return List<CephRbd> 
     * @throws
     */
    @Query("select c from CephRbd c where c.tenantName = ?1 and c.id not in ( select s.cephRbdId from ServiceCephRbd s )")
    List<CephRbd> findAvaliable(String tenantName);

    /**
     * 根据项目信息查找可用块存储
     * @Title: findAvaliableInProject
     * @Description: 根据项目信息查找可用块存储
     * @param projectId 项目信息
     * @return List<CephRbd> 
     * @throws
     */
    @Query("select c from CephRbd c where c.projectId = ?1 and c.id not in ( select s.cephRbdId from ServiceCephRbd s )")
    List<CephRbd> findAvaliableInProject(String projectId);
    
    /**
     * 根据租户名称查找块存储
     * @Title: findByTenantName
     * @Description: 根据租户名称查找块存储
     * @param tenantName 租户名称
     * @return List<CephRbd> 
     * @throws
     */
    List<CephRbd> findByTenantName(String tenantName);

    /**
     * 根据租户名称和块名称查找块存储
     * @Title: findByTenantNameAndName
     * @Description: 根据租户名称和块名称查找块存储
     * @param tenantName 租户名称
     * @param name 块名称
     * @return CephRbd 
     * @throws
     */
    CephRbd findByTenantNameAndName(String tenantName, String name);
    
    /**
     * 根据租户名称和块名称模糊查找块存储
     * @Title: findByNameAndTenantName
     * @Description: 根据租户名称和块名称模糊查找块存储
     * @param name 块名称
     * @param tenantName 租户名称
     * @return List<CephRbd> 
     * @throws
     */
    @Query("select s from CephRbd s where s.name like ?1% and s.tenantName = ?2")
    List<CephRbd> findByNameAndTenantName(String name, String tenantName);

    /**
     * 根据租户名称、块名称和项目信息分页查找块存储
     * @Title: findByNameAndTenantNameAndProjectId
     * @Description: 根据租户名称、块名称和项目信息分页查找块存储
     * @param name 块名称
     * @param tenantName 租户名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<CephRbd> 
     * @throws
     */
    @Query("select s from CephRbd s where s.name like ?1% and s.tenantName = ?2 and s.projectId = ?3")
    Page<CephRbd> findByNameAndTenantNameAndProjectId(String name, String tenantName, String projectId,
            Pageable pageable);

    /**
     * 根据租户名称和块名称分页查找块存储
     * @Title: findByNameAndTenantName
     * @Description: 根据租户名称和块名称分页查找块存储
     * @param name 块名称
     * @param tenantName 租户名称
     * @param pageable
     * @return Page<CephRbd> 
     * @throws
     */
    @Query("select s from CephRbd s where s.name like ?1% and s.tenantName = ?2")
    Page<CephRbd> findByNameAndTenantName(String name, String tenantName, Pageable pageable);

    /**
     * 根据块名称分页查找块存储
     * @Title: findByName
     * @Description: 根据块名称分页查找块存储
     * @param name 块名称
     * @return CephRbd 
     * @throws
     */
    CephRbd findByName(String name);

    /**
     * 根据租户名称删除块存储
     * @Title: deleteByTenantName
     * @Description: 根据租户名称删除块存储
     * @param tenantName 租户名称
     * @throws
     */
    void deleteByTenantName(String tenantName);

}
