/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceExecContainerDTO
 * @Description: 容器内部执行命令模型
 * @author zyh
 * @date 2019年10月29日
 *
 */
@Data
@ApiModel(value = "容器内部执行命令模型")
public class ServiceExecContainerDTO {

    @ApiModelProperty(value = "tenantName", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    @NotBlank(message = "租户名称为空")
    private String tenantName;

    @ApiModelProperty(value = "podName", required = true, example = "test-68999d8c7f-tkxtw", dataType = "String")
    @NotBlank(message = "pod名称为空")
    private String podName;

    @ApiModelProperty(value = "command", required = true, example = "ls", dataType = "String")
    @NotBlank(message = "执行命令为空")
    private String command;

    @ApiModelProperty(value = "appType", required = false, example = "", dataType = "String")
    private String appType;

}
