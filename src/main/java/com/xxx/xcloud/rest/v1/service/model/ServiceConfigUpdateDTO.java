package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceConfigUpdateDTO
 * @Description: 修改服务配置文件模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "修改服务配置文件模版")
public class ServiceConfigUpdateDTO {

    @ApiModelProperty(value = "service和配置文件关联ID", required = true, example = "", dataType = "String")
    private String id;

    @ApiModelProperty(value = "关联配置文件模板id", required = true, example = "", dataType = "String")
    @NotBlank(message = "关联配置文件模板id为空")
    private String configTemplateId;

    @ApiModelProperty(value = "挂载路径", required = true, example = "/test", dataType = "String")
    @NotBlank(message = "服务配置文件挂载路径为空")
    private String path;

}
