package com.xxx.xcloud.module.ci.model;

/**
 * @author mengaijun
 * @Description: sonar检查路径
 * @date: 2018年12月20日 下午5:30:39
 */
public class SonarRule {
    /**
     * 规则集key
     */
    private String profileKey;

    /**
     * 规则集名称
     */
    private String profileName;

    /**
     * 用逗号分隔的多个路径
     */
    private String pathStr;

    /**
     * 语言
     */
    private String language;

    public SonarRule() {
        super();
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPathStr() {
        return pathStr;
    }

    public void setPathStr(String pathStr) {
        this.pathStr = pathStr;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
