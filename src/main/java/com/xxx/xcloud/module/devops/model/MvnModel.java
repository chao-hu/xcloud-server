package com.xxx.xcloud.module.devops.model;

/**
 * maven 模型
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年3月18日 下午3:10:18
 */
public class MvnModel {
	/**
	 * maven 命令
	 */
	private String buildcmd;
	/**
	 * maven 版本
	 */
	private String mvnVersion;
	
	public String getBuildcmd() {
		return buildcmd;
	}
	public void setBuildcmd(String buildcmd) {
		this.buildcmd = buildcmd;
	}
	public String getMvnVersion() {
		return mvnVersion;
	}
	public void setMvnVersion(String mvnVersion) {
		this.mvnVersion = mvnVersion;
	}
	
}
