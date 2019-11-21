package com.xxx.xcloud.rest.v1.component.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: CodisOperatorDTO
 * @Description: codis扩缩容DTO
 * @author lnn
 * @date 2019年11月12日
 *
 */

@EqualsAndHashCode(callSuper=false)
@Data
@ApiModel(value = "Codis操作请求模型")
public class CodisOperatorDTO extends OperatorDTO {

    @ApiModelProperty(value = "新增后实例数(新增实例必传)", required = true, example = "1", dataType = "int")
    private Integer replicas;

    @ApiModelProperty(value = "新增后代理节点实例数(新增实例必传)", required = true, example = "1", dataType = "int")
    private Integer proxyReplicas;
    
    @Override
    public String toString() {
        return super.toString(); 
    }

}
