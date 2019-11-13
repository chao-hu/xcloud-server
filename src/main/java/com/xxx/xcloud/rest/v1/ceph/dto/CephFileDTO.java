/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 文件存储请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "文件存储请求模型")
public class CephFileDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "文件存储卷名称", required = true, example = "testfilename", dataType = "String")
    @NotBlank(message = "文件存储卷名称不能为空")
    private String storageFileName;

    @ApiModelProperty(value = "文件存储大小", required = true, example = "1", dataType = "Double")
    @Min(value = 1, message = "文件存储大小需为正整数")
    private Double storageFileSize;

    @ApiModelProperty(value = "描述", required = false, example = "描述", dataType = "String")
    private String description;

    @ApiModelProperty(value = "项目ID", required = false, example = "1", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testUser", dataType = "String")
    private String createdBy;

}
