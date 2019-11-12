package com.xxx.xcloud.module.ci.model;
/**
 * ftp路径信息
 * @author mengaijun
 * @date: 2019年1月16日 上午10:32:16
 */
public class FtpFilePath {
	/**
	 * 路径
	 */
	private String filePath;
	private String ftpHost;
	private int ftpPort;
	private String ftpUser;
	private String ftpPass;
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFtpHost() {
		return ftpHost;
	}
	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}
	
	public int getFtpPort() {
		return ftpPort;
	}
	public void setFtpPort(int ftpPort) {
		this.ftpPort = ftpPort;
	}
	public String getFtpUser() {
		return ftpUser;
	}
	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}
	public String getFtpPass() {
		return ftpPass;
	}
	public void setFtpPass(String ftpPass) {
		this.ftpPass = ftpPass;
	}
}
