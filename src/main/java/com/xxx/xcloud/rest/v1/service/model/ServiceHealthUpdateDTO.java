package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceHealthUpdateDTO
 * @Description: 服务健康模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "服务健康模版")
public class ServiceHealthUpdateDTO {

    @ApiModelProperty(value = "服务健康监测模版id(修改必传)", required = false, example = "", dataType = "String")
    private String id;

    @ApiModelProperty(value = "初始化等待时间(s)", required = true, example = "", dataType = "int")
    private Integer initialDelay;

    @ApiModelProperty(value = "间隔时间(s)", required = true, example = "", dataType = "int")
    private Integer periodDetction;

    @ApiModelProperty(value = "超时时间(s)", required = true, example = "", dataType = "int")
    private Integer timeoutDetction;

    @ApiModelProperty(value = "连接成功次数", required = true, example = "", dataType = "int")
    private Integer successThreshold;

    @ApiModelProperty(value = "健康检查探针类型（1表示运行时，2表示启动时）", required = true, example = "", dataType = "int")
    private Integer probe;

    @ApiModelProperty(value = "shell脚本(多条命令用逗号分割的json串)", required = true, example = "", dataType = "String")
    private String exec;

    @ApiModelProperty(value = "TCP对象", required = true)
    private ServiceHealthTcpUpdateDTO tcp;

    @ApiModelProperty(value = "Http对象", required = true)
    private ServiceHealthHttpUpdateDTO http;

    @ApiModelProperty(value = "是否启动当前探针", required = true, example = "", dataType = "boolean")
    private Boolean isTurnOn;

}
