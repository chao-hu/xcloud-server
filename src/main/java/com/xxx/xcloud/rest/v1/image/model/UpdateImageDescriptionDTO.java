package com.xxx.xcloud.rest.v1.image.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 镜像传输对象
 *
 * @author xjp
 * @Description: 更新镜像描述
 * @date: 2019年11月1日
 */
@ApiModel(value = "更新镜像描述模型")
@Data
public class UpdateImageDescriptionDTO {

    @ApiModelProperty(value = "描述", required = true, example = "hhh", dataType = "String")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
