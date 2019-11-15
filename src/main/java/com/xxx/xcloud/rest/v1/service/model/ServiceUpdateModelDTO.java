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
@ApiModel(value = "服务启动、停止服务模型")
@Data
public class ServiceUpdateModelDTO {

    @ApiModelProperty(value = "操作，停止:stop,启动:start", required = true, example = "start", dataType = "String")
    private String operation;

}
