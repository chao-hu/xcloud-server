package com.xxx.xcloud.rest.v1.code.check;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.sonar.model.Issues;
import com.xxx.xcloud.client.sonar.model.Rule;
import com.xxx.xcloud.client.sonar.model.SonarQualityProfile;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.entity.CodeInfo;
import com.xxx.xcloud.module.sonar.CodeCheckConstant;
import com.xxx.xcloud.module.sonar.entity.CodeCheckTask;
import com.xxx.xcloud.module.sonar.service.SonarService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.impl.TenantServiceImplV1;
import com.xxx.xcloud.rest.v1.code.check.model.*;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.utils.StringUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import com.xxx.xcloud.client.sonar.model.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @ClassName: com.xxx.xcloud.api.v1.CodeCheckController
 * @Description: codecheck controller
 * @author: lizhen
 * @date: 2019年4月25日 下午4:16:56
 */

@Controller
@RequestMapping("/v1/codecheck")
public class CodeCheckController {

    private static final Logger LOG = LoggerFactory.getLogger(CodeCheckController.class);

    @Autowired
    private SonarService sonarService;

    @Autowired
    private TenantServiceImplV1 tenantService;

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取代码检查任务列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "taskName", value = "任务名", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "2000", dataType = "int") })
    public ApiResult getCodeCheckTask(@RequestParam(value = "tenantName") String tenantName,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") Integer size) {

        PageRequest pageable = PageRequest.of(page, size);
        LOG.debug("代码检查任务列表-----收到的参数 tenantName={} ,pageable={}", tenantName, JSONObject.toJSONString(pageable));

        String retMessage = "代码检查任务列表获取失败!";
        Page<Map<String, String>> data = null;

        try {
            /*
             * id, task_name任务名,code_repos_name仓库名, STATUS状态, available可用,
             * health_degree健康度（1.0-5.0）, code_line_numbers代码行数, question_numbers问题数,
             * check_time检查时间, check_duration_time检查持续时间
             */
            data = sonarService.getCodeCheckTasks(tenantName, taskName, pageable);
        } catch (Exception e) {
            LOG.error("代码检查任务列表获取异常， e : {}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, data, retMessage);
        }

        retMessage = "代码检查任务列表获取成功!";
        return new ApiResult(ReturnCode.CODE_SUCCESS, data, retMessage);
    }

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "新建代码检查任务", notes = "")
    public ApiResult createCodeCheckTask(@RequestBody CreateCodeCheckTaskDTO model) {

        CodeCheckTask codeCheckTask = new CodeCheckTask();
        CodeInfo codeInfo = new CodeInfo();
        codeCheckTask.setLanguage(model.getLang());
        codeCheckTask.setSonarRuleJosnStr(model.getSonarQualityprofileName());
        codeCheckTask.setTaskDesc(model.getTaskDesc());
        codeCheckTask.setTaskName(model.getTaskName());
        codeCheckTask.setTenantName(model.getTenantName());
        codeCheckTask.setCron(model.getCron());
        codeCheckTask.setCronDescription(model.getCronDescription());
        codeCheckTask.setCreatedBy(model.getCreatedBy());

        codeInfo.setCiCodeCredentialsId(model.getCiCodeCredentialsId());
        codeInfo.setCodeBranch(model.getCodeBranch());
        codeInfo.setCodeControlType(model.getCodeControlType());
        codeInfo.setCodeReposId(model.getReposId());
        codeInfo.setCodeReposName(model.getReposName());
        codeInfo.setCodeUrl(model.getCodeUrl());
        codeInfo.setBranchOrTag(model.getBranchOrTag());

        if (Objects.equals(codeInfo.getCodeControlType(), CiConstant.CODE_TYPE_GITLAB)) {
            if (codeInfo.getBranchOrTag() == null) {
                codeInfo.setBranchOrTag(CiConstant.CODE_BRANCH_TYPE);
            }
        }

        ApiResult apiResult = checkCreateCodeCheckTask(codeCheckTask, codeInfo);
        if (apiResult != null) {
            return apiResult;
        }

        // 调用service方法
        try {

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, sonarService.addCodeCheckTask(codeCheckTask, codeInfo),
                    "新建代码检查任务成功！");

        } catch (ErrorMessageException e) {
            LOG.error("新建代码检查任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    private ApiResult checkCreateCodeCheckTask(CodeCheckTask codeCheckTask, CodeInfo codeInfo) {
        ApiResult apiResult = checkCodeCheckTask(codeCheckTask);
        if (apiResult != null) {
            return apiResult;
        }

        if (Objects.equals(codeInfo.getCodeControlType(), CiConstant.CODE_TYPE_GITLAB) || Objects
                .equals(codeInfo.getCodeControlType(), CiConstant.CODE_TYPE_GITHUB)) {
            if (StringUtils.isEmpty(codeInfo.getCodeBranch())) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码分支为空！");
            }
        }
        if (StringUtils.isEmpty(codeInfo.getCiCodeCredentialsId())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证信息为空！");
        }
        if (StringUtils.isEmpty(codeInfo.getCodeUrl())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码地址为空！");
        }
        if (Objects.equals(codeInfo.getCodeControlType(), CiConstant.CODE_TYPE_GITLAB) && (!isBranchOrTagLegal(
                codeInfo.getBranchOrTag()))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "选择分支/tag选项不合法，0：分支；1：标签！");
        }
        return null;
    }

    /**
     * branchOrTag字段是否合法
     *
     * @param branchOrTag
     * @return boolean
     * @date: 2019年7月29日 下午3:02:05
     */
    private boolean isBranchOrTagLegal(Byte branchOrTag) {
        if (branchOrTag == null) {
            return false;
        }

        if (branchOrTag == CiConstant.CODE_BRANCH_TYPE || branchOrTag == CiConstant.CODE_TAG_TYPE) {
            return true;
        }

        return false;
    }

    private ApiResult checkCodeCheckTask(CodeCheckTask codeCheckTask) {
        ApiResult apiResult = checkTenantName(codeCheckTask.getTenantName());
        if (apiResult != null) {
            return apiResult;
        }

        if (StringUtils.isEmpty(codeCheckTask.getLanguage())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "规则集语言信息为空！");
        }
        if (StringUtils.isEmpty(codeCheckTask.getSonarRuleJosnStr())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "规则集名称信息为空！");
        }
        if (StringUtils.isEmpty(codeCheckTask.getTaskName())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "任务名称为空！");
        }
        if (StringUtils.isEmpty(codeCheckTask.getTenantName())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "租户名称为空！");
        }
        return null;
    }

    /**
     * 验证租户是否合法
     *
     * @param tenantName
     * @return ApiResult
     * @date: 2019年2月28日 下午2:39:57
     */
    private ApiResult checkTenantName(String tenantName) {

        if (StringUtils.isEmpty(tenantName) || !tenantName.matches(Global.CHECK_TENANT_NAME)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "租户名称规则不符合规范");
        }

        Tenant tenant = null;
        try {
            tenant = tenantService.findTenantByTenantName(tenantName);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询tenantName: " + tenantName + " 失败");
        }
        if (null == tenant) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户tenantName: " + tenantName + " 不存在");
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}/operator" }, method = RequestMethod.PUT)
    @ApiOperation(value = "执行代码检查任务操作:启动,启用,禁用", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String") })
    public ApiResult startCheckTask(@PathVariable(value = "taskId") String taskId,
            @RequestBody OperatorTaskDTO json) {

        String operator = json.getOperator();
        // 解析参数

        ApiResult apiResult = null;

        // 调用service方法
        try {

            switch (operator) {
            case "start":
                sonarService.startupCheckCode(taskId);
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "启动代码检查任务成功");
                break;
            case "enable":
                sonarService.enableCodeCheckTask(taskId);
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "启用代码检查任务成功");
                break;
            case "disable":
                sonarService.disableCodeCheckTask(taskId);
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "禁用代码检查任务成功");
                break;
            default:
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改代码检查任务", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String") })
    public ApiResult modifyCheckTask(@PathVariable(value = "taskId") String taskId,
            @RequestBody UpdateCodeCheckTaskDTO model) {

        // 解析参数
        CodeCheckTask codeCheckTask = new CodeCheckTask();
        codeCheckTask.setId(taskId);
        codeCheckTask.setLanguage(model.getLang());
        codeCheckTask.setSonarRuleJosnStr(model.getSonarQualityprofileName());
        codeCheckTask.setTaskDesc(model.getTaskDesc());
        codeCheckTask.setTaskName(model.getTaskName());
        codeCheckTask.setTenantName(model.getTenantName());
        codeCheckTask.setCron(model.getCron());
        codeCheckTask.setCronDescription(model.getCronDescription());
        // codeCheckTask.setCreatedBy(model.getCreatedBy());

        ApiResult apiResult = checkCodeCheckTask(codeCheckTask);
        if (apiResult != null) {
            return apiResult;
        }

        // 调用service方法
        try {
            sonarService.modifyCodeCheckTask(codeCheckTask);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "配置代码检查任务成功！");
        } catch (ErrorMessageException e) {
            LOG.error("配置代码检查任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除代码检查任务", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String") })
    public ApiResult deleteCheckTask(@PathVariable(value = "taskId") String taskId) {

        ApiResult apiResult = null;
        try {
            sonarService.deleteCodeCheckTask(taskId);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "删除检查任务成功！");
        } catch (ErrorMessageException e) {
            LOG.error("删除检查任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/ruleset" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取规则集列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleSetName", value = "规则集名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "language", value = "语言", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleSetSource", value = "规则集来源1系统 2租户", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "10", dataType = "int") })
    public ApiResult getRuleSet(@RequestParam(value = "tenantName") String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "ruleSetName", required = false) String ruleSetName,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ruleSetSource", required = false) String ruleSetSource,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "starttime");
        LOG.debug(
                "获取规则集列表-----收到的参数 tenantName={} ,projectId={} ,ruleSetName={},language={},ruleSetSource={},pageable={}",
                tenantName, projectId, ruleSetName, language, ruleSetSource, JSONObject.toJSONString(pageable));

        String retMessage = "获取规则集列表失败!";
        Object data = null;

        try {
            List<SonarQualityProfile> profiles = sonarService
                    .getQualityProfiles(tenantName, ruleSetName, language, ruleSetSource); // ruleSetName
            // 模糊查询
            int start = page * size;
            int end = Math.min((page + 1) * size, profiles.size());
            List<SonarQualityProfile> rulepage = new ArrayList<>();
            if (isIndexLegal(start, end, profiles.size())) {
                rulepage = profiles.subList(start, end); // 分页,0为第一页

                for (SonarQualityProfile profile : rulepage) {
                    profile.setAllRuleCount(sonarService.countRuleNumsByLang(profile.getLanguage()));
                }
            }
            data = new PageImpl<SonarQualityProfile>(rulepage, PageUtil.getPageable(page, size), profiles.size());
        } catch (ErrorMessageException e) {
            LOG.error("获取规则集列表失败， e : {}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, e.getMessage());
        } catch (Exception e) {
            LOG.error("获取规则集列表失败， e : {}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, data, retMessage);
        }

        retMessage = "获取规则集列表成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, data, retMessage);
    }

    /**
     * 判断下标是否合法
     *
     * @param start
     * @param end
     * @param size
     * @return boolean
     * @date: 2019年6月28日 下午3:24:19
     */
    private boolean isIndexLegal(int start, int end, int size) {
        if (start < 0 || end < start || end > size) {
            return false;
        }
        return true;
    }

    @ResponseBody
    @RequestMapping(value = { "/ruleset/{rulesetKey}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改规则集名称", notes = "")
    @ApiImplicitParam(paramType = "path", name = "rulesetKey", value = "规则集的key", required = true, dataType = "String")
    public ApiResult modifyRuleSet(@PathVariable(value = "rulesetKey") String rulesetKey,
            @RequestBody ModifyRulesetDTO json) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            sonarService
                    .modifyQualityProfileName(json.getTenantName(), rulesetKey, json.getNewName(), json.getLanguage());
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "修改规则集名称成功！");

        } catch (ErrorMessageException e) {
            LOG.error("修改规则集名称失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/ruleset/{rulesetKey}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除规则集任务", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "rulesetKey", value = "规则集的key", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名", required = true, dataType = "String") })
    public ApiResult deleteRuleSet(@PathVariable(value = "rulesetKey") String rulesetKey,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            sonarService.deleteQualityProfile(tenantName, rulesetKey);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "删除规则集任务成功！");

        } catch (ErrorMessageException e) {
            LOG.error("删除规则集任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/ruleset" }, method = RequestMethod.POST)
    @ApiOperation(value = "复制规则集任务", notes = "")
    public ApiResult copyRuleSet(@RequestBody CopyRulesetDTO json) {

        ApiResult apiResult = null;

        // 解析参数
        if (XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM).equals(json.getNewName())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED,
                    "复制规则集名称不允许为：" + XcloudProperties.getConfigMap().get(Global.SONAR_QUALITYFILE_NAME_SYSTEM));
        }

        // 调用service方法
        try {
            sonarService.copyQualityProfile(json.getTenantName(), json.getRulesetKey(), json.getNewName(),
                    json.getLanguage());

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "复制规则集任务成功！");

        } catch (ErrorMessageException e) {
            LOG.error("复制规则集任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/rule" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取规则列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleName", value = "规则名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "language", value = "语言", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleType", value = "规则类型:CODE_SMELL,BUG,VULNERABILITY", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleLevel", value = "规则级别:INFO,MINOR,MAJOR,CRITICAL,BLOCKER", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "200", dataType = "int") })
    public ApiResult getRules(@RequestParam(value = "tenantName") String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "ruleName", required = false) String ruleName,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ruleType", required = false) String ruleType,
            @RequestParam(value = "ruleLevel", required = false) String ruleLevel,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "200") Integer size) {

        LOG.debug(
                "获取规则列表-----收到的参数 tenantName={} ,projectId={} ,ruleName={},language={},ruleType={},ruleLevel={},page={},size={}",
                tenantName, projectId, ruleName, language, ruleType, ruleLevel, page, size);
        ApiResult apiResult = checkRulePara(ruleType, ruleLevel, "true", language);
        if (apiResult != null) {
            return apiResult;
        }
        Object data = null;

        try {
            Rules sonarRules = sonarService
                    .getRules(tenantName, null, ruleName, ruleType, ruleLevel, null, language, page, size); // ruleName
            List<Rule> rules = sonarRules.getRules();
            if (!StringUtils.isEmpty(ruleName)) {
                rules = getRuleNameQurey(ruleName, rules); // ruleName不为空，模糊查询
            }

            int start = page * size;
            int end = Math.min((page + 1) * size, rules.size());
            List<Rule> rulepage = new ArrayList<>();
            if (isIndexLegal(start, end, rules.size())) {
                rulepage = rules.subList(start, end); // 分页, 0为第一页
            }

            data = new PageImpl<Rule>(rulepage, PageUtil.getPageable(page, size), rules.size());
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, data, "获取规则列表成功！");
        } catch (ErrorMessageException e) {
            LOG.error("获取规则列表失败， e : {}", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/rulesetdetail" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取规则集详情列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "rulesetKey", value = "规则集Key", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleName", value = "规则名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleType", value = "规则类型:CODE_SMELL,BUG,VULNERABILITY", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleLevel", value = "规则级别:INFO,MINOR,MAJOR,CRITICAL,BLOCKER", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ruleStatus", value = "规则状态：有效，无效:true,false", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "200", dataType = "int") })
    public ApiResult getRuleSetDetail(@RequestParam(value = "tenantName") String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "rulesetKey", required = true) String rulesetKey,
            @RequestParam(value = "ruleName", required = false) String ruleName,
            @RequestParam(value = "ruleType", required = false) String ruleType,
            @RequestParam(value = "ruleLevel", required = false) String ruleLevel,
            @RequestParam(value = "ruleStatus", required = true) String ruleStatus,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "200") Integer size) {

        LOG.debug(
                "获取规则集详情列表-----收到的参数 tenantName={} ,projectId={} ,rulesetKey={},ruleName={},language={},ruleSetSource={},pageable={},page={},size={}",
                tenantName, projectId, rulesetKey, ruleName, ruleType, ruleLevel, ruleStatus, page, size);

        ApiResult apiResult = checkRulePara(ruleType, ruleLevel, ruleStatus, null);
        if (apiResult != null) {
            return apiResult;
        }
        Object data = null;
        try {
            Rules sonarRules = sonarService
                    .getRules(tenantName, rulesetKey, ruleName, ruleType, ruleLevel, ruleStatus, null, page,
                            size); // ruleName 模糊查询 list<rule>
            List<Rule> rules = sonarRules.getRules();
            if (!StringUtils.isEmpty(ruleName)) {
                rules = getRuleNameQurey(ruleName, rules); // ruleName不为空，模糊查询
            }

            // 如果查询未激活的规则（可能会进行激活操作），把模版规则过滤掉，因为模版规则不能被激活
            if (Objects.equals(Boolean.valueOf(ruleStatus), Boolean.FALSE)) {
                rules = getRulesNotTemplate(rules);
            }

            int start = page * size;
            int end = Math.min((page + 1) * size, rules.size());
            List<Rule> rulepage = new ArrayList<>();
            if (isIndexLegal(start, end, rules.size())) {
                rulepage = rules.subList(start, end); // 分页, 0为第一页
            }

            data = new PageImpl<Rule>(rulepage, PageUtil.getPageable(page, size), rules.size());
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, data, "获取规则集详情列表成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取规则集详情列表失败， e : {}", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/rulesetdetail/{rulesetKey}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "规则集中规则值状态修改", notes = "")
    @ApiImplicitParam(paramType = "path", name = "rulesetKey", value = "规则集key", required = true, dataType = "String")
    public ApiResult modifyRuleActive(@PathVariable(value = "rulesetKey") String rulesetKey,
            @RequestBody List<OperatorRuleStatusDTO> ruleStatus) {

        ApiResult apiResult = null;
        // 解析参数
        // 调用service方法
        try {
            for (int i = 0; i < ruleStatus.size(); i++) {

                sonarService
                        .modifyRuleActivation(rulesetKey, ruleStatus.get(i).getRuleKey(), ruleStatus.get(i).getActive());
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "规则集中规则值状态修改成功！");

        } catch (ErrorMessageException e) {
            LOG.error("规则集中规则值状态修改失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}/issue" }, method = RequestMethod.GET)
    @ApiOperation(value = "代码检查问题列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "200", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "issueType", value = "问题类型", required = false, defaultValue = "CODE_SMELL,BUG,VULNERABILITY", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "issueSeverities", value = "问题级别", required = false, defaultValue = "INFO,MINOR,MAJOR,CRITICAL,BLOCKER", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "rule", value = "检查规则", required = false, defaultValue = "", dataType = "String") })
    public ApiResult CheckTaskIssue(@PathVariable(value = "taskId") String taskId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "200") Integer size,
            @RequestParam(value = "issueType", required = false, defaultValue = "CODE_SMELL,BUG,VULNERABILITY") String issueType,
            @RequestParam(value = "issueSeverities", required = false, defaultValue = "INFO,MINOR,MAJOR,CRITICAL,BLOCKER") String issueSeverities,
            @RequestParam(value = "rule", required = false, defaultValue = "") String rule) {

        ApiResult apiResult = null;

        // 解析参数
        if (page < 0) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "page应该大于等于0");
            return apiResult;
        }

        if (size <= 1) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "size应该大于等于1");
            return apiResult;
        }

        // check issueType
        if (StringUtils.isEmpty(issueType) || !(CodeCheckConstant.SONAR_RULE_TYPE_SET.contains(issueType)
                || "CODE_SMELL,BUG,VULNERABILITY".equals(issueType))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "问题类型参数不合法,请检查！");
        }

        // check issueSeverities
        if (StringUtils.isEmpty(issueSeverities) || !(
                CodeCheckConstant.SONAR_RULE_SEVERITIE_SET.contains(issueSeverities)
                        || "INFO,MINOR,MAJOR,CRITICAL,BLOCKER".equals(issueSeverities))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "问题级别参数不合法,请检查！");
        }

        // 调用service方法
        try {
            List<Issues> issues = sonarService.getCodeCheckResult(taskId, issueType, issueSeverities, rule);

            List<Issues> issuespage = new ArrayList<>();
            int start = page * size;
            int end = Math.min((page + 1) * size, issues.size());
            if (isIndexLegal(start, end, issues.size())) {
                issuespage = issues.subList(start, end); // 分页, 0为第一页
            }
            Page<Issues> pageData = new PageImpl<Issues>(issuespage, PageUtil.getPageable(page, size), issues.size());
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, pageData, "代码检查问题列表获取成功！");
        } catch (ErrorMessageException e) {
            LOG.error("代码检查问题列表获取失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "代码检查信息详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String") })
    public ApiResult getCodeCheckTask(@PathVariable(value = "taskId") String taskId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, sonarService.getCodeCheckInfo(taskId), "代码检查信息详情获取成功！");
        } catch (ErrorMessageException e) {
            LOG.error("代码检查信息详情获取失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}/history" }, method = RequestMethod.GET)
    @ApiOperation(value = "代码检查历史检查记录列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "200", dataType = "int") })
    public ApiResult CheckTaskHisIssue(@PathVariable(value = "taskId") String taskId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "200") Integer size) {

        ApiResult apiResult = null;

        // 解析参数
        if (page < 0) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "page应该大于等于0");
            return apiResult;
        }

        if (size <= 1) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "size应该大于等于1");
            return apiResult;
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "checkTime");
        Object data = null;
        String retMessage = "获取历史检查记录列表!";
        try {
            data = sonarService.getCodeCheckHistoryRecord(taskId, pageable);

        } catch (Exception e) {
            LOG.error("获取历史检查记录列表失败， e : {}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, e.getMessage());
        }

        retMessage = "获取历史检查记录列表成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, data, retMessage);
    }

    @ResponseBody
    @RequestMapping(value = { "/{taskId}/issue/top" }, method = RequestMethod.GET)
    @ApiOperation(value = "代码检查问题规则排名", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "taskId", value = "检查任务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "number", value = "数量", required = false, defaultValue = "10", dataType = "int") })
    public ApiResult CheckTaskIssueTop(@PathVariable(value = "taskId") String taskId,
            @RequestParam(value = "number", required = false, defaultValue = "10") Integer number) {

        ApiResult apiResult = null;
        Map<String, Long> resultMap = null;
        List<Map<String, Long>> resultList = new ArrayList<>();
        // 调用service方法
        try {
            List<Issues> issues = sonarService.getCodeCheckResult(taskId, null, null, null);
            Map<String, Long> resultCount = issues.stream()
                    .collect(Collectors.groupingBy(Issues::getRule, Collectors.counting()));
            Map<String, Long> resultsortData = new LinkedHashMap<>(number);
            resultCount.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEachOrdered(e -> resultsortData.put(e.getKey(), e.getValue()));
            int tmp = 0;
            for (Map.Entry<String, Long> entry : resultsortData.entrySet()) {
                tmp++;
                resultMap = new HashMap<>(2);
                resultMap.put(entry.getKey(), entry.getValue());
                resultList.add(resultMap);
                if (tmp == number) {
                    break;
                }
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, resultList, "代码检查问题规则排名获取成功！");
        } catch (ErrorMessageException e) {
            LOG.error("代码检查问题规则排名获取失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    /**
     * desc：模糊查询 ruleName
     *
     * @param ruleName
     * @param rule
     * @return
     */
    private List<Rule> getRuleNameQurey(String ruleName, List<Rule> rule) {

        List<Rule> results = new ArrayList<Rule>();
        Pattern pattern = Pattern.compile(ruleName);
        for (int i = 0; i < rule.size(); i++) {
            Matcher matcher = pattern.matcher(rule.get(i).getName());
            if (matcher.find()) {
                results.add(rule.get(i));
            }
        }
        return results;
    }

    /**
     * 获取非template规则
     *
     * @param rules
     * @return List<Rule>
     * @date: 2019年6月28日 下午5:01:46
     */
    private List<Rule> getRulesNotTemplate(List<Rule> rules) {
        List<Rule> results = new ArrayList<Rule>();
        for (int i = 0; i < rules.size(); i++) {
            if (!Objects.equals(rules.get(i).getIsTemplate(), Boolean.TRUE)) {
                results.add(rules.get(i));
            }
        }
        return results;
    }

    /**
     * 规则参数校验
     *
     * @param ruleType
     * @param ruleLevel
     * @param ruleStatus
     * @param language
     * @return
     */
    private ApiResult checkRulePara(String ruleType, String ruleLevel, String ruleStatus, String language) {
        if ((!StringUtils.isEmpty(ruleType)) && (!CodeCheckConstant.SONAR_RULE_TYPE_SET.contains(ruleType))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "规则类型参数不合法,请检查！");
        }
        if ((!StringUtils.isEmpty(ruleLevel)) && (!CodeCheckConstant.SONAR_RULE_SEVERITIE_SET.contains(ruleLevel))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "规则级别参数不合法,请检查！");
        }
        if (!StringUtils.isboolean(ruleStatus)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "规则状态参数不合法,请检查！");
        }
        if ((!StringUtils.isEmpty(language)) && (!CodeCheckConstant.SONAR_LANG_RUNENUM_MAP.containsKey(language))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "规则语言参数不合法,请检查！");
        }
        return null;
    }
}
