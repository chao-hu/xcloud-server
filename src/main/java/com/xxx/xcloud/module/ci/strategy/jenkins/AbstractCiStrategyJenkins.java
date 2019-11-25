package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.entity.Ci;
import com.xxx.xcloud.module.ci.entity.CiCodeCredentials;
import com.xxx.xcloud.module.ci.entity.CiFile;
import com.xxx.xcloud.module.ci.entity.CodeInfo;
import com.xxx.xcloud.module.devops.common.ScmType;
import com.xxx.xcloud.module.devops.credentials.service.CredentialService;
import com.xxx.xcloud.module.devops.job.service.JobService;
import com.xxx.xcloud.module.devops.model.*;
import com.xxx.xcloud.module.harbor.entity.HarborUser;
import com.xxx.xcloud.module.sonar.entity.CodeCheckTask;
import com.xxx.xcloud.module.ci.model.SonarRule;
import com.xxx.xcloud.utils.SpringContextHolder;
import com.xxx.xcloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 代码构建策略
 *
 * @author mengaijun
 * @date: 2018年12月20日 下午2:41:00
 */
public abstract class AbstractCiStrategyJenkins {

    /**
     * CodeManager对象代码类型
     */
    public static final int CODE_CHOICE_GIT = 1;
    public static final int CODE_CHOICE_SVN = 2;

