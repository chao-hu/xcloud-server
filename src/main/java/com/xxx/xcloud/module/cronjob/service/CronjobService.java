package com.xxx.xcloud.module.cronjob.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.module.cronjob.entity.Cronjob;
import com.xxx.xcloud.rest.v1.cronjob.dto.JobInfoDTO;

/**
 * 
 * <p>
 * Description: 定时任务功能接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
public interface CronjobService {

    /**
     * 
     * <p>
     * Description:创建定时任务
     * </p>
     *
     * @param cronjob
     *            定时任务
     * @return 成功返回Cronjob，失败抛出异常
     */
    Cronjob createCronjob(Cronjob cronjob);

    /**
     * 
     * <p>
     * Description:启动定时任务
     * </p>
     *
     * @param cronjobId
     *            定时任务id
     * @return 成功返回true，失败抛出异常
     */
    boolean startCronjob(String cronjobId);

    /**
     * 
     * <p>
     * Description: 停止定时任务
     * </p>
     *
     * @param cronjobId
     *            定时任务id
     * @return 成功返回true，失败抛出异常
     */
    boolean stopCronjob(String cronjobId);

    /**
     * 
     * <p>
     * Description: 删除定时任务
     * </p>
     *
     * @param cronjobId
     *            定时任务id
     * @return 成功返回true，失败抛出异常
     */
    boolean deleteCronjob(String cronjobId);

    /**
     * 
     * <p>
     * Description: 更新定时任务
     * </p>
     *
     * @param cronjob
     *            定时任务
     * @return Cronjob
     */
    Cronjob updateCronjob(Cronjob cronjob);

    /**
     * 
     * <p>
     * Description: 根据定时任务id获取定时任务详情
     * </p>
     *
     * @param cronjobId
     *            定时任务id
     * @return Cronjob
     */
    Cronjob getCronjobById(String cronjobId);

    /**
     * 
     * <p>
     * Description: 根据定时任务名称和租户名称获取定时任务详情
     * </p>
     *
     * @param cronjobName
     *            定时任务名称
     * @param tenantName
     *            租户名称
     * @return Cronjob
     */
    Cronjob getCronjobByNameAndTenantName(String cronjobName, String tenantName);

    /**
     * 
     * <p>
     * Description: 获取定时任务列表
     * </p>
     *
     * @param tenantName
     *            租户名
     * @param cronjobName
     *            模糊搜索的定时任务名称
     * @param projectId
     *            项目id
     * @param pageable
     *            分页参数信息
     * @return Page<Cronjob>
     */
    Page<Cronjob> getCronjobList(String tenantName, String cronjobName, String projectId, Pageable pageable);

    /**
     * 
     * <p>
     * Description: 验证定时任务名称是否可用
     * </p>
     *
     * @param tenantName
     *            租户名称
     * @param cronjobName
     *            定时任务名称
     * @return 可用时返回true
     */
    boolean validateCronjobName(String tenantName, String cronjobName);

    /**
     * 
     * <p>
     * Description: 获取当前cronjob创建的job的信息
     * </p>
     *
     * @param cronjobId
     *            定时任务id
     * @return List<JobInfoDTO>
     */
    List<JobInfoDTO> getRelatedJobs(String cronjobId);
}
