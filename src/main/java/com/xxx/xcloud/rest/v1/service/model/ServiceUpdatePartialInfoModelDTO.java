/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangkebiao
 *
 */
@ApiModel(value = "修改服务部分属性模型")
@Data
public class ServiceUpdatePartialInfoModelDTO {

	@ApiModelProperty(value = "cmd", required = false, example = "sleep 666", dataType = "String")
	private String cmd;

	@ApiModelProperty(value = "description", required = false, example = "just test", dataType = "String")
	private String description;

	@ApiModelProperty(value = "是否使用APM监控 0:不使用，1:使用", required = true, example = "json", dataType = "Boolean")
	private Boolean isUsedApm;

	@ApiModelProperty(value = "List<HostAliases>对象", required = false)
	private List<ServiceHostAliasesModelDTO> hostAliases;

	@ApiModelProperty(value = "InitContainer对象", required = false)
	private ServiceInitContainerModelDTO initContainer;

}
