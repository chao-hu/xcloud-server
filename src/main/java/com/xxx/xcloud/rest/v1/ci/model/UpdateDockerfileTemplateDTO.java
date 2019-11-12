package com.xxx.xcloud.rest.v1.ci.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 修改Dockerfile模型参数
 *
 * @author HBL
 */
@Data
@ApiModel(value = "Dockerfile修改模型")
public class UpdateDockerfileTemplateDTO {

    @ApiModelProperty(value = "Dockerfile文件内容", required = true, example = "", dataType = "String")
    private String dockerfileContent;

    public String getDockerfileContent() {
        return dockerfileContent;
    }

    public void setDockerfileContent(String dockerfileContent) {
        this.dockerfileContent = dockerfileContent;
    }

}
