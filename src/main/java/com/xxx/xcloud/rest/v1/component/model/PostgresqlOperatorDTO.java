package  com.xxx.xcloud.rest.v1.component.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @ClassName: PostgresqlOperatorDTO
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author lnn
 * @date 2019年11月14日
 *
 */
@EqualsAndHashCode(callSuper=false)
@Data
@ApiModel(value = "Postgresql操作请求模型")
public class PostgresqlOperatorDTO extends OperatorDTO {

    @ApiModelProperty(value = "新增实例数(新增实例必传)", required = false, example = "1", dataType = "int")
    private Integer addNum;
    
    @Override
    public String toString() {
        return super.toString(); 
    }

}
