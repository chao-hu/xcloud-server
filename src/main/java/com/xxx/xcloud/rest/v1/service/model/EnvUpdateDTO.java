package com.xxx.xcloud.rest.v1.service.model;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: EnvUpdateDTO
 * @Description: 修改环境变量模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "修改环境变量模版")
public class EnvUpdateDTO {

    @ApiModelProperty(value = "环境变量，json", required = true, example = "", dataType = "Map<String, Object>")
    private Map<String, Object> envData;

}
