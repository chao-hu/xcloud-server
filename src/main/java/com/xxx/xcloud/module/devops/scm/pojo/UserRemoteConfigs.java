package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlElement;

public class UserRemoteConfigs {
	private UserRemoteConfig userRemoteConfig;

	@XmlElement(name = "hudson.plugins.git.UserRemoteConfig")
	public UserRemoteConfig getUserRemoteConfig() {
		return userRemoteConfig;
	}

	public void setUserRemoteConfig(UserRemoteConfig userRemoteConfig) {
		this.userRemoteConfig = userRemoteConfig;
	}
}
