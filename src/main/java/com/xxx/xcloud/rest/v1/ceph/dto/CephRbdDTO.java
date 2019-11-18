/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.ceph.entity.CephRbd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 创建块存储请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "创建块存储请求模型")
public class CephRbdDTO {

    @ApiModelProperty(value = "块名称", required = true, example = "testRdb", dataType = "String")
    @NotBlank(message = "模版名称不能为空")
    private String rbdName;

    @ApiModelProperty(value = "块大小", required = true, example = "1", dataType = "Double")
    @Min(value = 1, message = "块大小需为正整数")
    private Double size;

    @ApiModelProperty(value = "描述", required = false, example = "", dataType = "String")
    private String description;

    @ApiModelProperty(value = "租户名称", required = true, example = "testTenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "项目ID", required = false, example = "1", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testUser", dataType = "String")
    private String createdBy;

    /**
     * 
     * 构造CephRbd
     * @Title: buildCephRbd
     * @Description: 构造CephRbd
     * @return CephRbd 
     * @throws
     */
    public CephRbd buildCephRbd() {
        return CephRbd.builder().withCreateTime(new Date()).withUpdateTime(new Date()).withDescription(description)
                .withName(rbdName).withTenantName(tenantName).withSize(size).withCreatedBy(createdBy)
                .withProjectId(projectId).build();
    }

}
