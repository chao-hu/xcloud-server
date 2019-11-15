/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ruzz
 *
 */
@ApiModel(value = "弹性伸缩模型")
@Data
public class ServiceScaleModelDTO {

    @ApiModelProperty(value = "总副本数", required = true, example = "3", dataType = "int")
    private Integer instance;

}
