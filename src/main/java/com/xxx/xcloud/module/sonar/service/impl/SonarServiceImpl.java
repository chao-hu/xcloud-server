package com.xxx.xcloud.module.sonar.service.impl;

import com.offbytwo.jenkins.model.*;
import com.xxx.xcloud.client.sonar.*;
import com.xxx.xcloud.client.sonar.exception.*;
import com.xxx.xcloud.client.sonar.model.*;
import com.xxx.xcloud.common.*;
import com.xxx.xcloud.common.exception.*;
import com.xxx.xcloud.module.ci.consts.*;
import com.xxx.xcloud.module.ci.entity.*;
import com.xxx.xcloud.module.ci.model.*;
import com.xxx.xcloud.module.ci.service.*;
import com.xxx.xcloud.module.ci.service.impl.*;
import com.xxx.xcloud.module.ci.strategy.jenkins.*;
import com.xxx.xcloud.module.ci.threadpool.*;
import com.xxx.xcloud.module.devops.common.*;
import com.xxx.xcloud.module.devops.job.service.*;
import com.xxx.xcloud.module.quartz.*;
import com.xxx.xcloud.module.sonar.*;
import com.xxx.xcloud.module.sonar.entity.*;
import com.xxx.xcloud.module.sonar.model.Component;
import com.xxx.xcloud.module.sonar.model.*;
import com.xxx.xcloud.module.sonar.repository.*;
import com.xxx.xcloud.module.sonar.service.*;
import com.xxx.xcloud.utils.*;
import org.quartz.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author mengaijun
 * @Description: sonar接口
 * @date: 2018年12月24日 下午2:12:34
 */
@Service
public class SonarServiceImpl implements SonarService {

    public static final String SONAR_PROBLEM_LEVEL_MINOR = "MINOR";

    public static final String SONAR_PROBLEM_LEVEL_MAJOR = "MAJOR";

    public static final String SONAR_PROBLEM_LEVEL_CRITICAL = "CRITICAL";

    public static final String SONAR_PROBLEM_LEVEL_INFO = "INFO";

    public static final String SONAR_PROBLEM_LEVEL_BLOCKER = "BLOCKER";

    private static final Logger LOG = LoggerFactory.getLogger(SonarServiceImpl.class);

    @Autowired
    private QualityProfileRepository qualityProfileRepository;

    @Autowired
    private CodeCheckTaskRepository codeCheckTaskRepository;

    @Autowired
    private CiCodeCredentialsService ciCodeCredentialsService;

    @Autowired
    private ICiService ciService;

    @Autowired
    private QuartzUtils quartzUtils;

    @Autowired
    private JobService jobService;

    @Autowired
    private CodeCheckResultRepository codeCheckResultRepository;

    /**
     * 更新语言对应规则数量的锁
     */
    private final Object UPDATE_SONAR_LANG_RULENUMS_LOCK = new Object();

    @Override
    public void copyQualityProfile(String tenantName, String fromKey, String toName, String lang) {
        String newName = generateSonarQPName(tenantName, toName);
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        // 判断from是否存在
        QualityProfileInheritance qualityProfileInheritance = getQualityProfileInheritance(fromKey, sonarApi);
        if (qualityProfileInheritance == null || qualityProfileInheritance.getProfile() == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "复制的规则集不存在！");
        }

        // 判断复制的名称在语言是否存在
        if (isQualityProfileExist(sonarApi, lang, newName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "规则集名称已经存在！");
        }
        try {
            sonarApi.copyQualityProfiles(fromKey, newName);
        } catch (SonarException e) {
            LOG.error("复制规则集错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "复制规则集失败！原因：" + e.getMessage());
        } catch (Exception e) {
            LOG.error("复制规则集错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "复制规则集失败！ 原因：" + e.getMessage());
        }
    }

    /**
     * 规则集是否存在
     *
     * @param sonarApi
     * @param language
     * @param
     * @param profileName
     * @return boolean
     * @date: 2019年5月23日 上午9:56:55
     */
    private boolean isQualityProfileExist(SonarApi sonarApi, String language, String profileName) {
        List<SonarQualityProfile> qualityProfiles = getSonarQualityprofiles(sonarApi, language, null, profileName);
        if (qualityProfiles != null && qualityProfiles.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteQualityProfile(String tenantName, String key) {
        // 删除sonar服务器记录
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        QualityProfileInheritance qp = getQualityProfileInheritance(key, sonarApi);
        // 如果已经被删除
        if (qp == null) {
            return true;
        }
        // 如果删除其他人的规则集，不允许
        if (!qp.getProfile().getName().startsWith(generateSonarQPNamePrefix(tenantName))) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "无法删除系统和其他租户的规则集！");
        }

        deleteQualityProfileSonarByProfileKey(key, sonarApi);

        return true;
    }

    @Override
    public Rules getQualityProfileRules(String qualityProfileId, boolean active, int page, int size) {
        QualityProfile qualityProfileLocal = getQualityProfileLocalById(qualityProfileId);
        if (qualityProfileLocal == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "信息不存在!");
        }

        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        return getQualityProfileRules(qualityProfileLocal.getKey(), active, page, size, sonarApi);
    }

    @Override
    public Set<String> getAllSonarLanguages() {
        return CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.keySet();
    }

