/**
 *
 */
package com.xxx.xcloud.rest.v1.env.dto;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.env.entity.EnvTemplate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 环境变量创建请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "环境变量创建请求模型")
public class EnvDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "模版名称", required = true, example = "testevn", dataType = "String")
    @NotBlank(message = "模版名称不能为空")
    private String templateName;

    @ApiModelProperty(value = "环境变量", required = false, example = "{'key1':'value1'}", dataType = "Map")
    @NotEmpty(message = "环境变量不能为空")
    private Map<String, Object> envData;

    @ApiModelProperty(value = "项目ID", required = false, example = "testproject", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testuser", dataType = "String")
    private String createdBy;

    
    /**
     * 构造EnvTemplate对象
     * @Title: getEnvTemplate
     * @Description: 构造EnvTemplate对象
     * @return EnvTemplate 
     * @throws
     */
    public EnvTemplate getEnvTemplate(){
        EnvTemplate envTemplate = new EnvTemplate();
        envTemplate.setCreatedBy(getCreatedBy());
        envTemplate.setEnvData(JSON.toJSONString(getEnvData()));
        envTemplate.setProjectId(getProjectId());
        envTemplate.setTemplateName(getTemplateName());
        envTemplate.setTenantName(getTenantName());
        
        return envTemplate;
    }
}
