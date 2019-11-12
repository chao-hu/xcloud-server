package com.xxx.xcloud.module.devops.credentials.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * PACKAGE_NAME.BasicSshUserPrivateKeyCredential
 *
 * @author xujiangpeng
 * @date 2019/3/20
 */
@XmlRootElement(name = "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
@XmlType(propOrder = { "scope", "id", "description", "username", "passphrase", "privateKeySource" })
public class BasicSshUserPrivateKeyCredential extends BaseCredential {

    /**
     * ssh-credentials plugin version.
     */
    private String pluginVersion;

    private String scope;

    private String id;

    private String description;

    private String username;

    private String passphrase;

    private PrivateKeySource privateKeySource;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @XmlElement(name = "privateKeySource")
    public PrivateKeySource getPrivateKeySource() {
        return privateKeySource;
    }

    public void setPrivateKeySource(PrivateKeySource privateKeySource) {
        this.privateKeySource = privateKeySource;
    }

    @XmlAttribute(name = "plugin")
    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }
}




