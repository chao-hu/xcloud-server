package com.xxx.xcloud.module.application.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.application.entity.Service;

/**
 *
 * <p>
 * Description: 服务持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月6日
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {

    /**
     * Description:通过服务名称和租户名称查询
     * 
     * @param serviceName
     *            服务名称
     * @param tenantname
     *            租户名称
     * @return Service
     */
    Service findByServiceNameAndTenantName(String serviceName, String tenantname);

    /**
     * Description:获取服务列表
     *
     * @param tenantName
     *            租户名
     * @param projectId
     *            项目id
     * @param serviceName
     *            模糊搜索的服务名
     * @param pageable
     *            分页数据
     * @return Page<Service>
     */
    Page<Service> findByTenantNameAndProjectIdAndServiceNameLikeOrderByCreateTimeDesc(String tenantName,
            String projectId, String serviceName, Pageable pageable);

    /**
     * 
     * <p>
     * Description: 根据服务名称和租户名称分页查询服务
     * </p>
     *
     * @param tenantName
     *            租户名称
     * @param serviceName
     *            服务名称(模糊查询)
     * @param pageable
     *            分页数据
     * @return Page<Service>
     */
    Page<Service> findByTenantNameAndServiceNameLikeOrderByCreateTimeDesc(String tenantName, String serviceName,
            Pageable pageable);

    /**
     * find
     * 
     * @param tenantName
     * @param projectId
     * @param pageable
     * @return Page<Service>
     * @date: 2019年11月11日 下午4:21:20
     */
    Page<Service> findByTenantNameAndProjectIdOrderByCreateTimeDesc(String tenantName, String projectId,
            Pageable pageable);

    /**
     * find
     * 
     * @param tenantName
     * @param pageable
     * @return Page<Service>
     * @date: 2019年11月11日 下午4:21:36
     */
    Page<Service> findByTenantNameOrderByCreateTimeDesc(String tenantName, Pageable pageable);

    /**
     * find
     * 
     * @param imageVersionId
     * @return List<Service>
     * @date: 2019年11月11日 下午4:21:43
     */
    List<Service> findByImageVersionId(String imageVersionId);

    /**
     * find
     * 
     * @param tenantName
     * @return List<Service>
     * @date: 2019年11月11日 下午4:21:50
     */
    List<Service> findByTenantName(String tenantName);

    /**
     * find
     * 
     * @param tenantName
     * @param serviceName
     * @return List<Service>
     * @date: 2019年11月11日 下午4:21:55
     */
    List<Service> findByTenantNameAndServiceName(String tenantName, String serviceName);

    /**
     * find
     * 
     * @param status
     * @return List<Service>
     * @date: 2019年11月11日 下午4:22:01
     */
    List<Service> findByStatus(Byte status);
}
