package com.xxx.xcloud.module.tenant.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.repository.TenantRepository;
import com.xxx.xcloud.module.tenant.service.ITenantService;

@Service
public class TenantServiceImpl implements ITenantService {

    private static Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * @Title: createTenant
     * @Description:
     * @param tenantName
     * @return
     * @throws ErrorMessageException
     * @see com.xxx.xcloud.module.tenant.service.ITenantService#createTenant(java.lang.String)
     */
    @Override
    public Tenant createTenant(String tenantName) {

        Tenant obj = tenantRepository.findByTenantName(tenantName);

        if (null == obj) {

            obj = new Tenant();
            obj.setTenantName(tenantName);
            obj.setCreateTime(new Date());

            tenantRepository.save(obj);
        }

        logger.debug("create tenant " + tenantName);
        return obj;
    }

    /**
     * @Title: deleteTenant
     * @Description:
     * @param tenantName
     * @throws ErrorMessageException
     * @see com.xxx.xcloud.module.tenant.service.ITenantService#deleteTenant(java.lang.String)
     */
    @Override
    public void deleteTenant(String tenantName) {

        Tenant obj = tenantRepository.findByTenantName(tenantName);

        if (null == obj) {

            tenantRepository.delete(obj);
        }

        logger.debug("delete tenant " + tenantName);
    }

    /**
     * @Title: findTenantByTenantName
     * @Description:
     * @param tenantName
     * @return
     * @throws ErrorMessageException
     * @see com.xxx.xcloud.module.tenant.service.ITenantService#findTenantByTenantName(java.lang.String)
     */
    @Override
    public Tenant findTenantByTenantName(String tenantName) {

        return tenantRepository.findByTenantName(tenantName);
    }

}
