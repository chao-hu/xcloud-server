package com.xxx.xcloud.client.gitlab.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * gitlab一个项目
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月21日 下午3:21:50
 */
public class GitlabRepos {
    private int id;
    private String name;

    @JsonProperty("name_with_namespace")
    private String nameWithNamespace;

    @JsonProperty("ssh_url_to_repo")
    private String sshUrlToRepo;

    @JsonProperty("http_url_to_repo")
    private String httpUrlToRepo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    public void setNameWithNamespace(String nameWithNamespace) {
        this.nameWithNamespace = nameWithNamespace;
    }

    public String getSshUrlToRepo() {
        return sshUrlToRepo;
    }

    public void setSshUrlToRepo(String sshUrlToRepo) {
        this.sshUrlToRepo = sshUrlToRepo;
    }

    public String getHttpUrlToRepo() {
        return httpUrlToRepo;
    }

    public void setHttpUrlToRepo(String httpUrlToRepo) {
        this.httpUrlToRepo = httpUrlToRepo;
    }

    @Override
    public String toString() {
        return "GitlabRepos [id=" + id + ", name=" + name + ", nameWithNamespace=" + nameWithNamespace
                + ", sshUrlToRepo=" + sshUrlToRepo + ", httpUrlToRepo=" + httpUrlToRepo + "]";
    }

}
