package com.xxx.xcloud.rest.v1.backup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.BackupReply;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.module.backup.entity.Job;
import com.xxx.xcloud.module.backup.entity.JobHistory;
import com.xxx.xcloud.module.backup.service.IJobHistoryService;
import com.xxx.xcloud.module.backup.service.IJobService;
import com.xxx.xcloud.rest.v1.backup.model.JobDTO;
import com.xxx.xcloud.rest.v1.backup.model.JobHistoryDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: BackupReplyController
 * @Description: 备份恢复
 * @author lnn
 * @date 2019年11月21日
 *
 */
@Controller
@RequestMapping("/v1/operations/")
public class BackupReplyController {

    private static final Logger LOG = LoggerFactory.getLogger(BackupReplyController.class);

    @Autowired
    IJobService jobService;

    @Autowired
    IJobHistoryService jobHistoryService;

    /**
     * 新增一条任务信息
     *
     * @param job
     *            接收参数的Job对象
     * @param errors
     *            参数校验的错误信息集合体
     */
    @ResponseBody
    @RequestMapping(value = "tasks", method = RequestMethod.POST)
    @ApiOperation(value = "新增任务信息", notes = "")
    public ApiResult create(@RequestBody JobDTO jobInfo, BindingResult errors) {
        LOG.debug("新增任务-----收到的参数 job={} ", JSONObject.toJSONString(jobInfo));

        String jobTypeName = BackupReply.getJobTypeName(jobInfo.getJobType());
        String retMessage = "新增" + jobTypeName + "任务失败!";
        Job savedJob = null;

        // 1、参数校验
        if (errors.hasErrors()) {
            retMessage = errors.getAllErrors().get(0).getDefaultMessage();
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, savedJob, retMessage);
        }

