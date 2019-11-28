package com.xxx.xcloud.module.springcloud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;


/**
 * @ClassName: SpringCloudServiceRepository
 * @Description: spring cloud 服务表
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Repository
public interface SpringCloudServiceRepository extends JpaRepository<SpringCloudService, String> {

    List<SpringCloudService> findByTenantNameAndAppName(String tenantName, String appName);

    /**
     * serviceId
     */
    public Optional<SpringCloudService> findById(String serviceId);

    /**
     * serviceId，非serviceState状态
     * 
     * @param serviceId
     * @param state
     * @return
     */
    public SpringCloudService findByIdAndServiceStateNot(String serviceId, String state);

    /**
     * @param appId
     * @param appType
     * @return SpringCloudService
     */
    SpringCloudService findByAppIdAndAppType(String appId, String appType);

    public Page<SpringCloudService> findByTenantNameAndAppId(String tenantName, String appId, PageRequest pageable);

    public List<SpringCloudService> findByAppId(String appId);

    Page<SpringCloudService> findByTenantNameAndAppIdAndServiceStateNot(String tenantName, String appId,
            String serviceState, Pageable pageable);

    /**
     * @param tenantName
     * @param serviceName
     * @return SpringCloudService
     */
    SpringCloudService findByTenantNameAndServiceNameAndServiceStateNot(String tenantName, String serviceName,
            String serviceState);
}
