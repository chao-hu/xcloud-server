package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:  <br/> 
 * date: 2019年10月15日 上午10:57:16 <br/> 
 * @author LYJ 
 * @version  
 * @since JDK 1.8
 */
@ApiModel(value = "灰度发布配置")
@Data
public class CanaryConfigModelDTO {

	@ApiModelProperty(value = "请求头的key", required = true, example = "", dataType = "String")
	private String headerKey;
	
	@ApiModelProperty(value = "请求头的value", required = true, example = "", dataType = "String")
	private String headerValue;
	
	@ApiModelProperty(value = "cookie", required = true, example = "", dataType = "String")
	private String cookie;
	
	@ApiModelProperty(value = "权重", required = true, example = "", dataType = "String")
	private String canaryWeight;

}
