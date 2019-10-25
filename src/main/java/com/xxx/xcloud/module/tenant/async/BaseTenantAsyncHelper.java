/**
 * @Title: BaseTenantAsyncHelper.java
 * @Package com.xxx.xcloud.module.tenant.async
 * @Description: TODO(用一句话描述该文件做什么)
 * @author huchao
 * @date 2019年10月24日
 * @version V1.0
 */
package com.xxx.xcloud.module.tenant.async;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.entity.TenantOptLog;
import com.xxx.xcloud.module.tenant.repository.TenantOptLogRepository;
import com.xxx.xcloud.module.tenant.repository.TenantRepository;

/**
 * @ClassName: BaseTenantAsyncHelper
 * @Description: 租户异步操作基类
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class BaseTenantAsyncHelper {

    @Autowired
    TenantOptLogRepository tenantOptLogRepository;

    @Autowired
    TenantRepository tenantRepository;

    public void saveTenantOpt(String tenantName, String opt) {

        TenantOptLog optLog = tenantOptLogRepository.findByTenantNameAndOpt(tenantName, opt);

        Integer count = 1;
        if (null == optLog) {

            optLog = new TenantOptLog();
            optLog.setOpt(opt);
            optLog.setTenantName(tenantName);
        } else {

            count = optLog.getRetries() + 1;
        }

        optLog.setRetries(count);

        optLog.setLastTime(new Date());

        tenantOptLogRepository.save(optLog);
    }

    public void saveTenantOpt(String optId) {

    }

    public void deleteTenantOpt(String tenantName, String opt) {

        TenantOptLog optLog = tenantOptLogRepository.findByTenantNameAndOpt(tenantName, opt);

        if (null != optLog) {

            tenantOptLogRepository.delete(optLog);
        }
    }

    public void deleteTenantOpt(String optId) {

    }

    public void saveTenant(String tenantName, String key, Object val) {

        Tenant tenant = tenantRepository.findByTenantName(tenantName);

        if (null == tenant) {

            tenant = new Tenant();
            tenant.setTenantName(tenantName);
            tenant.setCreateTime(new Date());
        }

        tenant.setUpdateTime(new Date());
        tenant.addOrReplaceOperatorInfo(key, val);

        tenantRepository.save(tenant);
    }

}
