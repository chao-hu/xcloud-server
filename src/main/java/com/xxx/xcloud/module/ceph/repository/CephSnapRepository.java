package com.xxx.xcloud.module.ceph.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ceph.entity.CephSnap;

/**
 * 
 * <p>
 * Description: 快照持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Repository
public interface CephSnapRepository extends CrudRepository<CephSnap, String> {

    /**
     * 根据块存储id查找快照列表
     * @Title: findByCephRbdId
     * @Description: 根据块存储id查找快照列表
     * @param cephRbdId 块存储id
     * @return List<CephSnap> 
     * @throws
     */
    List<CephSnap> findByCephRbdId(String cephRbdId);

    /**
     *  根据块存储id和快照名称查找快照列表
     * @Title: findByCephRbdIdAndName
     * @Description: 根据块存储id和快照名称查找快照列表
     * @param cephRbdId 块存储id
     * @param name 快照名称
     * @return CephSnap 
     * @throws
     */
    CephSnap findByCephRbdIdAndName(String cephRbdId, String name);

    /**
     * 根绝块存储id删除快照
     * @Title: deleteByCephRbdId
     * @Description: TODO详细描述
     * @param cephRbdId void 
     * @throws
     */
    @Query("delete from CephSnap s where s.cephRbdId = ?1 ")
    @Modifying
    void deleteByCephRbdId(String cephRbdId);

}
