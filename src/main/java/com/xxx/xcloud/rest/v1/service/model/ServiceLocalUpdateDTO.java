package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceLocalUpdateDTO
 * @Description: 修改本地存储模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "修改本地存储模版")
public class ServiceLocalUpdateDTO {

    @ApiModelProperty(value = "service和本地存储关联ID", required = true, example = "", dataType = "String")
    private String id;

    @ApiModelProperty(value = "主机路径", required = true, example = "", dataType = "String")
    @NotBlank(message = "主机路径为空")
    private String hostPath;

    @ApiModelProperty(value = "挂载路径", required = true, example = "/test", dataType = "String")
    @NotBlank(message = "挂载路径为空")
    private String mountPath;

}
