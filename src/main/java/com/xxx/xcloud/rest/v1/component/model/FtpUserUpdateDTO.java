package com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: FtpUserUpdateDTO
 * @Description: ftp用户更新DTO
 * @author lnn
 * @date 2019年11月13日
 *
 */

@Data
@ApiModel(value = "Ftp修改用户操作请求模型")
public class FtpUserUpdateDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "权限", required = true, example = "", dataType = "String")
    @NotBlank(message = "权限不能为空")
    private String permission;

    @ApiModelProperty(value = "用户密码", required = true, example = "123456", dataType = "String")
    @Pattern(regexp = CommonConst.CHECK_USER_PASSSWORD, message = "用户密码不符合规范")
    @NotBlank(message = "用户密码不能为空")
    private String password;

    @ApiModelProperty(value = "状态", required = true, example = "", dataType = "String")
    @NotBlank(message = "状态不能为空")
    private String status;

}
