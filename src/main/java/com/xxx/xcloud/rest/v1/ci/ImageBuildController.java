package com.xxx.xcloud.rest.v1.ci;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.github.model.GitHubBranch;
import com.xxx.xcloud.client.github.model.GithubRepos;
import com.xxx.xcloud.client.gitlab.model.GitlabBranch;
import com.xxx.xcloud.client.gitlab.model.GitlabRepos;
import com.xxx.xcloud.client.gitlab.model.GitlabTag;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.entity.*;
import com.xxx.xcloud.module.ci.model.*;
import com.xxx.xcloud.module.ci.service.CiCodeCredentialsService;
import com.xxx.xcloud.module.ci.service.DockerfileTemplateService;
import com.xxx.xcloud.module.ci.service.ICiService;
import com.xxx.xcloud.module.ci.strategy.jenkins.strategy.compile.JavaCompileStrategyEnum;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.impl.TenantServiceImplV1;
import com.xxx.xcloud.rest.v1.ci.model.*;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.utils.SafeCode;
import com.xxx.xcloud.utils.StringUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author xujiangpeng
 * @ClassName: class
 * @Description: 代码构建
 * @date
 */
@Validated
@Controller
@RequestMapping("/v1/ci")
public class ImageBuildController {

    private static final String HOOK_OPERATION_PUSH = "push";
    private static final String HOOK_OPERATION_TAG_PUSH = "tag_push";
    private static final Logger LOG = LoggerFactory.getLogger(ImageBuildController.class);

    static final int DEFAULT_LOG_ROW_NUM = 30000;

    @Autowired
    private ICiService ciServiceImpl;

    @Autowired
    private TenantServiceImplV1 tenantService;

    @Autowired
    private DockerfileTemplateService dockerfileTemplateServiceImpl;

