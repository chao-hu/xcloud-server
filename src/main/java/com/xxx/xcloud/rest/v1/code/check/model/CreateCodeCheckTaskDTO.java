package com.xxx.xcloud.rest.v1.code.check.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建构建任务参数
 *
 * @author HBL
 */
@ApiModel(value = "创建代码检查任务模型")
@Data
public class CreateCodeCheckTaskDTO {

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
     * 代码构建
     */
    @ApiModelProperty(value = "代码地址", required = true, example = "", dataType = "String")
    private String codeUrl;

    @ApiModelProperty(value = "代码项目名称", required = false, example = "", dataType = "String")
    private String reposName;

    @ApiModelProperty(value = "代码项目Id", required = false, example = "", dataType = "Integer")
    private Integer reposId;

    @ApiModelProperty(value = "分支或标签", required = true, example = "0：分支；1：标签", dataType = "byte")
    private Byte branchOrTag;

    @ApiModelProperty(value = "分支", required = true, example = "", dataType = "String")
    private String codeBranch;

    @ApiModelProperty(value = "代码库类型", required = true, example = "0:none 1:git 2:svn", dataType = "byte")
    private Byte codeControlType;

    @ApiModelProperty(value = "认证方式ID", required = true, example = "", dataType = "String")
    private String ciCodeCredentialsId;

    @ApiModelProperty(value = "规则集语言类型", required = true, example = "", dataType = "String")
    private String lang;

    @ApiModelProperty(value = "规则集名称", required = true, example = "", dataType = "String")
    private String sonarQualityprofileName;

    @ApiModelProperty(value = "创建者", required = false, example = "", dataType = "String")
    private String createdBy;

}
