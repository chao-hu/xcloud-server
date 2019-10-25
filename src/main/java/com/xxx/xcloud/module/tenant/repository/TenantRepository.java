package com.xxx.xcloud.module.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.tenant.entity.Tenant;

/**
 * @ClassName: TenantRepository
 * @Description: TenantRepository
 * @author huchao
 * @date 2019年10月25日
 *
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    /**
     * 通过租户名称查找租户
     * @Title: findByTenantName
     * @Description: 通过租户名称查找租户
     * @param tenantName
     * @return Tenant
     * @throws
     */
    Tenant findByTenantName(String tenantName);

}
