package com.xxx.xcloud.module.ci.model;

/**
 * gradle信息
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年8月6日 上午11:06:50
 */
public class CiInvokeJavaGradle {
    /**
     * 命令
     */
    private String tasks;

    /**
     * jenkins上的gradle版本
     */
    private String gradleName;

    /**
     * false：选用invoke gradle（使用jenkins配置的gradle） 
     * true：Use Gradle Wrapper，使用项目自带的gradle
     */
    private Boolean useWrapper;

    /**
     * 是否使用gradlew工具
     */
    private Boolean makeExecutable;

    /**
     * gradle在项目的地址
     */
    private String wrapperLocation;

    private String switchs;

    private String rootBuildScriptDir;

    private String buildFile;

    private Boolean useWorkspaceAsHome;

    private String systemProperties;

    private Boolean passAllAsSystemProperties;

    private String projectProperties;

    private Boolean passAllAsProjectProperties;

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
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

    public String getWrapperLocation() {
        return wrapperLocation;
    }

    public void setWrapperLocation(String wrapperLocation) {
        this.wrapperLocation = wrapperLocation;
    }

    public String getSwitchs() {
        return switchs;
    }

    public void setSwitchs(String switchs) {
        this.switchs = switchs;
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

    public Boolean getUseWorkspaceAsHome() {
        return useWorkspaceAsHome;
    }

    public void setUseWorkspaceAsHome(Boolean useWorkspaceAsHome) {
        this.useWorkspaceAsHome = useWorkspaceAsHome;
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
