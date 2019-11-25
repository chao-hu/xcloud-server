package com.xxx.xcloud.module.devops.job.service.impl;

import com.alibaba.fastjson.JSON;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.actions.service.ActionsService;
import com.xxx.xcloud.module.devops.build.service.BuildService;
import com.xxx.xcloud.module.devops.build.wrappers.service.BuildWrappersService;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.job.pojo.Project;
import com.xxx.xcloud.module.devops.job.service.JobService;
import com.xxx.xcloud.module.devops.properties.service.PropertiesService;
import com.xxx.xcloud.module.devops.publishers.service.PublishersService;
import com.xxx.xcloud.module.devops.scm.pojo.Scm;
import com.xxx.xcloud.module.devops.scm.service.ScmService;
import com.xxx.xcloud.module.devops.triggers.service.TriggersService;
import com.xxx.xcloud.module.devops.util.DevopsClient;
import com.xxx.xcloud.module.devops.util.JaxbUtil;
import com.xxx.xcloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author daien
 * @date 2019年3月15日
 */
@Service("devops_jobServiceImpl")
public class JobServiceImpl implements JobService {

    private static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    ScmService scmService;

    @Autowired
    BuildService buildService;

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    BuildWrappersService buildWrappersService;

    @Autowired
    ActionsService actionsService;

    @Autowired
    TriggersService triggersService;

    @Autowired
    PublishersService publishersService;

    @Override
    public void create(com.xxx.xcloud.module.devops.model.Job job, String jobType) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();

        // check namespace
        String namespace = job.getNamespace();
        if (StringUtils.isEmpty(namespace)) {
            throw new DevopsException(500, "namespace为空");
        }

        // check name
        String name = job.getName();
        if (StringUtils.isEmpty(name)) {
            throw new DevopsException(500, "name为空");
        }

        String jobName = generateJenkinsJobName(namespace, jobType, name);
        Scm scm = scmService.getScm(job.getScmModel());
        if (scm == null) {
            throw new DevopsException(500, "代码托管配置为空");
        }

        Project project = new Project();
        project.setDescription(job.getDescription());
        project.setJdk(buildService.getJdkVersion(job));
        project.setScm(scm);
        project.setProperties(propertiesService.getProperties(job.getScmModel()));
        project.setTriggers(triggersService.getTriggers(job));
        project.setBuildWrappers(buildWrappersService.getBuildWrappers(job));
        project.setBuilders(buildService.getBuilders(job, jobType));
        project.setActions(actionsService.getActions(job));
        project.setPublishers(publishersService.getPublishers(job));
        if (job.getLanguageModel() != null) {
            project.setCustomWorkspace(CiConstant.DEVOPS_LANG_GO.equals(job.getLanguageModel().getLangType())
                    ? "${JENKINS_HOME}/workspace/" + jobName + "/src"
                    : null);
        }

        JaxbUtil requestBinder = new JaxbUtil(Project.class, JaxbUtil.CollectionWrapper.class);
        String xmlString = requestBinder.toXml(project, "utf-8");

