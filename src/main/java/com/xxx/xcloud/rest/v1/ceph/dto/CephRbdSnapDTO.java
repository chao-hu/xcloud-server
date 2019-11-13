/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 块存储快照请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "块存储快照请求模型")
public class CephRbdSnapDTO {

    @ApiModelProperty(value = "快照名称", required = true, example = "snapName", dataType = "String")
    @NotBlank(message = "快照名称不能为空")
    private String snapName;

    @ApiModelProperty(value = "快照描述", required = false, example = "snapDescription", dataType = "String")
    private String snapDescription;

}
