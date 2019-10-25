package com.xxx.xcloud.rest.v1.tenant.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: TenantDTO
 * @Description: 租户请求模型
 * @author huchao
 * @date 2019年10月25日
 *
 */
@ApiModel(value = "租户请求模型")
@Data
public class TenantDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;
}
