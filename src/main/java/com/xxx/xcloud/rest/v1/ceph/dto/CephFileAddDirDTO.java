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
 * Description: 文件存储增加文件夹模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "文件存储增加文件夹模型")
public class CephFileAddDirDTO {

    @ApiModelProperty(value = "文件夹名称", required = true, example = "testdir", dataType = "String")
    @NotBlank(message = "文件夹名称不能为空")
    private String directoryName;

    @ApiModelProperty(value = "文件夹路径", required = true, example = "/test/", dataType = "String")
    @NotBlank(message = "文件夹路径不能为空")
    private String directoryPath;

}
