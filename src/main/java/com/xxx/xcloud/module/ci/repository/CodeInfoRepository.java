package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.CodeInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author mengaijun
 *
 * @date: 2018年12月7日 下午2:15:27
 */
@Repository
public interface CodeInfoRepository extends JpaRepository<CodeInfo, String> {
    /**
     * 根据ID查询
     * 
     * @param id
     * @return CodeInfo
     * @date: 2019年1月3日 下午6:12:30
     */
    @Query("select c from CodeInfo c where c.id=?1")
    CodeInfo getById(String id);

    /**
     * 根据认证ID删除
     * 
     * @param ciCodeCredentialsId
     *            void
     * @date: 2019年5月20日 下午5:19:09
     */
    void deleteByCiCodeCredentialsId(String ciCodeCredentialsId);

    /**
     * 根据认证ID查询
     * 
     * @param ciCodeCredentialsId
     * @param pageable
     * @return Page<CodeInfo>
     * @date: 2019年7月3日 上午10:29:25
     */
    Page<CodeInfo> findByCiCodeCredentialsId(String ciCodeCredentialsId, Pageable pageable);
}