    @Override
    public List<Issues> getCodeCheckResult(String id, String issueType, String issueSeverities, String rule) {
        CodeCheckTask task = getCodeCheckTaskById(id);
        if (task == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "数据记录不存在!");
        }
        CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(task.getCodeInfoId());
        if (codeInfo == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "数据记录代码信息不存在!");
        }

        String projectKey = jobService.generateJenkinsJobName(task.getTenantName(), JobService.JOB_TYPE_SONAR,
                task.getTaskName());
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();

        // check all
        List<Issues> tempList = new ArrayList<Issues>();
        for (int i = 1;; i++) {
            SonarCheckResult sonarCheckResult = getSonarCheckResult(i, 200, projectKey, issueType, issueSeverities,
                    sonarApi);
            if (sonarCheckResult.getIssues().size() <= 0) {
                break;
            } else {
                tempList.addAll(sonarCheckResult.getIssues());
            }
        }

        // filter
        List<Issues> resultList;
        if (StringUtils.isEmpty(rule)) {
            resultList = tempList;
        } else {
            resultList = new ArrayList<Issues>();
            for (Issues issues : tempList) {
                if (issues.getRule().contains(rule)) {
                    resultList.add(issues);
                }
            }
        }

        return resultList;
    }

    /**
     * 查询sonar检查结果
     *
     * @param page
     *            页数
     * @param length
     *            每页长度
     * @param projectKeys
     *            检查记录ID
     * @param sonarApi
     * @return SonarCheckResult 检查结果
     * @date: 2019年1月2日 下午5:04:01
     */
    private SonarCheckResult getSonarCheckResult(int page, int length, String projectKeys, String issueType,
            String issueSeverities, SonarApi sonarApi) {
        try {
            return sonarApi.searchIssues(page, length, projectKeys, issueType, issueSeverities);
        } catch (SonarException e) {
            LOG.error("sonar查询检查结果失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_GET_CHECK_RESULT_FAILED, "sonar获取检查结果失败!");
        }
    }

    @Override
    public List<QualityProfile> getAllQualityProfiles(String tenantName) {
        List<String> userList = new ArrayList<String>();
        userList.add(tenantName);
        userList.add(CiConstant.USER_ADMIN_NAMESPACE);

        try {
            return qualityProfileRepository.getAllQualityProfiles(userList);
        } catch (Exception e) {
            LOG.error("查询规则集列表失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库查询失败!");
        }
    }

    @Override
    public List<SonarQualityProfile> getQualityProfiles(String tenantName, String ruleSetName, String language,
            String ruleSetSource) {
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        if (StringUtils.isEmpty(language)) {
            language = null;
        } else { // 如果语言不为空
            if (!CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.containsKey(language)) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                        "语言信息只能填写" + CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.keySet());
            }
        }

        List<SonarQualityProfile> profiles = getSonarQualityprofiles(sonarApi, language, null, null);
        if (profiles == null || profiles.isEmpty()) {
            return profiles;
        }

        String ruleSetNamePrefix = generateSonarQPNamePrefix(tenantName);
        Iterator<SonarQualityProfile> it = profiles.iterator();
        while (it.hasNext()) {
            SonarQualityProfile profile = it.next();
            if (!isQualityProfileSatisfy(profile, ruleSetNamePrefix, ruleSetName, ruleSetSource)) {
                it.remove();
            }
        }

        return profiles;
    }

    @Override
    public Integer countRuleNumsByLang(String lang) {
        if (CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.containsKey(lang)
                && CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.get(lang) == null) {
            // 如果语言合法，并且语言规则信息还没有获取，更新语言规则数信息
            synchronized (UPDATE_SONAR_LANG_RULENUMS_LOCK) {
                if (CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.get(lang) == null) {
                    SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
                    Rules rules = sonarApi.searchRules(null, 1, lang, "types");
                    if (rules != null && rules.getTotal() != null) {
                        CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.put(lang, rules.getTotal());
                    }
                }
            }
        }
        return CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.get(lang);
    }

    /**
     * 判断规则集是否满足条件
     *
     * @param profile
     *            规则集
     * @param ruleSetNamePrefix
     *            规则集名称前缀
     * @param ruleSetName
     *            规则集名称
     * @param ruleSetSource
     *            规则集来源
     * @return boolean
     * @date: 2019年5月6日 下午5:10:38
     */
    private boolean isQualityProfileSatisfy(SonarQualityProfile profile, String ruleSetNamePrefix, String ruleSetName,
            String ruleSetSource) {
        // 判断规则集来源条件
        if (!isQualityProfileSatisfySource(profile, ruleSetNamePrefix, ruleSetSource)) {
            return false;
        }

        // 判断模糊查询
        if (!StringUtils.isEmpty(ruleSetName)) {
            if (profile.getName().toLowerCase().contains(ruleSetName.toLowerCase())) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    /**
     * 判断来源是否符合条件，如果属于租户规则集且符合条件，去掉租户前缀
     *
     * @param profile
     * @param ruleSetNamePrefix
     * @param ruleSetSource
     * @return boolean
     * @date: 2019年5月6日 下午6:53:03
     */
    boolean isQualityProfileSatisfySource(SonarQualityProfile profile, String ruleSetNamePrefix, String ruleSetSource) {
        String profileName = profile.getName();
        if (CodeCheckConstant.QUALITYFILE_SOURCE_SYSTEM.equals(ruleSetSource)) {
            // 系统规则集，判断名称是否是Sonar way
            if (!XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM).equals(profileName)) {
                return false;
            }
        } else if (CodeCheckConstant.QUALITYFILE_SOURCE_TENANT.equals(ruleSetSource)) {
            // 用户规则集，判断是否以租户开头
            if (!profileName.startsWith(ruleSetNamePrefix)) {
                return false;
            }
            profile.setName(profileName.substring(ruleSetNamePrefix.length()));
        } else {
            // 两种来源均可
            if (XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM)
                    .equals(profileName)) {} else if (profileName.startsWith(ruleSetNamePrefix)) {
                profile.setName(profileName.substring(ruleSetNamePrefix.length()));
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void modifyRuleActivation(String rulesetKey, String ruleKey, Boolean active) {
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        modifyRuleStatus(rulesetKey, ruleKey, active, sonarApi);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CodeCheckTask addCodeCheckTask(CodeCheckTask codeCheckTask, CodeInfo codeInfo) {
        // 获取认证信息
        CiCodeCredentials credentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
        checkCredentials(credentials, codeInfo.getCodeControlType());

        // 检查是否存在同名任务
        if (getCodeCheckTaskByNameAndTenant(codeCheckTask.getTenantName(), codeCheckTask.getTaskName()) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "名称已经存在");
        }

        // 验证cron表达式
        if (!StringUtils.isEmpty(codeCheckTask.getCron()) && !quartzUtils.isValidExpression(codeCheckTask.getCron())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "cron表达式不合规范!");
        }

        // 如果是gitlab，替换域名
        if (codeInfo.getCodeControlType() == CiConstant.CODE_TYPE_GITLAB) {
            // 替换代码url域名
            codeInfo.setCodeUrl(
                    StringUtils.replaceUrlDomainName(credentials.getRegistoryAddress(), codeInfo.getCodeUrl()));
        }

        // 保存信息
        codeInfo = ciCodeCredentialsService.saveCodeInfo(codeInfo);
        codeCheckTask.setCreateTime(new Date());
        codeCheckTask.setStatus(CodeCheckConstant.CODE_CHECK_STATUS_WAIT);
        codeCheckTask.setCodeInfoId(codeInfo.getId());
        codeCheckTask = saveCodeCheckTask(codeCheckTask);

        // jenkins
        AbstractCiStrategyJenkins ciStrategyJenkins = CiStrategyFactoryJenkins.getCiStrategy("");
        // com.xxx.xcloud.module.devops.model.Job job =
        // ciStrategyJenkins.generateJenkinsJob(null, codeCheckTask, codeInfo,
        // credentials, generateSonarRule(codeCheckTask), null);
        com.xxx.xcloud.module.devops.model.Job job = ciStrategyJenkins.generateJenkinsSonarJob(codeCheckTask, codeInfo,
                credentials, generateSonarRule(codeCheckTask));
        ciService.saveJenkinsJob(job, null, JobService.JOB_TYPE_SONAR);

        if (!StringUtils.isEmpty(codeCheckTask.getCron())) {
            addCodeCheckQuartz(codeCheckTask);
        }

        return codeCheckTask;
    }

    /**
     * 生成SonarRule对象
     *
     * @param codeCheckTask
     * @return SonarRule
     * @date: 2019年5月8日 下午3:38:17
     */
    private SonarRule generateSonarRule(CodeCheckTask codeCheckTask) {
        SonarRule sonarRule = new SonarRule();
        sonarRule.setLanguage(codeCheckTask.getLanguage());
        sonarRule.setPathStr("./");
        sonarRule.setProfileName(
                generateSonarQPName(codeCheckTask.getTenantName(), codeCheckTask.getSonarRuleJosnStr()));
        return sonarRule;
    }

    /**
     * 验证认证是否正确
     *
     * @param credentials
     * @param codeControlType
     *            void
     * @date: 2019年5月8日 下午2:48:09
     */
    private void checkCredentials(CiCodeCredentials credentials, byte codeControlType) {
        if (credentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "代码用户名信息不存在!");
        }
        if (credentials.getCodeControlType() != codeControlType) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "代码认证记录的认证方式与选择认证方式不匹配!");
        }
    }

    @Override
    public boolean addCodeCheckQuartz(CodeCheckTask codeCheckTask) {
        Map<String, Object> parameters = new HashMap<String, Object>(16);
        parameters.put("id", codeCheckTask.getId());
        boolean success = false;
        try {
            quartzUtils.addJob(codeCheckTask.getTaskName(), getCodeCheckQuartzGroup(codeCheckTask.getTenantName()),
                    CodeCheckJob.class, codeCheckTask.getCron(), parameters);
            success = true;
        } catch (SchedulerException e) {
            LOG.error("代码检查添加定时任务失败!", e);
        } catch (Exception e) {
            LOG.error("代码检查添加定时任务失败!", e);
        }
        return success;
    }

    /**
     * 修改代码构建定时任务
     *
     * @param codeCheckTask
     *            修改后的任务
     * @date: 2019年1月3日 下午2:54:49
     */
    private void modifyCodeCheckQuartz(CodeCheckTask codeCheckTask) {
        try {
            quartzUtils.modifyTime(codeCheckTask.getTaskName(), getCodeCheckQuartzGroup(codeCheckTask.getTenantName()),
                    codeCheckTask.getCron());
        } catch (SchedulerException e) {
            LOG.error("代码检查修改定时任务失败!", e);
        }
    }

    /**
     * 移除代码构建定时任务
     *
     * @param codeCheckTask
     *            修改后的任务
     * @date: 2019年1月3日 下午2:54:49
     */
    private void removeCodeCheckQuartz(CodeCheckTask codeCheckTask) {
        try {
            quartzUtils.removeJob(codeCheckTask.getTaskName(), getCodeCheckQuartzGroup(codeCheckTask.getTenantName()));
        } catch (SchedulerException e) {
            LOG.error("代码检查移除定时任务失败!", e);
        }
    }

    /**
     * 代码检查定时任务组
     *
     * @param tenantName
     *            租户名
     * @return String 组名
     * @date: 2019年1月3日 下午2:41:09
     */
    private String getCodeCheckQuartzGroup(String tenantName) {
        return CodeCheckConstant.CODE_CHECK_QUARTZ_GROUP + "_" + tenantName;
    }

    @Override
    public CodeCheckTask getCodeCheckTaskByNameAndTenant(String tenantName, String taskName) {
        try {
            return codeCheckTaskRepository.getByNameAndTenant(tenantName, taskName);
        } catch (Exception e) {
            LOG.error("根据任务名和租户查询代码检查任务失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询检查任务失败!");
        }
    }

    @Override
    public Page<Map<String, String>> getCodeCheckTasks(String tenantName, String taskName, Pageable pageable) {
        try {

            if (StringUtils.isEmpty(taskName)) {
                taskName = "";
            } else {
                taskName = taskName.trim();
            }

            int page = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();

            int start = page * pageSize;

            List<Map<String, String>> codeCheckTasks = codeCheckTaskRepository.getSonarTaskInfo(tenantName,
                    taskName.trim(), start, pageSize);

            int total = codeCheckTaskRepository.getSonarTaskCount(tenantName, taskName);

            return new PageImpl<Map<String, String>>(codeCheckTasks, pageable, total);
        } catch (Exception e) {
            LOG.error("查询列CodeCheckTask列表表失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyCodeCheckTask(CodeCheckTask codeCheckTask) {
        // 查询要修改的记录
        CodeCheckTask codeCheckTaskOld = getCodeCheckTaskById(codeCheckTask.getId());
        checkModifyCodeCheckTask(codeCheckTask, codeCheckTaskOld);
        String originalTaskName = codeCheckTaskOld.getTaskName();
        CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(codeCheckTaskOld.getCodeInfoId());
        if (codeInfo == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改记录的代码信息不存在!");
        }
        // 查询认证信息
        CiCodeCredentials credentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
        checkCredentials(credentials, codeInfo.getCodeControlType());

        if (!StringUtils.isEmpty(codeCheckTask.getCron()) && !quartzUtils.isValidExpression(codeCheckTask.getCron())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "cron表达式不合规范!");
        }

        // “禁用”以外的状态修改配置后，状态变为“未执行”；“禁用”状态修改配置后，状态仍为“禁用”
        if (codeCheckTaskOld.getStatus() != CodeCheckConstant.CODE_CHECK_STATUS_DISABLED) {
            codeCheckTaskOld.setStatus(CodeCheckConstant.CODE_CHECK_STATUS_WAIT);
        }

        String originalCron = codeCheckTaskOld.getCron();
        String cron = codeCheckTask.getCron();

        // 设置修改后的信息
        codeCheckTaskOld.setCron(codeCheckTask.getCron());
        codeCheckTaskOld.setCronDescription(codeCheckTask.getCronDescription());
        codeCheckTaskOld.setSonarRuleJosnStr(codeCheckTask.getSonarRuleJosnStr());
        codeCheckTaskOld.setTaskName(codeCheckTask.getTaskName());
        codeCheckTaskOld.setTaskDesc(codeCheckTask.getTaskDesc());
        codeCheckTaskOld.setLanguage(codeCheckTask.getLanguage());
        codeCheckTaskOld.setSonarRuleJosnStr(codeCheckTask.getSonarRuleJosnStr());

        // 保存数据库
        saveCodeCheckTask(codeCheckTaskOld);

        // 再更新 job, 更新失败, 保存也回滚
        AbstractCiStrategyJenkins ciStrategyJenkins = CiStrategyFactoryJenkins.getCiStrategy("");
        com.xxx.xcloud.module.devops.model.Job job = ciStrategyJenkins.generateJenkinsSonarJob(codeCheckTask, codeInfo,
                credentials, generateSonarRule(codeCheckTask));

        ciService.saveJenkinsJob(job, originalTaskName, JobService.JOB_TYPE_SONAR);

        // 判断定时任务
        if (!StringUtils.isEmpty(originalCron) && !originalCron.equals(cron)) {
            if (StringUtils.isEmpty(cron)) {
                // 删除定时任务
                removeCodeCheckQuartz(codeCheckTaskOld);
            } else {
                // 修改定时任务
                modifyCodeCheckQuartz(codeCheckTaskOld);
            }
        } else if (StringUtils.isEmpty(originalCron) && !StringUtils.isEmpty(cron)) {
            // 新增定时任务
            addCodeCheckQuartz(codeCheckTask);
        }

        return true;
    }

    /**
     * 验证修改的任务信息，不通过抛出异常
     *
     * @param codeCheckTask
     * @param codeCheckTaskOld
     *            void
     * @date: 2019年5月20日 上午11:01:34
     */
    private void checkModifyCodeCheckTask(CodeCheckTask codeCheckTask, CodeCheckTask codeCheckTaskOld) {
        if (codeCheckTaskOld == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的记录不存在!");
        }
        if (codeCheckTaskOld.getStatus() == CodeCheckConstant.CODE_CHECK_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "检查中的代码不允许修改!");
        }
        if (!Objects.equals(codeCheckTask.getTenantName(), codeCheckTaskOld.getTenantName())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "当前租户无法修改其他租户的任务!");
        }
        String originalTaskName = codeCheckTaskOld.getTaskName();
        if (!originalTaskName.equals(codeCheckTask.getTaskName())
                && getCodeCheckTaskByNameAndTenant(codeCheckTask.getTenantName(),
                        codeCheckTask.getTaskName()) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "名称已经存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteCodeCheckTask(String id) {
        CodeCheckTask codeCheckTask = getCodeCheckTaskById(id);
        if (codeCheckTask == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "信息不存在!");
        }
        if (codeCheckTask.getStatus() == CiConstant.CONSTRUCTION_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "正在检查中, 无法删除!");
        }

        // 删除代码信息和任务信息
        ciCodeCredentialsService.deleteCodeInfo(codeCheckTask.getCodeInfoId());
        codeCheckTaskRepository.deleteById(id);
        codeCheckResultRepository.deleteBySonarTaskId(id);

        // 数据库删除成功, 再删除jenkins任务
        try {
            // 如果job已经不存在，当成删除了
            if (jobService.getJob(codeCheckTask.getTenantName(), JobService.JOB_TYPE_SONAR,
                    codeCheckTask.getTaskName()) == null) {
                return true;
            }
            jobService.delete(codeCheckTask.getTenantName(), JobService.JOB_TYPE_SONAR, codeCheckTask.getTaskName());
        } catch (DevopsException e) {
            LOG.error("删除Jenkins端job失败!", e);
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean startupCheckCode(String id) {
        CodeCheckTask codeCheckTask = getCodeCheckTaskById(id);
        if (codeCheckTask == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "记录不存在!");
        }
        if (codeCheckTask.getStatus() == CodeCheckConstant.CODE_CHECK_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "正在检测中, 请等待检查测完成.");
        }
        if (codeCheckTask.getStatus() == CodeCheckConstant.CODE_CHECK_STATUS_DISABLED) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "任务被禁用，无法检查.");
        }
        CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(codeCheckTask.getCodeInfoId());
        if (codeInfo == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "记录代码信息不存在!");
        }
        CiCodeCredentials ciCodeCredentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
        if (ciCodeCredentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "启动失败，找不到代库！");
        }

        String codeBaseName = ICiServiceImpl.generateCodeBaseName(ciCodeCredentials, codeInfo,
                codeCheckTask.getTenantName());

        // 更新状态
        codeCheckTask.setStatus(CodeCheckConstant.CODE_CHECK_STATUS_ING);
        codeCheckTask.setCheckTime(new Date());
        saveCodeCheckTask(codeCheckTask);

        // 启动检查线程
        CiThreadPool.getExecotur().execute(new StartJenkinsJobThread(codeCheckTask, codeBaseName));

        return true;
    }

    @Override
    public void restartCodeCheckTask(CodeCheckTask codeCheckTask) {
        codeCheckTask.setStatus(CodeCheckConstant.CODE_CHECK_STATUS_FAIL);
        saveCodeCheckTask(codeCheckTask);
        startupCheckCode(codeCheckTask.getId());
    }

    /**
     * 根据ID查询记录
     *
     * @param id
     * @return CodeCheckTask
     * @date: 2018年12月25日 下午7:16:26
     */
    private CodeCheckTask getCodeCheckTaskById(String id) {
        try {
            return codeCheckTaskRepository.getById(id);
        } catch (Exception e) {
            LOG.error("根据ID获取CodeCheckTask记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询记录详情失败!");
        }
    }

    @Override
    public CodeCheckTask saveCodeCheckTask(CodeCheckTask codeCheckTask) {
        try {
            return codeCheckTaskRepository.save(codeCheckTask);
        } catch (Exception e) {
            LOG.error("保存检查任务失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存代码检查任务失败!");
        }
    }

    @Override
    public CodeCheckResult saveCodeCheckResult(CodeCheckResult codeCheckResult) {
        try {
            return codeCheckResultRepository.save(codeCheckResult);
        } catch (Exception e) {
            LOG.error("保存检查结果失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存代码检查结果失败!");
        }
    }

    /**
     * 修改规则状态为激活或未激活
     *
     * @param profileKey
     *            规则集key
     * @param ruleKey
     *            规则key
     * @param active
     *            激活或未激活
     * @param sonarApi
     * @date: 2018年12月25日 上午10:53:55
     */
    private void modifyRuleStatus(String profileKey, String ruleKey, boolean active, SonarApi sonarApi) {
        if (active) {
            activateRule(profileKey, ruleKey, sonarApi);
        } else {
            deactivateRule(profileKey, ruleKey, sonarApi);
        }
    }

    /**
     * 根据ID获取规则集
     *
     * @param id
     * @return QualityProfileLocal
     * @date: 2018年12月24日 下午7:37:23
     */
    private QualityProfile getQualityProfileLocalById(String id) {
        try {
            return qualityProfileRepository.getById(id);
        } catch (Exception e) {
            LOG.error("根据ID查询规则集失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询规则集失败!");
        }
    }

    /**
     * 根据条件查询sonar所有规则集，如果值为空，表示不需要该条件
     *
     * @param sonarApi
     * @param language
     * @param project（未知字段，调用不要使用，置为null）
     * @param profileName
     * @return List<QualityProfile>
     * @date: 2018年12月24日 下午4:42:57
     */
    private List<SonarQualityProfile> getSonarQualityprofiles(SonarApi sonarApi, String language, String project,
            String profileName) {
        List<SonarQualityProfile> qualityProfileList = null;
        try {
            QualityProfiles qualityProfiles = sonarApi.searchQualityprofiles(language, profileName, project);
            if (qualityProfiles != null) {
                qualityProfileList = qualityProfiles.getProfiles();
            }
        } catch (SonarException e) {
            LOG.error("查询sonar所有规则集错误!", e);
        } catch (Exception e) {
            LOG.error("查询sonar所有规则集错误!", e);
        }
        return qualityProfileList;
    }

    /**
     * 根据profileKey删除sonar端规则集
     *
     * @param profileKey
     * @date: 2018年12月24日 下午7:44:44
     */
    private void deleteQualityProfileSonarByProfileKey(String profileKey, SonarApi sonarApi) {
        try {
            sonarApi.deleteQualityProfiles(profileKey);
        } catch (SonarException e) {
            LOG.error("删除sonar端规则集失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_DELETE_QUALITYPROFILE_FAILED, "删除sonar端规则集失败!");
        } catch (Exception e) {
            LOG.error("删除sonar端规则集失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_DELETE_QUALITYPROFILE_FAILED, "删除sonar端规则集失败!");
        }
    }

    /**
     * 查询规则集下的规则
     *
     * @param qualityProfileKey
     *            规则集key
     * @param active
     *            是否激活
     * @param page
     *            第page页, 从第一页开始
     * @param length
     *            length条数据
     * @return Rules
     * @date: 2018年12月24日 下午8:12:15
     */
    private Rules getQualityProfileRules(String qualityProfileKey, boolean active, int page, int length,
            SonarApi sonarApi) {
        try {
            return null;
        } catch (Exception e) {
            LOG.error("sonar端查询规则集下的规则失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_GET_QUALITYPROFILE_RULES_FAILED, "查询规则集下的规则失败!");
        }
    }

    /**
     * 激活规则集中的规则
     *
     * @param profileKey
     *            规则集key
     * @param ruleKey
     *            规则key
     * @param sonarApi
     * @date: 2018年12月25日 上午9:57:40
     */
    private void activateRule(String profileKey, String ruleKey, SonarApi sonarApi) {
        try {
            sonarApi.activateRule(profileKey, ruleKey);
        } catch (Exception e) {
            LOG.error("激活规则失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_ACTIVATE_QUALITYPROFILE_RULE_FAILED, "激活规则集规则失败!");
        }
    }

    /**
     * 停用规则集中的规则
     *
     * @param profileKey
     *            规则集key
     * @param ruleKey
     *            规则key
     * @param sonarApi
     * @date: 2018年12月25日 上午9:57:40
     */
    private void deactivateRule(String profileKey, String ruleKey, SonarApi sonarApi) {
        try {
            sonarApi.deactivateRule(profileKey, ruleKey);
        } catch (Exception e) {
            LOG.error("激活规则失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_DEACTIVATE_QUALITYPROFILE_RULE_FAILED, "停用规则集规则失败!");
        }
    }

    /**
     * 获取规则集详情
     *
     * @param profileKey
     *            规则集key
     * @param sonarApi
     * @return QualityProfileInheritance
     * @throws SonarException
     * @date: 2018年12月25日 上午10:03:48
     */
    private QualityProfileInheritance getQualityProfileInheritance(String profileKey, SonarApi sonarApi) {
        try {
            return sonarApi.getQualityprofilesInheritance(profileKey);
        } catch (SonarException e) {
            LOG.error("根据key获取规则集详情失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_GET_QUALITYPROFILE_FAILED, "根据规则集key获取规则集失败!");
        } catch (Exception e) {
            LOG.error("根据key获取规则集详情失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_GET_QUALITYPROFILE_FAILED, "根据规则集key获取规则集失败!");
        }
    }

    @Override
    public List<CodeCheckTask> getCodeCheckTasksCronIsNotNull() {
        List<CodeCheckTask> codeCheckTasks = null;
        try {
            codeCheckTasks = codeCheckTaskRepository.getCodeCheckTasksCronIsNotNull();
        } catch (Exception e) {
            LOG.error("查询需要添加定时任务的CodeCheckTask失败!", e);
        }
        return codeCheckTasks;
    }

    @Override
    public String generateSonarQPName(String tenantName, String name) {
        // 如果是系统规则集
        if (XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM).equals(name)) {
            return name;
        }

        // 如果不是系统规则集，拼接
        return generateSonarQPNamePrefix(tenantName) + name;
    }

    /**
     * 生成规则集名称前缀
     *
     * @param tenantName
     * @return String
     * @date: 2019年4月23日 下午4:58:56
     */
    private String generateSonarQPNamePrefix(String tenantName) {
        return tenantName + "_";
    }

    @Override
    public boolean modifyQualityProfileName(String tenantName, String key, String newName, String lang) {
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        // 判断from是否存在
        QualityProfileInheritance qualityProfileInheritance = getQualityProfileInheritance(key, sonarApi);
        if (qualityProfileInheritance == null || qualityProfileInheritance.getProfile() == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的规则集不存在！");
        }

        if (qualityProfileInheritance.getProfile().getName()
                .equals(XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM))) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "系统规则集名称不允许修改！");
        }

        newName = generateSonarQPName(tenantName, newName);

        if (isQualityProfileExist(sonarApi, lang, newName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "修改的名称已经存在！");
        }
        try {
            sonarApi.renameQualityProfiles(key, newName);
        } catch (SonarException e) {
            LOG.error("修改规则集名称错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "修改规则集名称失败！原因：" + e.getMessage());
        } catch (Exception e) {
            LOG.error("修改规则集名称错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "修改规则集名称失败！ 原因：" + e.getMessage());
        }

        return true;
    }

    @Override
    public boolean disableCodeCheckTask(String id) {
        CodeCheckTask task = getCodeCheckTaskById(id);
        if (task.getStatus() == CodeCheckConstant.CODE_CHECK_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "检查中的任务不允许禁用!");
        }
        if (task.getStatus() == CodeCheckConstant.CODE_CHECK_STATUS_DISABLED) {
            return true;
        }
        task.setStatus(CodeCheckConstant.CODE_CHECK_STATUS_DISABLED);
        if (!StringUtils.isEmpty(task.getCron())) {
            removeCodeCheckQuartz(task);
        }
        try {
            codeCheckTaskRepository.save(task);
        } catch (Exception e) {
            LOG.error("代码检查任务禁用失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "代码检查任务禁用失败!");
        }
        return true;
    }

    @Override
    public boolean enableCodeCheckTask(String id) {
        CodeCheckTask task = getCodeCheckTaskById(id);
        if (task.getStatus() != CodeCheckConstant.CODE_CHECK_STATUS_DISABLED) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "任务未处于禁用状态，无法启用!");
        }

        if (!StringUtils.isEmpty(task.getCron())) {
            // 新增定时任务
            addCodeCheckQuartz(task);
        }
        task.setStatus(CodeCheckConstant.CODE_CHECK_STATUS_WAIT);
        try {
            codeCheckTaskRepository.save(task);
        } catch (Exception e) {
            LOG.error("代码检查任务启用失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "代码检查任务启用失败!");
        }
        return true;
    }

    @Override
    public Page<CodeCheckResult> getCodeCheckHistoryRecord(String id, Pageable pageable) {
        try {
            return codeCheckResultRepository.getCodeCheckHistoryRecord(id, pageable);
        } catch (Exception e) {
            LOG.error("查询检查结果失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询检查结果失败！");
        }

    }

    @Override
    public Rules getRules(String tenantName, String ruleSetName, String ruleName, String ruleType, String ruleLevel,
            String ruleStatus, String language, Integer pageNum, Integer size) {
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        try {
            Rules rules = sonarApi.searchRules(ruleSetName, ruleStatus, 1, 500, ruleLevel, language, ruleType);
            return rules;
        } catch (Exception e) {
            LOG.error("根据条件查询相应的规则失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SONAR_GET_QUALITYPROFILE_RULES_FAILED, "根据条件查询相应的规则失败!");
        }
    }

    @Override
    public CodeCheckResult transferSonarCheckResult(SonarCheckResultSummary sonarCheckResultSummary) {
        CodeCheckResult codeCheckResult = new CodeCheckResult();
        Component component = sonarCheckResultSummary.getComponent();
        List<Measure> measures = component.getMeasures();
        int questionNumTotal = 0;
        for (Measure measure : measures) {
            // 健康度
            if ("reliability_rating".equals(measure.getMetric())) {
                codeCheckResult.setHealthDegree(Double.valueOf(measure.getValue()));
            } else if ("code_smells".equals(measure.getMetric())) {
                questionNumTotal += Integer.valueOf(measure.getValue());
            } else if ("vulnerabilities".equals(measure.getMetric())) {
                questionNumTotal += Integer.valueOf(measure.getValue());
            } else if ("bugs".equals(measure.getMetric())) {
                questionNumTotal += Integer.valueOf(measure.getValue());
            } else if ("ncloc".equals(measure.getMetric())) {
                codeCheckResult.setCodeLineNumbers(Integer.valueOf(measure.getValue()));
            }
        }
        codeCheckResult.setQuestionNumbers(questionNumTotal);

        return codeCheckResult;
    }

    /**
     * 设置Severities信息
     *
     * @param codeCheckResult
     * @param sonarCheckResult
     *            void
     * @date: 2019年8月14日 上午11:13:16
     */
    private void addSeveritiesInfo(CodeCheckResult codeCheckResult, SonarCheckResult sonarCheckResult) {
        List<FacetValue> facetValues = sonarCheckResult.getFacets().get(0).getValues();

        codeCheckResult.setQuestionNumbers(0);
        facetValues.forEach((facetValue) -> {
            Integer count = facetValue.getCount();
            count = count == null ? 0 : count;
            codeCheckResult.setQuestionNumbers(count + codeCheckResult.getQuestionNumbers());
            if (Objects.equals(facetValue.getVal(), SONAR_PROBLEM_LEVEL_MINOR)) {
                codeCheckResult.setMinorQuestionNumbers(count);
                return;
            }
            if (Objects.equals(facetValue.getVal(), SONAR_PROBLEM_LEVEL_MAJOR)) {
                codeCheckResult.setMajorQuestionNumbers(count);
                return;
            }
            if (Objects.equals(facetValue.getVal(), SONAR_PROBLEM_LEVEL_CRITICAL)) {
                codeCheckResult.setCriticalQuestionNumbers(count);
                return;
            }
            if (Objects.equals(facetValue.getVal(), SONAR_PROBLEM_LEVEL_INFO)) {
                codeCheckResult.setInfoQuestionNumbers(count);
                return;
            }
            if (Objects.equals(facetValue.getVal(), SONAR_PROBLEM_LEVEL_BLOCKER)) {
                codeCheckResult.setBlockerQuestionNumbers(count);
                return;
            }
        });
    }

    @Override
    public CodeCheckInfo getCodeCheckInfo(String taskId) {
        CodeCheckTask codeCheckTask = getCodeCheckTaskById(taskId);
        if (codeCheckTask == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "代码检查任务不存在！");
        }
        addSonarQualityProfileInfo(codeCheckTask);
        CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(codeCheckTask.getCodeInfoId());
        CodeCheckInfo codeCheckInfo = new CodeCheckInfo();
        codeCheckInfo.setCodeCheckTask(codeCheckTask);
        codeCheckInfo.setCodeInfo(codeInfo);
        if (codeInfo == null) {
            return codeCheckInfo;
        }
        try {
            codeCheckInfo.setCiCodeCredentials(ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId()));
        } catch (Exception e) {
            LOG.error("查询认证信息错误", e);
        }

        return codeCheckInfo;
    }

    /**
     * 添加sonar规则集信息（添加（1）规则统计信息）
     *
     * @param codeCheckTask
     * @date: 2019年6月14日 上午11:26:04
     */
    private void addSonarQualityProfileInfo(CodeCheckTask codeCheckTask) {
        SonarQualityProfile qualityProfile = getSonarQualityProfile(codeCheckTask.getTenantName(),
                codeCheckTask.getLanguage(), codeCheckTask.getSonarRuleJosnStr());

        codeCheckTask.setRuleNumStatistics(getRuleNumStatisticsInfo(qualityProfile));
    }

    @Override
    public SonarQualityProfile getSonarQualityProfile(String tenantName, String lang, String profileName) {
        SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
        List<SonarQualityProfile> qualityProfiles = getSonarQualityprofiles(sonarApi, lang, null,
                generateSonarQPName(tenantName, profileName));
        if (qualityProfiles != null && !qualityProfiles.isEmpty()) {
            return qualityProfiles.get(0);
        }
        return null;
    }

    @Override
    public String getRuleNumStatisticsInfo(SonarQualityProfile qualityProfile) {
        if (qualityProfile == null) {
            return null;
        }

        return qualityProfile.getActiveRuleCount() + "/" + countRuleNumsByLang(qualityProfile.getLanguage());
    }

    @Override
    public List<CodeCheckTask> getTasksStatusIng() {
        return getTasksByStatus(CodeCheckConstant.CODE_CHECK_STATUS_ING);
    }

    /**
     * 根据状态查询检查任务
     *
     * @param status
     * @return List<CodeCheckTask>
     * @date: 2019年6月19日 上午9:56:31
     */
    private List<CodeCheckTask> getTasksByStatus(byte status) {
        try {
            return codeCheckTaskRepository.findByStatus(status);
        } catch (Exception e) {
            LOG.error("根据状态查询代码检查任务记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "根据状态查询代码检查任务记录失败!");
        }
    }

    /**
     * 代码检查任务
     *
     * @author mengaijun
     * @date: 2019年6月19日 上午10:49:48
     */
    public class StartJenkinsJobThread implements Runnable {
        /**
         * 代码检查类
         */
        private CodeCheckTask codeCheckTask;

        private String codeBaseName;

        public StartJenkinsJobThread(CodeCheckTask codeCheckTask, String codeBaseName) {
            this.codeCheckTask = codeCheckTask;
            this.codeBaseName = codeBaseName;
        }

        @Override
        public void run() {
            // 拼接sonar检查结果的key
            String jobName = codeCheckTask.getTaskName();
            String tenantName = codeCheckTask.getTenantName();
            String jobType = JobService.JOB_TYPE_SONAR;
            String projectKey = jobService.generateJenkinsJobName(tenantName, jobType, jobName);

            int number = startJob(tenantName, jobType, jobName);
            boolean isStartSuccess = number > 0 ? true : false;

            boolean isExeSuccess = isJobSuccess(isStartSuccess, number, tenantName, jobName, jobType);

            byte constructionStatus = isExeSuccess ? CiConstant.CONSTRUCTION_STATUS_SUCCESS
                    : CiConstant.CONSTRUCTION_STATUS_FAIL;

            updateCodeCheckResult(isExeSuccess, projectKey, constructionStatus);
        }

        /**
         * 启动任务，获取启动结果
         *
         * @param tenantName
         * @param jobType
         * @param jobName
         * @return int <0 启动失败； >0启动成功
         * @date: 2019年5月20日 下午4:49:05
         */
        private int startJob(String tenantName, String jobType, String jobName) {
            int number = -1;
            try {
                // 代码检查不需要镜像名称参数，设置为空串
                String imageName = "";
                // 启动构建时传入镜像名称参数
                Map<String, String> params = new HashMap<>(2);
                params.put(XcloudProperties.getConfigMap().get(Global.DEVOPS_PARAM), imageName);
                number = jobService.build(tenantName, jobType, jobName, params);
            } catch (Exception e) {
                LOG.error("启动代码检查jenkins任务失败!" + e.getMessage(), e);
            }
            return number;
        }

        /**
         * 等到xcloud job执行完成, 判断是否执行成功
         *
         * @param isStartSuccess
         *            xcloud job是否启动成功
         * @param seqNo
         * @param tenantName
         *            租户
         * @param jobName
         *            job名称
         * @param jobType
         * @return boolean 是否执行成功
         * @date: 2019年1月3日 下午6:34:57
         */
        private boolean isJobSuccess(boolean isStartSuccess, Integer seqNo, String tenantName, String jobName,
                String jobType) {
            if (!isStartSuccess) {
                return false;
            }

            // 判断是否执行完成
            boolean isCiSuccess = false;
            int count = 0;
            while (true) {
                try {
                    Thread.sleep(Integer
                            .valueOf(XcloudProperties.getConfigMap().get(Global.XCLOUD_CI_CHECK_RESULT_INTERVAL_TIME)));
                } catch (InterruptedException e) {
                    LOG.error("等待构建完成线程循环被中断!", e);
                }

                // 获取执行信息
                Build build = null;
                BuildResult buildResult = null;
                try {
                    build = jobService.getBuild(tenantName, jobType, jobName, seqNo);
                    if (null != build && null != build.details()) {
                        buildResult = build.details().getResult();
                    }
                } catch (Exception e) {
                    LOG.error("jenkins构建时获取job执行信息失败!", e);
                    // 当成执行失败
                    break;
                }

                if (buildResult != null && !buildResult.equals(BuildResult.BUILDING)
                        && !buildResult.equals(BuildResult.REBUILDING)) {
                    isCiSuccess = buildResult.equals(BuildResult.SUCCESS);
                    break;
                }

                // 等待超时, 180 *5 s, 当成构建完成, 但结果是失败处理
                count++;
                if (count > Integer
                        .valueOf(XcloudProperties.getConfigMap().get(Global.XCLOUD_CI_CHECK_RESULT_TIMEOUT_COUNT))) {
                    break;
                }
            }

            return isCiSuccess;
        }

        /**
         * 更新代码检查信息
         *
         * @param
         * @param projectKey
         * @param constructionStatus
         *            void
         * @date: 2019年5月20日 下午5:00:37
         */
        private void updateCodeCheckResult(boolean isExeSuccess, String projectKey, byte constructionStatus) {
            Date startTime = codeCheckTask.getCheckTime();
            CodeCheckResult codeCheckResult = null;

            // 检查记录信息修改
            codeCheckTask.setStatus(constructionStatus);
            codeCheckTask.setCheckTime(startTime);

            // 执行失败
            if (!isExeSuccess) {
                saveCodeCheckTask(codeCheckTask);
                return;
            }
            
            try {
                // 休眠30s，再查询sonar检查结果，避免数据不同步
                Integer time = 30;
                try {
                    time = Integer.valueOf(XcloudProperties.getConfigMap()
                            .get(Global.QUERY_SONAR_RESULT_INTERVAL_TIME_AFTER_JENKINS_FINISH));
                } catch(Exception e) {
                }
                TimeUnit.SECONDS.sleep(time);
            } catch (Exception e) {

            }
            
            codeCheckResult = getCodeCheckResultFirstTry(projectKey);
            if (codeCheckResult == null) {
                codeCheckResult = new CodeCheckResult();
            }

            Page<CodeCheckResult> resultPage = getCodeCheckHistoryRecord(codeCheckTask.getId(),
                    PageUtil.getPageable(0, 1));
            CodeCheckResult resultLastTime = !resultPage.isEmpty() ? resultPage.getContent().get(0) : null;
            if (resultLastTime != null) {
                if (resultLastTime.getQuestionNumbers() != null
                        && resultLastTime.getQuestionNumbers().equals(codeCheckResult.getQuestionNumbers())) {
                    // 如果本次检查结果和上一次相同，进行第二次尝试获取检查结果
                    codeCheckResult = getCodeCheckResultSecondTry(projectKey);
                }
            }
            // 设置激活规则集数量信息
            if (codeCheckResult != null) {
                setRuleNumStatisticsInfo(codeCheckResult);
            }

            // 检查持续时间
            int constructionDuration = new Long((System.currentTimeMillis() - startTime.getTime()) / 1000).intValue();
            // 检查记录信息修改
            codeCheckTask.setStatus(constructionStatus);
            codeCheckTask.setCheckTime(startTime);
            codeCheckTask.setCheckDurationTime(constructionDuration);
            codeCheckTask.setLastCheckTime(startTime);
            codeCheckResult.setCheckDurationTime(constructionDuration);
            codeCheckResult.setCheckTime(startTime);
            codeCheckResult.setSonarTaskId(codeCheckTask.getId());
            codeCheckResult.setLanguage(codeCheckTask.getLanguage());
            codeCheckResult.setProfileName(codeCheckTask.getSonarRuleJosnStr());
            codeCheckResult.setCodeBaseName(codeBaseName);
            codeCheckResult.setSystemProfile(XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM)
                    .equals(codeCheckTask.getSonarRuleJosnStr()) ? true : false);

            // 检查结果信息
            saveCodeCheckResult(codeCheckResult);
            saveCodeCheckTask(codeCheckTask);
        }

        /**
         * 设置规则数量信息
         *
         * @param codeCheckResult
         *            void
         * @date: 2019年5月21日 下午4:04:50
         */
        private void setRuleNumStatisticsInfo(CodeCheckResult codeCheckResult) {
            SonarQualityProfile qualityProfile = getSonarQualityProfile(codeCheckTask.getTenantName(),
                    codeCheckTask.getLanguage(), codeCheckTask.getSonarRuleJosnStr());
            codeCheckResult.setRuleNumStatistics(getRuleNumStatisticsInfo(qualityProfile));
        }

        /**
         * 获取检查结果(等待统计结果和代码检查详细问题结果都有值再返回；共等待)
         *
         * @param projectKey
         * @return CodeCheckResult
         * @date: 2019年5月9日 下午4:12:44
         */
        private CodeCheckResult getCodeCheckResultFirstTry(String projectKey) {
            CodeCheckResult codeCheckResult = null;
            try {
                SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
                SonarCheckResultSummary result;
                // Jenkins执行完成后，调用sonar接口可能获取不到结果，多重试几次，获取结果，共等待80s
                int cycleTimes = 40;
                for (int i = 0; i < cycleTimes; i++) {
                    result = sonarApi.getCheckResult(projectKey);
                    if (!result.getComponent().getMeasures().isEmpty()) {
                        // 设置检查结果信息
                        codeCheckResult = transferSonarCheckResult(result);
                        break;
                    }
                    Thread.sleep(2000);
                    LOG.info("共调用sonar结果接口查询是否存在统计结果" + (i + 2) + "次");
                }

                if (codeCheckResult == null) {
                    return null;
                }

                // 设置info,minor,major,critical,blocker信息
                SonarCheckResult sonarCheckResult;
                for (int i = 0; i < cycleTimes; i++) {
                    sonarCheckResult = sonarApi.searchIssues(projectKey);
                    if (sonarCheckResult.getFacets() != null && !sonarCheckResult.getFacets().isEmpty()) {
                        // 设置结果
                        addSeveritiesInfo(codeCheckResult, sonarCheckResult);
                        break;
                    }
                    Thread.sleep(2000);
                    LOG.info("共调用sonar结果接口查询是否存在统计结果" + (i + 2) + "次");
                }

                // 如果统计结果不为空，且存在问题，等待issues有信息
                // 等待issues结果有值
                // if (codeCheckResult != null &&
                // codeCheckResult.getQuestionNumbers() != null
                // && codeCheckResult.getQuestionNumbers() > 0) {
                // for (int i = 0; i < cycleTimes; i++) {
                // List<Issues> issues =
                // SonarServiceImpl.this.getCodeCheckResult(codeCheckTask.getId(),
                // "CODE_SMELL,BUG,VULNERABILITY",
                // "INFO,MINOR,MAJOR,CRITICAL,BLOCKER", "");
                // if (issues != null && !issues.isEmpty()) {
                // break;
                // }
                // Thread.sleep(2000);
                // LOG.info("共调用sonar结果接口查询是否存在问题信息" + (i + 2) + "次");
                // }
                // }

            } catch (Exception e) {
                LOG.error("获取sonar检测结果错误!" + e.getMessage(), e);
            }
            return codeCheckResult;
        }

        /**
         * 第二次获取检查结果(等待5s，再获取结果)
         *
         * @param projectKey
         * @return CodeCheckResult
         * @date: 2019年5月9日 下午4:12:44
         */
        private CodeCheckResult getCodeCheckResultSecondTry(String projectKey) {
            CodeCheckResult codeCheckResult = null;
            try {
                TimeUnit.SECONDS.sleep(5);
                SonarApi sonarApi = SonarClientFactory.getSonarInstanceWithBasicAuth();
                SonarCheckResultSummary result = sonarApi.getCheckResult(projectKey);
                // 设置检查结果信息
                if (!result.getComponent().getMeasures().isEmpty()) {
                    codeCheckResult = transferSonarCheckResult(result);
                }

                if (codeCheckResult == null) {
                    return null;
                }

                SonarCheckResult sonarCheckResult = sonarApi.searchIssues(projectKey);
                if (sonarCheckResult.getFacets() != null && !sonarCheckResult.getFacets().isEmpty()) {
                    // 设置结果
                    addSeveritiesInfo(codeCheckResult, sonarCheckResult);
                }

            } catch (Exception e) {
                LOG.error("获取sonar检测结果错误!" + e.getMessage(), e);
            }
            return codeCheckResult;
        }

    }

    @Override
    public Page<CodeCheckTask> getCodeCheckTaskList(String tenantName, Pageable pageable) {
        try {
            return codeCheckTaskRepository.getCodeCheckTasks(tenantName, pageable);
        } catch (Exception e) {
            LOG.error("查询列CodeCheckTask列表表失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    @Override
    public List<Map<String, Object>> getCodeCheckLatestResult(Set<String> codeBaseNameSet) {
        if (codeBaseNameSet == null || codeBaseNameSet.isEmpty()) {
            return new ArrayList<>(0);
        }
        try {
            return codeCheckTaskRepository.getNewestSonarCheckResult(DateUtil.getDayStart(), codeBaseNameSet);
        } catch (Exception e) {
            LOG.error("查询代码检查任务错误！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询代码检查任务错误！");
        }
    }

}
