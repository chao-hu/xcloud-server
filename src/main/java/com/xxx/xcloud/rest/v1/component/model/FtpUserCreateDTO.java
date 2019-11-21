 /**
 *
 */
package  com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @ClassName: FtpUserCreateDTO
 * @Description: ftp用户创建DTO
 * @author lnn
 * @date 2019年11月12日
 *
 */

@Data
@ApiModel(value = "Ftp创建用户操作请求模型")                                                                                       
public class FtpUserCreateDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "服务ID", required = true, example = "servicename", dataType = "String")
    @NotBlank(message = "服务ID不能为空")
    private String serviceId;

    @ApiModelProperty(value = "用户名称", required = true, example = "testuser", dataType = "String")
    @Pattern(regexp = CommonConst.CHECK_USER_NAME, message = "用户名称不符合规范")
    @NotBlank(message = "用户名称不能为空")
    private String userName;

    @ApiModelProperty(value = "密码", required = true, example = "123456", dataType = "String")
    @Pattern(regexp = CommonConst.CHECK_USER_PASSSWORD, message = "密码不符合规范")
    @NotBlank(message = "密码不能为空")
    private String password;

}
