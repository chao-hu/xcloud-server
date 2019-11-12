/**
 * Project Name:EPM_PAAS_CLOUD
 * File Name:QualityProfile.java
 * Package Name:com.bonc.epm.paas.sonar.model
 * Date:2018年2月5日下午4:55:34
 * Copyright (c) 2018, longkaixiang@bonc.com.cn All Rights Reserved.
 *
 */

package com.xxx.xcloud.client.sonar.model;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月10日 上午10:12:35
 */
public class SonarQualityProfile {
    /**
     * 规则集key
     */
	private String key;
    /**
     * 规则集名称
     */
	private String name;
    /**
     * 规则集语言（调用接口的语言参数是这个字段）
     */
	private String language;
    /**
     * 规则集语言名称
     */
	private String languageName;
	private Boolean isInherited;
    /**
     * 激活的规则数数量
     */
	private Integer activeRuleCount;

    /**
     * 总的规则数量
     */
    private Integer allRuleCount;

	private Integer activeDeprecatedRuleCount;
	private Boolean isDefault;
	private String rulesUpdatedAt;
	private String lastUsed;

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLanguageName() {
		return languageName;
	}
	public void setLanguageName(String languageName) {
		this.languageName = languageName;
	}
	public Boolean getIsInherited() {
		return isInherited;
	}
	public void setIsInherited(Boolean isInherited) {
		this.isInherited = isInherited;
	}
	public Integer getActiveRuleCount() {
		return activeRuleCount;
	}
	public void setActiveRuleCount(Integer activeRuleCount) {
		this.activeRuleCount = activeRuleCount;
	}

    public Integer getAllRuleCount() {
        return allRuleCount;
    }

    public void setAllRuleCount(Integer allRuleCount) {
        this.allRuleCount = allRuleCount;
    }

    public Integer getActiveDeprecatedRuleCount() {
		return activeDeprecatedRuleCount;
	}
	public void setActiveDeprecatedRuleCount(Integer activeDeprecatedRuleCount) {
		this.activeDeprecatedRuleCount = activeDeprecatedRuleCount;
	}
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
	public String getRulesUpdatedAt() {
		return rulesUpdatedAt;
	}
	public void setRulesUpdatedAt(String rulesUpdatedAt) {
		this.rulesUpdatedAt = rulesUpdatedAt;
	}
	public String getLastUsed() {
		return lastUsed;
	}
	public void setLastUsed(String lastUsed) {
		this.lastUsed = lastUsed;
	}
	@Override
	public String toString() {
		return "QualityProfile [key=" + key + ", name=" + name + ", language=" + language + ", languageName="
				+ languageName + ", isInherited=" + isInherited + ", activeRuleCount=" + activeRuleCount
				+ ", activeDeprecatedRuleCount=" + activeDeprecatedRuleCount + ", isDefault=" + isDefault
				+ ", rulesUpdatedAt=" + rulesUpdatedAt + ", lastUsed=" + lastUsed + "]";
	}

}
