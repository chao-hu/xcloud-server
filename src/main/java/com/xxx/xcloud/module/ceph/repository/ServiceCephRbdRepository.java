package com.xxx.xcloud.module.ceph.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;

/**
 * 
 * <p>
 * Description: 服务和块存储关联持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Repository
public interface ServiceCephRbdRepository extends CrudRepository<ServiceCephRbd, String> {

    /**
     * 根据服务id查找服务和块存储关联关系
     * @Title: findByCephRbdId
     * @Description: 根据服务id查找服务和块存储关联关系
     * @param cephRbdId 服务id
     * @return ServiceCephRbd 
     * @throws
     */
    ServiceCephRbd findByCephRbdId(String cephRbdId);

    /**
     * 根据服务id查找服务和块存储关联关系列表
     * @Title: findByServiceId
     * @Description: 根据服务id查找服务和块存储关联关系列表
     * @param serviceId 服务id
     * @return List<ServiceCephRbd> 
     * @throws
     */
    List<ServiceCephRbd> findByServiceId(String serviceId);

    /**
     * 根据服务id和块存储id查找服务和块存储关联关系
     * @Title: findByServiceIdAndCephRbdId
     * @Description: 根据服务id和块存储id查找服务和块存储关联关系
     * @param serviceId 服务id
     * @param cephRbdId 块存储id
     * @return ServiceCephRbd 
     * @throws
     */
    ServiceCephRbd findByServiceIdAndCephRbdId(String serviceId, String cephRbdId);

    /**
     * 根据块存储id删除服务和块存储关联关系
     * @Title: deleteByCephRbdId
     * @Description: 根据块存储id删除服务和块存储关联关系
     * @param cephRbdId 块存储id 
     * @throws
     */
    @Query("delete from ServiceCephRbd s where s.cephRbdId = ?1 ")
    @Modifying
    void deleteByCephRbdId(String cephRbdId);

    /**
     * 根据服务id删除服务和块存储关联关系
     * @Title: deleteByServiceId
     * @Description: 根据服务id删除服务和块存储关联关系
     * @param serviceId 服务id 
     * @throws
     */
    @Query("delete from ServiceCephRbd s where s.serviceId = ?1")
    @Modifying
    void deleteByServiceId(String serviceId);

    /**
     * 根据id、服务id和块存储id查找服务和块存储关联关系
     * @Title: findByIdAndServiceIdAndCephRbdId
     * @Description: 根据id、服务id和块存储id查找服务和块存储关联关系
     * @param id id
     * @param serviceId 服务id
     * @param cephRbdId 块存储id
     * @return ServiceCephRbd 
     * @throws
     */
    ServiceCephRbd findByIdAndServiceIdAndCephRbdId(String id, String serviceId, String cephRbdId);
}
