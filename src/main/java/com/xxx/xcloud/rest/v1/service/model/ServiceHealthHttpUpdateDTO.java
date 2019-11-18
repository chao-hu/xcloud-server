package com.xxx.xcloud.rest.v1.service.model;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceHealthHttpUpdateDTO
 * @Description: 服务健康Http模版
 * @author zyh
 * @date 2019年10月28日
 *
 */
@Data
@ApiModel(value = "服务健康Http模版")
public class ServiceHealthHttpUpdateDTO {

    @ApiModelProperty(value = "请求头", required = true, example = "", dataType = "String")
    private Map<String, String> httpHeade;

    @ApiModelProperty(value = "检查路径", required = true, example = "", dataType = "String")
    private String path;

    @ApiModelProperty(value = "查询端口", required = true, example = "", dataType = "int")
    private Integer port;

}
