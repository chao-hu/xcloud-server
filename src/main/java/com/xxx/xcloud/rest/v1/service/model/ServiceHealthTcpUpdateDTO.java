package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceHealthTcpUpdateDTO
 * @Description: 服务健康TCP模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "服务健康TCP模版")
public class ServiceHealthTcpUpdateDTO {

    @ApiModelProperty(value = "检查端口", required = true, example = "", dataType = "int")
    private Integer port;

}
