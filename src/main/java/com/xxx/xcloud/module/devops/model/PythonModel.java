package com.xxx.xcloud.module.devops.model;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年4月3日 上午10:31:14
 */
public class PythonModel {
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

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getPythonName() {
        return pythonName;
    }

    public void setPythonName(String pythonName) {
        this.pythonName = pythonName;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isIgnoreExitCode() {
        return ignoreExitCode;
    }

    public void setIgnoreExitCode(boolean ignoreExitCode) {
        this.ignoreExitCode = ignoreExitCode;
    }

}
