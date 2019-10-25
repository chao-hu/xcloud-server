package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassName: HarborAsyncHelper
 * @Description: harbor相关操作
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
public class HarborAsyncHelper extends BaseTenantAsyncHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HarborAsyncHelper.class);

    private static String OPT_ADD_HARBOE_USER = "addHarborUser";
    private static String OPT_DEL_HARBOE_USER = "delHarborUser";
    private static String OPT_ADD_HARBOE_PROJECT = "addHarborProject";
    private static String OPT_DEL_HARBOE_PROJECT = "delHarborProject";
    private static String OPT_ADD_HARBOE_SECRET = "addHarborSecret";
    private static String OPT_DEL_HARBOE_SECRET = "delHarborSecret";

    private static String OPT_HARBOE_USER_KEY = "harborUser";
    private static String OPT_HARBOE_PROJECT_KEY = "harborProject";
    private static String OPT_HARBOE_SECRET_KEY = "harborSecret";

    public void addHarborUser(String tenantName) {

    }

    public void delHarborUser(String tenantName) {

    }

    public void addHarborProject(String tenantName) {

    }

    public void delHarborProject(String tenantName) {

    }

    public void addHarborSecret(String tenantName) {

    }

    public void delHarborSecret(String tenantName) {

    }
}
