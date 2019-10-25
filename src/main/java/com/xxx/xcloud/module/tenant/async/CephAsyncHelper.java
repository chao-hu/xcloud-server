package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @ClassName: CephAsyncHelper
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
public class CephAsyncHelper extends BaseTenantAsyncHelper {

    private static final Logger logger = LoggerFactory.getLogger(CephAsyncHelper.class);

    private static String OPT_CEPH_SECRET_KEY = "cephSecret";
    private static String OPT_CEPH_NAMESPACE_KEY = "cephNamespace";

    @Async
    public void addCephSecret(String tenantName) {

    }

    @Async
    public void delCephSecret(String tenantName) {

    }

    @Async
    public void addCephNamespace(String tenantName) {

    }

    @Async
    public void delCephNamespace(String tenantName) {

    }
}
