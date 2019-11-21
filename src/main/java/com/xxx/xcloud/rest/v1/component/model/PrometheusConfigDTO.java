package com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ConfigDTO
 * @Description: prometheus修改配置DTO
 * @author lnn
 * @date 2019年11月21日
 *
 */

@Data
@ApiModel(value = "Prometheus修改配置请求模型")
public class PrometheusConfigDTO {
    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "配置信息", required = true, example = "{'targets':'test','job':'mysql','monitor_host':'172.16.111.111','monitor_port':'9115','monitor_type':'database','monitor_cluster':''}", dataType = "JSONObject")
    @NotNull(message = "配置信息不能为空")
    private JSONObject configuration;
    
    @ApiModelProperty(value = "配置信息", required = true, example = "AddTarget", dataType = "String")
    @NotBlank(message = "操作不能为空")
    private String opt;
    
    
}
