package com.xxx.xcloud.module.ceph.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ceph.entity.SnapStrategy;

/**
 * 
 * <p>
 * Description: 快照策略持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Repository
public interface SnapStrategyRepository extends CrudRepository<SnapStrategy, String> {

    /**
     * 根据块存储id查找快照策略
     * @Title: findByCephRbdId
     * @Description: 根据块存储id查找快照策略
     * @param cephRbdId 块存储id
     * @return SnapStrategy 
     * @throws
     */
    SnapStrategy findByCephRbdId(String cephRbdId);

    /**
     * 根据块存储id删除快照策略
     * @Title: deleteByCephRbdId
     * @Description: 根据块存储id删除快照策略
     * @param cephRbdId 块存储id 
     * @throws
     */
    @Query("delete from SnapStrategy s where s.cephRbdId = ?1 ")
    @Modifying
    void deleteByCephRbdId(String cephRbdId);
}
