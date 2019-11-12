package com.xxx.xcloud.module.devops.properties.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author daien
 * @date 2019年3月15日
 */
public class GitLabConnectionProperty {
	private String plugin;

	private String gitLabConnection;

	@XmlAttribute(name = "plugin")
	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	@XmlElement(name = "gitLabConnection")
	public String getGitLabConnection() {
		return gitLabConnection;
	}

	public void setGitLabConnection(String gitLabConnection) {
		this.gitLabConnection = gitLabConnection;
	}

}
