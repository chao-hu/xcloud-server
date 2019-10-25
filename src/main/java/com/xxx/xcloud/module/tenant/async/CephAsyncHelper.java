package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(CephAsyncHelper.class);

    private static String OPT_ADD_CEPH_SECRET = "addCephSecret";
    private static String OPT_DEL_CEPH_SECRET = "delCephSecret";
    private static String OPT_ADD_CEPH_NAMESPACE = "addCephNamespace";
    private static String OPT_DEL_CEPH_NAMESPACE = "delCephNamespace";

    private static String OPT_CEPH_SECRET_KEY = "cephSecret";
    private static String OPT_CEPH_NAMESPACE_KEY = "cephNamespace";

    public void addCephSecret(String tenantName) {

    }

    public void delCephSecret(String tenantName) {

    }

    public void addCephNamespace(String tenantName) {

    }

    public void delCephNamespace(String tenantName) {

    }
}
