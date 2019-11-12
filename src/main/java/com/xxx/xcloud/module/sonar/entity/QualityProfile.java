package com.xxx.xcloud.module.sonar.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.xxx.xcloud.client.sonar.model.SonarQualityProfile;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author mengaijun
 * @Description: SonarQualityProfile
 * @date: 2018年12月24日 下午4:46:14
 */
@Entity
@Table(name = "`BDOS_QUALITY_PROFILE`")
public class QualityProfile implements Serializable{

	private static final long serialVersionUID = 7501855819770793769L;

	/**
	 * 主键Id
	 */
	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid")
	@GeneratedValue(generator = "uuidGenerator")
	private String id;

	/**
	 * 租户
	 */
	@Column(name = "`TENANT_NAME`")
	private String tenantName;
	
	/**
	 * 规则集在sonar对应的key
	 */
	@Column(name = "`KEY`")
	private String key;
	
	/**
	 * 规则集名称
	 */
	@Column(name = "`NAME`")
	private String name;
	
	/**
	 * 规则集语言
	 */
	@Column(name = "`LANGUAGE`")
	private String language;
	@Column(name = "`LANGUAGE_NAME`")
	private String languageName;
	
	@Column(name = "`IS_INHERITED`")
	private Boolean isInherited;
	@Column(name = "`ACTIVE_RULE_COUNT`")
	private Integer activeRuleCount;
	@Column(name = "`ACTIVE_DEPRECATED_RULE_COUNT`")
	private Integer activeDeprecatedRuleCount;
	
	@Column(name = "`IS_DEFAULT`")
	private Boolean isDefault;
	
	@Column(name = "`RULES_UPDATED_AT`")
	private String rulesUpdatedAt;
	@Column(name = "`LAST_USED`")
	private String lastUsed;
	
	@Column(name = "`CREATE_TIME`")
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;
	
	/**
	 * 拷贝sonar对象的信息到本地数据库对象自己的信息
	 * @param qualityProfile
	 * @date: 2018年12月24日 下午5:22:06
	 */
	public void copy(SonarQualityProfile qualityProfile) {
		this.name = qualityProfile.getName();
		this.language = qualityProfile.getLanguage();
		this.languageName = qualityProfile.getLanguageName();
		this.isInherited = qualityProfile.getIsInherited();
		this.isDefault = qualityProfile.getIsDefault();
		this.key = qualityProfile.getKey();
		this.activeRuleCount = qualityProfile.getActiveRuleCount();
		this.activeDeprecatedRuleCount = qualityProfile.getActiveDeprecatedRuleCount();
		this.rulesUpdatedAt = qualityProfile.getRulesUpdatedAt();
		this.lastUsed = qualityProfile.getLastUsed();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public String toString() {
		return "QualityProfileLocal [id=" + id + ", tenantName=" + tenantName + ", key=" + key + ", name=" + name
				+ ", language=" + language + ", languageName=" + languageName + ", isInherited=" + isInherited
				+ ", activeRuleCount=" + activeRuleCount + ", activeDeprecatedRuleCount=" + activeDeprecatedRuleCount
				+ ", isDefault=" + isDefault + ", rulesUpdatedAt=" + rulesUpdatedAt + ", lastUsed=" + lastUsed
				+ ", createTime=" + createTime + "]";
	}
	
}
