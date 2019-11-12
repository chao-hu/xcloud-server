package com.xxx.xcloud.module.ci.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.xxx.xcloud.client.docker.DockerClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.entity.*;
import com.xxx.xcloud.module.ci.model.CiDetail;
import com.xxx.xcloud.module.ci.repository.CiFileRepository;
import com.xxx.xcloud.module.ci.repository.CiRecordRepository;
import com.xxx.xcloud.module.ci.repository.CiRepository;
import com.xxx.xcloud.module.ci.service.CiCodeCredentialsService;
import com.xxx.xcloud.module.ci.service.ICiService;
import com.xxx.xcloud.module.ci.strategy.jenkins.AbstractCiStrategyJenkins;
import com.xxx.xcloud.module.ci.strategy.jenkins.CiStrategyFactoryJenkins;
import com.xxx.xcloud.module.ci.threadpool.CiThreadPool;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.job.service.JobService;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.docker.DockerService;
import com.xxx.xcloud.module.harbor.entity.HarborUser;
import com.xxx.xcloud.module.image.consts.ImageConstant;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import com.xxx.xcloud.module.image.model.ImageDetail;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.quartz.CodeCiJob;
import com.xxx.xcloud.module.quartz.QuartzUtils;
import com.xxx.xcloud.utils.*;
import org.apache.commons.codec.language.bm.Lang;
import org.apache.commons.net.ftp.FTPClient;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mengaijun
 * @Description: 构建Service方法
 * @date: 2018年12月7日 下午5:31:05
 */
@Service
public class ICiServiceImpl implements ICiService {

    private static final Logger LOG = LoggerFactory.getLogger(ICiServiceImpl.class);

    @Autowired
    private CiRepository ciRepository;

    @Autowired
    private CiRecordRepository ciRecordRepository;

    @Autowired
    private CiFileRepository ciFileRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private DockerService dockerService;

    //    @Autowired todo
    //    private ApplicationContext applicationContext;

    @Autowired
    private CiCodeCredentialsService ciCodeCredentialsService;

    @Autowired
    private QuartzUtils quartzUtils;

    @Autowired
    private JobService jobService;

