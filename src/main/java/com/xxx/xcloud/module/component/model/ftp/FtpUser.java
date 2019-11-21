package com.xxx.xcloud.module.component.model.ftp;

/**
 * @ClassName: FtpUser
 * @Description: FtpUser
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpUser implements Comparable<FtpUser> {

    private String userName;

    private String password;

    /**
     * -1不启用 1启用
     */
    private int status;

    private String permission;

    /**
     * 数据存储路径
     */
    private String directory;

    private String effective;

    public String getEffective() {
        return effective;
    }

    public void setEffective(String effective) {
        this.effective = effective;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /** 
     * @Title: compareTo
     * @Description: 重写Comparable接口的compareTo方法
     * @param ftpuser
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FtpUser ftpuser) {

        return this.userName.compareTo(ftpuser.getUserName());
    }
}
