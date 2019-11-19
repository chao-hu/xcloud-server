package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceConfigAddDTO
 * @Description: 添加服务配置文件模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "添加服务配置文件模版")
public class ServiceConfigAddDTO {

    @ApiModelProperty(value = "关联配置文件模板id", required = true, example = "", dataType = "String")
    @NotBlank(message = "关联配置文件模板id为空")
    private String configTemplateId;

    @ApiModelProperty(value = "挂载路径", required = true, example = "/test", dataType = "String")
    @NotBlank(message = "服务配置文件挂载路径为空")
    private String path;

}
