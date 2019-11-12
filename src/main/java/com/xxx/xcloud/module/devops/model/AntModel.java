package com.xxx.xcloud.module.devops.model;

public class AntModel {
    private String plugin;
    /**
     * 命令
     */
    private String targets;
    /**
     * ant版本号名称
     */
    private String antName;
    /**
     * 对应ant build的 java选项字段
     */
    private String antOpts;
    /**
     * 不填为默认使用根目录下的build.xml; 指定文件: xxx/xxx.xml 或 ./xxx/xxx.xml 格式
     */
    private String buildFile;
    /**
     * Properties 用于设定一些变量，这些变量可以在target中直接引用，或覆盖已设定的属性值。
     */
    private String properties;
    
	public String getPlugin() {
		return plugin;
	}
	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
	public String getTargets() {
		return targets;
	}
	public void setTargets(String targets) {
		this.targets = targets;
	}
	public String getAntName() {
		return antName;
	}
	public void setAntName(String antName) {
		this.antName = antName;
	}
	public String getAntOpts() {
		return antOpts;
	}
	public void setAntOpts(String antOpts) {
		this.antOpts = antOpts;
	}
	public String getBuildFile() {
		return buildFile;
	}
	public void setBuildFile(String buildFile) {
		this.buildFile = buildFile;
	}
	public String getProperties() {
		return properties;
	}
	public void setProperties(String properties) {
		this.properties = properties;
	}
    
    
}
