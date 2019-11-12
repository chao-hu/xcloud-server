package com.xxx.xcloud.module.devops.build.mvn.pojo;

import javax.xml.bind.annotation.XmlElement;

public class Maven {
	/**
	 * maven命令
	 */
	private String targets;

	/**
	 * maven版本, maven名称
	 */
	private String mavenName;
	
	/**
	 * jvm 选项
	 */
	private String jvmOptions;
	
	/**
	 * pom文件
	 */
	private String pom;
	
	/**
	 * 是否使用私有仓库
	 */
	private Boolean usePrivateRepository;
	
	/**
	 * settings file
	 */
	private Settings settings;
	/**
	 * Global Settings file
	 */
	private GlobalSettings globalSettings;
	
	/**
	 * 
	 */
	private boolean injectBuildVariables;
	
	@XmlElement(name = "targets")
	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	@XmlElement(name = "mavenName")
	public String getMavenName() {
		return mavenName;
	}

	public void setMavenName(String mavenName) {
		this.mavenName = mavenName;
	}

	@XmlElement(name = "usePrivateRepository")
	public Boolean getUsePrivateRepository() {
		return usePrivateRepository;
	}

	public void setUsePrivateRepository(Boolean usePrivateRepository) {
		this.usePrivateRepository = usePrivateRepository;
	}

	@XmlElement(name = "settings")
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	@XmlElement(name = "globalSettings")
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	public void setGlobalSettings(GlobalSettings globalSettings) {
		this.globalSettings = globalSettings;
	}

	@XmlElement(name = "injectBuildVariables")
	public boolean isInjectBuildVariables() {
		return injectBuildVariables;
	}

	public void setInjectBuildVariables(boolean injectBuildVariables) {
		this.injectBuildVariables = injectBuildVariables;
	}

	@XmlElement(name = "pom")
	public String getPom() {
		return pom;
	}

	public void setPom(String pom) {
		this.pom = pom;
	}

	@XmlElement(name = "jvmOptions")
	public String getJvmOptions() {
		return jvmOptions;
	}

	public void setJvmOptions(String jvmOptions) {
		this.jvmOptions = jvmOptions;
	}
	
}

















