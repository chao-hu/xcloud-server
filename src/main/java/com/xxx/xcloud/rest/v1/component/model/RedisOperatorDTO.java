package  com.xxx.xcloud.rest.v1.component.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: RedisOperatorDTO
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "Redis操作请求模型")
public class RedisOperatorDTO extends OperatorDTO {

    @ApiModelProperty(value = "新增实例数(新增实例必传)", required = false, example = "1", dataType = "int")
    private Integer replicas;
    
    @Override
    public String toString() {
        return super.toString(); 
    }
}
