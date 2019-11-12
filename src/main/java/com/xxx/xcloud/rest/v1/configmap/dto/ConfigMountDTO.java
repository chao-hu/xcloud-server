/**
 *
 */
package com.xxx.xcloud.rest.v1.configmap.dto;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 创建配置挂载请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "创建配置挂载请求模型")
public class ConfigMountDTO {

    @ApiModelProperty(value = "服务ID", required = true, example = "", dataType = "String")
    @NotBlank(message = "服务ID不能为空")
    private String serviceId;

    @ApiModelProperty(value = "配置挂载信息", required = true, example = "{'configTemplateId':'path(挂载路径)'}", dataType = "Map")
    @NotEmpty(message = "配置挂载信息不能为空")
    private Map<String, String> configIdAndPath;

}
