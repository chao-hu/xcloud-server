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
@ApiModel(value = "服务创建代理信息")
@Data
public class ServcieIngressCreateModelDTO {

    @ApiModelProperty(value = "服务ID", required = true, example = "")
    private String serviceId;

    @ApiModelProperty(value = "ingress域名ID", required = true, example = "")
    private String ingressDomainId;

    @ApiModelProperty(value = "服务访问路径和服务端口", required = true, example = "")
    private String pathAndPort;

}
