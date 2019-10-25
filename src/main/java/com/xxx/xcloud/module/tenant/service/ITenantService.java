package com.xxx.xcloud.module.tenant.service;

import com.xxx.xcloud.module.tenant.entity.Tenant;

/**
 * @author ruzz
 *
 */
public interface ITenantService {

    public Tenant createTenant(String tenantName);

    public void deleteTenant(String tenantName);

    public Tenant findTenantByTenantName(String tenantName);
}
