package com.xxx.xcloud.rest.v1.springcloud.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @ClassName: SpringCloudConfigDTO
 * @Description: SpringCloudConfig 配置文件模型
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Data
@ApiModel(value = "SpringCloudConfig 配置文件模型")
public class SpringCloudConfigDTO {

    @ApiModelProperty(value = "ConfigService服务Id", required = true, example = "12345", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String serviceId;

    @ApiModelProperty(value = "配置文件名称", required = true, example = "application.properties", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String configName;

    @ApiModelProperty(value = "配置文件内容", required = true, example = "", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String configContent;

    @ApiModelProperty(value = "是否启用,0:不启用，1：启用", required = true, example = "0", dataType = "number")
    @NotNull(message = "配置是否启用不能为空")
    private int enable;
}
