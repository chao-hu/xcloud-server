package com.xxx.xcloud.module.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.application.entity.ExternalService;

/**
 *
 * <p>
 * Description: 外部外部服务持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2019年7月3日
 */
@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalService, String> {

    /**
     * Description:通过外部服务名称和租户名称查询
     * 
     * @param serviceName
     *            外部服务名称
     * @param tenantname
     *            租户名称
     * @return Service
     */
    ExternalService findByServiceNameAndTenantName(String serviceName, String tenantname);

    /**
     * Description:获取外部服务列表
     *
     * @param tenantName
     *            租户名
     * @param projectId
     *            项目id
     * @param serviceName
     *            模糊搜索的外部服务名
     * @param pageable
     *            分页数据
     * @return Page<ExternalService>
     */
    Page<ExternalService> findByTenantNameAndProjectIdAndServiceNameLikeOrderByCreateTimeDesc(String tenantName,
            String projectId, String serviceName, Pageable pageable);

    /**
     * find
     * 
     * @param tenantName
     * @param projectId
     * @param pageable
     * @return Page<ExternalService>
     * @date: 2019年11月11日 下午4:17:55
     */
    Page<ExternalService> findByTenantNameAndProjectIdOrderByCreateTimeDesc(String tenantName, String projectId,
            Pageable pageable);

    /**
     * find
     * 
     * @param tenantName
     * @param serviceName
     * @param pageable
     * @return Page<ExternalService>
     * @date: 2019年11月11日 下午4:18:02
     */
    Page<ExternalService> findByTenantNameAndServiceNameLikeOrderByCreateTimeDesc(String tenantName, String serviceName,
            Pageable pageable);

    /**
     * find
     * 
     * @param tenantName
     * @param pageable
     * @return Page<ExternalService>
     * @date: 2019年11月11日 下午4:18:51
     */
    Page<ExternalService> findByTenantNameOrderByCreateTimeDesc(String tenantName, Pageable pageable);

}
