package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceHpaDTO
 * @Description: 自动伸缩模型
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "自动伸缩模型")
public class ServiceHpaDTO {

    @ApiModelProperty(value = "最小实例个数，自动伸缩必填", required = true, example = "", dataType = "int")
    @Min(value = 1, message = "最小实例数需为正整数")
    private Integer minReplicas;

    @ApiModelProperty(value = "最大实例个数，自动伸缩必填", required = true, example = "", dataType = "int")
    @Min(value = 1, message = "最大实例数需为正整数")
    private Integer maxReplicas;

    @ApiModelProperty(value = "CPU使用率，自动伸缩必填", required = true, example = "", dataType = "int")
    @Min(value = 1, message = "CPU阈值为空或超出规定范围")
    @Max(value = 99, message = "CPU阈值为空或超出规定范围")
    private Integer cpuThreshold;

    @ApiModelProperty(value = "", required = true, example = "true", dataType = "boolean")
    @NotNull(message = "参数isTurnOn不能为空")
    private Boolean isEnable;

}
