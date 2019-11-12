package com.xxx.xcloud.module.devops.model;

public class SonarModel {

    /**
     * 本次检查项目名
     */
    private String projectName;
    /**
     * 本次检查项目名（同上name）
     */
    private String projectKey;
    /**
     * sonar本次检查分支（默认master）
     */
    private String projectVersion;
    /**
     * 检查文件
     */
    private String sources;
    /**
     * 规则集名称
     */
    private String profile;
    /**
     * 规则集语言
     */
    private String language;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}
