package com.xxx.xcloud.module.ci.model;

/**
 * 
 * @author mengaijun
 * @Description: go编译信息
 * @date: 2018年12月7日 下午5:26:19
 */
public class CiInvokeGo {
	private String buildcmd;
	private String langVersion;

	public String getBuildcmd() {
		return buildcmd;
	}

	public void setBuildcmd(String buildcmd) {
		this.buildcmd = buildcmd;
	}

	public String getLangVersion() {
		return langVersion;
	}

	public void setLangVersion(String langVersion) {
		this.langVersion = langVersion;
	}

}
