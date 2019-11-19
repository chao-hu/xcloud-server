package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceLocalAddDTO
 * @Description: 新增本地存储模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "新增本地存储模版")
public class ServiceLocalAddDTO {

    @ApiModelProperty(value = "主机路径", required = true, example = "/test", dataType = "String")
    @NotBlank(message = "主机路径为空")
    private String hostPath;

    @ApiModelProperty(value = "挂载路径", required = true, example = "/test1", dataType = "String")
    @NotBlank(message = "挂载路径为空")
    private String mountPath;

}
