package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:  <br/> 
 * date: 2019年9月3日 下午5:22:27 <br/> 
 * @author LYJ 
 * @version  
 * @since JDK 1.8
 */
@ApiModel(value = "容器ip和域名映射模版")
@Data
public class ServiceHostAliasesModelDTO {

	@ApiModelProperty(value = "ip", required = false, example = "", dataType = "String")
	private String ip;
	
	@ApiModelProperty(value = "domain", required = false, example = "", dataType = "List<String>")
	private List<String> domains;

	@Override
	public String toString() {
		return "SwServiceHostAliasesModel [ip=" + ip + ", domains=" + domains + "]";
	}

}
