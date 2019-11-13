package com.xxx.xcloud.module.ceph.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;

/**
 * 
 * <p>
 * Description: 服务和文件存储关联持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Repository
public interface ServiceAndCephFileRepository extends CrudRepository<ServiceAndCephFile, String> {

    /**
     * 根据服务id查找服务和文件存储关联表
     * @Title: findByServiceId
     * @Description: 根据服务id查找服务和文件存储关联表
     * @param serviceId 服务id
     * @return List<ServiceAndCephFile> 
     * @throws
     */
    List<ServiceAndCephFile> findByServiceId(String serviceId);

    /**
     * 根据文件存储id查找服务和文件存储关联表
     * @Title: findByCephFileId
     * @Description: 根据文件存储id查找服务和文件存储关联表
     * @param cephFileId 文件存储id
     * @return List<ServiceAndCephFile> 
     * @throws
     */
    List<ServiceAndCephFile> findByCephFileId(String cephFileId);

    /**
     * 根据文件存储id和服务id查找服务和文件存储关联表
     * @Title: findByCephFileIdAndServiceId
     * @Description: 根据文件存储id和服务id查找服务和文件存储关联表
     * @param cephFileId 文件存储id
     * @param serviceId 服务id
     * @return ServiceAndCephFile 
     * @throws
     */
    ServiceAndCephFile findByCephFileIdAndServiceId(String cephFileId, String serviceId);

    /**
     * 根据文件存储id和服务id删除服务和文件存储关联关系
     * @Title: deleteByCephFileIdAndServiceId
     * @Description: 根据文件存储id和服务id删除服务和文件存储关联关系
     * @param cephFileId 文件存储id 服务id
     * @param serviceId void 
     * @throws
     */
    @Query("delete from ServiceAndCephFile s where s.cephFileId = ?1 and s.serviceId = ?2")
    @Modifying
    void deleteByCephFileIdAndServiceId(String cephFileId, String serviceId);

    /**
     * 根据服务id删除服务和文件存储关联关系
     * @Title: deleteAllByServiceId
     * @Description: 根据服务id删除服务和文件存储关联关系
     * @param serviceId 服务id
     * @throws
     */
    @Query("delete from ServiceAndCephFile s where s.serviceId = ?1 ")
    @Modifying
    void deleteAllByServiceId(String serviceId);

    /**
     * 根据文件存储id删除服务和文件存储关联关系
     * @Title: deleteAllByCephFileId
     * @Description: 根据文件存储id删除服务和文件存储关联关系
     * @param cephFileId 文件存储id 
     * @throws
     */
    @Query("delete from ServiceAndCephFile s where s.cephFileId = ?1 ")
    @Modifying
    void deleteAllByCephFileId(String cephFileId);

}
