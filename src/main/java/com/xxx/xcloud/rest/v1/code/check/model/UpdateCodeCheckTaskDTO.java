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
     * 规则集
     */
    @ApiModelProperty(value = "规则集语言类型", required = true, example = "", dataType = "String")
    private String lang;

    @ApiModelProperty(value = "规则集名称", required = true, example = "", dataType = "String")
    private String sonarQualityprofileName;

}
