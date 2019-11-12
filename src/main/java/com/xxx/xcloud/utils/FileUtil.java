package com.xxx.xcloud.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FileUtil {

    /**
     * 在Java中, 统一的文件(Windows/Unix)和URL的分隔符.
     */
    public static final String UNIVERSAL_SEPARATOR = "/";

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {

    }

    public static boolean deleteFile(String strFile) {
        if (strFile == null) {
            return false;
        }
        return deleteFile(new File(strFile));
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        return file.delete();
    }

    public static String getFileExtension(String fileName) {
        int lastPos = fileName.lastIndexOf('.');
        if (lastPos != -1 && lastPos != 0) {
            return fileName.substring(lastPos + 1);
        }
        return "";
    }

    public static Boolean isAllowImageExtension(String filename) {

        List<String> allowList = new ArrayList<>();
        allowList.add("jpg");
        allowList.add("jpeg");
        allowList.add("png");
        allowList.add("bmp");
        allowList.add("gif");

        String ext = getFileExtension(filename);

        return allowList.contains(ext.toLowerCase());
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param sPath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) { // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) { // 为文件时调用删除文件方法
                return deleteFile(file);
            } else { // 为目录时调用删除目录方法
                return deleteDirectory(sPath);
            }
        }
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        String path = sPath;
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            path = sPath + File.separator;
        }
        File dirFile = new File(path);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        // 删除文件夹下的所有文件(包括子目录)
        boolean flag = deleteDirOrFile(dirFile);

        if (!flag) {

            return flag;
        }
        // 删除当前目录
        if (dirFile.delete()) {

            return dirFile.delete();
        } else {

            return !dirFile.delete();
        }
    }

    private static boolean deleteDirOrFile(File dirFile) {
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } // 删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 列出目录sPath下面所有满足模式的文件，当文件名模式为空时， 列回所有文件
     *
     * @param sPath
     * @return
     */
    public static List<String> getFileList(String sPath, String regex) {
        List<String> matchedFiles = new ArrayList<>();
        String path = sPath;

        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            path = sPath + File.separator;
        }

        File dirFile = new File(path);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {

            return Collections.emptyList();
        } else {
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {

                buildMatchedFiles(files, i, regex, matchedFiles);
            }
        }

        return matchedFiles;
    }

    public static void buildMatchedFiles(File[] files, int i, String regex, List<String> matchedFiles) {

        // 判断文件是需要的文件
        if (files[i].isFile()) {
            String fileName = files[i].getName();
            if (null != regex && !"".equalsIgnoreCase(regex.trim())) {
                if (fileName.matches(regex)) {
                    matchedFiles.add(files[i].getAbsolutePath());
                }
            } else {
                matchedFiles.add(files[i].getAbsolutePath());
            }

        }

    }

    public static String getFileByte(MultipartFile file) throws IOException {
        byte[] b = new byte[1024];
        String content = "";
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); InputStream stream = file.getInputStream();) {
            while ((stream.read(b)) != -1) {
                bos.write(b);
            }
            content = Base64Util.encode(bos.toByteArray());
        } catch (Exception e) {
            logger.error("错误信息 ：" + e);
        }

        return content;
    }

    /**
     * 浏览tar文件,判断是否包含estimate数组中的文件名
     *
     * @param targzFile tar包
     * @param estimate  文件数组
     * @throws IOException
     */
    public static boolean visitTAR(File targzFile, String... estimate) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(targzFile);
                BufferedInputStream bufIn = new BufferedInputStream(fileIn);
                TarArchiveInputStream taris = new TarArchiveInputStream(bufIn);) {
            TarArchiveEntry entry = null;
            while ((entry = taris.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (estimate.length > 0) {
                    for (int i = 0; i < estimate.length; i++) {
                        if (entry.getName().trim().equals(estimate[i])) {
                            logger.info("********find specific file name *********** filename:-" + estimate[i]);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * 解压出tar包中的fileName文件, 解压到directory目录
     *
     * @param targzFile tar文件
     * @param directory 解压目录
     * @param fileName  需要解压出的文件的名称
     * @return
     */
    public static boolean extTarFileList(File targzFile, String directory, String fileName) {
        boolean flag = false;

        OutputStream out = null;
        try {
            TarInputStream in = new TarInputStream(new FileInputStream(targzFile));
            TarEntry entry = null;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (fileName.equals(entry.getName())) {
                    File outfile = new File(directory + "/" + entry.getName());
                    new File(outfile.getParent()).mkdirs();
                    out = new BufferedOutputStream(new FileOutputStream(outfile));
                    int x = 0;
                    while ((x = in.read()) != -1) {
                        out.write(x);
                    }
                    out.close();
                    break;
                }
            }
            in.close();
            flag = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 从manifest.json文件中提取镜像ID信息
     *
     * @param directory 目录
     * @param fileName  文件名
     * @return String 镜像ID
     * @date: 2018年12月11日 上午9:53:49
     */
    public static String getImageId(String directory, String fileName) {
        String result = null;
        File file = new File(directory + "/" + fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String fileString = null;
            fileString = reader.readLine();
            if (fileString != null) {
                @SuppressWarnings("rawtypes") List<Map> array = JSONObject.parseArray(fileString, Map.class);
                if (array.size() > 0) {
                    @SuppressWarnings("unchecked") Map<String, String> map = array.get(0);
                    result = map.get("Config").replace(".json", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
