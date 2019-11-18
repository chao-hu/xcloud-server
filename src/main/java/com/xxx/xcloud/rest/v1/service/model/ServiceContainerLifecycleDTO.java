package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceContainerLifecycleDTO
 * @Description: 服务的容器生命周期模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "服务的容器生命周期模版")
public class ServiceContainerLifecycleDTO {

    @ApiModelProperty(value = "服务的容器生命周期ID(修改必传)", required = false, example = "", dataType = "String")
    private String id;

    @ApiModelProperty(value = "钩子类型类型（1表示容器创建后运行前，2表示容器终止前）", required = true, example = "", dataType = "Byte")
    private Byte lifecycleType;

    @ApiModelProperty(value = "shell脚本(多条命令用逗号分割的json串)", required = true, example = "", dataType = "String")
    private String exec;

    @ApiModelProperty(value = "TCP对象", required = true)
    private ServiceHealthTcpUpdateDTO tcp;

    @ApiModelProperty(value = "Http对象", required = true)
    private ServiceHealthHttpUpdateDTO http;

    @ApiModelProperty(value = "是否启动当前探针", required = true, example = "", dataType = "boolean")
    private Boolean isTurnOn;

    @ApiModelProperty(value = "要连接的主机名，默认为pod IP", required = false, example = "", dataType = "String")
    private String host;

}
