package com.xxx.xcloud.module.cronjob.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.cronjob.entity.Cronjob;

/**
 * 
 * <p>
 * Description: 定时任务持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
/**
 * @ClassName: CronjobRepository
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
@Repository
public interface CronjobRepository extends JpaRepository<Cronjob, String> {

    /**
     * 
     * <p>
     * Description: 根据任务名称和租户名称查询定时任务
     * </p>
     *
     * @param name
     *            定时任务名称
     * @param tenantName
     *            租户名称
     * @return Cronjob 定时任务
     */
    Cronjob findByNameAndTenantName(String name, String tenantName);

    /**
     * 
     * <p>
     * Description: 根据任务名称和租户名称分页查询定时任务
     * </p>
     *
     * @param name
     *            定时任务名称(模糊查询)
     * @param tenantName
     *            租户名称
     * @param pageable
     *            分页信息
     * @return Page<Cronjob>
     */
    Page<Cronjob> findByNameLikeAndTenantNameOrderByCreateTimeDesc(String name, String tenantName, Pageable pageable);

    /**
     * 
     * <p>
     * Description: 根据任务名称、租户名称和项目信息分页查询定时任务
     * </p>
     *
     * @param name
     *            定时任务名称(模糊查询)
     * @param tenantName
     *            租户名称
     * @param projectId
     *            项目id
     * @param pageable
     *            分页信息
     * @return Page<Cronjob>
     */
    Page<Cronjob> findByNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc(String name, String tenantName,
            String projectId, Pageable pageable);

    /**
     * 根据镜像版本id查询定时任务
     * @Title: findByImageVerisonId
     * @Description: 根据镜像版本id查询定时任务
     * @param imageVerisonId 镜像版本id
     * @return List<Cronjob> 
     * @throws
     */
    List<Cronjob> findByImageVerisonId(String imageVerisonId);

    /**
     * 根据租户名称删除定时任务
     * @Title: deleteByTenantName
     * @Description: TODO详细描述
     * @param tenantName 租户名称 
     * @throws
     */
    void deleteByTenantName(String tenantName);
}
