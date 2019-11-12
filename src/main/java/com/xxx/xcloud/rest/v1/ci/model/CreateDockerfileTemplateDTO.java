package com.xxx.xcloud.rest.v1.ci.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 创建Dockerfile模型参数
 * 
 * @author HBL
 */
@ApiModel(value = "Dockerfile模型")
@Data
public class CreateDockerfileTemplateDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "Dockerfile文件内容", required = true, example = "", dataType = "String")
    private String dockerfileContent;

    @ApiModelProperty(value = "Dockerfile文件名称", required = true, example = "", dataType = "String")
    private String dockerfileName;

    @ApiModelProperty(value = "项目Id", required = false, example = "", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建者", required = false, example = "", dataType = "String")
    private String createdBy;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getDockerfileContent() {
        return dockerfileContent;
    }

    public void setDockerfileContent(String dockerfileContent) {
        this.dockerfileContent = dockerfileContent;
    }

    public String getDockerfileName() {
        return dockerfileName;
    }

    public void setDockerfileName(String dockerfileName) {
        this.dockerfileName = dockerfileName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
