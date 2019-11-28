package com.xxx.xcloud.module.springcloud.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.springcloud.entity.SpringCloudApplication;


/**
 * @ClassName: SpringCloudApplicationRepository
 * @Description: springcloud应用表
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Repository
public interface SpringCloudApplicationRepository extends JpaRepository<SpringCloudApplication, String> {

    List<SpringCloudApplication> findByTenantNameAndAppName(String tenantName, String appName);

    List<SpringCloudApplication> findByTenantNameAndAppNameAndStateNot(String tenantName, String appName, String state);

    Page<SpringCloudApplication> findByTenantNameAndProjectId(String tenantName, String projectId, Pageable pageable);

    Page<SpringCloudApplication> findByTenantName(String tenantName, Pageable pageable);

    SpringCloudApplication findByTenantNameAndId(String tenantName, String appId);

    SpringCloudApplication findByTenantNameAndIdAndStateNot(String tenantName, String appId, String state);

    Page<SpringCloudApplication> findByTenantNameAndStateNot(String tenantName, String state, Pageable pageable);

    Page<SpringCloudApplication> findByTenantNameAndProjectIdAndStateNot(String tenantName, String projectId,
            String state, Pageable pageable);

    /**
     * @param state
     * @return
     * List<SpringCloudApplication>
     */
    List<SpringCloudApplication> findByState(String state);

}
