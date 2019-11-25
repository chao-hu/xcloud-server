package com.xxx.xcloud.module.devops.model;

/**
 * phing构建信息
 * 
 * @author mengaijun
 * @date: 2019年4月2日 上午10:36:34
 */
public class PhingModel {
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

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getBuildFile() {
        return buildFile;
    }

    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public boolean isUseModuleRoot() {
        return useModuleRoot;
    }

    public void setUseModuleRoot(boolean useModuleRoot) {
        this.useModuleRoot = useModuleRoot;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

}