    protected int type = CiConstant.DEVOPS_LANG_JAVA_INT;

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractCiStrategyJenkins.class);

    /**
     * 生成构建任务
     *
     * @param ci
     * @param codeInfo
     * @param credentials
     * @param harborUser
     * @return Job
     * @date: 2019年7月19日 上午11:27:40
     */
    public Job generateJenkinsCiJob(Ci ci, CodeInfo codeInfo, CiFile ciFile, CiCodeCredentials credentials,
            HarborUser harborUser) {
        Job job = new Job();
        // 设置公共信息
        job.setNamespace(ci.getTenantName());
        job.setName(ci.getCiName());
        job.setDescription(ci.getCiDescription());

        if (ci != null) {
            job.setLanguageModel(generateLanguageModel(ci.getCompile()));
            job.setBuildModel(generateBuildModel(ci.getCompile()));
        }

        job.setScmModel(generateScmModel(codeInfo, credentials));

        job.setDockerModel(generateDockerModel(ci, ciFile, harborUser));

        if (Objects.equals(ci.getHookUsed(), Boolean.TRUE)) {
            job.setHook(Boolean.TRUE);
        }

        return job;
    }

    /**
     * 生成检查任务
     *
     * @param codeCheckTask
     * @param codeInfo
     * @param credentials
     * @param sonarRule
     * @return Job
     * @date: 2019年7月19日 上午11:27:53
     */
    public Job generateJenkinsSonarJob(CodeCheckTask codeCheckTask, CodeInfo codeInfo, CiCodeCredentials credentials,
            SonarRule sonarRule) {
        Job job = new Job();

        job.setNamespace(codeCheckTask.getTenantName());
        job.setName(codeCheckTask.getTaskName());
        String jobType = JobService.JOB_TYPE_SONAR;

        job.setScmModel(generateScmModel(codeInfo, credentials));
        job.setSonarModel(generateSonarModel(sonarRule, job, jobType));

        return job;
    }

    /**
     * 生成pipeline类型job
     *
     * @return Job
     * @date: 2019年7月19日 上午11:45:49
     */
    public Job generateJenkinsPipelineJob() {
        Job job = new Job();

        return job;
    }

    /**
     * 给job添加LanguageModel和BuildModel信息
     *
     * @param job
     * @param compileJsonData
     * @date: 2019年4月15日 下午4:50:02
     */
    public void addLanguageModelAndBuildModel(Job job, String compileJsonData) {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成BuildModel 信息
     *
     * @param compileJsonData
     * @return BuildModel
     * @date: 2019年4月15日 下午4:54:38
     */
    public BuildModel generateBuildModel(String compileJsonData) {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成LanguageModel 信息
     *
     * @param compileJsonData
     * @return LanguageModel
     * @date: 2019年4月15日 下午4:54:38
     */
    public LanguageModel generateLanguageModel(String compileJsonData) {
        throw new UnsupportedOperationException();
    }

    public ScmModel generateScmModel(CodeInfo codeInfo, CiCodeCredentials credentials) {
        if (codeInfo == null || credentials == null) {
            return null;
        }

        ScmModel scmModel = new ScmModel();
        if (credentials.getCodeControlType() == CiConstant.CODE_TYPE_GITLAB) {
            scmModel.setScmType(ScmType.SCM_GIT_LAB);
            GitLabModel gitLabModel = new GitLabModel();
            String codeBranchPre = (codeInfo.getBranchOrTag() == CiConstant.CODE_TAG_TYPE ? "refs/tags/" : "");
            gitLabModel.setBranch(codeBranchPre + codeInfo.getCodeBranch());
            gitLabModel.setUrl(codeInfo.getCodeUrl());
            gitLabModel.setCredentialId(credentials.getUniqueKey());
            scmModel.setGitLabModel(gitLabModel);
        } else if (credentials.getCodeControlType() == CiConstant.CODE_TYPE_GITHUB) {
            scmModel.setScmType(ScmType.SCM_GIT_HUB);
            GitHubModel gitHubModel = new GitHubModel();
            gitHubModel.setBranch(codeInfo.getCodeBranch());
            gitHubModel.setUrl(codeInfo.getCodeUrl());
            gitHubModel.setCredentialId(credentials.getUniqueKey());
            scmModel.setGitHubModel(gitHubModel);
        } else if (credentials.getCodeControlType() == CiConstant.CODE_TYPE_SVN) {
            scmModel.setScmType(ScmType.SCM_SVN);
            SvnModel svnModel = new SvnModel();
            svnModel.setUrl(codeInfo.getCodeUrl());
            svnModel.setCredentialId(credentials.getUniqueKey());
            scmModel.setSvnModel(svnModel);
        }
        return scmModel;
    }

    /**
     * 生成SonarModel
     *
     * @param sonarRule
     * @param job
     * @return SonarModel
     * @date: 2019年4月16日 上午10:16:22
     */
    public SonarModel generateSonarModel(SonarRule sonarRule, Job job, String jobType) {
        if (sonarRule == null) {
            return null;
        }

        SonarModel sonarModel = new SonarModel();
        sonarModel.setProjectName(SpringContextHolder.getBean(JobService.class)
                .generateJenkinsJobName(job.getNamespace(), jobType, job.getName()));
        sonarModel.setProjectKey(sonarModel.getProjectName());
        sonarModel.setProjectVersion("master");

        sonarModel.setLanguage(sonarRule.getLanguage());
        sonarModel.setSources(sonarRule.getPathStr());
        sonarModel.setProfile(sonarRule.getProfileName());

        return sonarModel;
    }

    /**
     * 生成DockerModel
     *
     * @param ci
     * @param harborUser
     * @param ciFile
     * @return DockerModel
     * @date: 2019年4月15日 下午5:11:48
     */
    public DockerModel generateDockerModel(Ci ci, CiFile ciFile, HarborUser harborUser) {
        if (ci == null || harborUser == null) {
            return null;
        }

        DockerModel dockerModel = new DockerModel();

        // 设置dockerfile信息；
        // 之前数据dockerfilepath在ci表，新的数据dockerfilepath在ciFile表；
        // 先判断新的ciFile是否存在，如果存在，使用新的信息；如果不存在，使用旧的信息
        dockerModel.setUserDefined(false);
        dockerModel.setDockerFileContext("");
        if (ciFile != null && !StringUtils.isEmpty(ciFile.getDockerfileContent()) && Objects
                .equals(CiConstant.DOCKERFILE_WRITE_TYPE_WRITE_ONLINE, ciFile.getDockerfileWriteType())) {
            dockerModel.setUserDefined(true);
            dockerModel.setDockerFileContext(ciFile.getDockerfileContent());
        } else if (ciFile != null && !StringUtils.isEmpty(ciFile.getFilePath())) {
            dockerModel.setDockerFileDirectory(ciFile.getFilePath());
        } else if (ciFile == null) {
            dockerModel.setDockerFileContext(ci.getDockerfilePath());
        }

        dockerModel.setPushCredentialsId(
                SpringContextHolder.getBean(CredentialService.class).generatePushCredentialId(harborUser));
        // 构建成功后上传
        dockerModel.setPushOnSuccess(true);
        // 上传完成后清除本地镜像
        dockerModel.setCleanImages(true);
        // 镜像名称, 填写环境变量，在构建的时候传入相应的值
        dockerModel.setImageName(
                StringUtils.transferEnvironmentVariable(XcloudProperties.getConfigMap().get(Global.DEVOPS_PARAM)));

        return dockerModel;
    }


    /**
     * 根据编译json数据，获取编译的工具类型
     *
     * @param compileJsonData
     * @return String
     * @date: 2019年8月6日 下午3:42:45
     */
    protected String getComplieToolType(String compileJsonData) {
        return (String) JSON.parseObject(compileJsonData).get("compileToolType");
    }
}
