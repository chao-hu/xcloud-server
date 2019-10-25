package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
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

    private static final Logger logger = LoggerFactory.getLogger(HarborAsyncHelper.class);

    private static String OPT_HARBOE_USER_KEY = "harborUser";
    private static String OPT_HARBOE_PROJECT_KEY = "harborProject";
    private static String OPT_HARBOE_SECRET_KEY = "harborSecret";

    @Async
    public void addHarborUser(String tenantName) {

    }

    @Async
    public void delHarborUser(String tenantName) {

    }

    @Async
    public void addHarborProject(String tenantName) {

    }

    @Async
    public void delHarborProject(String tenantName) {

    }

    @Async
    public void addHarborSecret(String tenantName) {

    }

    @Async
    public void delHarborSecret(String tenantName) {

    }
}
