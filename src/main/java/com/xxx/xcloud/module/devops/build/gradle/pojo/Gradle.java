package com.xxx.xcloud.module.devops.build.gradle.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author daien
 * @date 2019年7月30日
 */
public class Gradle {

    private String plugin;

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

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "switchs")
    public String getSwitchs() {
        return switchs;
    }

    public void setSwitchs(String switchs) {
        this.switchs = switchs;
    }

    @XmlElement(name = "tasks")
    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    @XmlElement(name = "rootBuildScriptDir")
    public String getRootBuildScriptDir() {
        return rootBuildScriptDir;
    }

    public void setRootBuildScriptDir(String rootBuildScriptDir) {
        this.rootBuildScriptDir = rootBuildScriptDir;
    }

    @XmlElement(name = "buildFile")
    public String getBuildFile() {
        return buildFile;
    }

    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }

    @XmlElement(name = "gradleName")
    public String getGradleName() {
        return gradleName;
    }

    public void setGradleName(String gradleName) {
        this.gradleName = gradleName;
    }

    @XmlElement(name = "useWrapper")
    public Boolean getUseWrapper() {
        return useWrapper;
    }

    public void setUseWrapper(Boolean useWrapper) {
        this.useWrapper = useWrapper;
    }

    @XmlElement(name = "makeExecutable")
    public Boolean getMakeExecutable() {
        return makeExecutable;
    }

    public void setMakeExecutable(Boolean makeExecutable) {
        this.makeExecutable = makeExecutable;
    }

    @XmlElement(name = "useWorkspaceAsHome")
    public Boolean getUseWorkspaceAsHome() {
        return useWorkspaceAsHome;
    }

    public void setUseWorkspaceAsHome(Boolean useWorkspaceAsHome) {
        this.useWorkspaceAsHome = useWorkspaceAsHome;
    }

    @XmlElement(name = "wrapperLocation")
    public String getWrapperLocation() {
        return wrapperLocation;
    }

    public void setWrapperLocation(String wrapperLocation) {
        this.wrapperLocation = wrapperLocation;
    }

    @XmlElement(name = "systemProperties")
    public String getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    @XmlElement(name = "passAllAsSystemProperties")
    public Boolean getPassAllAsSystemProperties() {
        return passAllAsSystemProperties;
    }

    public void setPassAllAsSystemProperties(Boolean passAllAsSystemProperties) {
        this.passAllAsSystemProperties = passAllAsSystemProperties;
    }

    @XmlElement(name = "projectProperties")
    public String getProjectProperties() {
        return projectProperties;
    }

    public void setProjectProperties(String projectProperties) {
        this.projectProperties = projectProperties;
    }

    @XmlElement(name = "passAllAsProjectProperties")
    public Boolean getPassAllAsProjectProperties() {
        return passAllAsProjectProperties;
    }

    public void setPassAllAsProjectProperties(Boolean passAllAsProjectProperties) {
        this.passAllAsProjectProperties = passAllAsProjectProperties;
    }

}
