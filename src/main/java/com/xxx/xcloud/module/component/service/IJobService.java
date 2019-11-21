package com.xxx.xcloud.module.component.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.entity.Job;

/**
 * @author xujiangpeng
 * @date 2018/5/5
 */
public interface IJobService {

    /**
     * 获取任务列表
     *
     * @param serviceId
     *            集群ID
     * @param jobType
     *            任务类型
     * @param status
     *            任务启用状态 --是/否
     * @param pageable
     *            分页对象
     * @return a list for Job
     * @throws DcosException
     */
    Page<Job> getTasks(String serviceId, Integer jobType, Integer status, Pageable pageable);

    /**
     * 保存任务信息
     *
     * @param nameSpace
     * @param job 要保存的任务实体
     * @return 保存后的任务
     * @throws ErrorMessageException
     */
    Job save(Job job, String nameSpace) throws ErrorMessageException;

    /**
     * 通过任务ID查询单个任务对象
     *
     * @param id
     *            任务ID
     * @return 单个任务对象
     */
    Job getOneTask(String id);

    /**
     * 更新任务信息
     *
     * @param job 要更新的任务实体
     * @param nameSpace
     * @return 是否更新成功
     */
    Job update(Job job, String nameSpace);

    /**
     * 根据任务ID删除指定的任务
     *
     * @param id 任务ID
     * @param nameSpace
     * @return 是否删除成功
     */
    void delete(String id, String nameSpace);

    /**
     * 根据历史任务ID查询任务信息
     *
     * @param jobId
     * @return
     */
    Job getByRelationJobId(String jobId);

    /**
     * 通过节点ID查询所有的任务
     *
     * @param nodeId
     *            节点ID
     * @return
     */
    List<Job> getJobsByNodeId(String nodeId);

    /**
     * 通过节点ID查询所有的任务
     *
     * @param jobHistoryId
     * @param nameSpace
     * @return
     * @throws ErrorMessageException
     */
    boolean recover(String jobHistoryId, String nameSpace) throws ErrorMessageException;

    /**
     * 检查是否存在相同类型的任务
     * 
     * @param job job 当前任务
     * @throws ErrorMessageException
     */
    void checkJobExist(Job job) throws ErrorMessageException;
}
