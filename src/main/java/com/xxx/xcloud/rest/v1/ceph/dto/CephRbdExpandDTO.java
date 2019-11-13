/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 块存储扩容请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "块存储扩容请求模型")
public class CephRbdExpandDTO {

    @ApiModelProperty(value = "操作，expand", required = true, example = "expand", dataType = "String")
    @NotBlank(message = "操作不能为空")
    private String operation;

    @ApiModelProperty(value = "全值，非增量", required = true, example = "1", dataType = "Double")
    @Min(0)
    private Double size;

}
