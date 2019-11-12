/**
 *
 */
package com.xxx.xcloud.rest.v1.cronjob.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 定时任务请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "定时任务请求模型")
public class CronjobDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "任务名称", required = true, example = "testcron", dataType = "String")
    @Pattern(regexp = Global.CHECK_CRONJOB_NAME, message = "任务名称不符合规范")
    @NotBlank(message = "任务名称不能为空")
    private String name;

    @ApiModelProperty(value = "镜像版本id", required = true, example = "imageVersionId", dataType = "String")
    @NotBlank(message = "镜像版本id不能为空")
    private String imageVersionId;

    @ApiModelProperty(value = "CPU", required = true, example = "1", dataType = "Double")
    @Min(value = 1, message = "CPU需为正整数")
    private Double cpu;

    @ApiModelProperty(value = "内存", required = true, example = "1", dataType = "Double")
    @Min(value = 1, message = "内存需为正整数")
    private Double memory;

    @ApiModelProperty(value = "启动命令", required = false, example = "", dataType = "String")
    private String cmd;

    @ApiModelProperty(value = "任务时间  X月X日X时X分", required = false, example = "每分钟", dataType = "String")
    private String scheduleCh;

    @ApiModelProperty(value = "定时计划--cron表达式", required = true, example = "*/1 * * * *", dataType = "String")
    @NotBlank(message = "cron表达式不能为空")
    private String schedule;

    @ApiModelProperty(value = "项目ID", required = false, example = "projectid", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testtenant", dataType = "String")
    private String createdBy;

}
