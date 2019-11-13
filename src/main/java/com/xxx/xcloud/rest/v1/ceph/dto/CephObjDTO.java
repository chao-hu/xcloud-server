/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 对象存储新建桶模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "对象存储新建桶模型")
public class CephObjDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testTenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "项目id", required = false, example = "123456", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "桶名称", required = true, example = "testObj", dataType = "String")
    @NotBlank(message = "桶名称不能为空")
    private String bucketName;

    @ApiModelProperty(value = "访问权限:private,publicRead,publicReadWrite", required = true, example = "private", dataType = "String")
    @NotBlank(message = "访问权限不能为空")
    private String accessControlList;

}
