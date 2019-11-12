package com.xxx.xcloud.rest.v1.ci.model;

import com.xxx.xcloud.module.ci.model.Repository;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "hook信息模型")
@Data
public class HookDTO {

    @ApiModelProperty(value = "objectKind", required = true, example = "push", dataType = "String")
    private String object_kind;

    @ApiModelProperty(value = "eventName", required = true, example = "push", dataType = "String")
    private String event_name;

    @ApiModelProperty(value = "ref", required = true, example = "refs/heads/master", dataType = "String")
    private String ref;

    @ApiModelProperty(value = "userName", required = true, example = "", dataType = "String")
    private String user_name;

    @ApiModelProperty(value = "repository", required = true, example = "")
    private Repository repository;

    @Override
    public String toString() {
        return "SwHookModel [object_kind=" + object_kind + ", event_name=" + event_name + ", ref=" + ref
                + ", user_name=" + user_name + ", repository=" + repository + "]";
    }

}