        // 2. 包装job
        Job job = generateJob(jobInfo, null, null);
        // 3、校验任务是否存在
        try {
            jobService.checkJobExist(job);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, savedJob, e.getMessage());
        }

        // 4、保存数据
        try {
            savedJob = jobService.save(job, jobInfo.getTenantName());
        } catch (Exception e) {
            LOG.error("新增任务----保存JOB数据异常！{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, savedJob, retMessage);
        }

        if (null == savedJob) {
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, savedJob, retMessage);
        }

        // 3、新增任务成功
        retMessage = "新增" + jobTypeName + "任务成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, savedJob, retMessage);
    }

    private Job generateJob(JobDTO jobInfo, String id, Date date) {
        Job job = new Job();
        if (StringUtils.isNotEmpty(id)) {
            job.setId(id);
        }
        job.setAppType(jobInfo.getAppType());
        job.setCron(jobInfo.getCron());
        job.setDay(jobInfo.getDay());
        job.setEndtime(jobInfo.getEndtime());
        job.setHour(jobInfo.getHour());
        job.setJobType(jobInfo.getJobType());
        job.setMinute(jobInfo.getMinute());
        job.setMonth(jobInfo.getMonth());
        job.setName(jobInfo.getName());
        job.setNodeId(jobInfo.getNodeId());
        job.setNodeName(jobInfo.getNodeName());
        job.setRelationJobId(jobInfo.getRelationJobId());
        job.setYear(jobInfo.getYear());
        job.setWeek(jobInfo.getWeek());
        job.setStatus(jobInfo.getStatus());
        job.setStarttime(jobInfo.getStarttime());
        job.setServiceId(jobInfo.getServiceId());
        job.setSecond(jobInfo.getSecond());
        job.setScheduleType(jobInfo.getScheduleType());
        job.setCreatetime(date);
        return job;
    }

    /**
     * 更新单个任务信息
     *
     * @param id
     *            指定更新任务的ID
     * @param job
     *            新的JOb对象
     * @param errors
     *            参数校验的错误信息集合体
     */
    @ResponseBody
    @RequestMapping(value = "tasks/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "修改任务信息", notes = "")
    @ApiImplicitParam(paramType = "path", name = "id", value = "任务信息ID", required = true, dataType = "String")
    public ApiResult update(@PathVariable("id") String id, @RequestBody JobDTO jobInfo, BindingResult errors) {
        LOG.debug("更新单个任务信息-----收到的参数job {}", JSONObject.toJSONString(jobInfo));

        String retMessage = "更新单个任务信息失败!";
        Object nullObj = null;

        // 1、参数校验
        if (errors.hasErrors()) {
            retMessage = errors.getAllErrors().get(0).getDefaultMessage();
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, nullObj, retMessage);
        }

        Job oldJob = jobService.getOneTask(id);

        if (null == oldJob) {
            LOG.error("更新单个任务----该任务ID ：{}没有对应的记录！", id);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, nullObj, retMessage);
        }

        // 3、更新数据库JOB
        Job job = generateJob(jobInfo, id, oldJob.getCreatetime());
        try {
            jobService.update(job, jobInfo.getTenantName());
        } catch (Exception e) {
            LOG.error("更新单个任务异常!{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, nullObj, retMessage);
        }

        // 4、更新任务成功
        retMessage = "更新单个任务成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, nullObj, retMessage);
    }

    /**
     * 删除单个任务
     *
     * @param id
     *            the job id you want to delete
     */
    @ResponseBody
    @RequestMapping(value = "tasks/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除任务信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "id", value = "任务名称", required = false, dataType = "String") })
    public ApiResult delete(@PathVariable("id") String id,
            @RequestParam(value = "tenantName", required = true) String tenantName) {
        LOG.debug("删除单个任务-----收到的参数 jobId={}", id);

        String retMessage = "删除单个任务失败!";
        Object nullObj = null;

        // 1、参数校验
        if (null == jobService.getOneTask(id)) {
            LOG.error("删除单个任务----该任务ID ：{}没有对应的记录！", id);
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, nullObj, retMessage);
        }
        Job byRelationJobId = jobService.getByRelationJobId(id);
        if (null != byRelationJobId) {
            LOG.error("删除单个任务----该任务存在依赖关系！", id);
            retMessage = "当前任务存在依赖，请勿删除！";
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, nullObj, retMessage);
        }

        // 3、删除单个任务
        try {
            jobService.delete(id, tenantName);
        } catch (Exception e) {
            LOG.error("删除单个任务异常！{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, nullObj, retMessage);
        }

        // 4、删除任务成功
        retMessage = "删除单个任务成功！";

        return new ApiResult(ReturnCode.CODE_SUCCESS, nullObj, retMessage);
    }

    /**
     * 删除备份记录列表数据
     *
     * @param id
     *            the jobHistories ids you want to delete,eg:id1,id2,id3...
     */
    @ResponseBody
    @RequestMapping(value = "jobhistory/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除备份记录", notes = "")
    @ApiImplicitParam(paramType = "path", name = "id", value = "任务名称", required = false, dataType = "String")
    public ApiResult deleteJobHistories(@PathVariable("id") String id) {

        String retMessage = "删除备份记录失败!";
        Object nullObj = null;

        String[] jobHistoryIds = id.split(",");

        LOG.info("删除备份记录列表数据-----收到的参数 jobHistoriesId={}", JSON.toJSON(jobHistoryIds));

        try {
            for (String jobHistoryId : jobHistoryIds) {
                jobHistoryService.delete(jobHistoryId);
            }
        } catch (Exception e) {
            LOG.error("删除备份记录异常！{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, nullObj, retMessage);
        }

        retMessage = "删除备份记录成功！";

        return new ApiResult(ReturnCode.CODE_SUCCESS, nullObj, retMessage);
    }

    /**
     * 备份任务列表
     *
     * @param serviceId
     *            witch cluster's jobs you want to find.
     * @param jobType
     *            not required，default is full_amount_backup and
     *            incremental_backup.
     * @param status
     *            not required，whether the task is enabled.
     * @param pageable
     *            not required, page info.
     */
    @ResponseBody
    @RequestMapping(value = "tasks", method = RequestMethod.GET)
    @ApiOperation(value = "获取备份任务列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "jobType", value = "任务类型", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "任务状态", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "2000", dataType = "int") })
    public ApiResult getJobs(@RequestParam(value = "serviceId") String serviceId,
            @RequestParam(value = "jobType", required = false) Integer jobType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") Integer size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "starttime");
        LOG.debug("备份任务列表-----收到的参数 serviceId={} ,jobType={} ,status={} ,pageable={}", serviceId, jobType, status,
                JSONObject.toJSONString(pageable));

        String retMessage = "备份任务列表获取失败!";
        Object data = null;

        try {
            data = jobService.getTasks(serviceId, jobType, status, pageable);
        } catch (Exception e) {
            LOG.error("备份任务列表获取异常， e : {}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, data, retMessage);
        }

        retMessage = "备份任务列表获取成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, data, retMessage);
    }

    /**
     * 获取单个任务信息
     */
    @ResponseBody
    @RequestMapping(value = "tasks/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "获取单个任务信息", notes = "")
    @ApiImplicitParam(paramType = "path", name = "id", value = "任务名称", required = false, dataType = "String")
    public ApiResult getOneJob(@PathVariable("id") String id) {

        LOG.debug("获取单个任务-----收到的参数 id={}", id);

        String retMessage = "获取单个任务信息失败!";
        Job job = null;

        try {
            job = jobService.getOneTask(id);
        } catch (Exception e) {
            LOG.error("获取单个任务信息异常 : {}", e);
        }

        if (null == job) {
            LOG.error("获取单个任务信息失败,ID不存在！");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, job, retMessage);
        }

        retMessage = "获取单个任务信息成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, job, retMessage);
    }

    /**
     * 获取备份列表
     *
     * @param serviceId
     *            cluster's ID
     * @param jobType
     *            job type---full_amount_backup or incremental_backup.
     * @param starttime
     *            filtering through the start time.
     */
    @ResponseBody
    @RequestMapping(value = "jobhistory/recover", method = RequestMethod.GET)
    @ApiOperation(value = "获取备份列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "jobType", value = "任务类型", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "starttime", value = "启动时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "2000", dataType = "int") })
    public ApiResult getBackUpList(@RequestParam(value = "serviceId") String serviceId,
            @RequestParam(value = "jobType", required = false) Integer jobType,
            @RequestParam(value = "starttime", required = false) String starttime,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") Integer size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "starttime");

        String retMessage = "获取备份列表失败!";
        Object data = null;

        // 1、参数校验
        if (StringUtils.isEmpty(serviceId)) {
            LOG.error("获取备份列表-----集群ID为空！");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, data, retMessage);
        }

        // 2、备份列表获取
        try {
            data = jobHistoryService.getBackUpList(serviceId, jobType, starttime, pageable);
        } catch (Exception e) {
            LOG.error("获取备份列表异常， e : {}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, data, retMessage);
        }

        retMessage = "获取备份列表成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, data, retMessage);
    }

    /**
     * 操作记录列表:所有任务的操作记录
     * <p>
     * 任务类型包括采集、备份、恢复；
     * </p>
     * <p>
     * 任务状态包括执行成功、执行失败
     * </p>
     */
    @ResponseBody
    @RequestMapping(value = "jobhistory", method = RequestMethod.GET)
    @ApiOperation(value = "操作记录列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "jobType", value = "任务类型", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "starttime", value = "启动时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "任务状态", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "2000", dataType = "int") })
    public ApiResult getJobHistory(@RequestParam(value = "serviceId") String serviceId,
            @RequestParam(value = "jobType", required = false) Integer jobType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "starttime", required = false) String starttime,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") Integer size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "starttime");
        String retMessage = "获取任务记录列表失败!";
        Page<JobHistory> jobHistoryList = null;

        try {
            jobHistoryList = jobHistoryService.getJobHistory(serviceId, jobType, status, starttime, pageable);
        } catch (Exception e) {
            LOG.error("获取任务记录列表异常， e : {}", e);
        }

        if (null == jobHistoryList) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, jobHistoryList, retMessage);
        }

        retMessage = "获取任务记录列表成功!";

        return new ApiResult(ReturnCode.CODE_SUCCESS, jobHistoryList, retMessage);
    }

    /**
     * 获取关联任务名称
     *
     * @param jobType
     *            the job type must be full_amount_backup
     * @param nodeId
     *            the master node ID
     */
    @ResponseBody
    @RequestMapping(value = "tasks/relationTasks", method = RequestMethod.GET)
    @ApiOperation(value = "获取关联任务名称", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "nodeId", value = "节点ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "jobType", value = "任务类型", required = false, dataType = "int") })
    public ApiResult getRelationJobs(@RequestParam(value = "jobType") Integer jobType,
            @RequestParam(value = "nodeId") String nodeId) {

        LOG.debug("获取关联任务名称-----收到的参数 jobType={}, nodeId= {}", jobType, nodeId);

        // 非增量，直接退出。
        if (BackupReply.JOB_TYPE_INCREMENTAL_BACKUP != jobType) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, null, "任务类型必须是增量！");
        }

        String retMessage = "未查询到可用的全量任务!";
        List<Map<String, String>> data = new ArrayList<>();

        List<Job> jobs = jobService.getJobsByNodeId(nodeId);
        if (null == nodeId || null == jobs) {
            LOG.error("节点ID错误，或者该节点没有对应的任务！");
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, null, retMessage);
        }

        for (Job job : jobs) {
            if (BackupReply.JOB_TYPE_FULL_AMOUNT_BACKUP == job.getJobType()) {
                Map<String, String> map = new HashMap<>(2);
                map.put("jobId", job.getId());
                map.put("jobName", job.getName());
                data.add(map);

                retMessage = "获取关联任务名称成功!";
            }
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, data, retMessage);
    }

    /**
     * 判断当前是否有正在执行的任务
     */
    @ResponseBody
    @RequestMapping(value = "jobhistory/executing", method = RequestMethod.GET)
    @ApiOperation(value = "当前是否有正在执行的任务", notes = "")
    @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult hasExecutingTask(@RequestParam(value = "serviceId") String serviceId) {

        boolean result = true;
        String retMessage = "";

        try {
            result = jobHistoryService.hasExecutingTask(serviceId);
        } catch (Exception e) {
            LOG.error("判断当前是否有正在执行的任务异常！{}", e);
        }

        if (result) {
            retMessage = " 当前集群存在执行中任务，请检查操作记录! ";
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, null, retMessage);
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, null, retMessage);
    }

    /**
     * 通过备份任务ID，获取该任务最近的备份记录
     *
     * @param jobId
     *            任务ID
     */
    @ResponseBody
    @RequestMapping(value = "job/jobHistory", method = RequestMethod.GET)
    @ApiOperation(value = "通过备份任务ID，获取该任务最近的备份记录", notes = "")
    @ApiImplicitParam(paramType = "query", name = "jobId", value = "任务ID", required = true, dataType = "String")
    public ApiResult getLastJobHistory(@RequestParam(value = "jobId") String jobId) {

        String retMessage = "获取任务最近的备份记录失败!";
        JobHistory jobHistory;

        LOG.debug("通过备份任务ID，获取该任务最近的备份记录-----收到的参数 jobId={}", jobId);

        try {
            Job job = jobService.getOneTask(jobId);
            if (null == job) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, null, retMessage);
            }

            jobHistory = jobHistoryService.getLastJobHistoryByJobID(jobId);
        } catch (Exception e) {
            LOG.error("获取任务最近的备份记录异常！{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, null, retMessage);
        }

        retMessage = "获取任务最近的备份记录成功！";

        return new ApiResult(ReturnCode.CODE_SUCCESS, jobHistory, retMessage);
    }

    /**
     * 新增JobHistory
     */
    @ResponseBody
    @RequestMapping(value = "job/jobHistory", method = RequestMethod.POST)
    @ApiOperation(value = "新增JobHistory", notes = "")
    public ApiResult createJobHistory(@RequestBody JobHistoryDTO json, BindingResult errors) {

        String retMessage = "新增JobHistory记录失败!";
        JobHistory savedJobHistory = null;

        LOG.debug("新增JobHistory-----收到的参数 jobHistory={}", JSONObject.toJSONString(json));

        // 1、参数校验
        if (errors.hasErrors()) {
            retMessage = errors.getAllErrors().get(0).getDefaultMessage();
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, savedJobHistory, retMessage);
        }

        JobHistory jobHistory = generateJobHistory(json, null);

        // 2、保存数据
        try {
            savedJobHistory = jobHistoryService.save(jobHistory);
        } catch (Exception e) {
            LOG.error("新增JobHistory----保存savedJobHistory数据异常！{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, savedJobHistory, retMessage);
        }

        if (null == savedJobHistory) {
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, savedJobHistory, retMessage);
        }

        retMessage = "新增JobHistory记录成功！";

        return new ApiResult(ReturnCode.CODE_SUCCESS, savedJobHistory, retMessage);
    }

    private JobHistory generateJobHistory(JobHistoryDTO json, String id) {

        JobHistory jobHistory = new JobHistory();
        jobHistory.setId(id);
        jobHistory.setCosttime(json.getCosttime());
        jobHistory.setEndtime(json.getEndtime());
        jobHistory.setJobId(json.getJobId());
        jobHistory.setJobName(json.getJobName());
        jobHistory.setJobType(json.getJobType());
        jobHistory.setLastJobHistoryId(json.getLastJobHistoryId());
        jobHistory.setNodeId(json.getNodeId());
        jobHistory.setNodeName(json.getNodeName());
        jobHistory.setRelativePath(json.getRelativePath());
        jobHistory.setServiceId(json.getServiceId());
        jobHistory.setStarttime(json.getStarttime());
        jobHistory.setStatus(json.getStatus());
        return jobHistory;
    }

    /**
     * 更新JobHistory
     */
    @ResponseBody
    @RequestMapping(value = "job/jobHistory/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "更新JobHistory", notes = "")
    @ApiImplicitParam(paramType = "path", name = "id", value = "任务ID", required = true, dataType = "String")
    public ApiResult updateJobHistory(@PathVariable("id") String id, @RequestBody JobHistoryDTO json,
            BindingResult errors) {

        String retMessage = "更新JobHistory记录失败!";
        Object nullObj = null;

        LOG.debug("更新JobHistory记录-----收到的参数 id={} , jobHistory", id, JSONObject.toJSONString(json));

        // 1、参数校验
        if (errors.hasErrors()) {
            retMessage = errors.getAllErrors().get(0).getDefaultMessage();
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, nullObj, retMessage);
        }

        JobHistory jobHistory = generateJobHistory(json, id);

        if (null == jobHistoryService.findOne(id)) {
            LOG.error("更新JobHistory记录----该记录ID ：{}没有对应的记录！", id);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, nullObj, retMessage);
        }

        // 2、更新数据库JOB
        jobHistory.setId(id);
        try {
            jobHistoryService.update(jobHistory);
        } catch (Exception e) {
            LOG.error("更新JobHistory记录异常!{}", e);
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, nullObj, retMessage);
        }

        retMessage = "更新JobHistory记录成功！";

        return new ApiResult(ReturnCode.CODE_SUCCESS, nullObj, retMessage);
    }

    /**
     * 参数预处理
     *
     * @param serviceId
     * @param nodeId
     * @param jobType
     * @param status
     * @param starttime
     * @return
     */
    @SuppressWarnings("unused")
    private JobHistory initParam(String serviceId, String nodeId, Integer jobType, Integer status, String starttime) {
        JobHistory jobHistory = new JobHistory();

        if (StringUtils.isNotEmpty(serviceId)) {
            jobHistory.setServiceId(serviceId);
        }
        if (StringUtils.isNotEmpty(nodeId)) {
            jobHistory.setNodeId(nodeId);
        }

        if (null != jobType) {
            jobHistory.setJobType(jobType);
        }
        if (null != status) {
            jobHistory.setStatus(status);
        }

        // 时间拆分。
        if (StringUtils.isNotEmpty(starttime)) {
            Date firstTimeDate = new Date();
            Date lastTimeDate = new Date();
            try {
                String[] str = starttime.split("_");
                firstTimeDate = new Date(Long.valueOf(str[0]));
                lastTimeDate = new Date(Long.valueOf(str[1]));
            } catch (NumberFormatException e) {
                LOG.error("日期转换出现异常，筛选时间重置为现在！", e);
            }

            // 这里的开始时间就是筛选条件的第一个时间，结束时间是筛选条件的第二个时间
            jobHistory.setStarttime(firstTimeDate);
            jobHistory.setEndtime(lastTimeDate);
        }

        return jobHistory;
    }

    /**
     * 新增一条恢复任务信息
     *
     * @param job
     *            接收参数的Job对象
     * @param errors
     *            参数校验的错误信息集合体
     */
    @ResponseBody
    @RequestMapping(value = "tasks/recover", method = RequestMethod.POST)
    @ApiOperation(value = "新增一条恢复任务信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "jobHistoryId", value = "jobHistoryID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult recover(@RequestParam(value = "jobHistoryId", required = true) String jobHistoryId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {
        LOG.info("恢复任务-----收到的参数 job:" + jobHistoryId);

        // 1、参数校验
        if (StringUtils.isEmpty(jobHistoryId)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, null, "参数为空！");
        }

        // 3、查询JobHistory
        boolean flag = false;
        try {
            flag = jobService.recover(jobHistoryId, tenantName);
        } catch (Exception e) {
            LOG.error("恢复任务异常！", e);
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, null, "恢复任务失败！");
        }
        if (!flag) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, null, "恢复任务失败！");
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, null, "恢复任务成功！");
    }

}
