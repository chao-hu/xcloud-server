package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午5:39:52
 */
@ApiModel(value = "服务修改代理信息")
@Data
public class ServcieIngressUpdateModelDTO {

    @ApiModelProperty(value = "服务代理信息ID", required = true, example = "")
    private String id;

    @ApiModelProperty(value = "服务ID", required = true, example = "")
    private String serviceId;

    @ApiModelProperty(value = "ingress代理ID", required = true, example = "")
    private String ingressProxyId;

    @ApiModelProperty(value = "ingress域名ID", required = true, example = "")
    private String ingressDomainId;

    @ApiModelProperty(value = "服务访问路径和服务端口", required = true, example = "")
    private String pathAndPort;

}