package com.xxx.xcloud.module.sonar.service;

import com.xxx.xcloud.client.sonar.model.Issues;
import com.xxx.xcloud.client.sonar.model.SonarQualityProfile;
import com.xxx.xcloud.module.ci.entity.CodeInfo;
import com.xxx.xcloud.module.sonar.entity.CodeCheckInfo;
import com.xxx.xcloud.module.sonar.entity.CodeCheckResult;
import com.xxx.xcloud.module.sonar.entity.CodeCheckTask;
import com.xxx.xcloud.module.sonar.entity.QualityProfile;
import com.xxx.xcloud.module.sonar.model.SonarCheckResultSummary;
import com.xxx.xcloud.client.sonar.model.Rules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * sonar接口
 *
 * @author mengaijun
 * @date: 2018年12月24日 下午2:12:04
 */
public interface SonarService {

    /**
     * 根据现有规则集复制新的规则集
     *
     * @param tenantName
     *            租户
     * @param fromKey
     *            复制的规则集key
     * @param toName
     *            复制的新的规则集的名称
     * @param language
     *            规则集语言
     * @date: 2019年3月7日 上午11:30:52
     */
    void copyQualityProfile(String tenantName, String fromKey, String toName, String language);

    /**
     * 修改
     *
     * @param tenantName
     * @param key
     * @param newName
     * @param language
     * @return boolean
     * @date: 2019年3月7日 上午11:32:20
     */
    boolean modifyQualityProfileName(String tenantName, String key, String newName, String language);

    /**
     * 根据ID删除规则集
     *
     * @param tenantName
     *            租户
     * @param key
     *            规则集key
     * @return boolean
     * @date: 2018年12月24日 下午5:37:17
     */
    boolean deleteQualityProfile(String tenantName, String key);

    /**
     * 获取规则集下的所有规则
     *
     * @param qualityProfileId
     *            规则集ID
     * @param active
     *            ture:查询激活的 false:查询没有激活的
     * @param page
     *            第page页(从1开始)
     * @param size
     *            每页size条
     * @return Rules
     * @date: 2018年12月24日 下午7:59:18
     */
    Rules getQualityProfileRules(String qualityProfileId, boolean active, int page, int size);

    /**
     * 根据条件获取规则
     *
     * @param tenantName
     * @param ruleSetName
     * @param ruleName
     * @param ruleType
     * @param ruleLevel
     * @param ruleStatus
     * @param language
     * @param page
     * @param size
     * @return Rules
     * @date: 2019年5月6日 下午3:08:27
     */
    Rules getRules(String tenantName, String ruleSetName, String ruleName, String ruleType, String ruleLevel,
            String ruleStatus, String language, Integer page, Integer size);

    /**
     * 将规则集规则值为激活或未激活
     *
     * @param rulesetKey
     *            规则集key
     * @param ruleKey
     *            规则key
     * @param active
     *            是否激活
     * @date: 2018年12月25日 上午9:45:17
     */
    void modifyRuleActivation(String rulesetKey, String ruleKey, Boolean active);

    /**
     * 获取sonar支持的语言信息
     *
     * @return Set<String>
     * @date: 2018年12月24日 下午5:43:26
     */
    Set<String> getAllSonarLanguages();

    /**
     * 获取租户下所有规则集
     *
     * @param tenantName
     *            租户名
     * @return List<QualityProfileLocal>
     * @date: 2018年12月25日 上午10:57:07
     */
    List<QualityProfile> getAllQualityProfiles(String tenantName);

    /**
     * 获取租户的规则集
     *
     * @param tenantName
     * @param ruleSetName
     * @param language
     * @param ruleSetSource
     *            1:系统 2租户
     * @return List<SonarQualityProfile>
     * @date: 2019年5月6日 下午2:42:59
     */
    List<SonarQualityProfile> getQualityProfiles(String tenantName, String ruleSetName, String language,
            String ruleSetSource);

    /**
     * 添加代码检查任务
     *
     * @param codeCheckTask
     * @param codeInfo
     * @return CodeCheckTask
     * @date: 2018年12月25日 下午6:01:53
     */
    CodeCheckTask addCodeCheckTask(CodeCheckTask codeCheckTask, CodeInfo codeInfo);

    /**
     * 根据租户名和任务名查询记录
     *
     * @param tenantName
     *            租户名
     * @param taskName
     *            任务名
     * @return CodeCheckTask
     * @date: 2018年12月25日 下午6:20:20
     */
    CodeCheckTask getCodeCheckTaskByNameAndTenant(String tenantName, String taskName);

    /**
     * 获取检查任务列表
     *
     * @param tenantName
     *            租户
     * @param taskName
     *            任务名(模糊查询)
     * @param
     * @param pageable
     *            分页
     * @return Page<CodeCheckTask>
     * @date: 2018年12月25日 下午6:57:15
     */
    Page<Map<String, String>> getCodeCheckTasks(String tenantName, String taskName, Pageable pageable);

