package  com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @ClassName: KafkaOperatorDTO
 * @Description: kafka操作DTO
 * @author lnn
 * @date 2019年11月13日
 *
 */
@Data
@ApiModel(value = "Kafka操作请求模型")
public class KafkaOperatorDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "操作,停止：stop，启动：start，新增实例:expand", required = true, example = "stop", dataType = "String")
    @NotBlank(message = "操作不能为空")
    private String operator;
    @ApiModelProperty(value = "增加的节点个数", required = false, example = "1", dataType = "int")
    private Integer addNum;
    
    @Override
    public String toString() {
        return super.toString(); 
    }

}
