package com.xxx.xcloud.module.devops.model;

/**
 * @author daien
 * @date 2019年7月30日
 */
public class GradleModel {

    private String switchs;

    private String tasks;

    private String rootBuildScriptDir;

    private String buildFile;

    private String gradleName;

    private Boolean useWrapper;

    private Boolean makeExecutable;

    private Boolean useWorkspaceAsHome;

    private String wrapperLocation;

    private String systemProperties;

    private Boolean passAllAsSystemProperties;

    private String projectProperties;

    private Boolean passAllAsProjectProperties;

    public String getSwitchs() {
        return switchs;
    }

    public void setSwitchs(String switchs) {
        this.switchs = switchs;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public String getRootBuildScriptDir() {
        return rootBuildScriptDir;
    }

    public void setRootBuildScriptDir(String rootBuildScriptDir) {
        this.rootBuildScriptDir = rootBuildScriptDir;
    }

    public String getBuildFile() {
        return buildFile;
    }

    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }

    public String getGradleName() {
        return gradleName;
    }

    public void setGradleName(String gradleName) {
        this.gradleName = gradleName;
    }

    public Boolean getUseWrapper() {
        return useWrapper;
    }

    public void setUseWrapper(Boolean useWrapper) {
        this.useWrapper = useWrapper;
    }

    public Boolean getMakeExecutable() {
        return makeExecutable;
    }

    public void setMakeExecutable(Boolean makeExecutable) {
        this.makeExecutable = makeExecutable;
    }

    public Boolean getUseWorkspaceAsHome() {
        return useWorkspaceAsHome;
    }

    public void setUseWorkspaceAsHome(Boolean useWorkspaceAsHome) {
        this.useWorkspaceAsHome = useWorkspaceAsHome;
    }

    public String getWrapperLocation() {
        return wrapperLocation;
    }

    public void setWrapperLocation(String wrapperLocation) {
        this.wrapperLocation = wrapperLocation;
    }

    public String getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    public Boolean getPassAllAsSystemProperties() {
        return passAllAsSystemProperties;
    }

    public void setPassAllAsSystemProperties(Boolean passAllAsSystemProperties) {
        this.passAllAsSystemProperties = passAllAsSystemProperties;
    }

    public String getProjectProperties() {
        return projectProperties;
    }

    public void setProjectProperties(String projectProperties) {
        this.projectProperties = projectProperties;
    }

    public Boolean getPassAllAsProjectProperties() {
        return passAllAsProjectProperties;
    }

    public void setPassAllAsProjectProperties(Boolean passAllAsProjectProperties) {
        this.passAllAsProjectProperties = passAllAsProjectProperties;
    }

}