    /**
     * 修改代码检查任务
     *
     * @param codeCheckTask
     * @return boolean
     * @date: 2018年12月25日 下午6:01:53
     */
    boolean modifyCodeCheckTask(CodeCheckTask codeCheckTask);

    /**
     * 根据ID删除记录
     *
     * @param id
     * @return boolean
     * @date: 2018年12月25日 下午7:27:01
     */
    boolean deleteCodeCheckTask(String id);

    /**
     * 启动检查
     *
     * @param id
     * @return boolean
     * @date: 2018年12月25日 下午7:30:46
     */
    boolean startupCheckCode(String id);

    /**
     * 获取检查结果
     *
     * @param id
     *            检查记录ID
     * @param issueType
     *            问题类型
     * @param issueSeverities
     *            问题级别
     * @param rule
     *            检查规则（模糊查询）
     *
     * @return SonarCheckResult
     * @date: 2019年1月2日 下午4:54:44
     */
    List<Issues> getCodeCheckResult(String id, String issueType, String issueSeverities, String rule);

    /**
     * 保存CodeCheckTask
     *
     * @param codeCheckTask
     * @return CodeCheckTask
     * @date: 2018年12月25日 下午6:40:11
     */
    CodeCheckTask saveCodeCheckTask(CodeCheckTask codeCheckTask);

    /**
     * 查询所有时间表达式不为空的检查任务
     *
     * @return List<CodeCheckTask>
     * @date: 2019年1月3日 下午5:09:14
     */
    List<CodeCheckTask> getCodeCheckTasksCronIsNotNull();

    /**
     * 添加代码构建定时任务
     *
     * @param codeCheckTask
     * @return boolean 添加是否成功
     * @date: 2019年1月3日 下午2:53:19
     */
    boolean addCodeCheckQuartz(CodeCheckTask codeCheckTask);

    /**
     * 禁用任务
     *
     * @param id
     * @return boolean
     * @date: 2019年5月6日 上午10:53:41
     */
    boolean disableCodeCheckTask(String id);

    /**
     * 启用任务
     *
     * @param id
     * @return boolean
     * @date: 2019年5月6日 上午10:53:41
     */
    boolean enableCodeCheckTask(String id);

    /**
     * 获取代码检查任务的历史检查记录
     *
     * @Description
     * @param id
     * @param pageable
     * @return Page<CodeCheckResult>
     */
    Page<CodeCheckResult> getCodeCheckHistoryRecord(String id, Pageable pageable);

    /**
     * 根据sonar检查结果转换为数据库存储的检查结果
     *
     * @param sonarCheckResultSummary
     * @return CodeCheckResult
     * @date: 2019年5月9日 下午4:02:03
     */
    CodeCheckResult transferSonarCheckResult(SonarCheckResultSummary sonarCheckResultSummary);

    /**
     * 保存sonar检查结果
     *
     * @param codeCheckResult
     * @return CodeCheckResult
     * @date: 2019年5月9日 下午4:19:45
     */
    CodeCheckResult saveCodeCheckResult(CodeCheckResult codeCheckResult);

    /**
     * 根据任务ID获取详情
     *
     * @param taskId
     * @return CodeCheckInfo
     * @date: 2019年5月14日 下午4:55:45
     */
    CodeCheckInfo getCodeCheckInfo(String taskId);

    /**
     * 获取语言对应规则数量
     *
     * @param lang
     * @return Integer
     * @date: 2019年5月28日 下午2:38:26
     */
    Integer countRuleNumsByLang(String lang);

    /**
     * 生成规则集名称
     *
     * @param tenantName
     * @param name
     * @return String
     * @date: 2019年4月23日 下午4:58:56
     */
    String generateSonarQPName(String tenantName, String name);

    /**
     * 根据语言和名称，获取规则集
     *
     * @param tenantName
     * @param lang
     * @param profileName
     * @return SonarQualityProfile
     * @date: 2019年6月14日 下午2:43:51
     */
    SonarQualityProfile getSonarQualityProfile(String tenantName, String lang, String profileName);

    /**
     * 获取规则集的规则数量统计信息(激活数/总数)
     *
     * @param qualityProfile
     * @return String
     * @date: 2019年6月14日 下午2:44:59
     */
    String getRuleNumStatisticsInfo(SonarQualityProfile qualityProfile);

    /**
     * 获取检查中的任务
     *
     * @return List<CodeCheckTask>
     * @date: 2019年6月19日 上午9:51:06
     */
    List<CodeCheckTask> getTasksStatusIng();

    /**
     * 重新启动检查任务
     *
     * @param codeCheckTask
     *            void
     * @date: 2019年6月19日 下午3:12:19
     */
    void restartCodeCheckTask(CodeCheckTask codeCheckTask);

    Page<CodeCheckTask> getCodeCheckTaskList(String tenantName, Pageable pageable);

    /**
     * 获取当天代码检查的任务的最新结果
     * 
     * @param codeBaseNameSet
     * @return List<Map<String,Object>>
     * @date: 2019年8月16日 下午3:38:29
     */
    List<Map<String, Object>> getCodeCheckLatestResult(Set<String> codeBaseNameSet);
}
