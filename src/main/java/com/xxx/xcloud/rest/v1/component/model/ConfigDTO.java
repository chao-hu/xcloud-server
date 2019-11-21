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
 * @Description: 修改配置DTO
 * @author lnn
 * @date 2019年11月12日
 *
 */

@Data
@ApiModel(value = "修改配置请求模型")
public class ConfigDTO {
    
    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "配置信息", required = true, example = "{'mysqld':{'ordinary':{'max_connections':'1024'}}}", dataType = "JSONObject")
    @NotNull(message = "配置信息不能为空")
    private JSONObject info;

}
