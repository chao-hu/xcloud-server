package com.xxx.xcloud.module.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.tenant.entity.TenantOptLog;

/**
 * @ClassName: TenantOptLogRepository
 * @Description: TenantOptLogRepository
 * @author huchao
 * @date 2019年10月25日
 *
 */
@Repository
public interface TenantOptLogRepository extends JpaRepository<TenantOptLog, String> {

    /**
     * 通过租户名称和opt一起查找对象
     * @Title: findByTenantNameAndOpt
     * @Description: 通过租户名称和opt一起查找对象
     * @param tenantName
     * @param opt
     * @return TenantOptLog
     * @throws
     */
    TenantOptLog findByTenantNameAndOpt(String tenantName, String opt);
}