    /**
     * dockerfile构建中断标志, key为 tenantName_ciName_constructionTime拼接的字符串, 存在key
     * 即表示中断此任务的构建
     */
    private ConcurrentHashMap<String, String> dockerfileCiInInterruptFlagMap = new ConcurrentHashMap<String, String>();

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Ci addCodeCi(Ci ci, CodeInfo codeInfo, CiFile ciFile) {
        HarborUser harborUser = imageService.getHarborUserByByTenantName(ci.getTenantName());
        checkAddCiCommon(ci, harborUser);

        CiCodeCredentials credentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
        if (credentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "代码认证信息不存在!");
        }
        if (!credentials.getCodeControlType().equals(codeInfo.getCodeControlType())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "代码认证记录的认证方式与选择认证方式不匹配!");
        }
        if (codeInfo.getCodeControlType() == CiConstant.CODE_TYPE_SVN) {
            if (!credentials.getRegistoryAddress().equals(codeInfo.getCodeUrl())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "代码认证记录的代码地址与填写的代码地址不匹配!");
            }
        }

        if (codeInfo.getCodeControlType() == CiConstant.CODE_TYPE_GITLAB) {
            // 替换代码url域名
            codeInfo.setCodeUrl(
                    StringUtils.replaceUrlDomainName(credentials.getRegistoryAddress(), codeInfo.getCodeUrl()));
        }

        // 先保存
        codeInfo = ciCodeCredentialsService.saveCodeInfo(codeInfo);
        ci.setCodeInfoId(codeInfo.getId());
        ci = saveCi(ci);
        ciFile.setCiId(ci.getId());
        saveCiFile(ciFile);

        // 策略工厂, 生成job
        AbstractCiStrategyJenkins ciStrategy = CiStrategyFactoryJenkins.getCiStrategy(ci.getLang());
        // com.xxx.xcloud.module.devops.model.Job job =
        // ciStrategy.generateJenkinsJob(ci, null, codeInfo, credentials, null,
        // harborUser);
        com.xxx.xcloud.module.devops.model.Job job = ciStrategy
                .generateJenkinsCiJob(ci, codeInfo, ciFile, credentials, harborUser);
        saveJenkinsJob(job, null, JobService.JOB_TYPE_DOCKER);

        // 是否定时构建
        if (!StringUtils.isEmpty(ci.getCron())) {
            addCodeCiQuartz(ci);
        }
        return ci;
    }

    /**
     * @param ci
     * @return String
     * @date: 2019年9月19日 下午4:06:19
     */
    private String generateImageVersion(Ci ci) {
        return generateImageVersion(ci.getImageVersionGenerationStrategy(), ci.getImageVersion(), ci.getId());
    }

    /**
     * @param imageVersionGenerationStrategy
     * @param imageVersion
     * @param ciId
     * @return String
     * @date: 2019年9月18日 下午2:11:18
     */
    private String generateImageVersion(String imageVersionGenerationStrategy, String imageVersion, String ciId) {
        // 之前的都默认按手动生成的策略
        if (StringUtils.isEmpty(imageVersionGenerationStrategy)) {
            imageVersionGenerationStrategy = CiConstant.IMG_VER_GENERATE_STRATEBY_MANUAL;
        }

        if (imageVersionGenerationStrategy.contains(CiConstant.IMG_VER_GENERATE_STRATEBY_MANUAL)) {
            imageVersion = imageVersion == null ? "" : imageVersion;
            if (imageVersionGenerationStrategy.contains(CiConstant.IMG_VER_GENERATE_STRATEBY_AUTOMATIC)) {
                imageVersion += ".";
            }
        } else {
            imageVersion = "";
        }

        if (imageVersionGenerationStrategy.contains(CiConstant.IMG_VER_GENERATE_STRATEBY_AUTOMATIC)) {
            DateFormat format = new SimpleDateFormat("yyyyMMdd");
            String timeInfo = format.format(new Date());

            Long ciCount = ciRecordRepository.countByCiId(ciId);
            imageVersion = imageVersion + timeInfo + "." + (ciCount);
        }

        if (StringUtils.isEmpty(imageVersion)) {
            imageVersion = "latest";
        }

        return imageVersion;
    }

    /**
     * 检测添加构建任务的参数，不通过，抛出异常
     *
     * @param ci
     * @param harborUser
     * @date: 2019年4月24日 上午11:06:11
     */
    private void checkAddCiCommon(Ci ci, HarborUser harborUser) {
        if (getCiByCiNameType(ci.getTenantName(), ci.getCiName(), ci.getCiType()) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "构建名称已经存在!");
        }
        if (!imageService.isImageNameLegal(ci.getImageName())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "镜像名称只能是小写字母, 数字, 横线, 下划线, 且必须以字母开头.");
        }
        if (imageService.isImageExist(ci.getTenantName(), ci.getImageName(), ci.getImageVersion())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名称和版本已经存在!");
        }
        if (getCiByImageNameAndVersion(ci.getTenantName(), ci.getImageName(), ci.getImageVersion()) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名称和版本已经存在!");
        }
        if (!StringUtils.isEmpty(ci.getCron()) && !quartzUtils.isValidExpression(ci.getCron())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "cron表达式不合规范!");
        }
        if (harborUser == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户harbor信息不存在!");
        }
    }

    //    @Override
    //    public List<ExecConfig> getExecConfig(int execConfigType) {
    //        String config = null;
    //        if (execConfigType == CiConstant.EXEC_CONFIG_MAVEN) {
    //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_MAVEN);
    //        } else if (execConfigType == CiConstant.EXEC_CONFIG_ANT) {
    //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_ANT);
    //        } else if (execConfigType == CiConstant.EXEC_CONFIG_GRADLE) {
    //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_GRADLE);
    //        } else {
    //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_ANT);
    //        }
    //
    //        String[] configArr = config.split(",");
    //        List<ExecConfig> execConfigs = new ArrayList<>(configArr.length);
    //        for (String c : configArr) {
    //            ExecConfig execConfig = new ExecConfig();
    //            execConfig.setVersion(c.trim());
    //            execConfigs.add(execConfig);
    //        }
    //        return execConfigs;
    //    }

    /**
     * 删除Jenkins端的job
     *
     * @param namespace
     * @param jobName
     * @date: 2019年4月16日 下午3:08:37
     */
    public void deleteJenkinsJob(String namespace, String jobType, String jobName) {
        try {
            // 如果job已经不存在，当成删除了
            if (jobService.getJob(namespace, jobType, jobName) == null) {
                return;
            }
            jobService.delete(namespace, jobType, jobName);
        } catch (DevopsException e) {
            LOG.error("删除Jenkins端job失败！", e);
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        }
    }

    @Override
    public boolean addCodeCiQuartz(Ci ci) {
        Map<String, Object> parameters = new HashMap<String, Object>(16);
        parameters.put("id", ci.getId());
        parameters.put("createdBy", ci.getCreatedBy());
        boolean isSuccess = false;
        try {
            quartzUtils.addJob(ci.getCiName(), getCodeCiQuartzGroup(ci.getTenantName()), CodeCiJob.class, ci.getCron(),
                    parameters);
            isSuccess = true;
        } catch (SchedulerException e) {
            LOG.error("代码检查添加定时任务失败!", e);
        } catch (Exception e) {
            LOG.error("代码检查添加定时任务失败!", e);
        }
        return isSuccess;
    }

    /**
     * 修改代码构建定时任务
     *
     * @param ci
     * @date: 2019年1月3日 下午2:54:49
     */
    private void modifyCodeCiQuartz(Ci ci) {
        try {
            quartzUtils.modifyTime(ci.getCiName(), getCodeCiQuartzGroup(ci.getTenantName()), ci.getCron());
        } catch (SchedulerException e) {
            LOG.error("构建修改定时任务失败!", e);
        } catch (Exception e) {
            LOG.error("构建修改定时任务失败!", e);
        }
    }

    /**
     * 移除定时任务
     *
     * @param ci
     * @date: 2019年1月3日 下午3:38:57
     */
    private boolean removeCodeCiQuartz(Ci ci) {
        try {
            quartzUtils.removeJob(ci.getCiName(), getCodeCiQuartzGroup(ci.getTenantName()));
            return true;
        } catch (SchedulerException e) {
            LOG.error("移除修改定时任务失败!", e);
        } catch (Exception e) {
            LOG.error("移除修改定时任务失败!", e);
        }
        return false;
    }

    /**
     * 获取构建任务所属定时组
     *
     * @param tenantName
     * @return String
     * @date: 2019年1月3日 下午5:16:18
     */
    private String getCodeCiQuartzGroup(String tenantName) {
        return CiConstant.CODE_CI_QUARTZ_GROUP + tenantName;
    }

    @Override
    public List<Lang> getLangsByType(String langType) {
        //        String config = null;
        //        if (CiConstant.DEVOPS_LANG_JAVA.equals(langType)) {
        //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_JDK);
        //        } else if (CiConstant.DEVOPS_LANG_GO.equals(langType)) {
        //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_GO);
        //        } else if (CiConstant.DEVOPS_LANG_NODEJS.equals(langType)) {
        //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_NODEJS);
        //        } else if (CiConstant.DEVOPS_LANG_PYTHON.equals(langType)) {
        //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_PYTHON);
        //        } else {
        //            config = XcloudProperties.getConfigMap().get(Global.DEVOPS_GLOBAL_CONFIGURE_GO);
        //        }
        //
        //        String[] configArr = config.split(",");
        //        List<Lang> langs = new ArrayList<>(configArr.length);
        //        for (String c : configArr) {
        //            Lang lang = new Lang();
        //            lang.setVersion(c.trim());
        //            langs.add(lang);
        //        }
        //        return langs;
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Ci addDockerfileCi(Ci ci, CiFile ciFile) {

        HarborUser harborUser = imageService.getHarborUserByByTenantName(ci.getTenantName());
        checkAddCiCommon(ci, harborUser);

        // 保存Ci表
        ci = saveCi(ci);

        // 保存CiFile表
        ciFile.setCiId(ci.getId());
        saveCiFile(ciFile);

        return ci;
    }

    @Override
    public Ci getCiByCiNameType(String tenantName, String ciName, Byte ciType) {
        try {
            if (ciType != null) {
                return ciRepository.getByCiNameAndType(tenantName, ciName, ciType);
            }
            return ciRepository.getByCiName(tenantName, ciName);
        } catch (Exception e) {
            LOG.error("查询构建项目是否存在错误: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询该该构建名称是否已经存在错误!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean startCi(String ciId, String createdBy) {
        // 查询记录
        Ci ci = getCiForUpdate(ciId);
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "当前记录不存在!");
        }
        if (CiConstant.CONSTRUCTION_STATUS_ING == ci.getConstructionStatus()) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "项目正在构建中, 无法构建!");
        }
        if (CiConstant.CONSTRUCTION_STATUS_DISABLED == ci.getConstructionStatus()) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "项目被禁用，无法构建！");
        }

        CiFile ciFile = null;
        CodeInfo codeInfo = null;
        String codeBaseName = null;
        if (ci.getCiType() == CiConstant.TYPE_DOCKERFILE) {
            ciFile = getCiFileByCiId(ciId);
            if (ciFile == null) {
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "当前记录文件信息不存在!");
            }
        } else {
            codeInfo = ciCodeCredentialsService.getCodeInfoById(ci.getCodeInfoId());
            if (codeInfo == null) {
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "当前记录代码信息不存在!");
            }
            CiCodeCredentials credentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
            if (credentials == null) {
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "当前记录代码认证信息不存在, 请修改重新选择认证!");
            }
            codeBaseName = generateCodeBaseName(credentials, codeInfo, ci.getTenantName());
        }

        // 如果构建已经生成镜像, 需要按镜像信息堆到对应仓库（如：镜像可能从私有改为公有，再次构建，就需要推送到公有仓库）
        // 如果使用自动生成镜像版本，不会存在重复的镜像版本，就不需要查询判断了
        ImageDetail imageDetail = getImageDetailByCi(ci, codeBaseName);

        // 更新构建状态为构建中, 添加本次构建日志记录
        Date startConstructTime = new Date();
        CiRecord ciRecord = new CiRecord(ci.getId(), ci.getImageName(), ci.getImageVersion(), startConstructTime,
                CiConstant.CONSTRUCTION_STATUS_ING, createdBy);
        ci.setConstructionTime(startConstructTime);
        ci.setConstructionStatus(CiConstant.CONSTRUCTION_STATUS_ING);
        saveCi(ci);
        saveCiRecord(ciRecord);

        // 异步启动构建
        if (ci.getCiType() == CiConstant.TYPE_DOCKERFILE) {
            CiThreadPool.getExecotur().execute(new StartDockerFileCiThread(ci, ciRecord, ciFile, imageDetail));
        } else {
            CiThreadPool.getExecotur().execute(new StartCodeCiThread(ci, ciRecord, imageDetail, codeBaseName));
        }

        return true;
    }

    /**
     * 获取镜像详情
     *
     * @param ci
     * @param codeBaseName
     * @return ImageDetail
     * @date: 2019年9月19日 下午3:30:19
     */
    private ImageDetail getImageDetailByCi(Ci ci, String codeBaseName) {
        // 如果构建已经生成镜像, 需要按镜像信息堆到对应仓库（如：镜像可能从私有改为公有，再次构建，就需要推送到公有仓库）
        // 如果使用自动生成镜像版本，不会存在重复的镜像版本，就不需要查询判断了
        ImageDetail imageDetail = null;
        if (StringUtils.isEmpty(ci.getImageVersionGenerationStrategy()) || (!ci.getImageVersionGenerationStrategy()
                .contains(CiConstant.IMG_VER_GENERATE_STRATEBY_AUTOMATIC))) {
            String imageName = ci.getImageName();
            imageDetail = imageService
                    .getDetailByTenantNameImageNameAndImageVersoin(ci.getTenantName(), imageName, ci.getImageVersion());
            if (imageDetail.getImage() != null) {
                // 最新镜像版本
                imageDetail.getImage().setImageVersion(generateImageVersion(ci));
            }
            if (imageDetail.getImageVersion() != null) {
                // 设置新的环境变量
                imageDetail.getImageVersion().setEnvVariables(ci.getEnvVariables());
                imageDetail.getImageVersion().setCodeBaseName(codeBaseName);
            }
        }
        if (imageDetail == null) {
            imageDetail = new ImageDetail();
        }

        return imageDetail;
    }

    /**
     * 拼接代码仓库名称信息
     *
     * @param ciCodeCredentials
     * @param codeInfo
     * @param tenantName
     * @return String
     * @date: 2019年8月16日 上午10:41:57
     */
    public static String generateCodeBaseName(CiCodeCredentials ciCodeCredentials, CodeInfo codeInfo,
            String tenantName) {
        if (Objects.equals(ciCodeCredentials.getCodeControlType(), CiConstant.CODE_TYPE_GITLAB)) {
            Byte branchOrTag =
                    codeInfo.getBranchOrTag() == null ? CiConstant.CODE_BRANCH_TYPE : codeInfo.getBranchOrTag();
            return tenantName + "-" + ciCodeCredentials.getCodeControlType() + "-" + ciCodeCredentials
                    .getRegistoryAddress() + "-" + ciCodeCredentials.getUserName() + "-" + codeInfo.getCodeReposId()
                    + "-" + branchOrTag + "-" + codeInfo.getCodeBranch();
        }

        if (Objects.equals(ciCodeCredentials.getCodeControlType(), CiConstant.CODE_TYPE_GITHUB)) {
            Byte branchOrTag =
                    codeInfo.getBranchOrTag() == null ? CiConstant.CODE_BRANCH_TYPE : codeInfo.getBranchOrTag();
            return tenantName + "-" + ciCodeCredentials.getCodeControlType() + "-" + ciCodeCredentials
                    .getRegistoryAddress() + "-" + ciCodeCredentials.getUserName() + "-" + codeInfo.getCodeReposName()
                    + "-" + branchOrTag + "-" + codeInfo.getCodeBranch();
        }

        // svn拼接
        return tenantName + "-" + ciCodeCredentials.getCodeControlType() + "-" + ciCodeCredentials.getRegistoryAddress()
                + ciCodeCredentials.getUserName();

    }

    @Override
    public void restartCi(Ci ci) {
        if (ci.getCiType() == CiConstant.TYPE_CODE) {
            restartCodeCi(ci);
            return;
        }

        restartDockerfileCi(ci);
    }

    /**
     * 重启dockerfile构建任务
     *
     * @param ci void
     * @date: 2019年6月19日 上午11:24:29
     */
    private void restartDockerfileCi(Ci ci) {
        // 更新构建状态为失败
        ci.setConstructionStatus(CiConstant.CONSTRUCTION_STATUS_FAIL);
        saveCi(ci);
        CiRecord ciRecord = getCiRecordLatest(ci.getId());
        ciRecord.setConstructionResult(CiConstant.CONSTRUCTION_STATUS_FAIL);
        saveCiRecord(ciRecord);

        // 再次启动
        SpringContextHolder.getBean(ICiService.class).startCi(ci.getId(), ciRecord.getCreatedBy());
    }

    /**
     * 重启代码构建任务
     *
     * @param ci void
     * @date: 2019年6月19日 上午11:24:41
     */
    private void restartCodeCi(Ci ci) {
        // 获取构建日志记录
        CiRecord ciRecord = getCiRecordLatest(ci.getId());

        // 镜像信息
        String imageName = ci.getImageName();
        ImageDetail imageDetail = imageService
                .getDetailByTenantNameImageNameAndImageVersoin(ci.getTenantName(), imageName, ci.getImageVersion());

        if (imageDetail.getImage() != null) {
            imageDetail.getImage().setImageVersion(ci.getImageVersion());
        }
        CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(ci.getCodeInfoId());
        if (codeInfo == null) {
            LOG.error("重启构建任务失败，代码信息不存在!" + ci.getTenantName() + "-" + ci.getCiName());
            return;
        }
        CiCodeCredentials ciCodeCredentials = ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId());
        if (ciCodeCredentials == null) {
            LOG.error("重启构建任务失败，认证信息不存在!" + ci.getTenantName() + "-" + ci.getCiName());
            return;
        }

        // 启动
        CiThreadPool.getExecotur().execute(new StartCodeCiThread(ci, ciRecord, imageDetail,
                generateCodeBaseName(ciCodeCredentials, codeInfo, ci.getTenantName())));
    }

    /**
     * 根据Ci对象生成中断构建标记
     *
     * @param ci
     * @return String tenantName_ciName_constructionTime拼接的字符串
     * @date: 2018年12月18日 下午3:11:58
     */
    private String generateDockerfileCiInterruptFlag(Ci ci) {
        return ci.getTenantName() + "_" + ci.getCiName() + "_" + DateUtil
                .format2String(ci.getConstructionTime(), DateUtil.DEFAULT_TIME_PATTERN);
    }

    /**
     * @author mengaijun
     * @Description: dockerfile构建线程类
     * @date: 2018年12月14日 下午8:09:25
     */
    private class StartDockerFileCiThread implements Runnable {

        private Ci ci;
        private CiRecord ciRecord;
        private CiFile ciFile;
        private ImageDetail imageDetail;
        /**
         * 本次构建中断标记
         */
        private String interruptFlag;

        public StartDockerFileCiThread(Ci ci, CiRecord ciRecord, CiFile ciFile, ImageDetail imageDetail) {
            this.ci = ci;
            this.ciRecord = ciRecord;
            this.ciFile = ciFile;
            this.imageDetail = imageDetail;
            String imageVersionStr = generateImageVersion(ci.getImageVersionGenerationStrategy(), ci.getImageVersion(),
                    ci.getId());
            if (imageDetail.getImage() == null) {
                // 默认私有
                Image image = new Image(ci.getTenantName(), ci.getConstructionTime(), ImageConstant.IMAGE_TYPE_PRIVATE,
                        ci.getImageName(), null, imageVersionStr, 0.0, ImageConstant.IMAGE_CI_TYPE_DOCKERFILE,
                        ci.getCreatedBy(), ci.getProjectId());
                imageDetail.setImage(image);
            } else { // 设置最新镜像信息
                imageDetail.getImage().setImageVersion(imageVersionStr);
                imageDetail.getImage().setCiType(ImageConstant.IMAGE_CI_TYPE_DOCKERFILE);
                imageDetail.getImage().setCreatedBy(ci.getCreatedBy());
                imageDetail.getImage().setCreateTime(ci.getConstructionTime());
                imageDetail.getImage().setProjectId(ci.getProjectId());
            }
            if (imageDetail.getImageVersion() == null) {
                ImageVersion imageVersionObj = new ImageVersion();
                imageVersionObj.setImageVersion(imageVersionStr);
                imageVersionObj.setImageType(ImageConstant.IMAGE_TYPE_PRIVATE);
                imageVersionObj.setEnvVariables(ci.getEnvVariables());
                imageDetail.setImageVersion(imageVersionObj);
            }

            interruptFlag = generateDockerfileCiInterruptFlag(ci);
        }

        /**
         * 根据harbor用户, 生成对应DockerClient客户端
         *
         * @param harborUser
         * @return DockerClient
         * @date: 2019年1月17日 上午10:11:02
         */
        private DockerClient getDockerClient(HarborUser harborUser) {
            DockerClient dockerClient = null;
            if (harborUser != null) {
                try {
                    dockerClient = DockerClientFactory
                            .getDockerClientInstance(harborUser.getUsername(), harborUser.getPassword(),
                                    harborUser.getEmail());
                } catch (ErrorMessageException e) {
                    addLogPrint(ciRecord, "get docker client fail!");
                    LOG.error("获取docker客户端失败!" + e);
                }
            }
            return dockerClient;
        }

        /**
         * 生成了dockerfile, 获取租户的harbor用户信息; 没有生成dockerfile, 就不获取了
         *
         * @param tenantName           租户
         * @param isGenerateDockerfile 是否生成了dockerfile
         * @return HarborUser
         * @date: 2019年1月25日 上午10:07:58
         */
        private HarborUser getHarborUserByByTenantName(String tenantName, boolean isGenerateDockerfile) {
            HarborUser harborUser = null;
            if (isGenerateDockerfile) {
                try {
                    harborUser = imageService.getHarborUserByByTenantName(tenantName);
                } catch (ErrorMessageException e) {
                }
            }
            return harborUser;
        }

        @Override
        public void run() {
            // 等待CiRecord保存到数据库
            if (!isCiRecordUpdateSuccess(ciRecord.getId())) {
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "本次构建日志记录保存失败！");
            }

            addLogPrint(ciRecord, "start.");
            // 从ftp下载上传文件到本地
            String localPath =
                    XcloudProperties.getConfigMap().get(Global.CI_IMAGE_TEMP_PATH) + ci.getTenantName() + File.separator
                            + ci.getImageName() + File.separator + ci.getImageVersion();
            boolean isDownload = FtpUtils.downloadFtpFiles(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                    XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                    XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                    StringUtils.parseInt(XcloudProperties.getConfigMap().get(Global.FTP_PORT)), ciFile.getFilePath(),
                    localPath, ciFile.getFileName().split(","));
            if (!isDownload) {
                addLogPrint(ciRecord, "upload file search failed!");
            } else {
                addLogPrint(ciRecord, "download file success!");
            }

            // 根据Dockerfile内容生成文件
            boolean isGenerateDockerfile = false;
            if (isDownload) {
                isGenerateDockerfile = FileUtils
                        .writeContentToFile(ciFile.getDockerfileContent(), localPath + File.separator + "Dockerfile");
            }
            if (isDownload && !isGenerateDockerfile) {
                addLogPrint(ciRecord, "generate Dockerfile failed!");
            }

            // 获取租户的harbor用户名密码
            HarborUser harborUser = getHarborUserByByTenantName(ci.getTenantName(), isGenerateDockerfile);
            if (harborUser == null) {
                addLogPrint(ciRecord, "harbor user is not exist!");
            }

            // 下载文件和生成Dockerfile成功了, 继续向下走, 获取docker client
            DockerClient dockerClient = getDockerClient(harborUser);
            LOG.info("***************生成dockerClient" + dockerClient + "***********");

            // 获取dockerClient成功, 并且没有中断, 构建镜像
            String imageId = null;
            if (dockerClient != null && !dockerfileCiInInterruptFlagMap.containsKey(interruptFlag)) {
                imageId = buildImage(dockerClient, localPath);
            }

            // 构建成功, 并且没有中断, tag镜像
            boolean isTagSuccess = false;
            if (imageId != null && !dockerfileCiInInterruptFlagMap.containsKey(interruptFlag)) {
                isTagSuccess = tagImage(dockerClient, imageId);
            }

            // tag成功, 并且没有中断, push镜像
            boolean isPushSuccess = false;
            if (isTagSuccess && !dockerfileCiInInterruptFlagMap.containsKey(interruptFlag)) {
                isPushSuccess = pushImage(dockerClient);
                LOG.info("***************推送镜像完成: " + isPushSuccess + "***********");
            }

            // 如果isPushSuccess为true, 就是构建成功了, 数据库状态为成功; 为false, 就是构建失败了,
            // 数据库状态为失败
            byte constructionStatus = CiConstant.CONSTRUCTION_STATUS_FAIL;
            // 生成的新的镜像的信息, 如果上传失败, 为null
            ImageDetail addImageDetail = null;
            if (isPushSuccess) {
                InspectImageResponse inspectImageResponse = inspectImage(imageId, dockerClient);
                if (inspectImageResponse != null) {
                    imageService.setImageInspectInfo(imageDetail.getImage(), inspectImageResponse);
                }
                constructionStatus = CiConstant.CONSTRUCTION_STATUS_SUCCESS;
                addImageDetail = imageDetail;
            } else if (!isPushSuccess && dockerfileCiInInterruptFlagMap.containsKey(interruptFlag)) {
                // 如果push镜像失败, 且设置了中断标志, 就当成是中断导致的失败
                addLogPrint(ciRecord, "dockerfile ci interrupt!");
            }

            // 如果镜像Id不为空, 删除本地镜像
            if (imageId != null) {
                removeImage(dockerClient, imageId);
            }
            // 更新数据库状态
            try {
                updateCiStatus(constructionStatus, ci, ciRecord, addImageDetail);
                LOG.info("******************修改构建结果完成*************");
            } catch (ErrorMessageException e) {
            }

            // 清除中断标记
            if (dockerfileCiInInterruptFlagMap.containsKey(interruptFlag)) {
                dockerfileCiInInterruptFlagMap.remove(interruptFlag);
            }

            // 构建完成, 删除本地下载的文件
            FileUtils.delFolder(localPath);

            if (dockerClient != null) {
                try {
                    dockerClient.close();
                } catch (IOException e) {
                    LOG.error("docker客户端关闭失败!", e);
                }
            }
        }

        /**
         * 查询镜像信息
         *
         * @param imageId
         * @param dockerClient
         * @return InspectImageResponse
         * @date: 2019年3月22日 下午6:05:11
         */
        InspectImageResponse inspectImage(String imageId, DockerClient dockerClient) {
            try {
                return dockerService.inspectImage(imageId, dockerClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 构建镜像, 返回镜像ID
         *
         * @param dockerClient docker客户端
         * @param localPath    dockerfile和上传文件所在路径
         * @return String 镜像ID
         * @date: 2018年12月17日 下午4:54:52
         */
        private String buildImage(DockerClient dockerClient, String localPath) {
            String imageId = null;
            try {
                imageId = dockerService.buildImage(localPath, ciRecord, dockerClient);
                addLogPrint(ciRecord, "docker build complete. image id: " + imageId);
            } catch (Exception e) {
                LOG.error("docker build失败: " + e.getMessage(), e);
                addLogPrint(ciRecord, "docker build fail!" + e.getMessage());
            }
            return imageId;
        }

        /**
         * 给镜像重命名, 设置isTagSuccess标志为true
         *
         * @param dockerClient
         * @param imageId
         * @return boolean 是否tag成功
         * @date: 2018年12月17日 下午4:58:16
         */
        private boolean tagImage(DockerClient dockerClient, String imageId) {
            boolean result = false;
            try {
                dockerService.tagImage(imageId, imageDetail.getImage(), imageDetail.getImageVersion(), dockerClient);
                addLogPrint(ciRecord, "docker tag " + ci.getImageName() + ":" + ci.getImageVersion() + " complete.");
                result = true;
            } catch (ErrorMessageException e) {
                addLogPrint(ciRecord, "docker tag fail! ");
            }
            return result;
        }

        /**
         * 推送镜像到仓库, 如果推送成功, 设置推送标志为true
         *
         * @param dockerClient
         * @return boolean
         * @date: 2018年12月17日 下午5:03:05
         */
        private boolean pushImage(DockerClient dockerClient) {
            boolean isPushSuccess = false;
            try {
                dockerService.pushImage(imageDetail, ciRecord, dockerClient);
                addLogPrint(ciRecord, "docker push complete!");
                isPushSuccess = true;
            } catch (Exception e) {
                LOG.error("docker push失败: " + e.getMessage(), e);
                addLogPrint(ciRecord, "docker push fail! " + e.getMessage());
            }
            return isPushSuccess;
        }

        /**
         * 根据镜像ID删除本地镜像
         *
         * @param dockerClient
         * @param imageId
         * @date: 2018年12月17日 下午5:26:28
         */
        private void removeImage(DockerClient dockerClient, String imageId) {
            try {
                dockerService.removeImage(imageId, dockerClient);
                addLogPrint(ciRecord, "docker remove " + imageId + " complete!");
            } catch (ErrorMessageException e) {
                addLogPrint(ciRecord, "docker remove " + imageId + " fail!");
            }
        }

    }

    /**
     * 等待CiRecord记录保存完成，等待5s
     *
     * @param id
     * @return boolean 是否保存成功
     * @date: 2019年4月11日 下午3:39:41
     */
    boolean isCiRecordUpdateSuccess(String id) {
        boolean isCiRecordSaveSuccess = false;
        int num = 10;
        for (int i = 0; i < num; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (getCiRecordById(id) != null) {
                isCiRecordSaveSuccess = true;
                break;
            }
        }
        return isCiRecordSaveSuccess;
    }

    /**
     * 构建完成, 更新构建状态
     *
     * @param constructionStatus 构建结果
     * @param ci                 要更新的Ci对象
     * @param ciRecord           要更新的CiRecord对象
     * @param imageDetail        要更新的镜像信息
     * @date: 2018年12月22日 下午5:21:13
     */
    private void updateCiStatus(byte constructionStatus, Ci ci, CiRecord ciRecord, ImageDetail imageDetail) {
        Date endTime = new Date();
        Date startTime = ci.getConstructionTime();
        int constructionDuration = new Long((endTime.getTime() - startTime.getTime()) / 1000).intValue();
        // 设置构建状态
        ci.setConstructionStatus(constructionStatus);
        ciRecord.setConstructionResult(constructionStatus);
        // 设置构建持续时间
        ci.setConstructionDuration(constructionDuration);
        ciRecord.setConstructionDuration(constructionDuration);
        // 更新构建状态
        try {
            //todo
            //            applicationContext.getBean(ICiService.class)
            //                    .updateCiInfoTransactional(ci, null, null, ciRecord, imageDetail);
        } catch (Exception e) {
            LOG.error("构建完成后, 更新数据库构建状态失败! " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "构建完成后, 更新数据库构建状态失败!");
        }
    }

    /**
     * @author mengaijun
     * @Description: 代码构建线程
     * @date: 2018年12月22日 下午4:19:58
     */
    public class StartCodeCiThread implements Runnable {
        private Ci ci;
        private CiRecord ciRecord;
        private ImageDetail imageDetail;
        private String logBeginStr = "start!";

        public StartCodeCiThread(Ci ci, CiRecord ciRecord, ImageDetail imageDetail, String codeBaseName) {
            super();
            this.ci = ci;
            this.ciRecord = ciRecord;
            // 代码构建默认 镜像类型为私有
            this.imageDetail = imageDetail;
            String imageVersionStr = generateImageVersion(ci.getImageVersionGenerationStrategy(), ci.getImageVersion(),
                    ci.getId());
            if (imageDetail.getImage() == null) {
                Image image = new Image(ci.getTenantName(), ci.getConstructionTime(), ImageConstant.IMAGE_TYPE_PRIVATE,
                        ci.getImageName(), null, imageVersionStr, 0.0, ImageConstant.IMAGE_CI_TYPE_CODECI,
                        ci.getCreatedBy(), ci.getProjectId());
                imageDetail.setImage(image);
            } else { // 设置最新镜像信息
                imageDetail.getImage().setImageVersion(imageVersionStr);
                imageDetail.getImage().setCiType(ImageConstant.IMAGE_CI_TYPE_CODECI);
                imageDetail.getImage().setCreatedBy(ci.getCreatedBy());
                imageDetail.getImage().setCreateTime(ci.getConstructionTime());
                imageDetail.getImage().setProjectId(ci.getProjectId());
            }
            if (imageDetail.getImageVersion() == null) {
                ImageVersion imageVersion = new ImageVersion();
                imageVersion.setImageVersion(imageVersionStr);
                imageVersion.setImageType(ImageConstant.IMAGE_TYPE_PRIVATE);
                imageVersion.setEnvVariables(ci.getEnvVariables());
                imageVersion.setCodeBaseName(codeBaseName);
                imageDetail.setImageVersion(imageVersion);
            }

        }

        /**
         * 在CiRecord保存本次执行的ID信息
         *
         * @param isStartSuccess 是否启动本次执行成功
         * @param number
         * @date: 2019年1月3日 下午6:20:14
         */
        private void saveConstructionId(boolean isStartSuccess, int number) {
            if (isStartSuccess) {
                // 如果生成镜像
                ciRecord.setSheraConstructionId(number);
                try {
                    ciRecordRepository.save(ciRecord);
                } catch (Exception e) {
                    LOG.error("更新CiRecord的ConstructionId字段失败!", e);
                }
            }
        }

        /**
         * 等到 job执行完成, 判断是否执行成功
         *
         * @param isStartSuccess xcloud job是否启动成功
         * @param seqNo
         * @param tenantName     租户
         * @param jobName        job名称
         * @param jobType
         * @return boolean 是否执行成功
         * @date: 2019年1月3日 下午6:34:57
         */
        private boolean isJobSuccess(boolean isStartSuccess, Integer seqNo, String tenantName, String jobName,
                String jobType) {
            if (!isStartSuccess) {
                return false;
            }

            boolean isCiSuccess = false;
            int count = 0;
            while (true) {
                try {
                    Thread.sleep(Integer.valueOf(
                            XcloudProperties.getConfigMap().get(Global.XCLOUD_CI_CHECK_RESULT_INTERVAL_TIME)));
                } catch (InterruptedException e) {
                    LOG.error("等待构建完成线程休眠被中断!", e);
                }

                // 获取执行信息，如果出错，当成执行失败
                Build build = null;
                BuildResult buildResult = null;
                String logContent = "";
                try {
                    build = jobService.getBuild(tenantName, jobType, jobName, seqNo);
                    if (null != build && null != build.details()) {
                        buildResult = build.details().getResult();
                        // 生成镜像时，需要日志信息
                        logContent = build.details().getConsoleOutputText();
                    }
                } catch (Exception e) {
                    LOG.error("jenkins构建时获取job执行信息失败!", e);
                    ciRecord.setLogPrint(ciRecord.getLogPrint() + "\r\n" + "获取job执行信息错误！");
                    try {
                        saveCiRecord(ciRecord);
                    } catch (ErrorMessageException e1) {
                        LOG.error("代码构建, 添加日志信息, 保存数据库失败! ", e1.getMessage());
                    }
                    // 如果获取构建信息失败，当成失败
                    break;
                }

                if (!StringUtils.isEmpty(logContent)) {
                    ciRecord.setLogPrint(logBeginStr + "\r\n" + logContent);
                    try {
                        saveCiRecord(ciRecord);
                    } catch (ErrorMessageException e) {
                        LOG.error("代码构建, 添加日志信息, 保存数据库失败! ", e.getMessage());
                    }
                }

                if (buildResult != null && !buildResult.equals(BuildResult.BUILDING) && !buildResult
                        .equals(BuildResult.REBUILDING)) {
                    isCiSuccess = buildResult.equals(BuildResult.SUCCESS);
                    break;
                }

                // 等待超时, 180 * 5 s, 当成构建完成, 但结果是失败处理
                count++;
                if (count > Integer
                        .valueOf(XcloudProperties.getConfigMap().get(Global.XCLOUD_CI_CHECK_RESULT_TIMEOUT_COUNT))) {
                    ciRecord.setLogPrint(ciRecord.getLogPrint() + "\r\n" + "构建超时！");
                    try {
                        saveCiRecord(ciRecord);
                    } catch (ErrorMessageException e1) {
                        LOG.error("代码构建, 添加日志信息, 保存数据库失败! ", e1.getMessage());
                    }
                    break;
                }
            }

            return isCiSuccess;
        }

        /**
         * 初始化本次构建记录（等待CiRecord存入数据库，添加第一行“start”日志） void
         *
         * @date: 2019年5月20日 下午4:34:02
         */
        private void initCiRecord() {
            if (!isCiRecordUpdateSuccess(ciRecord.getId())) {
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "本次构建日志记录保存失败！");
            }
            // 记录线程启动的日志
            try {
                ciRecord.setLogPrint(logBeginStr);
                ciRecordRepository.save(ciRecord);
            } catch (Exception e) {
                LOG.error("代码构建, 添加日志信息, 保存数据库失败! ", e.getMessage());
            }
        }

        /**
         * 启动任务，获取启动编号
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
                String imageName = imageService
                        .getRegistryImageName(imageDetail.getImage(), imageDetail.getImageVersion());

                // 启动构建时传入镜像名称参数
                Map<String, String> params = new HashMap<>(2);
                params.put(XcloudProperties.getConfigMap().get(Global.DEVOPS_PARAM), imageName);
                number = jobService.build(tenantName, jobType, jobName, params);
            } catch (Exception e) {
                LOG.error("启动构建失败!" + e.getMessage(), e);
                try {
                    ciRecord.setLogPrint(logBeginStr + "\r\n" + "start Jenkins job failed！" + e.getMessage());
                    ciRecordRepository.save(ciRecord);
                } catch (Exception saveE) {
                    LOG.error("代码构建, 添加日志信息, 保存数据库失败! ", saveE.getMessage());
                }
            }
            return number;
        }

        @Override
        public void run() {
            String jobName = ci.getCiName();
            String tenantName = ci.getTenantName();
            String jobType = JobService.JOB_TYPE_DOCKER;
            int number = -1;
            boolean isStartSuccess = false;
            // 如果是项目重启，导致线程中断，重新获取Jenkins构建信息
            if (ciRecord.getSheraConstructionId() != null) {
                number = ciRecord.getSheraConstructionId();
                isStartSuccess = true;
            } else {
                // 等待构建记录日志入库，并添加第一行日志
                initCiRecord();
                // 启动构建
                number = startJob(tenantName, jobType, jobName);
                isStartSuccess = number > 0 ? true : false;

                // 启动成功, 获得构建seqNo, 保存
                saveConstructionId(isStartSuccess, number);
            }

            // 启动成功, 且没有构建完成, 不断进行判断
            boolean isCiSuccess = isJobSuccess(isStartSuccess, number, tenantName, jobName, jobType);

            // 构建完成, 判断构建状态
            byte constructionStatus = CiConstant.CONSTRUCTION_STATUS_SUCCESS;
            if (!isCiSuccess) {
                constructionStatus = CiConstant.CONSTRUCTION_STATUS_FAIL;
                // 构建失败，不更新镜像信息
                imageDetail = null;
            }
            if (isCiSuccess) {
                // 构建成功，查询查询镜像大小和开放端口信息
                try {
                    InspectImageResponse inspectImageResponse = imageService
                            .getInspectImageResponse(imageDetail.getImage(), imageDetail.getImageVersion());
                    if (inspectImageResponse != null) {
                        imageService.setImageInspectInfo(imageDetail.getImage(), inspectImageResponse);
                    }
                } catch (Exception e) {
                    LOG.error("获取镜像大小和端口信息错误", e);
                }
            }

            // 构建镜像, 设置镜像信息
            try {
                updateCiStatus(constructionStatus, ci, ciRecord, imageDetail);
            } catch (ErrorMessageException e) {
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCiInfoTransactional(Ci ci, CiFile ciFile, CodeInfo codeInfo, CiRecord ciRecord,
            ImageDetail imageDetail) {
        if (ci != null) {
            saveCi(ci);
        }
        if (ciFile != null) {
            saveCiFile(ciFile);
        }
        if (codeInfo != null) {
            ciCodeCredentialsService.saveCodeInfo(codeInfo);
        }
        if (ciRecord != null) {
            saveCiRecord(ciRecord);
        }
        if (imageDetail != null) {
            Date now = new Date();
            Image image = imageDetail.getImage();
            image.setCreateTime(now);
            ImageVersion imageVersion = imageDetail.getImageVersion();

            ImageVersion imageVersionObj = new ImageVersion(image.getId(), now, imageVersion.getImageType(),
                    imageVersion.getImageVersion(), image.getImageSize(), ci.getCiType() == CiConstant.TYPE_CODE ?
                    ImageConstant.IMAGE_CI_TYPE_CODECI :
                    ImageConstant.IMAGE_CI_TYPE_DOCKERFILE, image.getPorts(), image.getCreatedBy(),
                    image.getProjectId(), ci.getEnvVariables());
            imageVersionObj.setId(imageVersion.getId());
            imageVersionObj.setCodeBaseName(imageVersion.getCodeBaseName());
            try {
                imageService.updateImageVersionCountSync(image, imageVersionObj, ImageConstant.IMAGE_OPERATOR_ADD);
            } catch (Exception e) {
                LOG.error("添加镜像错误！", e);
            }
        }
    }

    /**
     * 根据ID查询记录
     *
     * @param id
     * @return CiRecord
     * @date: 2019年4月11日 下午3:16:52
     */
    private CiRecord getCiRecordById(String id) {
        try {
            return ciRecordRepository.getById(id);
        } catch (Exception e) {
            LOG.error("根据ID查询CiRecord失败！");
        }
        return null;
    }

    @Override
    public void addLogPrint(CiRecord ciRecord, String msg) {
        ciRecord.setLogPrint(
                ciRecord.getLogPrint() + "<br>" + "[" + DateUtil.date2String(new Date(), DateUtil.DEFAULT_TIME_PATTERN)
                        + "] " + msg);
        try {
            ciRecordRepository.save(ciRecord);
        } catch (Exception e) {
            LOG.error("dockerfile构建, 添加日志信息, 保存数据库失败! ", e.getMessage());
        }
    }

    @Override
    public boolean stopCi(String ciId) {
        Ci ci = getCi(ciId);
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "构建信息不存在!");
        }
        CiRecord ciRecord = getCiRecordLatest(ciId);
        if (ciRecord == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "没有构建任务!");
        }
        if (ci.getConstructionStatus() != CiConstant.CONSTRUCTION_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "此记录不处于构建中状态, 无法停止!");
        }

        // dockerfile构建, 设置中断构建标记
        if (ci.getCiType() == CiConstant.TYPE_DOCKERFILE) {
            String ciInterruptFlagName = generateDockerfileCiInterruptFlag(ci);
            dockerfileCiInInterruptFlagMap.put(ciInterruptFlagName, "");
        } else { // 代码构建, 调用停止接口
            // stopSheraJob(ci.getTenantName(), ci.getCiName(),
            // ciRecord.getSheraConstructionId());
            stopJenkinsJob(ci.getTenantName(), ci.getCiName(), JobService.JOB_TYPE_DOCKER,
                    ciRecord.getSheraConstructionId());
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyDockerfileCi(Ci ciModify, CiFile ciFileModify) {
        Ci ci = getCi(ciModify.getId());
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的构建记录不存在!");
        }
        checkModifyCiCommon(ci, ciModify, CiConstant.TYPE_DOCKERFILE);

        CiFile ciFile = getCiFileByCiId(ci.getId());
        if (ciFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的构建记录上传文件信息不存在!");
        }

        // “禁用”以外的状态修改配置后，状态变为“未执行”(未构建)；“禁用”状态修改配置后，状态仍为“禁用”
        if (ci.getConstructionStatus() != CiConstant.CONSTRUCTION_STATUS_DISABLED) {
            ci.setConstructionStatus(CiConstant.CONSTRUCTION_STATUS_WAIT);
        }

        ci.setCiDescription(ciModify.getCiDescription());
        ci.setCron(ciModify.getCron());
        ci.setCronDescription(ciModify.getCronDescription());
        ci.setCiName(ciModify.getCiName());
        ci.setImageName(ciModify.getImageName());
        ci.setImageVersion(ciModify.getImageVersion());
        ci.setEnvVariables(ciModify.getEnvVariables());
        ci.setImageVersionGenerationStrategy(ciModify.getImageVersionGenerationStrategy());

        ciFile.setFilePath(ciFileModify.getFilePath());
        ciFile.setDockerfileContent(ciFileModify.getDockerfileContent());
        ciFile.setFileName(ciFileModify.getFileName());
        ciFile.setDockerfileTypeId(ciFileModify.getDockerfileTypeId());
        ciFile.setDockerfileTemplateId(ciFileModify.getDockerfileTemplateId());
        ciFile.setAdvanced(ciFileModify.isAdvanced());
        ciFile.setUploadfileSize(ciFileModify.getUploadfileSize());

        // 保存信息到数据库
        saveCi(ci);
        saveCiFile(ciFile);

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyCodeCi(Ci ciModify, CodeInfo codeInfoModify, CiFile ciFileModify) {
        // 代码构建修改
        Ci ci = getCi(ciModify.getId());
        checkModifyCiCommon(ci, ciModify, CiConstant.TYPE_CODE);

        CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(ci.getCodeInfoId());
        if (codeInfo == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的构建记录代码信息不存在!");
        }
        CiCodeCredentials ciCodeCredentials = ciCodeCredentialsService.getById(codeInfoModify.getCiCodeCredentialsId());
        if (ciCodeCredentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "认证信息不存在!");
        }
        CiFile ciFile = getCiFileOfCodeCi(ci);
        if (ciFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的构建记录dockerfile信息不存在!");
        }

        HarborUser harborUser = imageService.getHarborUserByByTenantName(ci.getTenantName());
        if (harborUser == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户harbor信息不存在!");
        }

        // “禁用”以外的状态修改配置后，状态变为“未执行”(未构建)；“禁用”状态修改配置后，状态仍为“禁用”
        if (ci.getConstructionStatus() != CiConstant.CONSTRUCTION_STATUS_DISABLED) {
            ci.setConstructionStatus(CiConstant.CONSTRUCTION_STATUS_WAIT);
        }

        if (codeInfoModify.getCodeControlType() == CiConstant.CODE_TYPE_GITLAB) {
            // gitlab方式替换代码url域名
            codeInfoModify.setCodeUrl(StringUtils
                    .replaceUrlDomainName(ciCodeCredentials.getRegistoryAddress(), codeInfoModify.getCodeUrl()));
        }

        String originalCron = ci.getCron();
        ci.setCiDescription(ciModify.getCiDescription());
        ci.setCron(ciModify.getCron());
        ci.setCronDescription(ciModify.getCronDescription());
        ci.setCompile(ciModify.getCompile());
        String originalCiName = ci.getCiName();
        ci.setCiName(ciModify.getCiName());
        ci.setImageName(ciModify.getImageName());
        ci.setImageVersion(ciModify.getImageVersion());
        ci.setDockerfilePath(ciModify.getDockerfilePath());
        ci.setImageVersionGenerationStrategy(ciModify.getImageVersionGenerationStrategy());
        if (!StringUtils.isEmpty(ciModify.getLang())) {
            ci.setLang(ciModify.getLang());
        }
        ci.setEnvVariables(ciModify.getEnvVariables());
        ci.setHookUsed(ciModify.getHookUsed());

        ciFile.setDockerfileContent(ciFileModify.getDockerfileContent());
        ciFile.setFilePath(ciFileModify.getFilePath());
        ciFile.setDockerfileTypeId(ciFileModify.getDockerfileTypeId());
        ciFile.setDockerfileTemplateId(ciFileModify.getDockerfileTemplateId());
        ciFile.setAdvanced(ciFileModify.isAdvanced());
        ciFile.setDockerfileWriteType(ciFileModify.getDockerfileWriteType());
        ciFile.setFileName(ciFileModify.getFileName());

        codeInfo.setCodeBranch(codeInfoModify.getCodeBranch());
        codeInfo.setCiCodeCredentialsId(codeInfoModify.getCiCodeCredentialsId());
        codeInfo.setCodeControlType(codeInfoModify.getCodeControlType());
        codeInfo.setCodeUrl(codeInfoModify.getCodeUrl());
        codeInfo.setCodeReposName(codeInfoModify.getCodeReposName());
        codeInfo.setCodeReposId(codeInfoModify.getCodeReposId());
        codeInfo.setBranchOrTag(codeInfoModify.getBranchOrTag());

        // 更新数据库
        saveCi(ci);
        ciCodeCredentialsService.saveCodeInfo(codeInfo);
        saveCiFile(ciFile);

        // 更新Jenkins job
        AbstractCiStrategyJenkins ciStrategy = CiStrategyFactoryJenkins.getCiStrategy(ci.getLang());
        com.xxx.xcloud.module.devops.model.Job job = ciStrategy
                .generateJenkinsCiJob(ci, codeInfo, ciFile, ciCodeCredentials, harborUser);
        saveJenkinsJob(job, originalCiName, JobService.JOB_TYPE_DOCKER);

        // 判断定时任务是否改变
        if (!StringUtils.isEmpty(originalCron) && !originalCron.equals(ciModify.getCron())) {
            if (!StringUtils.isEmpty(ciModify.getCron())) {
                // 修改定时任务
                modifyCodeCiQuartz(ci);
            } else {
                // 移除定时任务
                removeCodeCiQuartz(ci);
            }
        } else if (StringUtils.isEmpty(originalCron) && !StringUtils.isEmpty(ciModify.getCron())) {
            // 新增定时任务
            addCodeCiQuartz(ci);
        }

        return true;
    }

    /**
     * 获取代码构建的dockerfile构建文件信息
     *
     * @param ci
     * @return CiFile
     * @date: 2019年7月19日 下午5:42:54
     */
    private CiFile getCiFileOfCodeCi(Ci ci) {
        CiFile ciFile = getCiFileByCiId(ci.getId());
        if (ciFile == null) {
            ciFile = CiFile.builder().withAdvanced(true).withCiId(ci.getId()).withFilePath(ci.getDockerfilePath())
                    .withId(ci.getId()).build();
            ciFile = saveCiFile(ciFile);
        }
        return ciFile;
    }

    /**
     * 检查修改构建任务信息，不符合抛出异常
     *
     * @param ci
     * @param ciModify
     * @param ciType   void
     * @date: 2019年4月24日 上午11:17:10
     */
    private void checkModifyCiCommon(Ci ci, Ci ciModify, byte ciType) {
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的构建记录不存在!");
        }
        String tenantName = ci.getTenantName();
        // 如果任务名称修改了, 判断修改后的项目名是否重复
        if (!ci.getCiName().equals(ciModify.getCiName())
                && getCiByCiNameType(ciModify.getTenantName(), ciModify.getCiName(), ciType) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "构建名称已经存在!");
        }
        // 如果镜像名称或版本改变了, 判断镜像名称是否合法, 镜像名和版本是否已经存在
        if ((!ci.getImageName().equals(ciModify.getImageName()) || !ci.getImageVersion()
                .equals(ciModify.getImageVersion()))) {
            if (!imageService.isImageNameLegal(ciModify.getImageName())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "镜像名称只能是小写字母, 数字, 横线, 下划线, 且必须以字母开头.");
            }
            if (imageService.isImageExist(tenantName, ciModify.getImageName(), ciModify.getImageVersion())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名称和版本已经存在!");
            }
            if (getCiByImageNameAndVersion(tenantName, ciModify.getImageName(), ciModify.getImageVersion()) != null) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名称和版本已经存在!");
            }
        }

        if (ci.getConstructionStatus() == CiConstant.CONSTRUCTION_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_NODE_NOT_ALLOWED_FAILED, "构建中的记录无法修改, 请等待构建完成!");
        }

        if (!StringUtils.isEmpty(ciModify.getCron()) && !quartzUtils.isValidExpression(ciModify.getCron())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "cron表达式不合规范!");
        }
    }

    /**
     * 创建job
     *
     * @param job
     * @param jobType
     * @date: 2019年4月16日 下午2:08:57
     */
    public void createJenkinsJob(Job job, String jobType) {
        try {
            if (jobService.getJob(job.getNamespace(), jobType, job.getName()) != null) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "名称在Jenkins已经存在！");
            }
            jobService.create(job, jobType);
        } catch (DevopsException e) {
            LOG.error("创建Jenkins job失败！", e);
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新Job
     *
     * @param job
     * @param jobType void
     * @date: 2019年4月16日 下午2:16:44
     */
    public void updateJenkinsJob(Job job, String jobType) {
        try {
            if (jobService.getJob(job.getNamespace(), jobType, job.getName()) == null) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的任务名称在Jenkins不存在！");
            }
            jobService.update(job, jobType);
        } catch (DevopsException e) {
            LOG.error("jobService接口调用失败！", e);
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改job名称，更新job
     *
     * @param job
     * @param originalJobName
     * @param jobType
     * @date: 2019年4月16日 下午2:29:28
     */
    public void updateJenkinsJob(com.xxx.xcloud.module.devops.model.Job job, String originalJobName, String jobType) {
        try {
            if (jobService.getJob(job.getNamespace(), jobType, originalJobName) == null) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "修改的任务名称在Jenkins不存在！");
            }
            jobService.renameJob(job.getNamespace(), jobType, originalJobName, job.getName());
            jobService.update(job, jobType);
        } catch (DevopsException e) {
            LOG.error("创建Jenkins job失败！", e);
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        }
    }

    @Override
    public void saveJenkinsJob(com.xxx.xcloud.module.devops.model.Job job, String originalJobName, String jobType) {
        if (StringUtils.isEmpty(originalJobName)) {
            // 新建job
            createJenkinsJob(job, jobType);
        } else if (job.getName().equals(originalJobName)) {
            // 修改job, 但job名称没有改变, 进行更新操作
            updateJenkinsJob(job, jobType);
        } else {
            // 修改job, 但job名称改变, 新建新的job，删除原来的job
            updateJenkinsJob(job, originalJobName, jobType);
        }
    }

    /**
     * 停止Jenkins任务
     *
     * @param namespace
     * @param jobName
     * @param jobType
     * @param buildNumber void
     * @date: 2019年4月16日 下午2:56:13
     */
    public void stopJenkinsJob(String namespace, String jobName, String jobType, int buildNumber) {
        try {
            jobService.interupt(namespace, jobType, jobName, buildNumber);
        } catch (DevopsException e) {
            LOG.error("停止失败!", e);
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新CiRecord记录
     *
     * @param ciRecord
     * @return {@link CiRecord}
     * @date: 2018年12月26日 下午7:27:35
     */
    private CiRecord saveCiRecord(CiRecord ciRecord) {
        try {
            return ciRecordRepository.save(ciRecord);
        } catch (Exception e) {
            LOG.error("数据库更新CiRecord记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新数据库失败!");
        }
    }

    /**
     * 更新Ci记录
     *
     * @param ci
     * @return Ci
     * @date: 2018年12月26日 下午7:27:35
     */
    private Ci saveCi(Ci ci) {
        try {
            return ciRepository.save(ci);
        } catch (Exception e) {
            LOG.error("数据库更新Ci记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新数据库失败!");
        }
    }

    /**
     * 更新CiFile记录
     *
     * @param ciFile
     * @return {@link CiFile}
     * @date: 2018年12月26日 下午7:30:55
     */
    private CiFile saveCiFile(CiFile ciFile) {
        try {
            return ciFileRepository.save(ciFile);
        } catch (Exception e) {
            LOG.error("数据库更新CiFile记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新数据库失败!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteCi(String ciId) {
        Ci ci = getCi(ciId);
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "删除的构建记录不存在!");
        }
        if (ci.getConstructionStatus() == CiConstant.CONSTRUCTION_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_NODE_NOT_ALLOWED_FAILED, "正在构建中，无法删除!");
        }

        // 删除dockerfile构建
        if (ci.getCiType() == CiConstant.TYPE_DOCKERFILE) {
            return deleteDockerFileCi(ciId);
        }
        // 删除代码构建
        return deleteCodeCi(ci);
    }

    /**
     * 删除dockerfile构建相关信息
     *
     * @param ciId 构建记录ID
     * @return boolean
     * @date: 2018年12月17日 上午10:53:05
     */
    private boolean deleteDockerFileCi(String ciId) {
        // 获取CiFile信息
        CiFile ciFile = getCiFileByCiId(ciId);
        LOG.info("----------ciFile------------" + JSONObject.toJSONString(ciFile));
        if (null == ciFile) {
            return true;
        }
        String ftpFilePath = ciFile.getFilePath();
        LOG.info("----------ftpFilePath------------" + ftpFilePath);
        // FtpFilePath ftpFile = JSONObject.parseObject(filePath,
        // FtpFilePath.class);
        // LOG.info("----------ciFile------------" +
        // JSONObject.toJSONString(ftpFile));
        if (null == ftpFilePath) {
            return true;
        }
        // String ftpFilePath = filePath.getFilePath();
        if (!StringUtils.isEmpty(ftpFilePath)) {
            // 删除FTP上的文件
            FTPClient ftpClient = null;
            try {
                ftpClient = FtpUtils.initFtpClient();
            } catch (Exception e) {
                throw new ErrorMessageException(ReturnCode.CODE_XCLOUD_DELETE_JOB_FAILED, "初始化FTP失败!");
            }

            LOG.info("-----------ftpFilePath-----------" + ftpFilePath);
            if (!FtpUtils.deleteDir(ftpClient, ftpFilePath)) {
                throw new ErrorMessageException(ReturnCode.CODE_XCLOUD_DELETE_JOB_FAILED, "删除ftp文件失败!");
            }
        }

        // 删除数据库信息
        deleteCiDatabase(ciId);
        deleteCiFileByCiId(ciId);
        deleteCiRecordsByCiId(ciId);

        // 删除上传文件信息(删除数据库信息成功, 就当成删除成功)
        // if (ciFile != null) {
        // String filePath = ciFile.getFilePath();
        // try {
        // FtpUtils.removeDirAndSubFile(FTP_HOST, FTP_USER_NAME, FTP_PASSWORD,
        // FTP_PORT,
        // filePath, ciFile.getFileName().split(","));
        // } catch (Exception e) {
        // LOG.error("删除dockerfile构件记录时, 删除上传文件失败! " + e.getMessage(), e);
        // }
        // }
        return true;
    }

    /**
     * 删除代码构建记录
     *
     * @param ci 构建记录
     * @return boolean
     * @date: 2018年12月17日 上午10:59:05
     */

    private boolean deleteCodeCi(Ci ci) {
        // 删除数据库信息
        deleteCiRecordsByCiId(ci.getId());
        deleteCiDatabase(ci.getId());
        ciCodeCredentialsService.deleteCodeInfo(ci.getCodeInfoId());
        deleteCiFileByCiId(ci.getId());

        deleteJenkinsJob(ci.getTenantName(), JobService.JOB_TYPE_DOCKER, ci.getCiName());
        return true;
    }

    /**
     * 根据ciId删除CiFile信息
     *
     * @param ciId
     * @date: 2018年12月26日 下午7:22:21
     */
    private void deleteCiFileByCiId(String ciId) {
        try {
            ciFileRepository.deleteByCiId(ciId);
        } catch (Exception e) {
            LOG.error("根据ciId删除CiFile记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除数据库信息失败!");
        }
    }

    /**
     * 根据ciId删除CiRecord记录
     *
     * @param ciId
     * @date: 2018年12月26日 下午7:19:58
     */
    private void deleteCiRecordsByCiId(String ciId) {
        try {
            ciRecordRepository.deleteByCiId(ciId);
        } catch (Exception e) {
            LOG.error("根据ciId删除CiRecord记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除数据库信息失败!");
        }
    }

    /**
     * 根据ID删除Ci记录
     *
     * @param ciId
     * @date: 2018年12月26日 下午7:17:29
     */
    private void deleteCiDatabase(String ciId) {
        try {
            ciRepository.deleteById(ciId);
        } catch (Exception e) {
            LOG.error("删除Ci记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除数据库信息失败!");
        }
    }

    @Override
    public Page<Ci> getCis(String tenantName, String projectId, String ciName, Pageable pageable) {
        try {
            if (StringUtils.isNotEmpty(projectId)) {
                return ciRepository
                        .findByTenantNameAndProjectIdAndCiNameLikeOrderByCreateTimeDesc(tenantName, projectId, ciName,
                                pageable);
            } else {
                return ciRepository.findByTenantNameAndCiNameLikeOrderByCreateTimeDesc(tenantName, ciName, pageable);
            }
        } catch (Exception e) {
            LOG.error("查询Ci记录失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询数据库信息失败!");
        }

    }

    @Override
    public Page<Ci> getCis(String tenantName, String projectId, byte ciType, String ciName, Pageable pageable) {
        try {
            if (StringUtils.isNotEmpty(projectId)) {
                return ciRepository
                        .findByTenantNameAndProjectIdAndCiTypeAndCiNameLikeOrderByCreateTimeDesc(tenantName, projectId,
                                ciType, ciName, pageable);
            } else {
                return ciRepository
                        .findByTenantNameAndCiTypeAndCiNameLikeOrderByCreateTimeDesc(tenantName, ciType, ciName,
                                pageable);
            }
        } catch (Exception e) {
            LOG.error("查询Ci记录失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询数据库信息失败!");
        }

    }

    @Override
    public Ci getCi(String ciId) {
        try {
            return ciRepository.getById(ciId);
        } catch (Exception e) {
            LOG.error("获取Ci记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询数据库信息失败!");
        }
    }

    /**
     * 根据ciId查询(for update)
     *
     * @param ciId
     * @return Ci
     * @date: 2019年4月10日 下午5:17:30
     */
    public Ci getCiForUpdate(String ciId) {
        try {
            return ciRepository.getByIdForUpdate(ciId);
        } catch (Exception e) {
            LOG.error("获取Ci记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询数据库信息失败!");
        }
    }

    @Override
    public CiRecord getCiRecordLatest(String ciId) {
        try {
            Page<CiRecord> ciRecords = getCiRecords(ciId, PageUtil.getPageable(0, 1));
            if (!ciRecords.isEmpty()) {
                return ciRecords.getContent().get(0);
            }
            return null;
        } catch (Exception e) {
            LOG.error("查询最新构建信息失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询最新日志失败!");
        }
    }

    // @Override
    // public List<CiRecord> getCiRecords(String ciId) {
    // try {
    // return ciRecordRepository.getCiRecordsByCiId(ciId);
    // } catch (Exception e) {
    // LOG.error("查询构建日志信息列表失败: " + e.getMessage(), e);
    // throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
    // "查询构建记录失败!");
    // }
    //
    // }

    @Override
    public Page<CiRecord> getCiRecords(String ciId, Pageable pageable) {
        try {
            return ciRecordRepository.findByCiIdOrderByConstructionTimeDesc(ciId, pageable);
        } catch (Exception e) {
            LOG.error("查询构建日志信息分页列表失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询构建记录分页信息失败!");
        }
    }

    @Override
    public CiDetail getCiDetail(String ciId) {
        CiDetail ciDetail = new CiDetail();
        // 查询Ci
        Ci ci = getCi(ciId);
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "构建记录不存在!");
        }
        ciDetail.setCi(ci);

        // 查询CiFile
        CiFile ciFile = getCiFileByCiId(ciId);
        // 代码构建，dockerfile路径原来存在了ci表，现修改到了CiFile表；之前的数据，将信息移到CiFile表
        if (ciFile == null && ci.getCiType() == CiConstant.TYPE_CODE) {
            ciFile = CiFile.builder().withAdvanced(true).withCiId(ci.getId()).withFilePath(ci.getDockerfilePath())
                    .withId(ci.getId()).build();
            ciFile = saveCiFile(ciFile);
        }
        ciDetail.setCiFile(ciFile);

        if (ci.getCiType() == CiConstant.TYPE_CODE) {
            CodeInfo codeInfo = ciCodeCredentialsService.getCodeInfoById(ci.getCodeInfoId());
            ciDetail.setCodeInfo(codeInfo);
            if (codeInfo != null) {
                ciDetail.setCiCodeCredentials(ciCodeCredentialsService.getById(codeInfo.getCiCodeCredentialsId()));
            }
        }

        return ciDetail;
    }

    @Override
    public CiDetail getCiConstructDetail(String ciId) {
        // 先获取记录信息
        CiDetail ciDetail = getCiDetail(ciId);
        // // 再设置构建日志信息
        List<CiRecord> ciRecords = new ArrayList<>();
        CiRecord ciRecord = getCiRecordLatest(ciId);
        if (ciRecord != null) {
            ciRecord.setLogPrint(StringUtils.splitStrByLines(ciRecord.getLogPrint(), 1000));
            ciRecords.add(ciRecord);
        }

        ciDetail.setCiRecords(ciRecords);
        // // 添加统计信息
        // // 总构建时长
        // int constructionDurationTotal = 0;
        // // 构建成功次数
        // int constructionOkTotal = 0;
        // // 构建失败次数
        // int constructionFailTotal = 0;
        // for (CiRecord ciRecord : ciRecords) {
        // if (ciRecord.getConstructionResult() ==
        // CiConstant.CONSTRUCTION_STATUS_SUCCESS) {
        // constructionOkTotal++;
        // } else if (ciRecord.getConstructionResult() ==
        // CiConstant.CONSTRUCTION_STATUS_FAIL) {
        // constructionFailTotal++;
        // }
        // if (ciRecord.getConstructionDuration() != null) {
        // constructionDurationTotal += ciRecord.getConstructionDuration();
        // }
        // }
        // ciDetail.setConstructionDurationTotal(constructionDurationTotal);
        // ciDetail.setConstructionOkTotal(constructionOkTotal);
        // ciDetail.setConstructionFailTotal(constructionFailTotal);

        return ciDetail;
    }

    @Override
    public boolean deleteCiRecord(String id) {
        try {
            ciRecordRepository.deleteById(id);
        } catch (Exception e) {
            LOG.error("根据ID删除构建记录失败!" + e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "数据库删除构建记录失败!");
        }
        return true;
    }

    /**
     * 根据ciId获取CiFile记录
     *
     * @param ciId
     * @return CiFile
     * @date: 2018年12月26日 下午7:12:15
     */
    @Override
    public CiFile getCiFileByCiId(String ciId) {
        try {
            return ciFileRepository.getByCiId(ciId);
        } catch (Exception e) {
            LOG.error("查询CiFile信息失败! " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询CiFile记录失败!");
        }
    }

    @Override
    public List<Ci> getCisCronIsNotNull() {
        List<Ci> cis = null;
        try {
            cis = ciRepository.getAllCronIsNotNull();
        } catch (Exception e) {
            LOG.error("查询cron不为空的Ci信息失败!", e);
        }
        return cis;
    }

    @Override
    public Ci getCiByImageNameAndVersion(String tenantName, String imageName, String imageVersion) {
        try {
            return ciRepository.getByCiImageNameAndVersion(tenantName, imageName, imageVersion);
        } catch (Exception e) {
            LOG.error("根据镜像名称和版本查询构建记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "根据镜像名称和版本查询构建记录失败!");
        }
    }

    @Override
    public boolean disableCi(String id) {
        Ci ci = getCi(id);
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "数据不存在！");
        }
        if (ci.getConstructionStatus() == CiConstant.CONSTRUCTION_STATUS_ING) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "该记录处于构建中，无法禁用！");
        }
        if (ci.getConstructionStatus() == CiConstant.CONSTRUCTION_STATUS_DISABLED) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "该记录已经处于禁用中状态！");
        }

        if (!StringUtils.isEmpty(ci.getCron())) {
            removeCodeCiQuartz(ci);
        }

        ci.setConstructionStatus(CiConstant.CONSTRUCTION_STATUS_DISABLED);
        saveCi(ci);
        return true;
    }

    @Override
    public boolean enableCi(String id) {
        Ci ci = getCi(id);
        if (ci == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "数据不存在！");
        }
        if (ci.getConstructionStatus() != CiConstant.CONSTRUCTION_STATUS_DISABLED) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "该记录未处于禁用中，无法启用！");
        }

        // 如果存在定时任务，且添加定时任务失败
        if (!StringUtils.isEmpty(ci.getCron()) && !addCodeCiQuartz(ci)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_QUARTZ_ADD_FAILED, "添加构建定时任务失败！");
        }

        ci.setConstructionStatus(CiConstant.CONSTRUCTION_STATUS_WAIT);
        saveCi(ci);
        return true;
    }

    @Override
    public List<Ci> getCisStatusIng() {
        return getCisByStatus(CiConstant.CONSTRUCTION_STATUS_ING);
    }

    /**
     * 根据状态查询 构建任务
     *
     * @param status
     * @return List<Ci>
     * @date: 2019年6月19日 上午9:49:09
     */
    private List<Ci> getCisByStatus(byte status) {
        try {
            return ciRepository.findByConstructionStatus(status);
        } catch (Exception e) {
            LOG.error("根据状态查询构建记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "根据状态查询构建记录失败!");
        }
    }

    @Override
    public List<Ci> getCiList(String tenantName, String projectId) {
        try {
            if (StringUtils.isEmpty(projectId)) {
                return ciRepository.findByTenantName(tenantName);
            } else {
                return ciRepository.findByTenantNameAndProjectId(tenantName, projectId);
            }
        } catch (Exception e) {
            LOG.error("根据租户和项目ID查询构建记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "根据租户和项目ID查询构建记录失败");
        }
    }

    @Override
    public List<Ci> getAllCi() {
        return ciRepository.findAll();
    }

    @Override
    public List<Map<String, Object>> getCiStatisticsToday(Set<String> tenantNameSet, Set<String> imageNameSet) {
        try {
            return ciRepository
                    .getCiStatistics(tenantNameSet, imageNameSet, com.xxx.xcloud.utils.DateUtil.getDayStart());
        } catch (Exception e) {
            LOG.error("查询代码构建生成的镜像，构建统计信息错误");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询构建统计信息错误!");
        }

    }

    @Override
    public List<Map<String, Object>> getCiStatistics(Set<String> tenantNameSet, Set<String> imageNameSet) {
        try {
            return ciRepository.getCiStatistics(tenantNameSet, imageNameSet);
        } catch (Exception e) {
            LOG.error("查询代码构建生成的镜像，构建统计信息错误");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询构建统计信息错误!");
        }

    }

    @Override
    public Map<String, Object> getCiStatistics(String serviceId) {
        // 1，查使用中的镜像
        Map<String, Object> imageUsed = null;
        try {
            imageUsed = ciRepository.getImagesUsed(serviceId);
        } catch (Exception e) {
            LOG.error("查询被服务使用的镜像错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询使用中的镜像错误！");
        }
        String imageName = String.valueOf(imageUsed.get("imageName"));
        String tenantName = String.valueOf(imageUsed.get("tenantName"));

        // 查询经镜像对应的构建任务
        List<String> cis = ciRepository.getCiIdsByTenantNameAndImageName(tenantName, imageName);
        if (cis.isEmpty()) {
            return new HashMap<>(0);
        }

        // 统计构建任务信息
        try {
            Map<String, Object> ciStatistics = ciRepository.getCiStatistics(cis);
            Map<String, Object> ciStatisticsSevenDays = ciRepository
                    .getCiStatistics(cis, com.xxx.xcloud.utils.DateUtil.getDateDaysAgo(7));
            Map<String, Object> ciStatisticsOneMonth = ciRepository
                    .getCiStatistics(cis, com.xxx.xcloud.utils.DateUtil.getDateMonthAgo(1));
            Map<String, Object> ciStatisticsSixMonths = ciRepository
                    .getCiStatistics(cis, com.xxx.xcloud.utils.DateUtil.getDateMonthAgo(6));
            Map<String, Object> ciStatisticsToday = ciRepository
                    .getCiStatistics(cis, com.xxx.xcloud.utils.DateUtil.getDayStart());
            Map<String, Object> newestCiStatus = ciRepository.getNewstCiStatus(cis);
            ciStatistics = new HashMap<>(ciStatistics);
            ciStatistics.put("constructionTotalToday", ciStatisticsToday.get("constructionTotal"));
            ciStatistics.put("constructionTotalSevenDays", ciStatisticsSevenDays.get("constructionTotal"));
            ciStatistics.put("constructionTotalOneMonth", ciStatisticsOneMonth.get("constructionTotal"));
            ciStatistics.put("constructionTotalSixMonths", ciStatisticsSixMonths.get("constructionTotal"));
            ciStatistics.put("constructionResult", newestCiStatus.get("constructionResult"));
            ciStatistics.put("constructionTime",
                    DateUtil.date2String((Date) newestCiStatus.get("constructionTime"), DateUtil.DEFAULT_TIME_PATTERN));

            return ciStatistics;
        } catch (Exception e) {
            LOG.error("查询构建统计信息错误！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询构建信息错误!");
        }

    }

}
