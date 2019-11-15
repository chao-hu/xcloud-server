/*
* Created on 2009-9-14
* Copyright 2009 by www.xfok.net. All Rights Reserved
*
*/

package com.xxx.xcloud.utils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.xxx.xcloud.module.ceph.model.FileInfo;
import com.xxx.xcloud.module.ceph.model.FileUploadProgress;

/**
 * SFTPUtil
 * @author YuanPeng
 * @version 2016年9月1日
 * @see SftpUtil
 * @since
 */
public class SftpUtil {

    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(SftpUtil.class);
    /**
     *
     * Description: <br>
     * 连接sftp服务器
     * @param host 主机
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     * @return sftp ChannelSftp
     * @see
     */
    public static ChannelSftp connect(String host, int port, String username,String password) {
        try {
            JSch jsch = new JSch();
            Session sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            LOG.info("Session created==========>Session connected=========>Opening Channel=================>Connected to " + host + ".");
            return (ChannelSftp) channel;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * Description: <br>
     * 上传文件
     * @param directory 上传的目录
     * @param sftp ChannelSftp
     * @param file 上传的文件
     * @see
     */
    public void upload(String directory, MultipartFile file, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            sftp.put(new ByteArrayInputStream(file.getBytes()), file.getOriginalFilename());
            LOG.info("upload directory :-" + directory + "; fileName:-" + file.getOriginalFilename());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Description: <br>
     * 下载文件 暂时无效
     * @deprecated
     * @param directory 下载目录
     * @param downloadFile 下载的文件
     * @param sftp ChannelSftp
     * @return InputStream
     * @see
     */
    public InputStream download2(String downloadFile,String directory, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            return sftp.get(downloadFile);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * Description:
     * 下载文件新方法
     * @param downloadFile
     * @param directory
     * @param sftp
     * @param dirPath
     * @return
     * @see
     */
    public void download(String downloadFile,String directory, ChannelSftp sftp ,String  dirPath) {
        try {
            sftp.cd(directory);
            File file =new File(dirPath);
            if(!file .exists()  && !file .isDirectory()){
                file .mkdir();
            }
            File dfile =new File(dirPath+downloadFile);
            sftp.get(downloadFile, new FileOutputStream(dfile));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * 删除文件
    * @param directory 要删除文件所在目录
    * @param deleteFile 要删除的文件
    * @param sftp ChannelSftp
    */
    public void delete(String directory, String deleteFile, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * 列出目录下的文件
    * @param directory 要列出的目录
    * @return fileList List<FileInfo>
    */
    public static List<FileInfo> listFileInfo(String directory){
        List<FileInfo> fileList = new ArrayList<FileInfo>();
        File file = new File(directory);
        File [] tempList =file.listFiles();
        fileList.add(fillFileInfo(true, "..", directory, "", ""));
        for(int i=0;i<tempList.length;i++){
            if(!".".equals(tempList[i].getName())) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                FileInfo fi = fillFileInfo(tempList[i].isDirectory(),tempList[i].getName(),
                                                directory,String.valueOf(tempList[i].length()/1024),
                                                formatter.format(tempList[i].lastModified()));
                fileList.add(fi);
            }
        }
        return fileList;
    }

    /**
    * 列出目录下的文件
    * @param directory 要列出的目录
    * @param sftp
    * @return fileList List<FileInfo>
    * @throws SftpException
    */
    @SuppressWarnings("unchecked")
	public static List<FileInfo> listFileInfo(String directory, ChannelSftp sftp) throws SftpException{
        List<FileInfo> fileList = new ArrayList<FileInfo>();
        Vector<LsEntry> v =sftp.ls(directory);
        fileList.add(fillFileInfo(true, "..", directory, "", ""));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(int i=0;i<v.size();i++){
            if(!".".equals(v.get(i).getFilename()) && !"..".equals(v.get(i).getFilename())) {
                FileInfo fi = fillFileInfo(v.get(i).getAttrs().isDir(),v.get(i).getFilename(),
                                                directory,String.valueOf(v.get(i).getAttrs().getSize()/1024),
                                                formatter.format((long)v.get(i).getAttrs().getMTime()*1000));
                if (v.get(i).getLongname().startsWith("l")) {
					fi.setLink(true);
				}
                fileList.add(fi);
            }
        }
        fileList.sort(new Comparator<FileInfo>() {

			@Override
			public int compare(FileInfo o1, FileInfo o2) {
				if (o1.isDir()) {
					if (o2.isDir()) {
						return o1.getFileName().compareTo(o2.getFileName());
					}else {
						return -1;
					}
				}else {
					if (o2.isDir()) {
						return 1;
					} else {
						return o1.getFileName().compareTo(o2.getFileName());
					}
				}
			}
		});
        return fileList;
    }
    /**
    * 列出目录下的文件
    * @param directory 要列出的目录
    * @param sftp
    * @return
    * @throws SftpException
    */
    @SuppressWarnings("unchecked")
	public static Vector<LsEntry> listFiles(String directory, ChannelSftp sftp) throws SftpException{
    	return sftp.ls(directory);
    }

    /**
     *
     * Description: <br>
     * 创建并初始化一个fileInfo
     * @param isDir boolean
     * @param fileName String
     * @param filePath String
     * @param fileSize String
     * @param time String
     * @return ff FileInfo
     * @see FileInfo
     */
    private static FileInfo fillFileInfo(boolean isDir, String fileName , String filePath, String fileSize, String time) {
        FileInfo ff = new FileInfo();
        ff.setDir(isDir);
        ff.setFileName(fileName);
        ff.setPath(filePath);
        ff.setSize(fileSize);
        ff.setModifiedTime(time);
        return ff;
    }

    /**
     *
     * Description:
     * 读取修改时间的方法
     * @param file  File
     * @return String 例如： 2009-08-07 10:00:00
     * @see
     */
    public static String getModifiedTime(File file){
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat();
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }

    /**
     *
     * Description:
     * 获取文件的大小 MB
     * @param file File
     * @return String
     * @see
     */
    public static String getFileSize(File file){
        long size=file.length(); 
        size = size/1024/1024;
        return  String.valueOf(size);
    }

    /**
     *
     * Description:
     * 批量上传文件
     * @param ChannelSftp sftp
     * @param file File
     * @param String pwd
     */
    public static void upLoadFile(ChannelSftp sftp, File file, String pwd, FileUploadProgress progress) {

        if (file.isDirectory()) {
            File[] list = file.listFiles();
            try {
                try {
                    String fileName = file.getName();
                    sftp.cd(pwd);
                    System.out.println("正在创建目录:" + sftp.pwd() + "/" + fileName);
                    sftp.mkdir(fileName);
                    System.out.println("目录创建成功:" + sftp.pwd() + "/" + fileName);
                } catch (Exception e) {
                	e.printStackTrace();
                }
                pwd = pwd + "/" + file.getName();
                try {
                    sftp.cd(file.getName());
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < list.length; i++) {
            	upLoadFile(sftp, list[i], pwd, progress);
            }
        } else {

            try {
                sftp.cd(pwd);
            } catch (SftpException e1) {
                e1.printStackTrace();
            }
            System.out.println("正在复制文件:" + file.getAbsolutePath());
            InputStream instream = null;
            OutputStream outstream = null;
            try {
                outstream = sftp.put(file.getName());
                instream = new FileInputStream(file);

                byte b[] = new byte[1024];
                int n;
                try {
                    while ((n = instream.read(b)) != -1) {
                        outstream.write(b, 0, n);
                        progress.setRead(progress.getRead() + n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (SftpException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    outstream.flush();
                    outstream.close();
                    instream.close();

                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * Description: 统计卷组的使用情况<br>
     *  返回的单位为byte
     * @param directory
     * @return
     * @see
     */
    public static long getHasUsed(String directory) {
        File file = new File(directory);
        File [] tempList =file.listFiles();
        long hasUsed=0;
        for(int i=0;i<tempList.length;i++){
            if(!".".equals(tempList[i].getName())) {
               hasUsed+= tempList[i].length();
            }
        }
        return hasUsed;
    }

}