package com.xxx.xcloud.module.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.tenant.entity.TenantOptLog;

@Repository
public interface TenantOptLogRepository extends JpaRepository<TenantOptLog, String> {

    TenantOptLog findByTenantNameAndOpt(String tenantName, String opt);
}
