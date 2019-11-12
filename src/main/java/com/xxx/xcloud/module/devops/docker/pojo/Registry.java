package com.xxx.xcloud.module.devops.docker.pojo;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author xujiangpeng
 * @date 2019/3/15
 */
public class Registry {

    /**
     * The docker-commons plugin version.
     */
    private String plugin;

    private String url;

    private String credentialsId;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
}
