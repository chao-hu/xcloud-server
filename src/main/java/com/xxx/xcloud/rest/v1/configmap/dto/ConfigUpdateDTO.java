/**
 *
 */
package com.xxx.xcloud.rest.v1.configmap.dto;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 配置文件模版修改请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "配置文件模版修改请求模型")
public class ConfigUpdateDTO {

    @ApiModelProperty(value = "配置信息", required = true, example = "{'key1':'value1'}", dataType = "Map")
    @NotEmpty(message = "配置信息不能为空")
    private Map<String, String> configData;

}
