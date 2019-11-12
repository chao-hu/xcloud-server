/**
 *
 */
package com.xxx.xcloud.rest.v1.env.dto;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 环境变量模版修改请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "修改环境变量模版")
public class EnvUpdateDTO {

    @ApiModelProperty(value = "环境变量", required = true, example = "{'key1':'value1'}", dataType = "Map")
    @NotEmpty(message = "环境变量不能为空")
    private Map<String, Object> envData;

}
