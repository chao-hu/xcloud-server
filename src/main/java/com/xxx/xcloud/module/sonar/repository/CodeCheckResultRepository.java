package com.xxx.xcloud.module.sonar.repository;

import com.xxx.xcloud.module.sonar.entity.CodeCheckResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 
 * @author mengaijun
 * @date: 2019年5月20日 上午11:13:47
 */
@Repository
public interface CodeCheckResultRepository extends JpaRepository<CodeCheckResult, String> {

    /**
     * 通过任务ID删除任务检查结果信息
     * 
     * @param sonarTaskId
     *            void
     * @date: 2019年5月20日 上午11:14:32
     */
    void deleteBySonarTaskId(String sonarTaskId);

    /**
     * 根据任务ID查询任务检查结果
     * 
     * @param id
     * @param pageable
     * @return Page<CodeCheckResult>
     * @date: 2019年5月20日 上午11:14:36
     */
    @Query(value = "SELECT c FROM CodeCheckResult c WHERE sonarTaskId = ?1 order by checkTime desc")
    Page<CodeCheckResult> getCodeCheckHistoryRecord(String id, Pageable pageable);

    /**
     *  未使用
     * @return List<CodeCheckResult>
     * @date: 2019年8月21日 下午3:52:25
     */
    List<CodeCheckResult> findByCodeBaseNameIsNull();
}
