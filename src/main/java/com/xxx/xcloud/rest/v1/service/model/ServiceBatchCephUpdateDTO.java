package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceBatchCephUpdateDTO
 * @Description: 批量修改服务存储模版
 * @author zyh
 * @date 2019年10月26日
 *
 */
@ApiModel(value = "批量修改服务存储模版")
@Data
public class ServiceBatchCephUpdateDTO {

    @ApiModelProperty(value = "修改后存储卷", required = true, dataType = "List<ServiceAndCephFileDTO>")
    private List<ServiceAndCephFileDTO> mountCephs;

}
