package com.xxx.xcloud.module.devops.build.phing.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * phing构建参数
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年4月2日 上午10:15:39
 */
public class PhingBuilder {
    private String plugin;

    /**
     * build.xml 配置文件
     */
    private String buildFile;

    /**
     * 配置的phing版本信息
     */
    private String name;

    /**
     * 构建命令
     */
    private String targets;

    private String properties;

    private boolean useModuleRoot;

    private String options;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "buildFile")
    public String getBuildFile() {
        return buildFile;
    }

    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "targets")
    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    @XmlElement(name = "properties")
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @XmlElement(name = "useModuleRoot")
    public boolean isUseModuleRoot() {
        return useModuleRoot;
    }

    public void setUseModuleRoot(boolean useModuleRoot) {
        this.useModuleRoot = useModuleRoot;
    }

    @XmlElement(name = "options")
    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

}
