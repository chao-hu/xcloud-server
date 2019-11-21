/**
 *
 */
package  com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: OperatorDTO
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author lnn
 * @date 2019年11月13日
 *
 */
@Data
@ApiModel(value = "操作请求模型")
public class OperatorDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "操作,停止：stop，启动：start，新增实例:expand", required = true, example = "stop", dataType = "String")
    @NotBlank(message = "操作不能为空")
    private String operator;
}
