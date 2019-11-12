package com.xxx.xcloud.module.ci.model;

public class Repository {

    private String name;

    private String git_http_url;

    private String git_ssh_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGit_http_url() {
        return git_http_url;
    }

    public void setGit_http_url(String git_http_url) {
        this.git_http_url = git_http_url;
    }

    public String getGit_ssh_url() {
        return git_ssh_url;
    }

    public void setGit_ssh_url(String git_ssh_url) {
        this.git_ssh_url = git_ssh_url;
    }

    @Override
    public String toString() {
        return "Repository [name=" + name + ", git_http_url=" + git_http_url + ", git_ssh_url=" + git_ssh_url + "]";
    }

}
