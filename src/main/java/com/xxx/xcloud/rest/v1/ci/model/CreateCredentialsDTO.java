package com.xxx.xcloud.rest.v1.ci.model;

import io.swagger.annotations.*;
import lombok.*;

/**
 * 添加认证参数
 * 
 * @author HBL
 */
@ApiModel(value = "添加认证模型")
@Data
public class CreateCredentialsDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testTenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "代码托管工具", required = true, example = "1:gitlab|2:svn|3:github", dataType = "int")
    private Integer codeControlType;

    @ApiModelProperty(value = "用户名", required = true, example = "", dataType = "String")
    private String userName;

    @ApiModelProperty(value = "密码", required = false, example = " ", dataType = "String")
    private String password;

    @ApiModelProperty(value = "仓库地址", required = false, example = "", dataType = "String")
    private String registoryAddress;

    @ApiModelProperty(value = "accessToken,github认证", required = false, example = "", dataType = "String")
    private String accessToken;

    @ApiModelProperty(value = "是否公有,0:公有|1:私有", required = false, example = "1", dataType = "byte")
    private Byte publicOrPrivateFlag;

    @ApiModelProperty(value = "项目ID", required = false, example = "testproject", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testuser", dataType = "String")
    private String createdBy;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public int getCodeControlType() {
        return codeControlType;
    }

    public void setCodeControlType(int codeControlType) {
        this.codeControlType = codeControlType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegistoryAddress() {
        return registoryAddress;
    }

    public void setRegistoryAddress(String registoryAddress) {
        this.registoryAddress = registoryAddress;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Byte getPublicOrPrivateFlag() {
        return publicOrPrivateFlag;
    }

    public void setPublicOrPrivateFlag(Byte publicOrPrivateFlag) {
        this.publicOrPrivateFlag = publicOrPrivateFlag;
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
