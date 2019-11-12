package com.xxx.xcloud.module.ci.model;

/**
 * 
 * @author mengaijun
 * @Description: java编译信息（maven构建）
 * @date: 2018年12月7日 下午5:26:43
 */
public class CiInvokeJava {
	private Byte invokeType;
	private Byte jobOrderId;
	private String buildcmd;
	private String mvnVersion;
	private String langVersion;
	public Byte getInvokeType() {
		return invokeType;
	}
	public void setInvokeType(Byte invokeType) {
		this.invokeType = invokeType;
	}
	public Byte getJobOrderId() {
		return jobOrderId;
	}
	public void setJobOrderId(Byte jobOrderId) {
		this.jobOrderId = jobOrderId;
	}
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
	public String getLangVersion() {
		return langVersion;
	}
	public void setLangVersion(String langVersion) {
		this.langVersion = langVersion;
	}
	
	
}
