package com.xxx.xcloud.rest.v1.image.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 镜像传输对象
 *
 * @author xjp
 * @Description: 更新镜像类型
 * @date: 2019年11月1日
 */
@ApiModel(value = "更新镜像模型")
@Data
public class UpdateImageTypeDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "镜像类型", example = "1公用2私有", dataType = "String")
    private Byte imageType;

    @ApiModelProperty(value = "镜像环境变量", example = "{a:a, b:b}", dataType = "String")
    private String envVariables;

}
