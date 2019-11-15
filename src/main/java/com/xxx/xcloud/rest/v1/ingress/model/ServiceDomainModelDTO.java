/**
 *
 */
package com.xxx.xcloud.rest.v1.ingress.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ruzz
 *
 */
@ApiModel(value = "域名相关信息模型")
@Data
public class ServiceDomainModelDTO {

    @ApiModelProperty(value = "一级域名", required = true, example = "testdomain", dataType = "String")
    private String domain;

    @ApiModelProperty(value = "项目编码", required = false, example = "", dataType = "String")
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
    private String httpsTlsKey;

}