    @Autowired
    private CiCodeCredentialsService ciCodeCredentialsServiceImpl;

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建构建任务", notes = "")
    public ApiResult createCiJob(@Valid @RequestBody CreateCiDTO ciModel) {

        ApiResult apiResult = null;

        // 解析参数
        String tenantName = ciModel.getTenantName();

        // 构建类型（1：代码构建 2：DockerFile构建）
        Byte ciType = ciModel.getCiType();

        String ciName = ciModel.getCiName();
        String ciDescription = ciModel.getCiDescription();

        // 镜像信息
        String imageName = ciModel.getImageName();
        String imageVersion = ciModel.getImageVersion();
        String imageVersionPre = ciModel.getImageVersionPre();

        // 构建计划
        String cron = ciModel.getCron();
        String cronDescription = ciModel.getCronDescription();

        // 代码构建
        String codeBranch = ciModel.getCodeBranch();
        String ciCodeCredentialsId = ciModel.getCiCodeCredentialsId();
        Byte codeControlType = ciModel.getCodeControlType();
        String codeUrl = ciModel.getCodeUrl();

        // git项目名称
        String reposName = ciModel.getReposName();
        // git项目Id
        Integer reposId = ciModel.getReposId();

        String dockerfilePath = ciModel.getDockerfilePath();
        String lang = ciModel.getLang();
        String compile = ciModel.getCompile();

        // dockerfile构建
        String filePath = ciModel.getFilePath();
        String dockerfileContent = ciModel.getDockerfileContent();
        String fileName = ciModel.getFileName();

        String createdBy = ciModel.getCreatedBy();
        String projectId = ciModel.getProjectId();
        String imageVersionGenerationStrategy = ciModel.getImageVersionGenerationStrategy();

        if (StringUtils.isEmpty(imageName)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "镜像名称不能为空！");
        }
        if (ciType == CiConstant.TYPE_CODE && Objects.equals(codeControlType, CiConstant.CODE_TYPE_GITLAB)
                && (!isBranchOrTagLegal(ciModel.getBranchOrTag()))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "选择分支/tag选项不合法，0：分支；1：标签！");
        }
        if (ciType == CiConstant.TYPE_CODE && !isDockerfileTypeWriteLegal(ciModel.getDockerfileWriteType())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "dockerfile填写方式不合法，0：在线编辑；1：引用代码库！");
        }

        // 参数校验
        apiResult = checkCreateCiModel(tenantName, ciType, codeBranch, ciCodeCredentialsId, codeControlType, codeUrl,
                reposName, reposId, dockerfilePath, lang, compile, filePath, dockerfileContent, fileName);

        if (null != apiResult) {
            LOG.error("创建构建任务参数校验失败", apiResult.getMessage());
            return apiResult;
        }

        // 调用service方法
        try {
            Ci ci = null;
            // 代码构建
            if (CiConstant.TYPE_CODE == ciType) {

                Ci ciInfo = Ci.builder().withCiName(ciName).withTenantName(tenantName).withImageName(imageName)
                        .withImageVersion(imageVersion).withImageVersionPre(imageVersionPre)
                        .withEnvVariables(ciModel.getEnvVariables()).withCiDescription(ciDescription)
                        .withConstructionStatus(CiConstant.CONSTRUCTION_STATUS_WAIT).withCreateTime(new Date())
                        .withCiType(ciType).withCron(cron).withCronDescription(cronDescription)
                        .withDockerfilePath(dockerfilePath).withLang(lang).withCompile(compile).withCreatedBy(createdBy)
                        .withProjectId(projectId).withHookUsed(ciModel.getHookUsed())
                        .withImageVersionGenerationStrategy(imageVersionGenerationStrategy).build();
                CodeInfo codeInfo = new CodeInfo(reposName, reposId, codeBranch, ciCodeCredentialsId, codeControlType,
                        codeUrl);
                codeInfo.setBranchOrTag(ciModel.getBranchOrTag());
                CiFile ciFileInfo = new CiFile(dockerfilePath, dockerfileContent, null);
                ciFileInfo.setDockerfileTemplateId(ciModel.getDockerfileTemplateId());
                ciFileInfo.setDockerfileTypeId(ciModel.getDockerfileTypeId());
                ciFileInfo.setAdvanced(ciModel.getAdvanced());
                ciFileInfo.setDockerfileWriteType(ciModel.getDockerfileWriteType());
                ciFileInfo.setFileName(ciModel.getPackageName());
                ci = ciServiceImpl.addCodeCi(ciInfo, codeInfo, ciFileInfo);

            } else if (CiConstant.TYPE_DOCKERFILE == ciType) {
                // dockerfile 构建
                Ci ciInfo = new Ci(ciName, tenantName, imageName, imageVersion, imageVersionPre,
                        ciModel.getEnvVariables(), ciDescription, null, null, CiConstant.CONSTRUCTION_STATUS_WAIT,
                        new Date(), CiConstant.TYPE_DOCKERFILE, cron, cronDescription, null, null, null, createdBy,
                        projectId, ciModel.getHookUsed());
                ciInfo.setImageVersionGenerationStrategy(imageVersionGenerationStrategy);

                CiFile ciFileInfo = CiFile.builder().withFilePath(filePath).withDockerfileContent(dockerfileContent)
                        .withFileName(fileName).withUploadfileSize(ciModel.getUploadfileSize())
                        .withDockerfileTemplateId(ciModel.getDockerfileTemplateId())
                        .withDockerfileTypeId(ciModel.getDockerfileTypeId()).withAdvanced(ciModel.getAdvanced())
                        .build();
                ci = ciServiceImpl.addDockerfileCi(ciInfo, ciFileInfo);
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, ci, "创建构建任务成功！");

        } catch (ErrorMessageException e) {
            LOG.error("创建构建任务失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
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

    private boolean isDockerfileTypeWriteLegal(Byte dockerfileWriteType) {
        if (dockerfileWriteType == null) {
            return false;
        }
        if (dockerfileWriteType == CiConstant.DOCKERFILE_WRITE_TYPE_CODE_BASE
                || dockerfileWriteType == CiConstant.DOCKERFILE_WRITE_TYPE_WRITE_ONLINE) {
            return true;
        }

        return false;
    }

    private ApiResult checkCreateCiModel(String tenantName, Byte ciType, String codeBranch, String ciCodeCredentialsId,
            Byte codeControlType, String codeUrl, String reposName, Integer reposId, String dockerfilePath, String lang,
            String compile, String filePath, String dockerfileContent, String fileName) {

        ApiResult apiResult = null;

        apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }

        // 1 代码构建
        if (ciType == CiConstant.TYPE_CODE) {

            if (null == ciCodeCredentialsId) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证方式不能为空！");
            }
            if (null == codeControlType) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码库类型不能为空！");
            }
            if (codeControlType == CiConstant.CODE_TYPE_GITHUB || codeControlType == CiConstant.CODE_TYPE_GITLAB) {
                if (StringUtils.isEmpty(codeBranch)) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码分支不能为空！");
                }
            }
            if (StringUtils.isEmpty(codeUrl)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码地址不能为空！");
            }
            // if (StringUtils.isEmpty(dockerfilePath)) {
            // return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
            // "dockerfile路径不能为空！");
            // }
            if (StringUtils.isEmpty(lang) || !lang.matches(Global.CHECK_CI_LANG)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "语言类型不能为空, 仅支持java|go|python|php|nodejs！");
            }
            if (StringUtils.isEmpty(compile)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "编译信息不能为空!");
            }
            apiResult = checkCiCodeCompileInfo(lang, compile);
            if (apiResult != null) {
                return apiResult;
            }

        } else if (ciType == CiConstant.TYPE_DOCKERFILE) {
            // 2 DockerFile构建
            if (StringUtils.isEmpty(filePath)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "dockerfile构建上传文件的位置不能为空！");
            }
            if (StringUtils.isEmpty(dockerfileContent)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "dockerfile文件内容不能为空！");
            }
            if (StringUtils.isEmpty(fileName)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "资源名(dockerfile构建上传文件的名字, 多个文件逗号分隔)不能为空！");
            }

            // apiResult = checkCiDockerfileFilePathInfo(filePath);
            // if (apiResult != null) {
            // return apiResult;
            // }
        } else {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码构建类型有误，1：代码构建|2：DockerFile构建！");
        }

        return apiResult;
    }

    /**
     * 验证dockerfile构建filePath信息
     *
     * @param filePath
     * @return ApiResult
     * @date: 2019年3月1日 下午6:01:28
     */
    private ApiResult checkCiDockerfileFilePathInfo(String filePath) {
        ApiResult apiResult = null;
        FtpFilePath filePathObj = transferJsonToObj(filePath, FtpFilePath.class);
        if (filePathObj == null || StringUtils.isEmpty(filePathObj.getFilePath()) || StringUtils
                .isEmpty(filePathObj.getFtpHost()) || StringUtils.isEmpty(filePathObj.getFtpPass()) || StringUtils
                .isEmpty(filePathObj.getFtpUser())) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "filePath为json格式, 且需要包含filePath, ftpHost, ftpPass, ftpUser信息!");
        }
        return apiResult;
    }

    /**
     * 验证代码构建编译信息
     *
     * @param lang
     * @param compile
     * @return ApiResult
     * @date: 2019年3月1日 下午5:52:34
     */
    private ApiResult checkCiCodeCompileInfo(String lang, String compile) {
        ApiResult apiResult = null;
        if (lang.equals(CiConstant.DEVOPS_LANG_JAVA)) {
            String compileToolType = JSONObject.parseObject(compile).getString(CiConstant.COMPILE_TOOL_TYPE);
            compileToolType = StringUtils.isEmpty(compileToolType) ?
                    JavaCompileStrategyEnum.MAVEN.name() :
                    compileToolType;
            if (StringUtils.isEmpty(compileToolType)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "请选择编译工具类型，GRADLE|MAVEN!");
            }
            JavaCompileStrategyEnum compileStrategy = JavaCompileStrategyEnum.valueOf(compileToolType);
            if (compileStrategy == null) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "编译工具类型仅支持两种：GRADLE|MAVEN!");
            }
            return compileStrategy.checkCompile(compile);
        }

        if (lang.equals(CiConstant.DEVOPS_LANG_GO)) {
            CiInvokeGo ciInvokeGo = transferJsonToObj(compile, CiInvokeGo.class);
            if (ciInvokeGo == null || StringUtils.isEmpty(ciInvokeGo.getBuildcmd()) || StringUtils
                    .isEmpty(ciInvokeGo.getLangVersion())) {
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "编译信息需要为json格式, 且包含buildcmd, langVersion信息!");
            }
            return apiResult;
        }
        if (lang.equals(CiConstant.DEVOPS_LANG_PYTHON)) {
            CiInvokePython ciInvokePython = transferJsonToObj(compile, CiInvokePython.class);
            if (ciInvokePython == null || StringUtils.isEmpty(ciInvokePython.getBuildcmd())) {
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "编译信息需要为json格式, 且包含buildcmd信息!");
            }
            return apiResult;
        }
        if (lang.equals(CiConstant.DEVOPS_LANG_NODEJS)) {
            CiInvokeNodeJs ciInvokeNodeJs = transferJsonToObj(compile, CiInvokeNodeJs.class);
            if (ciInvokeNodeJs == null || StringUtils.isEmpty(ciInvokeNodeJs.getBuildcmd()) || StringUtils
                    .isEmpty(ciInvokeNodeJs.getLangVersion())) {
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "编译信息需要为json格式, 且包含buildcmd, langVersion信息!");
            }
            return apiResult;
        }
        if (lang.equals(CiConstant.DEVOPS_LANG_PHP)) {
            CiInvokePhp ciInvokePhp = transferJsonToObj(compile, CiInvokePhp.class);
            if (ciInvokePhp == null || StringUtils.isEmpty(ciInvokePhp.getTargets())) {
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "编译信息需要为json格式, 且包含targets信息!");
            }
            return apiResult;
        }

        apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "构建语言仅支持java|go|python|php|nodejs!");
        return apiResult;
    }

    /**
     * 字符串转换为json格式
     *
     * @param compile
     * @param clazz
     * @return T
     * @date: 2019年3月1日 下午5:54:25
     */
    private static <T> T transferJsonToObj(String compile, Class<T> clazz) {
        try {
            return JSONObject.parseObject(compile, clazz);
        } catch (Exception e) {
            LOG.error("传入字符串不符合json格式!", e);
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = { "/{ciId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "构建任务操作：启动，停止，修改，禁用，启用", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "ciId", value = "构建任务ID", required = true, dataType = "String") })
    public ApiResult modifyCiJob(@PathVariable(value = "ciId") String ciId, @Valid @RequestBody UpdateCiDTO ciModel) {

        ApiResult apiResult = null;

        // 解析参数
        String operator = ciModel.getOperator();

        // 调用service方法
        if (CiConstant.CI_OPERATOR_START.equals(operator)) {
            apiResult = startCi(ciId, ciModel.getCreatedBy());

        } else if (CiConstant.CI_OPERATOR_STOP.equals(operator)) {
            apiResult = stopCi(ciId);

        } else if (CiConstant.CI_OPERATOR_MODIFY.equals(operator)) {
            apiResult = modifyCi(ciId, ciModel);

        } else if (CiConstant.CI_OPERATOR_DISABLE.equals(operator)) {
            apiResult = disableCi(ciId);

        } else if (CiConstant.CI_OPERATOR_ENABLE.equals(operator)) {
            apiResult = enableCi(ciId);

        } else {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "operator参数不合法！");
        }

        return apiResult;
    }

    /**
     * 禁用构建任务
     *
     * @param ciId
     * @return ApiResult
     * @date: 2019年4月23日 上午9:58:53
     */
    private ApiResult disableCi(String ciId) {
        ApiResult apiResult = null;
        try {
            ciServiceImpl.disableCi(ciId);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "禁用构建任务成功！");
        } catch (ErrorMessageException e) {
            LOG.error("禁用构建任务失败！", e.getMessage());
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    /**
     * 启用构建任务
     *
     * @param ciId
     * @return ApiResult
     * @date: 2019年4月23日 上午10:01:05
     */
    private ApiResult enableCi(String ciId) {
        ApiResult apiResult = null;
        try {
            ciServiceImpl.enableCi(ciId);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "启用构建任务成功！");
        } catch (ErrorMessageException e) {
            LOG.error("启用构建任务失败！", e.getMessage());
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    private ApiResult startCi(String ciId, String createdBy) {
        ApiResult apiResult = null;
        try {
            ciServiceImpl.startCi(ciId, createdBy);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "启动构建任务成功！");
        } catch (ErrorMessageException e) {
            LOG.error("启动构建任务失败！", e.getMessage());
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    private ApiResult stopCi(String ciId) {
        ApiResult apiResult = null;
        try {
            ciServiceImpl.stopCi(ciId);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "停止构建任务成功！");
        } catch (ErrorMessageException e) {
            LOG.error("停止构建任务失败！", e.getMessage());
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    private ApiResult modifyCi(String ciId, UpdateCiDTO ciModel) {
        ApiResult apiResult = null;
        // 构建类型（1：代码构建 2：DockerFile构建）
        Ci ci = ciServiceImpl.getCi(ciId);
        if (ci == null) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "ciId输入有误，该构建任务不存在！");
        }
        Byte ciType = ci.getCiType();

        String ciDescription = ciModel.getCiDescription();
        String cron = ciModel.getCron();
        String cronDescription = ciModel.getCronDescription();
        String codeBranch = ciModel.getCodeBranch();
        String ciCodeCredentialsId = ciModel.getCiCodeCredentialsId();
        Byte codeControlType = ciModel.getCodeControlType();
        String codeUrl = ciModel.getCodeUrl();
        String dockerfilePath = ciModel.getDockerfilePath();
        String compile = ciModel.getCompile();
        String lang = ciModel.getLang();

        String ciName = ciModel.getCiName();
        String imageName = ciModel.getImageName();
        String imageVersion = ciModel.getImageVersion();
        String envVariables = ciModel.getEnvVariables();
        String imageVersionGenerationStrategy = ciModel.getImageVersionGenerationStrategy();

        // git项目名称
        String reposName = ciModel.getReposName();
        // git项目Id
        Integer reposId = ciModel.getReposId();

        // 文件
        String filePath = ciModel.getFilePath();
        String dockerfileContent = ciModel.getDockerfileContent();
        String fileName = ciModel.getFileName();

        if (StringUtils.isEmpty(imageName)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "镜像名称不能为空！");
        }

        if (ciType == CiConstant.TYPE_CODE && Objects.equals(codeControlType, CiConstant.CODE_TYPE_GITLAB)
                && (!isBranchOrTagLegal(ciModel.getBranchOrTag()))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "选择分支/tag选项不合法，0：分支；1：标签！");
        }
        if (ciType == CiConstant.TYPE_CODE && !isDockerfileTypeWriteLegal(ciModel.getDockerfileWriteType())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "dockerfile填写方式不合法，0：在线编辑；1：引用代码库！");
        }

        // 参数校验
        apiResult = checkUpdateCiModel(ciType, ciDescription, cron, cronDescription, codeBranch, ciCodeCredentialsId,
                codeControlType, codeUrl, dockerfilePath, compile, reposName, reposId, filePath, dockerfileContent,
                fileName);
        if (null != apiResult) {
            LOG.error("修改构建任务参数校验失败", apiResult.getMessage());
            return apiResult;
        }

        try {
            if (CiConstant.TYPE_CODE == ciType) {

                Ci ciModify = Ci.builder().withId(ciId).withCiDescription(ciDescription).withCron(cron)
                        .withCronDescription(cronDescription).withDockerfilePath(dockerfilePath).withCompile(compile)
                        .withCiName(ciName).withImageName(imageName).withImageVersion(imageVersion).withLang(lang)
                        .withEnvVariables(envVariables).withHookUsed(ciModel.getHookUsed())
                        .withImageVersionGenerationStrategy(imageVersionGenerationStrategy).build();
                CodeInfo codeInfoModify = CodeInfo.builder().withCodeBranch(codeBranch)
                        .withCiCodeCredentialsId(ciCodeCredentialsId).withCodeControlType(codeControlType)
                        .withCodeReposName(reposName).withCodeReposId(reposId).withBranchOrTag(ciModel.getBranchOrTag())
                        .withCodeUrl(codeUrl).build();
                CiFile ciFileModify = CiFile.builder().withDockerfileTypeId(ciModel.getDockerfileTypeId())
                        .withDockerfileTemplateId(ciModel.getDockerfileTemplateId())
                        .withDockerfileContent(dockerfileContent).withAdvanced(ciModel.getAdvanced())
                        .withFilePath(dockerfilePath).withFileName(ciModel.getPackageName()).build();
                ciFileModify.setDockerfileWriteType(ciModel.getDockerfileWriteType());

                ciServiceImpl.modifyCodeCi(ciModify, codeInfoModify, ciFileModify);

            } else if (CiConstant.TYPE_DOCKERFILE == ciType) {

                Ci cImodify = Ci.builder().withId(ciId).withCiDescription(ciDescription).withCron(cron)
                        .withCronDescription(cronDescription).withCiName(ciName).withImageName(imageName)
                        .withImageVersion(imageVersion).withEnvVariables(envVariables)
                        .withImageVersionGenerationStrategy(imageVersionGenerationStrategy).build();
                CiFile ciFileModify = CiFile.builder().withFilePath(filePath)
                        .withDockerfileTypeId(ciModel.getDockerfileTypeId())
                        .withDockerfileTemplateId(ciModel.getDockerfileTemplateId())
                        .withDockerfileContent(dockerfileContent).withAdvanced(ciModel.getAdvanced())
                        .withFileName(fileName).withUploadfileSize(ciModel.getUploadfileSize()).build();
                ciServiceImpl.modifyDockerfileCi(cImodify, ciFileModify);
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "修改构建任务成功！");

        } catch (ErrorMessageException e) {
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    private ApiResult checkUpdateCiModel(byte ciType, String ciDescription, String cron, String cronDescription,
            String codeBranch, String ciCodeCredentialsId, Byte codeControlType, String codeUrl, String dockerfilePath,
            String compile, String reposName, Integer reposId, String filePath, String dockerfileContent,
            String fileName) {

        ApiResult apiResult = null;

        // 1 代码构建
        if (ciType == CiConstant.TYPE_CODE) {

            if (codeControlType == CiConstant.CODE_TYPE_GITHUB || codeControlType == CiConstant.CODE_TYPE_GITLAB) {
                if (StringUtils.isEmpty(codeBranch)) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码分支不能为空！");
                }
            }
            if (null == ciCodeCredentialsId) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证方式不能为空！");
            }
            if (null == codeControlType) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码库类型不能为空！");
            }
            if (StringUtils.isEmpty(codeUrl)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码地址不能为空！");
            }
            if (StringUtils.isEmpty(dockerfilePath)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "dockerfile路径不能为空！");
            }
            if (StringUtils.isEmpty(compile)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "编译命令不能为空！");
            }

        }
        // 2 DockerFile构建
        else if (ciType == CiConstant.TYPE_DOCKERFILE) {
            if (StringUtils.isEmpty(filePath)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "dockerfile构建上传文件的位置不能为空！");
            }
            if (StringUtils.isEmpty(dockerfileContent)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "dockerfile文件内容不能为空！");
            }
            if (StringUtils.isEmpty(fileName)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "资源名(dockerfile构建上传文件的名字, 多个文件逗号分隔)不能为空！");
            }
            apiResult = checkCiDockerfileFilePathInfo(filePath);
            if (apiResult != null) {
                return apiResult;
            }

        } else {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "代码构建类型有误，1：代码构建|2：DockerFile构建！");
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{ciId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除构建任务", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "ciId", value = "构建任务ID", required = true, dataType = "String") })
    public ApiResult deleteCiJob(@PathVariable(value = "ciId") String ciId) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            ciServiceImpl.deleteCi(ciId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "构建任务删除成功！");

        } catch (ErrorMessageException e) {
            LOG.error("删除构建任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取构建任务列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ciType", value = "构建类型，1：代码构建，2：DockerFile构建", required = false, dataType = "Byte"),
            @ApiImplicitParam(paramType = "query", name = "ciName", value = "构建名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000"), })
    public ApiResult getCiJobList(@RequestParam String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "ciType", required = false) Byte ciType,
            @RequestParam(value = "ciName", required = false) String ciName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult = null;

        // 解析参数

        apiResult = checkGetCiListParams(tenantName, ciType);
        if (null != apiResult) {
            LOG.error("获取构建任务列表参数有误！", apiResult);
            return apiResult;
        }

        if (StringUtils.isEmpty(ciName)) {
            ciName = "";
        }
        ciName = "%" + ciName + "%";

        // 调用service方法
        try {
            Page<CiDetail> ciDetailpage = null;
            Page<Ci> cis = null;

            // 查询构建任务列表
            if (null != ciType) {
                cis = ciServiceImpl.getCis(tenantName, projectId, ciType, ciName, PageUtil.getPageable(page, size));
            } else {
                cis = ciServiceImpl.getCis(tenantName, projectId, ciName, PageUtil.getPageable(page, size));
            }

            // 遍历查询构建任务的详细信息（除构建记录外）
            if (null != cis) {
                List<CiDetail> ciDetails = new ArrayList<>();
                for (Ci ci : cis) {
                    CiDetail ciDetail = ciServiceImpl.getCiDetail(ci.getId());
                    ciDetails.add(ciDetail);
                }
                ciDetailpage = new PageImpl<>(ciDetails, cis.getPageable(), cis.getTotalElements());
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, ciDetailpage, "获取构建任务成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取构建任务失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    private ApiResult checkGetCiListParams(String tenantName, Byte ciType) {

        Object a = new ArrayList<>();

        Tenant tenant = tenantService.findTenantByTenantName(tenantName);
        if (null == tenant) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "当前租户不存在！");
        }

        boolean flag = (ciType == CiConstant.TYPE_CODE || ciType == CiConstant.TYPE_DOCKERFILE);
        if (null != ciType && !flag) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "构建类型不符合规范， 1：代码构建，2：DockerFile构建！");
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = { "/ciName" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取构建任务，判断是否已经存在", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ciType", value = "构建类型，1：代码构建，2：DockerFile构建", required = false, dataType = "Byte"),
            @ApiImplicitParam(paramType = "query", name = "ciName", value = "构建名称", required = true, dataType = "String"), })
    public ApiResult getCiJobByName(@RequestParam String tenantName,
            @RequestParam(value = "ciType", required = false) Byte ciType,
            @RequestParam(value = "ciName", required = true) String ciName) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            Ci ci = ciServiceImpl.getCiByCiNameType(tenantName, ciName, ciType);

            String message = "该名称已经被使用!";
            if (ci == null) {
                message = "名称未被使用!";
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, null != ci ? true : false, message);

        } catch (ErrorMessageException e) {
            LOG.error("获取构建任务失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{ciId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取构建任务详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "ciId", value = "构建任务ID", required = true, dataType = "String") })
    public ApiResult getCiJobDetail(@PathVariable(value = "ciId") String ciId) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            CiDetail ciDetail = ciServiceImpl.getCiConstructDetail(ciId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, ciDetail, "获取构建任务详情成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取构建任务详情失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{ciId}/record" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取构建记录分页列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "ciId", value = "构建任务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000"),
            @ApiImplicitParam(paramType = "query", name = "logRowNum", value = "日志行数", required = false, dataType = "int", defaultValue = "30000") })
    public ApiResult getCiRecords(@PathVariable(value = "ciId") String ciId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "logRowNum", required = false, defaultValue = "30000") int logRowNum) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            Page<CiRecord> ciRecords = ciServiceImpl.getCiRecords(ciId, PageUtil.getPageable(page, size));
            // 截取日志
            if (logRowNum < DEFAULT_LOG_ROW_NUM) {
                // 截取
                List<CiRecord> ciRecordsIntercept = ciRecords.getContent();
                for (CiRecord ciRecord : ciRecordsIntercept) {
                    if (logRowNum == 0) {
                        // 截取0行，设置为空串
                        ciRecord.setLogPrint("");
                    } else if (!StringUtils.isEmpty(ciRecord.getLogPrint())) {
                        // 截取指定行数
                        ciRecord.setLogPrint(StringUtils.splitStrByLines(ciRecord.getLogPrint(), logRowNum));
                    }
                }
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, ciRecords, "获取构建记录分页列表成功！");
        } catch (ErrorMessageException e) {
            LOG.error("获取构建记录分页列表失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/record/{ciRecordId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除单条构建记录", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "ciRecordId", value = "构建记录ID", required = true, dataType = "String") })
    public ApiResult deleteCiRecords(@PathVariable(value = "ciRecordId") String ciRecordId) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            ciServiceImpl.deleteCiRecord(ciRecordId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "删除构建记录成功！");

        } catch (ErrorMessageException e) {
            LOG.error("删除构建记录失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/dockerfile" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建dockerfile模板", notes = "")
    public ApiResult createDockerfile(@RequestBody CreateDockerfileTemplateDTO obj) {

        ApiResult apiResult = null;

        // 解析参数
        String tenantName = obj.getTenantName();
        String dockerfileContent = obj.getDockerfileContent();
        String dockerfileName = obj.getDockerfileName();

        // 验证是否合法
        apiResult = checkCreateDockerfileParam(tenantName, dockerfileContent, dockerfileName);
        if (apiResult != null) {
            return apiResult;
        }

        String createdBy = obj.getCreatedBy();
        String projectId = obj.getProjectId();

        // 调用service方法
        try {
            if (tenantService.findTenantByTenantName(tenantName) == null) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户不存在!");
            }
            DockerfileTemplate dockerfile = dockerfileTemplateServiceImpl
                    .addTemplate(tenantName, dockerfileContent, dockerfileName, createdBy, projectId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, JSON.toJSON(dockerfile), "创建dockerfile模板成功！");

        } catch (ErrorMessageException e) {
            LOG.error("创建dockerfile模板失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    /**
     * 验证创建dockerfile内容参数
     *
     * @param tenantName        租户名
     * @param dockerfileContent dockerFile内容
     * @param dockerfileName    dockerfile名称
     * @return ApiResult 如果都和法, apiResult为空, 否则, 返回错误信息
     * @date: 2019年2月28日 下午2:48:01
     */
    private ApiResult checkCreateDockerfileParam(String tenantName, String dockerfileContent, String dockerfileName) {
        if (StringUtils.isEmpty(dockerfileContent)) {
            return new ApiResult(ReturnCode.CODE_CLUSTER_PARAM_IS_EMPTY, "dockerfile内容不能为空!");
        }
        if (StringUtils.isEmpty(dockerfileName) || !dockerfileName.matches(Global.CHECK_DOCKERFILE_TEMPLATE_NAME)) {
            return new ApiResult(ReturnCode.CODE_CLUSTER_PARAM_IS_EMPTY,
                    "dockerfile名称不能为空, 且需要符合正则表达式: ^[a-zA-Z][a-zA-Z0-9-/.]*$ ");
        }
        return checkTenantName(tenantName);
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
    @RequestMapping(value = { "/dockerfile/{dockerfileId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改dockerfile模板", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "dockerfileId", value = "dockerfileId", required = true, dataType = "String") })
    public ApiResult modifyDockerfile(@PathVariable(value = "dockerfileId") String dockerfileId,
            @RequestBody UpdateDockerfileTemplateDTO obj) {

        ApiResult apiResult = null;

        // 解析参数
        String dockerfileContent = obj.getDockerfileContent();

        if (StringUtils.isEmpty(dockerfileContent)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "dockerfile内容不能为空！");
        }

        // 调用service方法
        try {

            boolean result = dockerfileTemplateServiceImpl.modifyTemplate(dockerfileId, dockerfileContent);

            if (result) {
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "修改dockerfile模板成功！");
            } else {
                apiResult = new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "修改dockerfile模板失败！");
            }

        } catch (ErrorMessageException e) {
            LOG.error("修改dockerfile模板失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/dockerfile/{dockerfileId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除dockerfile模板", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "dockerfileId", value = "dockerfileId", required = true, dataType = "String") })
    public ApiResult deleteDockerfile(@PathVariable(value = "dockerfileId") String dockerfileId) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            boolean result = dockerfileTemplateServiceImpl.deleteTemplate(dockerfileId);

            if (result) {
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "删除dockerfile模板成功！");
            } else {
                apiResult = new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "删除dockerfile模板失败！");
            }

        } catch (ErrorMessageException e) {
            LOG.error("删除dockerfile模板失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/dockerfile/{dockerfileId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取dockerfile模板", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "dockerfileId", value = "dockerfileId", required = true, dataType = "String") })
    public ApiResult getDockerfileDetail(@PathVariable(value = "dockerfileId") String dockerfileId) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {
            DockerfileTemplate dockerfile = dockerfileTemplateServiceImpl.getTemplateById(dockerfileId);
            String dockerfileContent = buildDockerfileContent(dockerfile.getDockerfileContent());
            dockerfile.setDockerfileContent(dockerfileContent);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, dockerfile, "获取dockerfile模板成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取dockerfile模板失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    private String buildDockerfileContent(String dockerfileContent) {
        String publicHarboruri =
                XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/" + XcloudProperties
                        .getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME);
        LOG.info(publicHarboruri);
        String dockerfileContentNew = StringUtils.replaceAll(dockerfileContent, "${baseImageUrl}", publicHarboruri);
        LOG.info(dockerfileContentNew);
        return dockerfileContentNew;
    }

    @ResponseBody
    @RequestMapping(value = { "/dockerfile" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取dockerfile模板列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "typeId", value = "类型ID", required = false, dataType = "String", defaultValue = ""),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getDockerfilePage(
            @RequestParam(value = "typeId", required = false, defaultValue = "") String typeId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult = null;
        // 调用service方法
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
            //Sort sort = new Sort(Sort.Direction.DESC, "createTime");
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DockerfileTemplate> templates = null;
            if (StringUtils.isEmpty(typeId)) {
                templates = dockerfileTemplateServiceImpl.getTemplatesPage(pageable);
            } else {
                templates = dockerfileTemplateServiceImpl.getTemplatesPage(typeId, pageable);
            }

            for (DockerfileTemplate template : templates.getContent()) {
                String dockerfileContent = buildDockerfileContent(template.getDockerfileContent());
                String dockerfileContentNew = SafeCode.encode(dockerfileContent);
                template.setDockerfileContent(dockerfileContentNew);
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, templates, "获取dockerfile模板列表成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取dockerfile模板列表失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/dockerfile/type" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取dockerfile模板类型列表", notes = "")
    public ApiResult getDockerfileType() {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            List<DockerfileType> types = dockerfileTemplateServiceImpl.getTypes();
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, types, "获取dockerfile模板类型列表成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取dockerfile模板类型列表失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/lang" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取构建语言版本信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "langType", value = "语言类型 java|go|python|php|nodejs", required = true, dataType = "String") })
    public ApiResult getCiLang(
            @NotEmpty @Pattern(regexp = "^java|go|python|php|nodejs$", message = "语言类型仅支持: java|go|python|php|nodejs") @RequestParam String langType) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            List<Lang> langs = ciServiceImpl.getLangsByType(langType);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, langs, "获取构建语言版本信息成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取构建语言版本信息失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    //    @ResponseBody
    //    @RequestMapping(value = { "/xcloud" }, method = RequestMethod.GET)
    //    @ApiOperation(value = "获取xcloud工具版本信息", notes = "")
    //    @ApiImplicitParams({
    //            @ApiImplicitParam(paramType = "query", name = "execConfigType", value = "Shear工具类型 1:maven|2:ant|3:sonar|4:gradle", required = true, dataType = "int") })
    //    public ApiResult getCiShera(
    //            @NotEmpty @Pattern(regexp = "^1|2|3|4$", message = "构建工具仅支持: 1:maven|2:ant|3:sonar|4:gradle, 参数为对应数字!") @RequestParam String execConfigType) {
    //
    //        ApiResult apiResult = null;
    //
    //        // 调用service方法
    //        try {
    //            List<ExecConfig> execConfig = ciServiceImpl.getExecConfig(Integer.valueOf(execConfigType));
    //
    //            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, execConfig, "获取xcloud工具版本信息成功！");
    //
    //        } catch (ErrorMessageException e) {
    //            LOG.error("获取xcloud工具版本信息失败！", e);
    //            apiResult = new ApiResult(e.getCode(), e.getMessage());
    //        }
    //
    //        return apiResult;
    //    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials" }, method = RequestMethod.POST)
    @ApiOperation(value = "添加认证", notes = "")
    public ApiResult createCredentials(@RequestBody CreateCredentialsDTO credential) {

        ApiResult apiResult;

        // The type of code version control. Gitlab|Github|SVN
        int codeControlType = credential.getCodeControlType();

        String tenantName = credential.getTenantName();
        String userName = credential.getUserName();
        String accessToken = credential.getAccessToken();
        String password = credential.getPassword();
        String registryAddress = credential.getRegistoryAddress();

        // 0:公有|1:私有
        Byte publicOrPrivateFlag = credential.getPublicOrPrivateFlag();

        String projectId = credential.getProjectId();
        String createdBy = credential.getCreatedBy();

        // 参数校验
        apiResult = checkCreateCredentials(codeControlType, tenantName, userName, accessToken, password,
                registryAddress, publicOrPrivateFlag);

        if (null != apiResult) {
            LOG.error("添加认证参数校验失败!", apiResult.getMessage());
            return apiResult;
        }

        // 调用service方法
        try {
            CiCodeCredentials credentials = null;
            if (codeControlType == CiConstant.CODE_TYPE_GITLAB) {
                synchronized (CiConstant.CODE_TYPE_GITLAB_STR) {
                    credentials = ciCodeCredentialsServiceImpl
                            .addGitlabCredentials(tenantName, userName, password, registryAddress, projectId,
                                    createdBy);
                }

            } else if (codeControlType == CiConstant.CODE_TYPE_SVN) {
                synchronized (CiConstant.CODE_TYPE_SVN_STR) {
                    credentials = ciCodeCredentialsServiceImpl
                            .addSvnCredentials(tenantName, userName, password, registryAddress, publicOrPrivateFlag,
                                    projectId, createdBy);
                }
            } else if (codeControlType == CiConstant.CODE_TYPE_GITHUB) {
                synchronized (CiConstant.CODE_TYPE_GITHUB_STR) {
                    credentials = ciCodeCredentialsServiceImpl
                            .addGithubCredentials(tenantName, userName, accessToken, projectId, createdBy);
                }
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, credentials, "添加认证成功！");

        } catch (ErrorMessageException e) {
            LOG.error("添加认证失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    /**
     * 添加认证参数校验
     *
     * @param codeControlType
     * @param tenantName
     * @param userName
     * @param accessToken
     * @param password
     * @param registoryAddress
     * @param publicOrPrivateFlag
     * @return
     */
    private ApiResult checkCreateCredentials(int codeControlType, String tenantName, String userName,
            String accessToken, String password, String registoryAddress, Byte publicOrPrivateFlag) {

        ApiResult apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }

        // github
        if (codeControlType == CiConstant.CODE_TYPE_GITHUB) {
            if (StringUtils.isEmpty(userName)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证用户不能为空！");
            }
            if (StringUtils.isEmpty(accessToken)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "token不能为空！");
            }
        } else if (codeControlType == CiConstant.CODE_TYPE_GITLAB) {
            if (StringUtils.isEmpty(userName)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证用户不能为空！");
            }
            if (StringUtils.isEmpty(password)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证密码不能为空！");
            }
            if (StringUtils.isEmpty(registoryAddress)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "仓库地址不能为空！");
            }

        } else if (codeControlType == CiConstant.CODE_TYPE_SVN) {
            if (StringUtils.isEmpty(registoryAddress)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "仓库地址不能为空！");
            }

            boolean flag = (publicOrPrivateFlag == 0 || publicOrPrivateFlag == 1);
            if (codeControlType == CiConstant.CODE_TYPE_SVN && !flag) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "是否公有不能为空！");
            }
            if (publicOrPrivateFlag == 1) {
                if (StringUtils.isEmpty(userName)) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "认证用户不能为空！");
                }
            }
            if (registoryAddress.startsWith(XcloudProperties.getConfigMap().get(Global.GITHUB_CODE_URL))) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "请填写svn项目地址!");
            }
        } else {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "代码托管工具不符合规范,1:gitlab|2:svn|3:github！");
        }

        return null;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取认证列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目Id", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "codeControlType", value = "代码托管工具,1:gitlab|2:svn|3:github", required = true, dataType = "Byte"), })
    public ApiResult getCredentials(@RequestParam String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @Pattern(regexp = "^1|2|3$", message = "托管工具仅支持: 1:gitlab|2:svn|3:github, 填写对应数字!") @RequestParam(value = "codeControlType", required = true) String codeControlType) {

        ApiResult apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }

        // 调用service方法
        try {
            List<CiCodeCredentials> credentials = ciCodeCredentialsServiceImpl
                    .getCredentials(tenantName, projectId, Byte.valueOf(codeControlType));

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, credentials, "获取认证成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取认证失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/{credentialsId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取认证详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "credentialsId", value = "认证Id", required = true, dataType = "String"), })
    public ApiResult getCredentialsDetail(@PathVariable(value = "credentialsId") String credentialsId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            CiCodeCredentials credentials = ciCodeCredentialsServiceImpl.getById(credentialsId);
            String message = null;
            if (credentials == null) {
                message = "无对应记录! ";
            } else {
                message = "获取认证详情成功! ";
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, credentials, message);

        } catch (ErrorMessageException e) {
            LOG.error("获取认证详情失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/{credentialsId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除认证", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "credentialsId", value = "认证Id", required = true, dataType = "String"), })
    public ApiResult deleteCredentials(@PathVariable(value = "credentialsId") String credentialsId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            boolean result = ciCodeCredentialsServiceImpl.deleteCredentials(credentialsId);

            if (result) {
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "删除认证成功！");
            } else {
                apiResult = new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "删除认证失败！");
            }

        } catch (ErrorMessageException e) {
            LOG.error("删除认证失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/github/repos" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取github项目", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "credentialsId", value = "认证Id", required = true, dataType = "String"), })
    public ApiResult getGitHubRepos(@RequestParam(value = "credentialsId", required = true) String credentialsId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            List<GithubRepos> gitHubRepos = ciCodeCredentialsServiceImpl.getGitHubRepos(credentialsId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, gitHubRepos, "获取github项目成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取github项目失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/github/repos/branch" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取github项目分支", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "credentialsId", value = "认证Id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "reposName", value = "git项目名称", required = true, dataType = "String"), })
    public ApiResult getGitHubBranches(@RequestParam(value = "credentialsId", required = true) String credentialsId,
            @RequestParam(value = "reposName", required = true) String reposName) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            List<GitHubBranch> gitHubBranches = ciCodeCredentialsServiceImpl
                    .getGitHubBranches(credentialsId, reposName);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, gitHubBranches, "获取github项目分支成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取github项目失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/gitlab/repos" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取gitlab项目", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "credentialsId", value = "认证Id", required = true, dataType = "String"), })
    public ApiResult getGitlabRepos(@RequestParam(value = "credentialsId", required = true) String credentialsId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {
            List<GitlabRepos> gitlabRepos = ciCodeCredentialsServiceImpl.getGitlabRepos(credentialsId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, gitlabRepos, "获取gitlab项目成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取gitlab项目失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/gitlab/repos/branch" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取gitlab项目分支", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "credentialsId", value = "认证Id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "reposId", value = "git项目Id", required = true, dataType = "int"), })
    public ApiResult getGitlabBranches(@RequestParam(value = "credentialsId", required = true) String credentialsId,
            @RequestParam(value = "reposId", required = true) int reposId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {

            List<GitlabBranch> gitlabBranches = ciCodeCredentialsServiceImpl.getGitlabBranchs(credentialsId, reposId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, gitlabBranches, "获取gitlab项目分支成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取gitlab项目分支失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/code/credentials/gitlab/repos/tag" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取gitlab项目tag", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "credentialsId", value = "认证Id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "reposId", value = "git项目Id", required = true, dataType = "int"), })
    public ApiResult getGitlabTags(@RequestParam(value = "credentialsId", required = true) String credentialsId,
            @RequestParam(value = "reposId", required = true) int reposId) {

        ApiResult apiResult = null;

        // 调用service方法
        try {

            List<GitlabTag> gitlabTags = ciCodeCredentialsServiceImpl.getGitlabTags(credentialsId, reposId);

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, gitlabTags, "获取gitlab项目标签成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取gitlab项目分支失败！", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/hook" }, method = RequestMethod.POST)
    @ApiOperation(value = "hook触发地址", notes = "")
    public void hook(@Valid @RequestBody HookDTO swHookModel, HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("------------hook触发------------");
        System.out.println("POST info: " + swHookModel.toString());

        if (swHookModel == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String objectKind = swHookModel.getObject_kind();
        String ref = swHookModel.getRef();
        String repName = swHookModel.getRepository().getName();

        List<Ci> ciList = ciServiceImpl.getAllCi();

        // check object_kind push / tag_push
        // push events
        if (objectKind.equals(HOOK_OPERATION_PUSH)) {
            String branch = ref.substring(ref.lastIndexOf("/") + 1);

            for (Ci ci : ciList) {
                if (ci.getHookUsed() != null && ci.getHookUsed().booleanValue()) {
                    String codeInfoId = ci.getCodeInfoId();
                    CodeInfo codeInfo = ciCodeCredentialsServiceImpl.getCodeInfoById(codeInfoId);
                    if (codeInfo != null && codeInfo.getBranchOrTag() == 0 && codeInfo.getCodeReposName()
                            .equals(repName) && codeInfo.getCodeBranch().equals(branch)) {
                        ciServiceImpl.startCi(ci.getId(), ci.getCreatedBy());

                        String msg = String.format("任务:%s被触发", ci.getCiName());
                        System.out.println(msg);
                        LOG.info(msg);
                    }
                }
            }
        } else if (objectKind.equals(HOOK_OPERATION_TAG_PUSH)) {
            // tag events
            String tag = ref.substring(ref.lastIndexOf("/") + 1);

            for (Ci ci : ciList) {
                if (ci.getHookUsed() != null && ci.getHookUsed().booleanValue()) {
                    String codeInfoId = ci.getCodeInfoId();
                    CodeInfo codeInfo = ciCodeCredentialsServiceImpl.getCodeInfoById(codeInfoId);
                    if (codeInfo != null && codeInfo.getBranchOrTag() == 1 && codeInfo.getCodeReposName()
                            .equals(repName)) {

                        // 1.update ci,将ci中的tag更改为新的tag
                        codeInfo.setCodeBranch(tag);
                        ciServiceImpl.modifyCodeCi(ci, codeInfo, ciServiceImpl.getCiFileByCiId(ci.getId()));

                        // 2.execute ci
                        ciServiceImpl.startCi(ci.getId(), ci.getCreatedBy());

                        String msg = String.format("任务:%s被触发", ci.getCiName());
                        System.out.println(msg);
                        LOG.info(msg);
                    }
                }
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @ResponseBody
    @RequestMapping(value = { "/cistatistics/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务构建统计信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult getCiStatistics(@PathVariable(value = "serviceId") String serviceId) {

        ApiResult apiResult = null;

        // 解析参数

        // 调用service方法
        try {

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, ciServiceImpl.getCiStatistics(serviceId),
                    "获取服务构建统计信息成功！");

        } catch (ErrorMessageException e) {
            LOG.error("获取服务构建统计信息失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }
}

