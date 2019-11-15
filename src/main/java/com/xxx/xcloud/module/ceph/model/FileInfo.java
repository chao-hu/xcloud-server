package com.xxx.xcloud.module.ceph.model;


/**
 * @ClassName: FileInfo
 * @Description: 文件描述信息
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
public class FileInfo {

    /**
     * 是否是目录
     */
    private boolean isDir;
    /**
     * 是否是链接
     */
    private boolean isLink = false;
    /**
     * 文件大小kb
     */
    private String size;
    /**
     * 时间
     */
    private String time;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 修改时间
     */
    private String modifiedTime;
    /**
     * 路径
     */
    private String path;

    public boolean isDir() {
        return isDir;
    }

    /**
     * 是否是目录
     * 
     * @param dir
     * @see
     */
    public void setDir(boolean dir) {
        this.isDir = dir;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean isLink) {
        this.isLink = isLink;
    }
}
