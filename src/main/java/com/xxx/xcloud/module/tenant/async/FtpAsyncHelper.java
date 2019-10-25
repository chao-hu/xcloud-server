package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassName: FtpAsyncHelper
 * @Description: ftp相关操作
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
public class FtpAsyncHelper extends BaseTenantAsyncHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FtpAsyncHelper.class);

    private static String OPT_ADD_FTP_PATH = "addFtpPath";
    private static String OPT_DEL_FTP_PATH = "delFtpPath";

    private static String OPT_FTP_KEY = "ftpPath";

    public void addFtpPath(String tenantName) {

    }

    public void delFtpPath(String tenantName) {

    }

}
