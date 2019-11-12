package com.xxx.xcloud.module.devops.build.wrappers.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * NodeJsWrapper
 *
 * @author xujiangpeng
 * @date 2019/6/5
 */
public class NodeJsWrapper {

    /**
     * The NodeJS-plugin version.
     */
    private String plugin;

    private String installationName;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "nodeJSInstallationName")
    public String getInstallationName() {
        return installationName;
    }

    public void setInstallationName(String installationName) {
        this.installationName = installationName;
    }
}

