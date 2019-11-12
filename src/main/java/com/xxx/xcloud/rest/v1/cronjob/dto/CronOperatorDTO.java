/**
 *
 */
package com.xxx.xcloud.rest.v1.cronjob.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 定时任务操作请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "定时任务操作请求模型")
public class CronOperatorDTO {

    @ApiModelProperty(value = "操作,启动:start,停止:stop,修改:modify", required = true, example = "", dataType = "String")
    @NotBlank(message = "操作内容不能为空")
    private String operation;

    @ApiModelProperty(value = "CPU", required = false, example = "", dataType = "Double")
    private Double cpu;

    @ApiModelProperty(value = "内存", required = false, example = "", dataType = "Double")
    private Double memory;

    @ApiModelProperty(value = "启动命令", required = false, example = "", dataType = "String")
    private String cmd;

    @ApiModelProperty(value = "任务时间  X月X日X时X分", required = false, example = "", dataType = "String")
    private String schedule;

}
