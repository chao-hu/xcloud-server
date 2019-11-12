package com.xxx.xcloud.module.devops.job.service;

import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.xxx.xcloud.module.devops.common.DevopsException;

import java.util.List;
import java.util.Map;

/**
 * @author daien
 * @date 2019年2月28日
 */
public interface JobService {

    public static final String JOB_TYPE_DOCKER = "docker";
    public static final String JOB_TYPE_SONAR = "sonar";

    /**
     * @Description 创建任务
     * @param job
     * @param jobType
     * @throws DevopsException
     */
    void create(com.xxx.xcloud.module.devops.model.Job job, String jobType) throws DevopsException;

    /**
     * @Description 删除任务
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @throws DevopsException
     */
    void delete(String namespace, String jobType, String jobName) throws DevopsException;

    /**
     * @Description 更新任务
     * @param job
     * @param jobType
     * @throws DevopsException
     */
    void update(com.xxx.xcloud.module.devops.model.Job job, String jobType) throws DevopsException;

    /**
     * @Description 获取任务详情
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @return
     * @throws DevopsException
     */
    JobWithDetails getJob(String namespace, String jobType, String jobName) throws DevopsException;

    /**
     * 修改job名称
     *
     * @param namespace
     * @param oldJobName
     * @param newJobName
     * @throws DevopsException
     * @date: 2019年4月16日 下午3:51:15
     */
    void renameJob(String namespace, String jobType, String oldJobName, String newJobName) throws DevopsException;

    /**
     * @Description 获取任务列表
     * @param namespace
     *            租户名
     * @return
     * @throws DevopsException
     */
    List<Job> listJob(String namespace, String jobType) throws DevopsException;

    /**
     * @Description 指定的任务执行构建
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     *
     * @return 返回构建的序号
     * @throws DevopsException
     */
    int build(String namespace, String jobType, String jobName) throws DevopsException;

    /**
     * @Description
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @param params
     *            参数
     * @return 返回构建的序号
     * @throws DevopsException
     */
    int build(String namespace, String jobType, String jobName, Map<String, String> params) throws DevopsException;

    /**
     * @Description 中止指定的构建
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @param buildNumber
     *            构建序号
     * @throws DevopsException
     */
    void interupt(String namespace, String jobType, String jobName, int buildNumber) throws DevopsException;

    /**
     * @Description 获取构建详情
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @param buildNumber
     *            构建序号
     * @return
     * @throws DevopsException
     */
    Build getBuild(String namespace, String jobType, String jobName, int buildNumber) throws DevopsException;

    /**
     * @Description 刪除指定的构建
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @param buildNumber
     *            构建序号
     * @throws DevopsException
     */
    void deleteBuild(String namespace, String jobType, String jobName, int buildNumber) throws DevopsException;

    /**
     * @Description 获取构建列表
     * @param namespace
     *            租户名
     * @param jobName
     *            任务名
     * @return
     * @throws DevopsException
     */
    List<Build> listBuild(String namespace, String jobType, String jobName) throws DevopsException;

    /**
     * 生成Jenkins任务名
     * 
     * @param namespace
     * @param jobType
     * @param jobName
     * @return String
     * @date: 2019年5月10日 下午2:22:37
     */
    String generateJenkinsJobName(String namespace, String jobType, String jobName);
}
