package com.xxx.xcloud.module.component.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.Job;

/**
 * @author xujiangpeng
 * @date 2018/5/5
 */
@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    /**
     * 通过集群分页ID查询任务列表
     *
     * @param serviceId
     * @param backUpTypes
     * @param status
     * @param pageable
     * @return
     */
    Page<Job> findByServiceIdAndJobTypeInAndStatusNot(String serviceId, Integer[] backUpTypes, int status,
            Pageable pageable);

    /**
     * 通过集群ID和任务类型分页查询任务列表
     *
     * @param serviceId
     * @param jobType
     * @param status
     * @param pageable
     * @return
     */
    Page<Job> findByServiceIdAndJobTypeAndStatusNot(String serviceId, int jobType, int status, Pageable pageable);

    /**
     * 通过集群ID和任务状态分页查询任务列表
     *
     * @param serviceId
     * @param status
     * @param backUpTypes
     * @param pageable
     * @return
     */
    Page<Job> findByServiceIdAndStatusAndJobTypeIn(String serviceId, int status, Integer[] backUpTypes,
            Pageable pageable);

    /**
     * 通过集群ID+任务类型+任务状态分页查询任务列表
     *
     * @param serviceId
     * @param jobType
     * @param status
     * @param pageable
     * @return
     */
    Page<Job> findByServiceIdAndJobTypeAndStatus(String serviceId, int jobType, int status, Pageable pageable);

    /**
     * 通过节点ID查询任务列表
     *
     * @param nodeId
     * @return
     */
    List<Job> findByNodeId(String nodeId);

    /**
     * 通过关联任务ID查询任务信息
     *
     * @param jobId
     * @return
     */
    Job findByRelationJobId(String jobId);

    /**
     * 通过集群ID+任务类型范围
     *
     * @param serviceId
     * @param backUpTaskTypes
     * @return
     */
    List<Job> findByServiceIdAndJobTypeIn(String serviceId, Integer[] backUpTaskTypes);

    /**
     * 通过框架类型+节点ID+任务类型
     *
     * @param framework
     * @param nodeId
     * @param jobTypeCollect
     * @return 通过框架类型+节点ID+任务类型获取任务信息
     */
    Job findByAppTypeAndNodeIdAndJobType(String framework, String nodeId, int jobTypeCollect);

    /**
     * 通过集群ID+任务类型+任务状态分页查询任务列表
     *
     * @param serviceId
     * @param jobType
     * @param status
     * @param pageable
     * @return
     */
    List<Job> findByServiceId(String serviceId);

    /**
     * 通过serviceId删除记录
     *
     * @param serviceId
     * @return
     */
    void deleteByServiceId(String serviceId);
}
