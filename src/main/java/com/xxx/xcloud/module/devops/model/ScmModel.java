package com.xxx.xcloud.module.devops.model;

/**
 * @author daien
 * @date 2019年3月15日
 */
public class ScmModel {

    private String scmType;

    private String credentialType;

    private GitLabModel gitLabModel;

    private GitHubModel gitHubModel;

    private SvnModel svnModel;

    public String getScmType() {
        return scmType;
    }

    public void setScmType(String scmType) {
        this.scmType = scmType;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    public GitLabModel getGitLabModel() {
        return gitLabModel;
    }

    public void setGitLabModel(GitLabModel gitLabModel) {
        this.gitLabModel = gitLabModel;
    }

    public GitHubModel getGitHubModel() {
        return gitHubModel;
    }

    public void setGitHubModel(GitHubModel gitHubModel) {
        this.gitHubModel = gitHubModel;
    }

    public SvnModel getSvnModel() {
        return svnModel;
    }

    public void setSvnModel(SvnModel svnModel) {
        this.svnModel = svnModel;
    }

}
