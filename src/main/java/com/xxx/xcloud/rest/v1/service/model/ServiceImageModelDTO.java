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
@ApiModel(value = "服务版本升级模型")
@Data
public class ServiceImageModelDTO {

    @ApiModelProperty(value = "镜像版本ID", required = true, example = "", dataType = "String")
    private String imageId;

    @ApiModelProperty(value = "步长(步长和百分比只能其中填写一个)", required = false, example = "", dataType = "int")
    private Integer stepLength;

    @ApiModelProperty(value = "百分比(步长和百分比只能其中填写一个)", required = false, example = "", dataType = "String")
    private Integer percentage;

}
