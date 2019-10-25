package com.xxx.xcloud.module.tenant.async;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.utils.FtpUtils;

/**
 * @ClassName: FtpAsyncHelper
 * @Description: ftp相关操作
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
public class FtpAsyncHelper extends BaseTenantAsyncHelper {

    private static final Logger logger = LoggerFactory.getLogger(FtpAsyncHelper.class);

    private static String OPT_FTP_KEY = "ftpPath";

    @Async
    public void addFtpPath(String tenantName) {

        String path = XcloudProperties.getConfigMap().get(Global.FTP_PATH) + FtpUtils.FTP_SEPARATOR + tenantName;
        String ciPath = path + FtpUtils.FTP_SEPARATOR + "ci";
        String imagePath = path + FtpUtils.FTP_SEPARATOR + "image";

        FTPClient ftpClient = null;
        try {
            ftpClient = FtpUtils.initFtpClient();

            String localpath = ftpClient.printWorkingDirectory();

            FtpUtils.createDir(ciPath, ftpClient);

            ftpClient.changeWorkingDirectory(localpath);

            FtpUtils.createDir(imagePath, ftpClient);

            saveTenant(tenantName, OPT_FTP_KEY, true);
            deleteTenantOpt(tenantName, buildOpt());

            logger.debug("租户 " + tenantName + " 建立  ftp目录 " + path + " 成功!");
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
            saveTenantOpt(tenantName, buildOpt());

            logger.debug("租户 " + tenantName + " 建立  ftp目录 " + path + " 失败!");
        }
        finally {

            FtpUtils.releaseFtpClient(ftpClient);
        }
    }

    @Async
    public void delFtpPath(String tenantName) {

        FTPClient ftpClient = null;
        ftpClient = FtpUtils.initFtpClient();

        String path = XcloudProperties.getConfigMap().get(Global.FTP_PATH) + FtpUtils.FTP_SEPARATOR + tenantName;
        Boolean res = FtpUtils.deleteDir(ftpClient, path);

        if (!res) {

            saveTenantOpt(tenantName, buildOpt());

            logger.debug("租户 " + tenantName + " 删除  ftp目录 " + path + " 失败!");
        } else {

            deleteTenantOpt(tenantName, buildOpt());
            logger.debug("租户 " + tenantName + " 删除  ftp目录 " + path + " 成功!");
        }
    }

}
