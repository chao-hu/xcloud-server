package com.xxx.xcloud.module.devops.sonar.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author xingej SonarRunnerBuilder 模块 bean
 */
public class SonarRunnerBuilder {

    private String plugin;
    private String installationName;
    private String project;
    private String properties;
    private String javaOpts;
    private String additionalArguments;
    private String jdk;
    private String task;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "installationName")
    public String getInstallationName() {
        return installationName;
    }

    public void setInstallationName(String installationName) {
        this.installationName = installationName;
    }

    @XmlElement(name = "project")
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    @XmlElement(name = "properties")
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @XmlElement(name = "javaOpts")
    public String getJavaOpts() {
        return javaOpts;
    }

    public void setJavaOpts(String javaOpts) {
        this.javaOpts = javaOpts;
    }

    @XmlElement(name = "additionalArguments")
    public String getAdditionalArguments() {
        return additionalArguments;
    }

    public void setAdditionalArguments(String additionalArguments) {
        this.additionalArguments = additionalArguments;
    }

    @XmlElement(name = "jdk")
    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    @XmlElement(name = "task")
    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
