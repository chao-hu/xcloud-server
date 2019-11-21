/**
 *
 */
package  com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: UpdateResourceDTO
 * @Description: 修改资源DTO
 * @author lnn
 * @date 2019年11月14日
 *
 */

@Data
@ApiModel(value = "修改资源请求模型")
public class UpdateResourceDTO {                      

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "CPU", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "CPU不符合规范")
    @NotNull(message = "CPU不能为空")
    private Double cpu;
    
    @ApiModelProperty(value = "内存", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "内存不符合规范")
    @NotNull(message = "内存不能为空")
    private Double memory;
    
    @ApiModelProperty(value = "存储", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CAPACITY, message = "存储不符合规范")
    @NotNull(message = "存储不能为空")
    private Double capacity;

}
