package com.xxx.xcloud.module.backup.service.impl;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.backup.entity.JobHistory;
import com.xxx.xcloud.module.backup.repository.JobHistoryRepository;
import com.xxx.xcloud.module.backup.service.IJobHistoryService;
import com.xxx.xcloud.module.component.consts.MysqlBackupConst;
import com.xxx.xcloud.utils.FtpUtils;
import com.xxx.xcloud.utils.StringUtils;

/**
 * jobHistory接口实现类
 * 
 * @author LiuYue
 * @date 2018年12月17日
 */
@Component
public class JobHistoryServiceImpl implements IJobHistoryService {

    private static Logger LOG = LoggerFactory.getLogger(JobHistoryServiceImpl.class);

    @Autowired
    JobHistoryRepository jobHistoryRepository;

    @Override
    public Page<JobHistory> getBackUpList(String serviceId, Integer jobType, String starttime, Pageable pageable) {

        int[] backUpJobTypes = new int[2];
        int taskStatusSucceed = MysqlBackupConst.JOB_HISTORY_STATUS_SUCCEED;
        Date[] taskStartTime = initTaskStartTime(starttime);

        // 任务类型处理
        if (null == jobType) {
            backUpJobTypes[0] = MysqlBackupConst.JOB_TYPE_FULL_AMOUNT_BACKUP;
            backUpJobTypes[1] = MysqlBackupConst.JOB_TYPE_INCREMENTAL_BACKUP;
        } else {
            backUpJobTypes[0] = jobType;
        }

        return jobHistoryRepository.findByServiceIdAndStatusAndJobTypeInAndStarttimeBetween(serviceId,
                taskStatusSucceed, backUpJobTypes, taskStartTime[0], taskStartTime[1], pageable);
    }

    @Override
    public Page<JobHistory> getJobHistory(String serviceId, Integer jobType, Integer status, String starttime,
            Pageable pageable) {

        Date[] taskStartTime = initTaskStartTime(starttime);

        int deletedRecord = MysqlBackupConst.JOB_HISTORY_STATUS_DETETED;

        /**
         * 单个条件分类-----3个
         */
        // 1 通过集群ID+任务类型获取任务操作记录
        if (jobType != null && status == null && null == starttime) {
            return jobHistoryRepository.findByServiceIdAndJobTypeAndStatusNot(serviceId, jobType, deletedRecord,
                    pageable);
        }

        // 2 通过集群ID+任务状态获取任务操作记录
        if (jobType == null && status != null && null == starttime) {
            return jobHistoryRepository.findByServiceIdAndStatus(serviceId, status, pageable);
        }

        // 3 通过集群ID+任务开始时间获取任务操作记录
        if (jobType == null && status == null && null != starttime) {
            return jobHistoryRepository.findByServiceIdAndStatusNotAndStarttimeBetween(serviceId, deletedRecord,
                    taskStartTime[0], taskStartTime[1], pageable);
        }

        /**
         * 两个条件分类---3个
         */
        // 12 通过集群ID+jobType+status获取任务操作记录
        if (jobType != null && status != null && null == starttime) {
            return jobHistoryRepository.findByServiceIdAndJobTypeAndStatus(serviceId, jobType, status, pageable);
        }

        // 13 通过集群ID+jobType+starttime获取任务操作记录
        if (jobType != null && status == null && null != starttime) {
            return jobHistoryRepository.findByServiceIdAndJobTypeAndStatusNotAndStarttimeBetween(serviceId, jobType,
                    deletedRecord, taskStartTime[0], taskStartTime[1], pageable);
        }

        // 23 通过集群ID+status+starttime获取任务操作记录
        if (jobType == null && status != null && null != starttime) {
            return jobHistoryRepository.findByServiceIdAndStatusAndStarttimeBetween(serviceId, status, taskStartTime[0],
                    taskStartTime[1], pageable);
        }

        /**
         * 三个判断条件--4个
         */
        // 123 通过集群ID+jobType+status+starttime获取任务操作记录
        if (jobType != null && status != null && null != starttime) {
            return jobHistoryRepository.findByServiceIdAndJobTypeAndStatusAndStarttimeBetween(serviceId, jobType,
                    status, taskStartTime[0], taskStartTime[1], pageable);
        }

        return jobHistoryRepository.findByServiceId(serviceId, pageable);
    }

