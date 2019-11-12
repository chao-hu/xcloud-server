package com.xxx.xcloud.module.devops.model;

public class GitHubModel {

    private String url;

    private String branch;

    private String credentialId;

    private String name;

    private String refspec;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefspec() {
        return refspec;
    }

    public void setRefspec(String refspec) {
        this.refspec = refspec;
    }

}
