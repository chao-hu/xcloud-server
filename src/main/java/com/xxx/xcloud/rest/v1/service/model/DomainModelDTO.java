/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ruzz
 *
 */
@ApiModel(value = "Domain请求模型(一级域名创建只填tenantName，domain，type=TLD)")
@Data
public class DomainModelDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "tenantname", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    private String tenantName;

    @ApiModelProperty(value = "域名名称", required = true, example = "testdomain", dataType = "String")
    @NotBlank(message = "域名不能为空")
    private String domain;

    @ApiModelProperty(value = "项目编码", required = true, example = "", dataType = "String")
    private String projectCode;

    @ApiModelProperty(value = "是否使用高级参数,0:不使用,1:使用", required = false, example = "testtenant", dataType = "Byte")
    private Byte configStatus;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String addBaseUrl;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String baseUrlScheme;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String xForwardedPrefix;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String proxyPassparams;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String serverAlias;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String limitRate;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String limitRateAfter;

    @ApiModelProperty(value = "是否使用https配置,0:不使用,1:使用", required = false, example = "", dataType = "Byte")
    private Byte httpsStatus;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String httpsSecretName;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String httpsTlsCrt;

    @ApiModelProperty(value = "", required = false, example = "", dataType = "String")
    private String httpsTlsKet;

    @ApiModelProperty(value = "类型 TLD:一级域名 SLD:二级域名", required = false, example = "SLD", dataType = "String")
    @NotBlank(message = "type不能为空")
    private String type;

}
