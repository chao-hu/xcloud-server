package  com.xxx.xcloud.rest.v1.component.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: EsExpandDTO
 * @Description: es增加节点DTO
 * @author lnn
 * @date 2019年11月12日
 *
 */
@EqualsAndHashCode(callSuper=false)
@Data
@ApiModel(value = "Es操作请求模型")
public class EsOperatorDTO extends OperatorDTO {

    @ApiModelProperty(value = "增加的节点个数", required = false, example = "1", dataType = "int")
    private Integer addReplicas;
    
    @Override
    public String toString() {
        return super.toString(); 
    }

}
