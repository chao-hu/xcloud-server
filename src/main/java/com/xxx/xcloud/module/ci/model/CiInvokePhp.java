package com.xxx.xcloud.module.ci.model;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年5月30日 上午11:13:39
 */
public class CiInvokePhp {
    /**
     * phing版本
     */
    private String phingVersion;

    /**
     * 目标命令
     */
    private String targets;

    /**
     * build文件
     */
    private String phingBuildFile;

    public String getPhingVersion() {
        return phingVersion;
    }

    public void setPhingVersion(String phingVersion) {
        this.phingVersion = phingVersion;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public String getPhingBuildFile() {
        return phingBuildFile;
    }

    public void setPhingBuildFile(String phingBuildFile) {
        this.phingBuildFile = phingBuildFile;
    }

}
