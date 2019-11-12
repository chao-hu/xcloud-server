package com.xxx.xcloud.module.devops.build.wrappers.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class GolangBuildWrapper {

    private String plugin;

    private String goVersion;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "goVersion")
    public String getGoVersion() {
        return goVersion;
    }

    public void setGoVersion(String goVersion) {
        this.goVersion = goVersion;
    }

}
