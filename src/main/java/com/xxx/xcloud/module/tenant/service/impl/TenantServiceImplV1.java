/**
 * @Title: TenantServiceImplV1.java
 * @Package com.xxx.xcloud.module.tenant.service.impl
 * @Description: TODO(用一句话描述该文件做什么)
 * @author huchao
 * @date 2019年10月25日
 * @version V1.0
 */
package com.xxx.xcloud.module.tenant.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.tenant.async.CephAsyncHelper;
import com.xxx.xcloud.module.tenant.async.FtpAsyncHelper;
import com.xxx.xcloud.module.tenant.async.HarborAsyncHelper;
import com.xxx.xcloud.module.tenant.async.K8sAsyncHelper;

/**
 * @ClassName: TenantServiceImplV1
 * @Description: TenantServiceImplV1
 * @author huchao
 * @date 2019年10月25日
 *
 */
@Service
public class TenantServiceImplV1 extends TenantServiceImpl {

    @Autowired
    CephAsyncHelper cephAsyncHelper;

    @Autowired
    FtpAsyncHelper ftpAsyncHelper;

    @Autowired
    HarborAsyncHelper harborAsyncHelper;

    @Autowired
    K8sAsyncHelper k8sAsyncHelper;

    @Override
    public void afterCreateTenant(String tenantName) {

        // step 1
        k8sAsyncHelper.addK8sNamespace(tenantName);
        // step 2
        ftpAsyncHelper.addFtpPath(tenantName);
        // step 3
        cephAsyncHelper.addCephNamespace(tenantName);
        // step 4
        cephAsyncHelper.addCephSecret(tenantName);
        // step 5
        harborAsyncHelper.addHarborProject(tenantName);
        // step 6
        harborAsyncHelper.addHarborUser(tenantName);
        // step 7
        harborAsyncHelper.addHarborSecret(tenantName);
    };

    /**
     * @Title: afterDelTenant
     * @Description:
     * @param tenantName
     * @see com.xxx.xcloud.module.tenant.service.impl.TenantServiceImpl#afterDelTenant(java.lang.String)
     */
    @Override
    public void afterDelTenant(String tenantName) {
        // step 1
        k8sAsyncHelper.delK8sNamespace(tenantName);
        // step 2
        ftpAsyncHelper.delFtpPath(tenantName);
        // step 3
        cephAsyncHelper.delCephNamespace(tenantName);
        // step 4
        cephAsyncHelper.delCephSecret(tenantName);
        // step 5
        harborAsyncHelper.delHarborProject(tenantName);
        // step 6
        harborAsyncHelper.delHarborUser(tenantName);
        // step 7
        harborAsyncHelper.delHarborSecret(tenantName);
    };
}
