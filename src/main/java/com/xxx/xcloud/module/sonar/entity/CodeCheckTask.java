package com.xxx.xcloud.module.sonar.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author mengaijun
 * @Description: sonar任务
 * @date: 2018年12月25日 下午3:13:10
 */
@Entity
@Table(name = "`BDOS_CODE_CHECK_TASK`")
public class CodeCheckTask  implements Serializable {

    private static final long serialVersionUID = 2158454962288757072L;

    /**
     * 主键Id
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 租户名
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * 任务名称
     */
    @Column(name = "`TASK_NAME`")
    private String taskName;

    /**
     *  任务描述
     */
    @Column(name = "`TASK_DESC`")
    private String taskDesc;

    /**
     * 状态 1未检查2运行中3完成4失败5禁用
     */
    @Column(name = "`STATUS`")
    private byte status;

    /**
     * 创建时间
     */
    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 时间表达式
     */
    @Column(name = "`CRON`")
    private String cron;

    /**
     * 时间表达式描述
     */
    @Column(name = "`CRON_DESCRIPTION`")
    private String cronDescription;

    /**
     * 代码信息
     */
    @Column(name = "`CODE_INFO_ID`")
    private String codeInfoId;

    /**
     * 规则集
     */
    @Column(name = "`SONAR_RULE_JOSN_STR`")
    private String sonarRuleJosnStr;

    /**
     * 规则集语言
     */
    @Column(name = "`LANGUAGE`")
    private String language;

    /**
     * 最新一次检查成功检查持续时间
     */
    @Column(name = "`CHECK_DURATION_TIME`")
    private Integer checkDurationTime;

    /**
     * 检查时间
     */
    @Column(name = "`CHECK_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /**
     * 最新一次检查成功时间
     */
    @Column(name = "`LAST_CHECK_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastCheckTime;

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Transient
    private String ruleNumStatistics;

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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCronDescription() {
        return cronDescription;
    }

    public void setCronDescription(String cronDescription) {
        this.cronDescription = cronDescription;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCodeInfoId() {
        return codeInfoId;
    }

    public void setCodeInfoId(String codeInfoId) {
        this.codeInfoId = codeInfoId;
    }

    public String getSonarRuleJosnStr() {
        return sonarRuleJosnStr;
    }

    public void setSonarRuleJosnStr(String sonarRuleJosnStr) {
        this.sonarRuleJosnStr = sonarRuleJosnStr;
    }

    public Integer getCheckDurationTime() {
        return checkDurationTime;
    }

    public void setCheckDurationTime(Integer checkDurationTime) {
        this.checkDurationTime = checkDurationTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public String getRuleNumStatistics() {
        return ruleNumStatistics;
    }

    public void setRuleNumStatistics(String ruleNumStatistics) {
        this.ruleNumStatistics = ruleNumStatistics;
    }
    
}
