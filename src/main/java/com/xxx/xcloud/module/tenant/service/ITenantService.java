package com.xxx.xcloud.module.tenant.service;

import com.xxx.xcloud.module.tenant.entity.Tenant;

/**
 * @ClassName: ITenantService
 * @Description: ITenantService
 * @author huchao
 * @date 2019年10月25日
 *
 */
public interface ITenantService {

    /**
     *  创建租户
     * @Title: createTenant
     * @Description: 创建租户 及后端相关联的信息
     * @param tenantName
     * @return Tenant
     * @throws
     */
    public Tenant createTenant(String tenantName);

    /**
     * 删除租户
     * @Title: deleteTenant
     * @Description: 删除租户 及后端相关联的信息
     * @param tenantName void
     * @throws
     */
    public void deleteTenant(String tenantName);

    /**
     * 查找租户
     * @Title: findTenantByTenantName
     * @Description: 查找租户，一般用于判断租户是否存在
     * @param tenantName
     * @return Tenant
     * @throws
     */
    public Tenant findTenantByTenantName(String tenantName);
}
