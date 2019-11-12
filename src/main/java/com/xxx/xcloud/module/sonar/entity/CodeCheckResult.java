package com.xxx.xcloud.module.sonar.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author daien
 * @date 2019年5月6日
 */
@Entity
@Table(name = "`BDOS_CODE_CHECK_RESULT`")
public class CodeCheckResult {

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`SONAR_TASK_ID`")
    private String sonarTaskId;

    /**
     * 健康度
     */
    @Column(name = "`HEALTH_DEGREE`")
    private Double healthDegree;

    /**
     * 代码行数
     */
    @Column(name = "`CODE_LINE_NUMBERS`")
    private Integer codeLineNumbers;

    /**
     * 问题数
     */
    @Column(name = "`QUESTION_NUMBERS`")
    private Integer questionNumbers;

    /**
     * info,minor,major,critical,blocker
     */
    @Column(name = "`INFO_QUESTION_NUMBERS`")
    private Integer infoQuestionNumbers;

    @Column(name = "`MINOR_QUESTION_NUMBERS`")
    private Integer minorQuestionNumbers;

    @Column(name = "`MAJOR_QUESTION_NUMBERS`")
    private Integer majorQuestionNumbers;

    @Column(name = "`CRITICAL_QUESTION_NUMBERS`")
    private Integer criticalQuestionNumbers;

    @Column(name = "`BLOCKER_QUESTION_NUMBERS`")
    private Integer blockerQuestionNumbers;

    /**
     * 检查时间
     */
    @Column(name = "`CHECK_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /**
     * 检查持续时间
     */
    @Column(name = "`CHECK_DURATION_TIME`")
    private Integer checkDurationTime;


    /**
     * 规则集名称
     */
    @Column(name = "`PROFILE_NAME`")
    private String profileName;

    /**
     * 规则集语言
     */
    @Column(name = "`LANGUAGE`")
    private String language;

    /**
     * 是否是系统规则集
     */
    @Column(name = "`SYSTEM_PROFILE`")
    private Boolean systemProfile;

    /**
     * 规则数量统计
     */
    @Column(name = "`RULE_NUM_STATISTICS`")
    private String ruleNumStatistics;

    /**
     * 代码库名称
     */
    @Column(name = "`CODE_BASE_NAME`")
    private String codeBaseName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getHealthDegree() {
        return healthDegree;
    }

    public void setHealthDegree(Double healthDegree) {
        this.healthDegree = healthDegree;
    }

    public Integer getCodeLineNumbers() {
        return codeLineNumbers;
    }

    public void setCodeLineNumbers(Integer codeLineNumbers) {
        this.codeLineNumbers = codeLineNumbers;
    }

    public Integer getQuestionNumbers() {
        return questionNumbers;
    }

    public void setQuestionNumbers(Integer questionNumbers) {
        this.questionNumbers = questionNumbers;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public Integer getCheckDurationTime() {
        return checkDurationTime;
    }

    public void setCheckDurationTime(Integer checkDurationTime) {
        this.checkDurationTime = checkDurationTime;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getSystemProfile() {
        return systemProfile;
    }

    public void setSystemProfile(Boolean systemProfile) {
        this.systemProfile = systemProfile;
    }

    public String getSonarTaskId() {
        return sonarTaskId;
    }

    public void setSonarTaskId(String sonarTaskId) {
        this.sonarTaskId = sonarTaskId;
    }

    public String getRuleNumStatistics() {
        return ruleNumStatistics;
    }

    public void setRuleNumStatistics(String ruleNumStatistics) {
        this.ruleNumStatistics = ruleNumStatistics;
    }

    public String getCodeBaseName() {
        return codeBaseName;
    }

    public void setCodeBaseName(String codeBaseName) {
        this.codeBaseName = codeBaseName;
    }

    public Integer getInfoQuestionNumbers() {
        return infoQuestionNumbers;
    }

    public void setInfoQuestionNumbers(Integer infoQuestionNumbers) {
        this.infoQuestionNumbers = infoQuestionNumbers;
    }

    public Integer getMinorQuestionNumbers() {
        return minorQuestionNumbers;
    }

    public void setMinorQuestionNumbers(Integer minorQuestionNumbers) {
        this.minorQuestionNumbers = minorQuestionNumbers;
    }

    public Integer getMajorQuestionNumbers() {
        return majorQuestionNumbers;
    }

    public void setMajorQuestionNumbers(Integer majorQuestionNumbers) {
        this.majorQuestionNumbers = majorQuestionNumbers;
    }

    public Integer getCriticalQuestionNumbers() {
        return criticalQuestionNumbers;
    }

    public void setCriticalQuestionNumbers(Integer criticalQuestionNumbers) {
        this.criticalQuestionNumbers = criticalQuestionNumbers;
    }

    public Integer getBlockerQuestionNumbers() {
        return blockerQuestionNumbers;
    }

    public void setBlockerQuestionNumbers(Integer blockerQuestionNumbers) {
        this.blockerQuestionNumbers = blockerQuestionNumbers;
    }

}
