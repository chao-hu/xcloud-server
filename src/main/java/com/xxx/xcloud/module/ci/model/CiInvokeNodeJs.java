package com.xxx.xcloud.module.ci.model;

/**
 * com.xxx.xcloud.module.ci.strategy.jenkins.CiInvokeNodeJs
 *
 * @author xujiangpeng
 * @date 2019/6/4
 */
public class CiInvokeNodeJs {

    private String buildcmd;
    private String langVersion;

    public String getBuildcmd() {
        return buildcmd;
    }

    public void setBuildcmd(String buildcmd) {
        this.buildcmd = buildcmd;
    }

    public String getLangVersion() {
        return langVersion;
    }

    public void setLangVersion(String langVersion) {
        this.langVersion = langVersion;
    }

}
