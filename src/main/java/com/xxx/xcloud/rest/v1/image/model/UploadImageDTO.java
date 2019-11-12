package com.xxx.xcloud.rest.v1.image.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 镜像传输对象
 *
 * @author xjp
 * @Description: 上传镜像接口参数
 * @date: 2019年11月1日
 */
@ApiModel(value = "上传镜像模型")
@Data
public class UploadImageDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "test-tenant", dataType = "String")
    private String tenantName;

    /**
     * 1公用2私有
     */
    @ApiModelProperty(value = "镜像类型，1公用，2私有", required = true, example = "2", dataType = "byte")
    private Byte imageType;

    @ApiModelProperty(value = "镜像名称", required = true, example = "jdk8-tomcat8", dataType = "String")
    private String imageName;

    @ApiModelProperty(value = "描述", example = "测试镜像", dataType = "String")
    private String description;

    @ApiModelProperty(value = "镜像版本", required = true, example = "v1", dataType = "String")
    private String imageVersion;

    @ApiModelProperty(value = "镜像tar包路径", required = true, dataType = "String")
    private String imageFilePath;

    @ApiModelProperty(value = "创建人", dataType = "String")
    private String createdBy;

    @ApiModelProperty(value = "项目ID", dataType = "String")
    private String projectId;

    @Override
    public String toString() {
        return "UploadImageModel [tenantName=" + tenantName + ", imageType=" + imageType + ", imageName=" + imageName
                + ", description=" + description + ", imageVersion=" + imageVersion + ", imageFilePath=" + imageFilePath
                + ", createdBy=" + createdBy + ", projectId=" + projectId + "]";
    }

}
