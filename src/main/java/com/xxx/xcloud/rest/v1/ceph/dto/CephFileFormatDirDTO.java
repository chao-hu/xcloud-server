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
 * Description: 格式化文件存储请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "格式化文件存储请求模型")
public class CephFileFormatDirDTO {

    @ApiModelProperty(value = "操作类型", required = true, example = "format", dataType = "String")
    @NotBlank(message = "操作类型不能为空")
    private String operation;

}
