package com.xxx.xcloud.module.ci.model;

/**
 * 
 * @author mengaijun
 * @Description: python编译信息
 * @date: 2018年12月7日 下午5:27:03
 */
public class CiInvokePython {
	private String buildcmd;
    /*
     * private String appNames; private String licenseCont; private String
     * licensePath; private String manifestCont; private String manifestPath;
     */
    /*
     * private Byte pythonType; private String readMeCont; private String
     * readMePath; private String setupCont; private String setupPath;
     */
    /**
     * 使用框架: 1Dgango(仅支持这种)
     */
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
