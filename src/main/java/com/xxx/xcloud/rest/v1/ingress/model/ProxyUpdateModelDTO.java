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
@ApiModel(value = "HTTP代理请求模型")
@Data
public class ProxyUpdateModelDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "服务代理信息", required = true)
    private ServcieIngressUpdateModelDTO serviceIngress;

    @ApiModelProperty(value = "http代理相关参数", required = true)
    private IngressProxyUpdateModelDTO ingressProxy;

}
