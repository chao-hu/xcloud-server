package com.xxx.xcloud.module.devops.model;

public class SvnModel {

    private String url;

    //default value,可以将代码下载至workspace根路径，而不是多一级项目文件夹
    private String local = ".";

    private String credentialId;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

}
