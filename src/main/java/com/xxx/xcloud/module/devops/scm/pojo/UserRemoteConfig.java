package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author daien
 * @date 2019年3月4日
 */
public class UserRemoteConfig {

    private String name;

    private String refspec;

	private String url;

	private String credentialsId;

	@XmlElement(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement(name = "credentialsId")
	public String getCredentialsId() {
		return credentialsId;
	}

	public void setCredentialsId(String credentialsId) {
		this.credentialsId = credentialsId;
	}

	@XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "refspec")
    public String getRefspec() {
        return refspec;
    }

    public void setRefspec(String refspec) {
        this.refspec = refspec;
    }
}
