package com.xxx.xcloud.module.component.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.JobHistory;

/**
 * @author xujiangpeng
 * @date 2018/5/5
 */
@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory, String> {

    /**
     * 通过集群ID 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceId(String serviceId, Pageable pageable);

    /**
     * 通过集群ID+任务类型 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param type
     * @param jobType
     *            任务类型
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndJobTypeAndStatusNot(String serviceId, Integer type, int jobType,
            Pageable pageable);

    /**
     * 通过集群ID+任务完成状态 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param status
     *            任务完成状态
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndStatus(String serviceId, int status, Pageable pageable);

    /**
     * 通过集群ID+任务开始时间段 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param deletedRecord
     * @param firstTime
     *            任务开始时间段First
     * @param lastTime
     *            任务开始时间段Last
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndStatusNotAndStarttimeBetween(String serviceId, int deletedRecord, Date firstTime,
            Date lastTime, Pageable pageable);

    /**
     * 通过集群ID+任务类型+任务完成状态 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param jobType
     *            任务类型
     * @param status
     *            任务完成状态
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndJobTypeAndStatus(String serviceId, int jobType, int status, Pageable pageable);

    /**
     * 通过集群ID+任务类型+任务开始时间段 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param type
     * @param jobType
     *            任务类型
     * @param firstTime
     *            任务开始时间段First
     * @param lastTime
     *            任务开始时间段Last
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndJobTypeAndStatusNotAndStarttimeBetween(String serviceId, Integer type,
            int jobType, Date firstTime, Date lastTime, Pageable pageable);

    /**
     * 通过集群ID+任务完成状态+任务开始时间段 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param status
     *            任务完成状态
     * @param firstTime
     *            任务开始时间段First
     * @param lastTime
     *            任务开始时间段Last
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndStatusAndStarttimeBetween(String serviceId, int status, Date firstTime,
            Date lastTime, Pageable pageable);

    /**
     * 通过集群ID+任务类型+任务完成状态+任务开始时间段 分页获取任务历史记录
     *
     * @param serviceId
     *            集群ID
     * @param jobType
     *            任务类型
     * @param status
     *            任务完成状态
     * @param firstTime
     *            任务开始时间段First
     * @param lastTime
     *            任务开始时间段Last
     * @param pageable
     *            分页对象
     * @return 含有分页信息的历史任务记录---Page集合
     */
    Page<JobHistory> findByServiceIdAndJobTypeAndStatusAndStarttimeBetween(String serviceId, int jobType, int status,
            Date firstTime, Date lastTime, Pageable pageable);

    /**
     * 通过任务ID+任务完成状态获取单个历史任务记录
     *
     * @param jobId 任务ID
     * @param status 任务完成状态
     * @param jobType  job类型
     * @return 单个历史任务
     */
    JobHistory findTopByJobIdInAndJobTypeNotAndStatusOrderByEndtimeDesc(String[] jobId, int jobType, int status);

    /**
     * 备份列表的条件查询集合
     *
     * @param serviceId
     * @param taskStatusSucceed
     * @param backUpJobTypes
     * @param firstTime
     * @param lastTime
     * @param pageable
     * @return
     */
    Page<JobHistory> findByServiceIdAndStatusAndJobTypeInAndStarttimeBetween(String serviceId, int taskStatusSucceed,
            int[] backUpJobTypes, Date firstTime, Date lastTime, Pageable pageable);

    /**
     * 通过集群ID获取所有任务
     *
     * @param serviceId
     * @return
     */
    List<JobHistory> findByServiceId(String serviceId);

    /**
     * 通过节点ID获取任务记录
     *
     * @param nodeId
     *            节点ID
     * @return
     */
    List<JobHistory> findByNodeId(String nodeId);

    /**
     * 通过JobHistoryID获取任务记录
     *
     * @param jobHistoryId job记录ID
     * @return
     */
    Optional<JobHistory> findById(String jobHistoryId);

    /**
     * 通过serviceId和nodeName获取任务历史记录
     *
     * @param serviceId  service ID
     * @param nodeName  节点名称
     * @return
     */
    List<JobHistory> findByServiceIdAndNodeName(String serviceId, String nodeName);

    /**
     * 通过serviceId删除记录
     *
     * @param serviceId  service ID
     * @return
     */
    void deleteByServiceId(String serviceId);

}
