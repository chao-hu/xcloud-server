package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.CiRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 
 * @author mengaijun
 *
 * @date: 2018年12月7日 下午2:19:21
 */
@Repository
public interface CiRecordRepository extends JpaRepository<CiRecord, String> {
    /**
     * 查询
     * 
     * @param ciId
     * @return List<CiRecord>
     * @date: 2019年1月3日 下午6:10:22
     */
    @Query("select r from CiRecord r where r.ciId=?1 order by r.constructionTime desc")
    List<CiRecord> getCiRecordsByCiId(String ciId);

    /**
     * 查询分页信息
     * 
     * @param ciId
     * @param pageable
     * @return Page<CiRecord>
     * @date: 2019年1月18日 下午5:20:22
     */
    Page<CiRecord> findByCiIdOrderByConstructionTimeDesc(String ciId, Pageable pageable);

    /**
     * 根据ciId计算构建次数
     * 
     * @param ciId
     * @return Long
     * @date: 2019年9月18日 下午2:30:01
     */
    Long countByCiId(String ciId);

    /**
     * 删除
     * 
     * @param ciId
     * @date: 2019年1月3日 下午6:10:29
     */
    @Query("delete from CiRecord r where r.ciId=?1")
    @Modifying
    void deleteByCiId(String ciId);

    /**
     * 根据ID获取
     * 
     * @param id
     * @return CiRecord
     * @date: 2019年1月10日 下午7:47:43
     */
    @Query("select r from CiRecord r where r.id=?1")
    CiRecord getById(String id);
}
