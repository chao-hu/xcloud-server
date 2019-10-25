package com.xxx.xcloud.module.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.tenant.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    Tenant findByTenantName(String tenantName);

}
