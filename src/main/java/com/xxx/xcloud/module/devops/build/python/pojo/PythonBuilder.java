package com.xxx.xcloud.module.devops.build.python.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * python构建信息
 * 
 * @author mengaijun
 * @date: 2019年4月3日 上午10:23:01
 */
public class PythonBuilder {
    /**
     * 
     */
    private String plugin;

    /**
     * 配置的python版本信息
     */
    private String pythonName;

    /**
     * Shell || Xshell || Python
     */
    private String nature;

    /**
     * 命令
     */
    private String command;

    /**
     * 
     */
    private boolean ignoreExitCode;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "pythonName")
    public String getPythonName() {
        return pythonName;
    }

    public void setPythonName(String pythonName) {
        this.pythonName = pythonName;
    }

    @XmlElement(name = "nature")
    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    @XmlElement(name = "command")
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @XmlElement(name = "ignoreExitCode")
    public boolean isIgnoreExitCode() {
        return ignoreExitCode;
    }

    public void setIgnoreExitCode(boolean ignoreExitCode) {
        this.ignoreExitCode = ignoreExitCode;
    }

}
