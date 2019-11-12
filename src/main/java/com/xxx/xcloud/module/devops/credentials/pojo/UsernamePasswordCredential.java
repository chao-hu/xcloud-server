package com.xxx.xcloud.module.devops.credentials.pojo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * com.xxx.xcloud.module.devops.model.UsernamePasswordCredential
 *
 * @author xujiangpeng
 * @date 2019/3/15
 */
@XmlRootElement(name = "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
@XmlType(propOrder = { "scope", "id", "description", "username", "password" })
public class UsernamePasswordCredential extends BaseCredential {

    private String scope;

    private String id;

    private String description;

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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
}