        try {
            jenkinsServer.createJob(jobName, xmlString, true);
        } catch (IOException e) {
            String msg = "任务:" + jobName + "[" + JSON.toJSONString(job) + "]创建失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public void delete(String namespace, String jobType, String jobName) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        try {
            jenkinsServer.deleteJob(generateJenkinsJobName(namespace, jobType, jobName), true);
        } catch (IOException e) {
            String msg = "任务:" + jobName + "删除失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public void update(com.xxx.xcloud.module.devops.model.Job job, String jobType) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();

        String jobName = generateJenkinsJobName(job.getNamespace(), jobType, job.getName());
        Scm scm = scmService.getScm(job.getScmModel());
        if (scm == null) {
            throw new DevopsException(500, "代码托管配置为空");
        }

        Project project = new Project();
        project.setDescription(job.getDescription());
        project.setJdk(buildService.getJdkVersion(job));
        project.setScm(scm);
        project.setProperties(propertiesService.getProperties(job.getScmModel()));
        project.setTriggers(triggersService.getTriggers(job));
        project.setBuildWrappers(buildWrappersService.getBuildWrappers(job));
        project.setBuilders(buildService.getBuilders(job, jobType));
        project.setActions(actionsService.getActions(job));
        project.setPublishers(publishersService.getPublishers(job));
        if (job.getLanguageModel() != null) {
            project.setCustomWorkspace(CiConstant.DEVOPS_LANG_GO.equals(job.getLanguageModel().getLangType())
                    ? "${JENKINS_HOME}/workspace/" + jobName + "/src"
                    : null);
        }

        JaxbUtil requestBinder = new JaxbUtil(Project.class, JaxbUtil.CollectionWrapper.class);
        String xmlString = requestBinder.toXml(project, "utf-8");

        try {
            jenkinsServer.updateJob(jobName, xmlString, true);
        } catch (IOException e) {
            String msg = "任务:" + jobName + "[" + JSON.toJSONString(job) + "]更新失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public JobWithDetails getJob(String namespace, String jobType, String jobName) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JobWithDetails jobWithDetails = null;
        try {
            JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
            jobWithDetails = jenkinsServer.getJob(generateJenkinsJobName(namespace, jobType, jobName));
        } catch (IOException e) {
            String msg = "获取任务:" + jobName + "信息失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
        return jobWithDetails;
    }

    @Override
    public void renameJob(String namespace, String jobType, String oldJobName, String newJobName)
            throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        oldJobName = generateJenkinsJobName(namespace, jobType, oldJobName);
        newJobName = generateJenkinsJobName(namespace, jobType, newJobName);
        try {
            JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
            jenkinsServer.renameJob(oldJobName, newJobName, true);
        } catch (IOException e) {
            String msg = "修改:" + oldJobName + "名称为" + newJobName + "失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public List<Job> listJob(String namespace, String jobType) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        List<Job> list = new ArrayList<>();
        try {
            Map<String, Job> map = jenkinsServer.getJobs();
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                if (name.split("_").length > 2 && name.split("_")[0].equals(namespace)
                        && name.split("_")[1].equals(jobType)) {
                    list.add(map.get(name));
                }
            }
        } catch (IOException e) {
            String msg = "获取任务列表失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
        return list;
    }

    @Override
    public int build(String namespace, String jobType, String jobName) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        try {
            JobWithDetails jobWithDetails = jenkinsServer.getJob(generateJenkinsJobName(namespace, jobType, jobName));
            if (jobWithDetails == null) {
                String msg = "任务:" + jobName + "不存在";
                throw new DevopsException(500, msg);
            }
            int number = jobWithDetails.getNextBuildNumber();
            jobWithDetails.build(true);
            return number;
        } catch (IOException e) {
            String msg = "任务:" + jobName + "构建失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public int build(String namespace, String jobType, String jobName, Map<String, String> params)
            throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        try {
            JobWithDetails jobWithDetails = jenkinsServer.getJob(generateJenkinsJobName(namespace, jobType, jobName));
            if (jobWithDetails == null) {
                String msg = "任务:" + jobName + "不存在";
                throw new DevopsException(500, msg);
            }
            int number = jobWithDetails.getNextBuildNumber();
            jobWithDetails.build(params, true);
            return number;
        } catch (IOException e) {
            String msg = "任务:" + jobName + "构建失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public void interupt(String namespace, String jobType, String jobName, int buildNumber) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        try {
            JobWithDetails jobWithDetails = jenkinsServer.getJob(generateJenkinsJobName(namespace, jobType, jobName));
            if (jobWithDetails == null) {
                String msg = "任务:" + jobName + "不存在";
                throw new DevopsException(500, msg);
            }

            Build build = jobWithDetails.getBuildByNumber(buildNumber);
            if (build == null) {
                String msg = "任务:" + jobName + ",序号:" + buildNumber + "构建不存在";
                throw new DevopsException(500, msg);
            }

            build.Stop(true);
        } catch (IOException e) {
            String msg = "任务:" + jobName + ",序号:" + buildNumber + "中止失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    @Override
    public Build getBuild(String namespace, String jobType, String jobName, int buildNumber) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        Build build = null;
        try {
            JobWithDetails jobWithDetails = jenkinsServer.getJob(generateJenkinsJobName(namespace, jobType, jobName));
            if (jobWithDetails == null) {
                String msg = "任务:" + jobName + "不存在";
                throw new DevopsException(500, msg);
            }

            build = jobWithDetails.getBuildByNumber(buildNumber);
        } catch (IOException e) {
            String msg = "获取任务:" + jobName + ",序号:" + buildNumber + "信息失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
        return build;
    }

    @Override
    public void deleteBuild(String namespace, String jobType, String jobName, int buildNumber) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        devopsClient.deleteBuild(generateJenkinsJobName(namespace, jobType, jobName), buildNumber);
    }

    @Override
    public List<Build> listBuild(String namespace, String jobType, String jobName) throws DevopsException {
        DevopsClient devopsClient = DevopsClient.getClient();
        JenkinsServer jenkinsServer = devopsClient.getJenkinsServer();
        List<Build> list = null;
        try {
            JobWithDetails jobWithDetails = jenkinsServer.getJob(generateJenkinsJobName(namespace, jobType, jobName));
            if (jobWithDetails == null) {
                String msg = "任务:" + jobName + "不存在";
                throw new DevopsException(500, msg);
            }
            list = jobWithDetails.getBuilds();
        } catch (IOException e) {
            String msg = "任务:" + jobName + "构建列表失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
        return list;
    }

    @Override
    public String generateJenkinsJobName(String namespace, String jobType, String jobName) {
        return namespace + "_" + jobType + "_" + jobName;
    }

}
