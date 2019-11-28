package com.xxx.xcloud.module.backup.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.backup.entity.JobHistory;

/**
 * @ClassName: IJobHistoryService
 * @Description: jobHistory接口
 * @author lnn
 * @date 2019年11月21日
 *
 */
public interface IJobHistoryService {

    /**
     * 通过集群ID获取备份历史任务列表
     *
     * @param serviceId
     * @param jobType
     * @param starttime
     * @param pageable
     * @return
     */
    Page<JobHistory> getBackUpList(String serviceId, Integer jobType, String starttime, Pageable pageable);

    /**
     * 获取历史任务记录
     *
     * @param serviceId
     * @param jobType
     * @param status
     * @param starttime
     * @param pageable
     * @return
     */
    Page<JobHistory> getJobHistory(String serviceId, Integer jobType, Integer status, String starttime,
            Pageable pageable);

    /**
     * 是否存在执行中的任务
     *
     * @param serviceId
     * @return
     */
    boolean hasExecutingTask(String serviceId);

    /**
     * 通过节点ID查询所有的操作记录
     *
     * @param nodeId
     *            节点ID
     * @return
     */
    List<JobHistory> getJobHistoryByNodeId(String nodeId);

    /**
     * 删除操作记录（软删）
     *
     * @param jobHistoryId 操作记录ID
     * @throws ErrorMessageException
     */
    void delete(String jobHistoryId) throws ErrorMessageException;
    
    /**
     * TODO 通过任务ID获取最新的执行记录
     * @Title: getLastJobHistoryByJobID
     * @Description: 通过任务ID获取最新的执行记录
     * @param jobId
     * @return JobHistory 
     * @throws
     */
    JobHistory getLastJobHistoryByJobID(String jobId);

    /**
     * 新增任务执行记录
     *
     * @param savedJobHistory
     * @return
     */
    JobHistory save(JobHistory savedJobHistory);

    /**
     * 查找任务执行记录
     *
     * @param id
     * @return
     */
    Object findOne(String id);

    /**
     * 更新任务执行记录
     *
     * @param jobHistory
     * @return
     */
    void update(JobHistory jobHistory);
}
