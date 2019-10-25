package com.xxx.xcloud.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;

/**
 * @ClassName: FtpUtils
 * @Description: TenantServiceImpl
 * @author huchao
 * @date 2019年10月25日
 *
 */
public class FtpUtils {

    private static Logger logger = LoggerFactory.getLogger(FtpUtils.class);

    public static final String FTP_SEPARATOR = "/";

    /**
     * 获取 ftp操作的client
     *
     * @Title: getFTPClient
     * @Description: 获取 ftp操作的client
     * @param @param ftpHost
     * @param @param ftpUserName
     * @param @param ftpPassword
     * @param @param ftpPort
     * @param @return 参数
     * @return FTPClient 返回类型
     * @throws
     */
    public static FTPClient getFTPClient(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient = new FTPClient();
            // 连接FTP服务器
            ftpClient.connect(ftpHost, ftpPort);
            // 登陆FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.warn("未连接到FTP，用户名或密码错误。");
                ftpClient.disconnect();
            } else {
                logger.info("FTP连接成功。");
            }
        } catch (SocketException e) {
            logger.error("FTP的IP地址可能错误，请正确配置。", e);
        } catch (IOException e) {
            logger.error("FTP的端口错误,请正确配置。", e);
        }
        return ftpClient;
    }

    /**
     * 下载文件
     * @Title: downloadFtpFile
     * @Description: 下载文件
     * @param @param ftpHost
     * @param @param ftpUserName
     * @param @param ftpPassword
     * @param @param ftpPort
     * @param @param ftpPath
     * @param @param localPath
     * @param @param fileName 参数
     * @return void 返回类型
     * @throws
     */
    public static void downloadFtpFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort,
            String ftpPath, String localPath, String fileName) {

        FTPClient ftpClient = null;

        try {
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            // 中文支持
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);

            // 创建目录
            File dir = new File(localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File localFile = new File(localPath + File.separatorChar + fileName);
            OutputStream os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(fileName, os);
            os.close();
            ftpClient.logout();

        } catch (FileNotFoundException e) {
            logger.error("没有找到" + ftpPath + "文件", e);
        } catch (SocketException e) {
            logger.error("连接FTP失败.", e);
        } catch (IOException e) {
            logger.error("文件读取错误。", e);
        }

    }

    /**
     * 从FTP服务器下载文件
     *
     * @param ftpHost
     *            FTP IP地址
     *
     * @param ftpUserName
     *            FTP 用户名
     *
     * @param ftpPassword
     *            FTP用户名密码
     *
     * @param ftpPort
     *            FTP端口
     *
     * @param ftpPath
     *            FTP服务器中文件所在路径 格式： ftptest/aa
     *
     * @param localPath
     *            下载到本地的位置 格式：H:/download
     *
     * @param fileName
     *            文件名称(多个文件, 以逗号分隔)
     *
     * @return boolean 是否下载成功
     */
    public static boolean downloadFtpFiles(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort,
            String ftpPath, String localPath, String[] fileNames) {

        FTPClient ftpClient = null;
        boolean isDownloadSuccess = false;
        try {
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            // 中文支持
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);
            // 创建目录
            File dir = new File(localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (String fileName : fileNames) {
                fileName = fileName.trim();
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                File localFile = new File(localPath + File.separatorChar + fileName);
                OutputStream os = new FileOutputStream(localFile);
                isDownloadSuccess = ftpClient.retrieveFile(fileName, os);
                os.close();
                if (!isDownloadSuccess) {
                    break;
                }
            }
            ftpClient.logout();

        } catch (FileNotFoundException e) {
            logger.error("没有找到" + ftpPath + "文件", e);
        } catch (SocketException e) {
            logger.error("连接FTP失败.", e);
        } catch (IOException e) {
            logger.error("文件读取错误。", e);
        } catch (Exception e) {
            logger.error("文件下载错误。", e);
        }
        return isDownloadSuccess;

    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param ftpHost
     *            FTP服务器hostname
     * @param ftpUserName
     *            账号
     * @param ftpPassword
     *            密码
     * @param ftpPort
     *            端口
     * @param ftpPath
     *            FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName
     *            ftp文件名称
     * @param input
     *            文件流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort,
            String ftpPath, String fileName, InputStream input) {
        boolean success = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }

            if (!createDir(ftpPath, ftpClient)) {
                return success;
            }

            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            ftpClient.storeFile(fileName, input);

            input.close();
            ftpClient.logout();
            success = true;
        } catch (IOException e) {
            logger.error("文件读取错误。", e);
        }
        finally {
            if (null != ftpClient && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {}
            }
        }
        return success;
    }

    /**
     * 创建目录(有则切换目录，没有则创建目录)
     *
     * @param dir
     * @param localpath
     * @return
     */
    public static boolean createDir(String dir, FTPClient ftp) {
        if (StringUtils.isEmpty(dir)) {
            return false;
        }

        logger.info("---------------dir---------------" + dir);
        String d;
        try {
            d = new String(dir.toString());
            // 尝试切入目录
            if (ftp.changeWorkingDirectory(d)) {
                return true;
            }

            String[] arr = dir.split(FTP_SEPARATOR);
            // 循环生成子目录
            // int num = 0;
            for (String s : arr) {
                d = new String(s);
                // 尝试切入目录
                if (ftp.changeWorkingDirectory(d)) {
                    continue;
                }
                logger.info("-----d--------" + d);
                if (!ftp.makeDirectory(d)) {
                    logger.info("[失败]ftp创建目录：" + d);
                    return false;
                }
                logger.info("[成功]创建ftp目录：" + d);
                if (ftp.changeWorkingDirectory(d)) {
                    continue;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("切入目录异常", e);
            return false;
        }
    }

    /**
     *
     * 删除ftp上的文件
     *
     * @param ftpFileName
     * @return true || false
     */
    public static boolean removeFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort,
            String ftpPath, String fileName) {
        if (!ftpPath.startsWith(FTP_SEPARATOR)) {
            ftpPath = FTP_SEPARATOR + ftpPath;
        }
        boolean flag = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return flag;
            }

            List<String> nameStrings = Arrays.asList(ftpClient.listNames(ftpPath));
            if (nameStrings.contains(fileName) || nameStrings.contains(ftpPath + FTP_SEPARATOR + fileName)) {
                String ftpFileName = new String(ftpPath + FTP_SEPARATOR + fileName);
                return ftpClient.deleteFile(ftpFileName);
            }
            flag = true;
        } catch (IOException e) {
            flag = false;
            logger.error("读取文件异常。", e);
        }
        finally {
            if (null != ftpClient && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {}
            }
        }
        return flag;
    }

    /**
     * 删除空目录
     *
     * @param dir
     * @return
     */
    public static boolean removeDir(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort,
            String ftpPath) {
        if (!ftpPath.startsWith(FTP_SEPARATOR)) {
            ftpPath = FTP_SEPARATOR + ftpPath;
        }

        boolean flag = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return flag;
            }

            String d = new String(ftpPath.toString());
            if (ftpClient.removeDirectory(d)) {
                flag = true;
            } else if (!ftpClient.changeWorkingDirectory(d)) {
                flag = true;
            }
        } catch (IOException e) {
            flag = false;
            logger.error("读取文件异常。", e);
        }
        finally {
            if (null != ftpClient && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {}
            }
        }
        return flag;
    }

    /**
     * 删除ftpPath目录下的fileNames文件, 和ftpPath目录自身(需要删除fileNames所有文件后,
     * ftpPath就成为一个空目录)
     *
     * @param ftpHost
     * @param ftpUserName
     * @param ftpPassword
     * @param ftpPort
     * @param ftpPath
     * @param fileNames
     * @return boolean
     * @date: 2019年1月15日 下午3:44:02
     */
    public static boolean removeDirAndSubFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort,
            String ftpPath, String[] fileNames) {
        if (!ftpPath.startsWith(FTP_SEPARATOR)) {
            ftpPath = FTP_SEPARATOR + ftpPath;
        }

        boolean flag = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return flag;
            }

            for (String fileName : fileNames) {
                ftpClient.deleteFile(ftpPath + FTP_SEPARATOR + fileName);
            }
            if (ftpClient.removeDirectory(ftpPath)) {
                flag = true;
            }
        } catch (IOException e) {
            flag = false;
            logger.error("读取文件异常。", e);
        }
        finally {
            if (null != ftpClient && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {}
            }
        }
        return flag;
    }

    /**
     *
     * 【功能描述：删除文件夹】 【功能详细描述：功能详细描述】
     *
     * @param ftpClient
     * @param ftpPath
     *            文件夹的地址
     * @return true 表似成功，false 失败
     * @throws IOException
     */
    public static boolean iterateDelete(FTPClient ftpClient, String ftpPath) throws IOException {
        logger.info("----------ftpPath--------" + ftpPath);
        FTPFile[] files = ftpClient.listFiles(ftpPath);
        logger.info("----------FTPFile--------" + JSON.toJSONString(files));
        boolean flag = false;
        for (FTPFile f : files) {
            String path = ftpPath + FTP_SEPARATOR + f.getName();
            logger.info("----------path----delete----" + path);
            if (f.isFile()) {
                // 是文件就删除文件
                ftpClient.deleteFile(path);
            } else if (f.isDirectory()) {
                iterateDelete(ftpClient, path);
            }
        }
        // 每次删除文件夹以后就去查看该文件夹下面是否还有文件，没有就删除该空文件夹
        FTPFile[] files2 = ftpClient.listFiles(ftpPath);
        if (files2.length == 0) {
            flag = ftpClient.removeDirectory(ftpPath);
        } else {
            flag = false;
        }
        return flag;
    }

    /**
     *
     * 【功能描述：删除ftp 上指定的文件】 【功能详细描述：功能详细描述】
     *
     * @param ftpPath
     *            ftp上的文件路径
     * @return true 成功，false，失败
     */
    public static boolean deleteDir(FTPClient ftpClient, String ftpPath) {
        boolean flag = false;
        try {
            flag = iterateDelete(ftpClient, ftpPath);
        } catch (IOException e) {

            logger.error(e.getMessage());
        }
        finally {
            releaseFtpClient(ftpClient);
        }
        return flag;
    }

    /**
     * 释放ftpClient 链接
     */
    public static void releaseFtpClient(FTPClient ftpClient) {
        if (ftpClient == null) {
            return;
        }

        if (null != ftpClient && ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException ioe) {}
        }
    }

    public static FTPClient initFtpClient() {
        return FtpUtils.getFTPClient(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                Integer.parseInt(XcloudProperties.getConfigMap().get(Global.FTP_PORT)));
    }

    /**
     * 从FTP服务器下载文件
     * @param ftpPath FTP服务器中文件所在路径 格式： ftptest/aa
     * @param localPath 下载到本地的位置 格式：H:/download
     * @param fileName 文件名称
     */
    public static void downloadFtpFile(String ftpPath, String localPath, String fileName) {
        downloadFtpFile(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                Integer.parseInt(XcloudProperties.getConfigMap().get(Global.FTP_PORT)), ftpPath, localPath, fileName);
    }

    /**
    *
    * 删除ftp上的文件及其文件夹
    *
    * @param ftpPath FTP服务器中文件所在路径
    * @param fileName 文件名称
    */
    public static void removeDirAndSubFile(String ftpPath, String fileName) {
        removeDirAndSubFile(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                Integer.valueOf(XcloudProperties.getConfigMap().get(Global.FTP_PORT)), ftpPath,
                new String[] { fileName });
    }
}