    @Override
    public boolean hasExecutingTask(String serviceId) {
        List<JobHistory> jobHistories = jobHistoryRepository.findByServiceId(serviceId);

        if (null != jobHistories) {
            for (JobHistory jobHistory : jobHistories) {
                if (MysqlBackupConst.JOB_HISTORY_STATUS_DOING == jobHistory.getStatus()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public List<JobHistory> getJobHistoryByNodeId(String nodeId) {
        return jobHistoryRepository.findByNodeId(nodeId);
    }

    /**
     * 修改任务记录的状态为软删除 删除备份记录产生的数据
     */
    @Override
    public void delete(String jobHistoryId) throws ErrorMessageException {

        // 1、修改任务记录的状态为软删除
        JobHistory jobHistory = null;
        Optional<JobHistory> jobHistoryOptional = jobHistoryRepository.findById(jobHistoryId);
        if (jobHistoryOptional.isPresent()) {
            jobHistory = jobHistoryOptional.get();
        }
        if (null == jobHistory) {
            LOG.error("jobHistoryId:" + jobHistoryId + "没有对应的记录！");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "jobHistoryId:" + jobHistoryId + "没有对应的记录！");
        }
        jobHistory.setStatus(MysqlBackupConst.JOB_HISTORY_STATUS_DETETED);
        jobHistoryRepository.save(jobHistory);

        // 2、删除备份记录产生的数据

        File file = new File(jobHistory.getRelativePath());

        String fileName = file.getName();
        String filePath = file.getPath().replaceAll(fileName, "");

        FtpUtils.removeFile(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                Integer.valueOf(XcloudProperties.getConfigMap().get(Global.FTP_PORT)), filePath, fileName);
    }

    @Override
    public JobHistory getLastJobHistoryByJobID(String jobId) {

        String[] strs = new String[] { jobId };

        return jobHistoryRepository.findTopByJobIdInAndJobTypeNotAndStatusOrderByEndtimeDesc(strs,
                MysqlBackupConst.JOB_TYPE_RECOVER, MysqlBackupConst.JOB_HISTORY_STATUS_SUCCEED);
    }

    @Override
    public JobHistory save(JobHistory savedJobHistory) {
        return jobHistoryRepository.save(savedJobHistory);
    }

    @Override
    public Object findOne(String id) {
        Object obj = null;
        Optional<JobHistory> jobHistoryOptional = jobHistoryRepository.findById(id);
        if (jobHistoryOptional.isPresent()) {
            obj = jobHistoryOptional.get();
        }
        return obj;
    }

    @Override
    public void update(JobHistory jobHistory) {
        jobHistoryRepository.save(jobHistory);
    }

    /**
     * 时间处理。 默认筛选10年前和10年后
     */
    private Date[] initTaskStartTime(String starttime) {

        Date[] taskStartTime = new Date[2];

        if (StringUtils.isEmpty(starttime)) {
            Calendar ca = Calendar.getInstance();
            ca.setTime(new Date());

            ca.add(Calendar.YEAR, -10);
            taskStartTime[0] = ca.getTime();

            ca.add(Calendar.YEAR, +10);
            taskStartTime[1] = ca.getTime();
        } else {

            String[] times = starttime.split("_");
            if (times.length >= 1 && !times[0].isEmpty()) {
                taskStartTime[0] = new Date(Long.valueOf(times[0]));
            } else {
                Calendar ca = Calendar.getInstance();
                ca.setTime(new Date());
                ca.add(Calendar.YEAR, -10);
                taskStartTime[0] = ca.getTime();
            }
            if (times.length == 2 && !times[1].isEmpty()) {
                taskStartTime[1] = new Date(Long.valueOf(times[1]));
            } else {
                Calendar ca = Calendar.getInstance();
                ca.setTime(new Date());
                ca.add(Calendar.YEAR, +10);
                taskStartTime[1] = ca.getTime();
            }

        }
        return taskStartTime;
    }

}
