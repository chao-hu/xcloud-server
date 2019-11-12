package com.xxx.xcloud.module.devops.credentials.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * PACKAGE_NAME.GitLabApiTokenCredential
 *
 * @author xujiangpeng
 * @date 2019/3/20
 */
@XmlRootElement(name = "com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl")
@XmlType(propOrder = { "scope", "id", "description", "apiToken" })
public class GitLabApiTokenCredential extends BaseCredential {

    /**
     * gitlab-plugin version.
     */
    private String pluginVersion;

    private String scope;

    private String id;

    private String description;

    private String apiToken;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    @XmlAttribute(name = "plugin")
    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }
}

