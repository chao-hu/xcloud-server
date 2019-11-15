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
@ApiModel(value = "修改配额模型")
@Data
public class ServiceQuotaModelDTO {

    @ApiModelProperty(value = "CPU", required = true, example = "1", dataType = "Double")
    private Double cpu;

    @ApiModelProperty(value = "内存", required = true, example = "1", dataType = "Double")
    private Double memory;

    @ApiModelProperty(value = "GPU", required = true, example = "1", dataType = "Double")
    private Integer gpu;

}
