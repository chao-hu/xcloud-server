package com.xxx.xcloud.rest.v1.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: AffinityUpdateDTO
 * @Description: 服务的亲和属性模版
 * @author zyh
 * @date 2019年11月11日
 *
 */
@Data
@ApiModel(value = "服务的亲和属性模版")
public class AffinityUpdateDTO {

    @ApiModelProperty(value = "ID", required = true, example = "", dataType = "String")
    private String id;

    @ApiModelProperty(value = "服务亲和状态(0:不使用亲和行,1:亲和,2:反亲和)", required = true, example = "", dataType = "int")
    private Integer serviceAffinityType;

    @ApiModelProperty(value = "服务名称", required = true, example = "serviceName", dataType = "String")
    private String serviceAffinity;

    @ApiModelProperty(value = "节点亲和状态(0:不使用亲和行,1:亲和,2:反亲和)", required = true, example = "", dataType = "String")
    private Integer nodeAffinityType;

    @ApiModelProperty(value = "节点列表(多个以逗号分割)", required = true, example = "node1", dataType = "String")
    private String nodeAffinity;

}
