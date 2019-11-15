package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:  <br/> 
 * date: 2019年9月3日 下午4:45:48 <br/> 
 * @author LYJ 
 * @version  
 * @since JDK 1.8
 */
@ApiModel(value = "初始化容器命令模版")
@Data
public class ServiceInitContainerCommandModelDTO {

	@ApiModelProperty(value = "Tcp", required = false, example = "", dataType = "List<Map<String,String>>")
	private List<Map<String,String>> tcp;
	
	@ApiModelProperty(value = "Http", required = false, example = "", dataType = "List<Map<String,String>>")
	private List<Map<String,String>> http;

	@Override
	public String toString() {
		return "SwServiceInitContainerCommandModel [tcp=" + tcp + ", http=" + http + "]";
	}
	
}
