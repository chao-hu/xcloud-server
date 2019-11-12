package com.xxx.xcloud.rest.v1.code.check.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 修改构建任务参数
 *
 * @author HBL
 */
@ApiModel(value = "修改代码检查任务模型")
@Data
public class UpdateCodeCheckTaskDTO {
    @ApiModelProperty(value = "租户名称", required = true, example = "testTenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "检查任务名称", required = true, example = "test", dataType = "String")
    private String taskName;

    @ApiModelProperty(value = "构建任务描述", required = false, example = "", dataType = "String")
    private String taskDesc;

    /**
     * 构建计划
     */
    @ApiModelProperty(value = "执行计划表达式", required = false, example = "", dataType = "String")
    private String cron;

    @ApiModelProperty(value = "执行计划描述", required = false, example = "", dataType = "String")
    private String cronDescription;

    /**
     * 代码
     */
    // @ApiModelProperty(value = "代码地址", required = true, example = "", dataType
    // = "String")
    // private String codeUrl;
    //
    // @ApiModelProperty(value = "代码项目名称", required = false, example = "",
    // dataType = "String")
    // private String reposName;
    //
    // @ApiModelProperty(value = "代码项目Id", required = false, example = "",
    // dataType = "Integer")
    // private Integer reposId;
    //
    // @ApiModelProperty(value = "分支", required = true, example = "", dataType =
    // "String")
    // private String codeBranch;
    //
    // @ApiModelProperty(value = "代码库类型", required = true, example = "0:none
    // 1:git 2:svn", dataType = "byte")
    // private Byte codeControlType;
    //
    // @ApiModelProperty(value = "认证方式ID", required = true, example = "",
    // dataType = "String")
    // private String ciCodeCredentialsId;

    /**
     * 规则集
     */
    @ApiModelProperty(value = "规则集语言类型", required = true, example = "", dataType = "String")
    private String lang;

    @ApiModelProperty(value = "规则集名称", required = true, example = "", dataType = "String")
    private String sonarQualityprofileName;

    // @ApiModelProperty(value = "创建者", required = false, example = "", dataType
    // = "String")
    // private String createdBy;

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

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
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

    // public String getCodeUrl() {
    // return codeUrl;
    // }
    //
    // public void setCodeUrl(String codeUrl) {
    // this.codeUrl = codeUrl;
    // }
    //
    // public String getReposName() {
    // return reposName;
    // }
    //
    // public void setReposName(String reposName) {
    // this.reposName = reposName;
    // }
    //
    // public Integer getReposId() {
    // return reposId;
    // }
    //
    // public void setReposId(Integer reposId) {
    // this.reposId = reposId;
    // }
    //
    // public String getCodeBranch() {
    // return codeBranch;
    // }
    //
    // public void setCodeBranch(String codeBranch) {
    // this.codeBranch = codeBranch;
    // }
    //
    // public Byte getCodeControlType() {
    // return codeControlType;
    // }
    //
    // public void setCodeControlType(Byte codeControlType) {
    // this.codeControlType = codeControlType;
    // }
    //
    // public String getCiCodeCredentialsId() {
    // return ciCodeCredentialsId;
    // }
    //
    // public void setCiCodeCredentialsId(String ciCodeCredentialsId) {
    // this.ciCodeCredentialsId = ciCodeCredentialsId;
    // }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSonarQualityprofileName() {
        return sonarQualityprofileName;
    }

    public void setSonarQualityprofileName(String sonarQualityprofileName) {
        this.sonarQualityprofileName = sonarQualityprofileName;
    }

    // public String getCreatedBy() {
    // return createdBy;
    // }
    //
    // public void setCreatedBy(String createdBy) {
    // this.createdBy = createdBy;
    // }

}
