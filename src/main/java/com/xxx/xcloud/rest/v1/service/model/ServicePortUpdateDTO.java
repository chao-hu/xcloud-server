package com.xxx.xcloud.rest.v1.service.model;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServicePortUpdateDTO
 * @Description: 修改服务端口信息
 * @author zyh
 * @date 2019年10月25日
 *
 */
@ApiModel(value = "修改服务端口信息")
@Data
public class ServicePortUpdateDTO {

    @ApiModelProperty(value = "端口信息", required = true, dataType = "Map<String, String>")
    private Map<String, String> ports;

}
