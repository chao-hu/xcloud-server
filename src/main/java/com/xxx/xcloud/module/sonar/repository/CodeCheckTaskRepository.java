package com.xxx.xcloud.module.sonar.repository;

import com.xxx.xcloud.module.sonar.entity.CodeCheckTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mengaijun
 * @date: 2018年12月7日 下午2:19:49
 */
@Repository
public interface CodeCheckTaskRepository extends JpaRepository<CodeCheckTask, String> {

    /**
     * 查询任务
     *
     * @param tenantName
     * @param taskName
     * @param pageOffset
     * @param pageSize
     * @return
     */
    @Query(value =
            "SELECT c.id, c.task_name taskName, c.task_desc taskDesc, i.code_control_type codeControlType, i.code_repos_name codeReposName, c.created_by createdBy, DATE_FORMAT(c.create_time, '%Y-%m-%d %k:%i:%s') createTime, c.cron cron, c.cron_description cronDescription, c.status status, DATE_FORMAT(c.last_check_time, '%Y-%m-%d %k:%i:%s') lastCheckTime, c.check_duration_time checkDurationTime, i.code_branch codeBranch, i.code_url codeUrl, credential.user_name userName "
                    + "FROM bdos_code_check_task c  " + "left join bdos_code_info i on c.code_info_id = i.id "
                    + "left join bdos_ci_code_credentials credential  on credential.id = i.ci_code_credentials_id "
                    + "where c.tenant_name = ?1 AND c.task_name LIKE %?2% order by c.create_time desc "
                    + "limit ?3, ?4", nativeQuery = true)
    List<Map<String, String>> getSonarTaskInfo(String tenantName, String taskName, int pageOffset, int pageSize);

    /**
     * 最新代码检查结果
     *
     * @param time
     * @param codeBaseNameSet
     * @return
     */
    @Query(value =
            "select r.question_numbers questionNumbers, t.task_name taskName, t.id taskId, r.code_base_name codeBaseName, "
                    + "r.language lang, r.code_line_numbers codeLineNumbers, r.health_degree healthDegree, r.info_question_numbers infoQuestionNumbers, minor_question_numbers minorQuestionNumbers, "
                    + "major_question_numbers majorQuestionNumbers, critical_question_numbers criticalQuestionNumbers, "
                    + "blocker_question_numbers blockerQuestionNumbers, r.check_time checkTime " + "from "
                    + "(select sonar_task_id, max(check_time) latest_check_time " + "from bdos_code_check_result "
                    + " where check_time > ?1 and code_base_name in ?2 group by sonar_task_id) latestR "
                    + " inner join "
                    + "bdos_code_check_result r on r.sonar_task_id = latestR.sonar_task_id and  r.check_time = latestR.latest_check_time "
                    + "inner join " + "bdos_code_check_task t  on t.id = r.sonar_task_id ", nativeQuery = true)
    List<Map<String, Object>> getNewestSonarCheckResult(Date time, Collection<String> codeBaseNameSet);

    /**
     * 获取任务数量
     *
     * @param tenantName
     * @param taskName
     * @return int
     * @date: 2019年7月12日 下午4:04:39
     */
    @Query(value = "select count(*) " + "FROM bdos_code_check_task c  "
            + "where c.tenant_name = ?1 AND c.task_name LIKE %?2% ", nativeQuery = true)
    int getSonarTaskCount(String tenantName, String taskName);

    /**
     * get
     *
     * @param tenantName
     * @param taskName
     * @return CodeCheckTask
     * @date: 2019年1月3日 下午6:57:54
     */
    @Query("select c from CodeCheckTask c where c.tenantName=?1 and c.taskName=?2")
    CodeCheckTask getByNameAndTenant(String tenantName, String taskName);

    /**
     * get
     *
     * @param id
     * @return CodeCheckTask
     * @date: 2019年1月3日 下午6:58:04
     */
    @Query("select c from CodeCheckTask c where c.id=?1")
    CodeCheckTask getById(String id);

    /**
     * get
     *
     * @param tenantName
     * @param pageable
     * @return Page<CodeCheckTask>
     * @date: 2019年1月3日 下午6:58:21
     */
    @Query("select c from CodeCheckTask c where c.tenantName=?1 order by c.createTime desc")
    Page<CodeCheckTask> getCodeCheckTasks(String tenantName, Pageable pageable);

    /**
     * get
     *
     * @param tenantName
     * @param taskName
     * @param pageable
     * @return Page<CodeCheckTask>
     * @date: 2019年1月3日 下午6:58:32
     */
    @Query("select c from CodeCheckTask c where c.tenantName=?1 and c.taskName like ?2 order by c.createTime desc")
    Page<CodeCheckTask> getCodeCheckTasksByNameLike(String tenantName, String taskName, Pageable pageable);

    /**
     * get
     *
     * @return List<CodeCheckTask>
     * @date: 2019年1月3日 下午6:58:42
     */
    @Query("select c from CodeCheckTask c where c.cron is not null")
    List<CodeCheckTask> getCodeCheckTasksCronIsNotNull();

    /**
     * 删除租户下的任务
     *
     * @param tenantName void
     * @date: 2019年5月20日 上午11:16:10
     */
    public void deleteByTenantName(String tenantName);

    /**
     * 根据状态查询任务
     *
     * @param status
     * @return List<CodeCheckTask>
     * @date: 2019年6月19日 上午9:52:35
     */
    List<CodeCheckTask> findByStatus(byte status);
}
