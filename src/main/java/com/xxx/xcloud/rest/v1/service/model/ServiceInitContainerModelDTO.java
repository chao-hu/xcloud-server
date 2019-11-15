package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:  <br/> 
 * date: 2019年9月3日 下午4:42:03 <br/> 
 * @author LYJ 
 * @version  
 * @since JDK 1.8
 */
@ApiModel(value = "初始化容器模版")
@Data
public class ServiceInitContainerModelDTO {

	@ApiModelProperty(value = "检查端口", required = false, example = "", dataType = "Command")
    private ServiceInitContainerCommandModelDTO command;

	@Override
	public String toString() {
		return "SwServiceInitContainerModel [command=" + command + "]";
	}
	
}
