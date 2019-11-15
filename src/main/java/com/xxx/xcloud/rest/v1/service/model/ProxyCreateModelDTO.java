/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import com.xxx.xcloud.rest.v1.ingress.model.ServiceDomainModelDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ruzz
 *
 */
@ApiModel(value = "HTTP代理创建请求模型")
@Data
public class ProxyCreateModelDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "服务代理信息", required = true)
    private ServcieIngressCreateModelDTO serviceIngress;

    @ApiModelProperty(value = "http代理相关参数", required = true)
    private IngressProxyCreateModelDTO ingressProxy;

    @ApiModelProperty(value = "域名相关信息", required = true)
    private ServiceDomainModelDTO serviceDomain;

}
