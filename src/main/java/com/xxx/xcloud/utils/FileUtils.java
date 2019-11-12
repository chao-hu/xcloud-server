package com.xxx.xcloud.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 附件处理工具类
 *
 * @author yangjian@bonc.com.cn
 * @version 2016年8月31日
 * @see FileUtils
 * @since
 */
public class FileUtils {
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    private static Pattern READ_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    /**
     *
     * Description: 存储文件 内存1024byte，以流的形式循环读取上传,防止内存溢出
     *
     * @param in
     *            InputStream
     * @param fileName
     *            String
     * @return boolean
     * @throws IOException
     * @see InputStream
     */
    public static boolean storeFile(InputStream in, String fileName) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));) {
            int b = -1;
            byte[] buffer = new byte[1024];
            while ((b = in.read(buffer)) != -1) {
                bos.write(buffer, 0, b);
            }
            bos.flush();
            return true;
        } catch (IOException e) {
            LOG.error("FileUtils storeFile error:" + e.getMessage());
            return false;
        }
        finally {
            in.close();
        }
    }

    /**
     *
     * Description: 写文件
     *
     * @param fileTemplate
     *            模板文件
     * @param data
     *            模板中需要替换的值
     * @param toFile
     * @throws IOException
     * @see
     */
    public static void writeFileByLines(String fileTemplate, Map<String, String> data, String toFile)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileTemplate)));
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(toFile)));) {
            String tempString = "";
            String bufferstr = "";
            while (null != (tempString = reader.readLine())) {
                Matcher matcher = READ_PATTERN.matcher(tempString);
                if (matcher.find()) {
                    tempString = tempString.replace(matcher.group(0), data.get(matcher.group(0)));
                }
                bufferstr += tempString + "\n";
            }
            writer.write(bufferstr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Description: 读取dockerFile文件中的文本数据；
     *
     * @param fileTemplate
     *            ： dockerfile文件路径
     * @return dockerFile String
     * @throws IOException
     * @see
     */
    public static String readFileByLines(String fileTemplate) throws IOException {
        String dockerFile = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileTemplate)));) {
            String tempString = "";
            while (null != (tempString = reader.readLine())) {
                dockerFile += tempString + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dockerFile;
    }

    /**
     * readFileByLines:读文件并替换${}值. <br/>
     *
     * @param fileTemplate
     * @param data
     *            void
     * @throws IOException
     */
    public static String readFileByLines(String fileTemplate, Map<String, String> data) throws IOException {
        StringBuffer bufferstr = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileTemplate)));) {
            String tempString = "";
            while (null != (tempString = reader.readLine())) {
                Matcher matcher = READ_PATTERN.matcher(tempString);
                if (matcher.find()) {
                    for (String key : data.keySet()) {
                        tempString = tempString.replace(key, data.get(key));
                    }
                }
                bufferstr.append(tempString + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bufferstr.toString();
    }

    /**
     * 删除文件夹下的所有文件
     *
     * @param path
     *            文件夹路径
     * @return flag 成功或失败
     * @see
     */
    public static boolean delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            LOG.info("指定的文件路径：" + path + "不存在");
            return false;
        }
        if (!file.isDirectory()) {
            LOG.info("指定的文件路径：" + path + "不不是文件夹");
            return false;
        }
        String[] tempList = file.list();
        File temp = null;
        for (String element : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + element);
            } else {
                temp = new File(path + File.separator + element);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + element);// 先删除文件夹里面的文件
                delFolder(path + "/" + element);// 再删除空文件夹
            }
        }

        LOG.info("指定的文件路径：" + path + "清空成功");
        return true;
    }

    /**
     *
     * 删除空文件夹
     *
     * @param folderPath
     *            文件夹路径
     * @see
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向filePath指定的文件写入content内容(覆盖).
     *
     * @param content
     * @param filePath
     * @return boolean 是否写入成功
     * @date: 2019年1月15日 下午2:27:18
     */
    public static boolean writeContentToFile(String content, String filePath) {
        boolean isSuccess = false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)));) {
            writer.write(content);
            isSuccess = true;
            writer.flush();
        } catch (IOException e) {
            LOG.error("文件IO异常", e);
        }
        return isSuccess;
    }
}
