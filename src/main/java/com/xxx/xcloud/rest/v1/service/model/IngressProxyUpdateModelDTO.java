/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ruzz
 *
 */
@ApiModel(value = "http代理修改相关参数")
@Data
public class IngressProxyUpdateModelDTO {

	@ApiModelProperty(value = "http代理信息ID", required = true, example = "")
	private String id;

	@ApiModelProperty(value = "是否使用高级参数,0:不使用,1:使用", required = false, example = "testdomain", dataType = "Byte")
	private Byte configStatus;

	@ApiModelProperty(value = "", required = false, example = "", dataType = "json")
	private String tlsConfig;

	@ApiModelProperty(value = "", required = false, example = "", dataType = "json")
	private String approveConfig;

	@ApiModelProperty(value = "url配置", required = false, example = "", dataType = "json")
	private String urlConfig;

	@ApiModelProperty(value = "资源配置", required = false, example = "", dataType = "json")
	private String resourceConfig;

	@ApiModelProperty(value = "nginx配置", required = false, example = "", dataType = "json")
	private String nginxConfig;

	@ApiModelProperty(value = "其他配置", required = false, example = "", dataType = "json")
	private String otherConfig;

	@ApiModelProperty(value = "是否使用灰度发布,0:不使用,1:使用", required = true, example = "", dataType = "Byte")
	private Byte canaryStatus;

	@ApiModelProperty(value = "灰度发布配置", required = false, example = "", dataType = "canaryConfig")
    private CanaryConfigModelDTO canaryConfig;

}