package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceBatchConfigUpdateDTO
 * @Description: 批量修改服务配置文件模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "批量修改服务配置文件模版")
public class ServiceBatchConfigUpdateDTO {

    @ApiModelProperty(value = "修改后配置文件", required = true, dataType = "List<ServiceConfigUpdateDTO>")
    private List<ServiceConfigUpdateDTO> config;

}
