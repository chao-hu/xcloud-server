package  com.xxx.xcloud.rest.v1.component.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: StormOperatorDTO
 * @Description: storm操作DTO
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "Storm操作请求模型")
public class StormOperatorDTO extends OperatorDTO {

    @ApiModelProperty(value = "增加的节点个数", required = false, example = "1", dataType = "int")
    private Integer supervisorAddNum;
    
    @Override
    public String toString() {
        return super.toString(); 
    }

}
