package com.xxx.xcloud.module.devops.build.ant.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Ant {

    private String plugin;
    private String targets;
    private String antName;
    private String antOpts;
    private String buildFile;
    private String properties;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "targets")
    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    @XmlElement(name = "antName")
    public String getAntName() {
        return antName;
    }

    public void setAntName(String antName) {
        this.antName = antName;
    }

    @XmlElement(name = "antOpts")
    public String getAntOpts() {
        return antOpts;
    }

    public void setAntOpts(String antOpts) {
        this.antOpts = antOpts;
    }

    @XmlElement(name = "buildFile")
    public String getBuildFile() {
        return buildFile;
    }

    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }

    @XmlElement(name = "properties")
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

}
