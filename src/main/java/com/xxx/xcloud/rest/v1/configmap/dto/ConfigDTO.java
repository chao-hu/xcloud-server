/**
 *
 */
package com.xxx.xcloud.rest.v1.configmap.dto;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.configmap.entity.ConfigTemplate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 配置模版创建请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "配置模版创建请求模型")
public class ConfigDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "模版名称", required = true, example = "testconfig", dataType = "String")
    @Pattern(regexp = Global.CHECK_CONFIGMAP_NAME, message = "模版名称规则不符合规范")
    @NotBlank(message = "模版名称不能为空")
    private String templateName;

    @ApiModelProperty(value = "配置信息", required = true, example = "{'key1':'value1'}", dataType = "Map")
    @NotEmpty(message = "配置信息不能为空")
    private Map<String, Object> configData;

    @ApiModelProperty(value = "项目ID", required = false, example = "testproject", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testuser", dataType = "String")
    private String createdBy;
    

    /**
     * 构造ConfigTemplate对象
     * @Title: getConfigTemplate
     * @Description: 构造ConfigTemplate对象
     * @return ConfigTemplate 
     * @throws
     */
    public ConfigTemplate getConfigTemplate(){
        ConfigTemplate configTemplate = new ConfigTemplate();
        configTemplate.setConfigData(JSON.toJSONString(getConfigData()));
        configTemplate.setCreatedBy(getCreatedBy());
        configTemplate.setProjectId(getProjectId());
        configTemplate.setTemplateName(getTemplateName());
        configTemplate.setTenantName(getTenantName());

        return configTemplate;
    }

}
