package com.xxx.xcloud.rest.v1.ci.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建构建任务参数
 * 
 * @author HBL
 */
@ApiModel(value = "修改构建任务模型")
@Data
public class UpdateCiDTO {

    @ApiModelProperty(value = "操作，start|stop|modify|disable|enable", required = true, example = "modify", dataType = "String")
    private String operator;

    @ApiModelProperty(value = "构建任务描述", required = false, example = "", dataType = "String")
    private String ciDescription;

    /**
     * 构建计划
     */
    @ApiModelProperty(value = "执行计划表达式", required = false, example = "", dataType = "String")
    private String cron;

    @ApiModelProperty(value = "执行计划描述", required = false, example = "", dataType = "String")
    private String cronDescription;
    
    @ApiModelProperty(value = "构建任务名", required = true, example = "", dataType = "String")
    private String ciName;
    
    @ApiModelProperty(value = "镜像名", required = true, example = "", dataType = "String")
    private String imageName;
    
    @ApiModelProperty(value = "版本名", required = true, example = "", dataType = "String")
    private String imageVersion;

    @ApiModelProperty(value = "镜像版本生成策略", required = true, example = "1,2", dataType = "String")
    private String imageVersionGenerationStrategy;

    //todo @Env
    @ApiModelProperty(value = "环境变量", required = false, example = "", dataType = "String")
    private String envVariables;

    /**
     * 文件
     */
    @ApiModelProperty(value = "dockerfile构建上传文件的位置", required = false, example = "", dataType = "String")
    private String filePath;

    @ApiModelProperty(value = "dockerfile文件内容", required = false, example = "", dataType = "String")
    private String dockerfileContent;

    @ApiModelProperty(value = "资源名(dockerfile构建上传文件的名字, 多个文件逗号分隔)", required = false, example = "", dataType = "String")
    private String fileName;

    @ApiModelProperty(value = "dockerfile类型，0：在线编辑；1：引用代码库", required = false, example = "", dataType = "Byte")
    private Byte dockerfileWriteType;

    @ApiModelProperty(value = "程序包名", required = false, example = "", dataType = "String")
    private String packageName;

    @ApiModelProperty(value = "上传文件大小", required = false, example = "", dataType = "String")
    private String uploadfileSize;

    /**
     * 代码构建
     */
    @ApiModelProperty(value = "代码构建语言类型", required = false, example = "java|go|python", dataType = "String")
    private String lang;

    @ApiModelProperty(value = "代码地址", required = false, example = "", dataType = "String")
    private String codeUrl;

    @ApiModelProperty(value = "代码项目名称", required = false, example = "", dataType = "String")
    private String reposName;

    @ApiModelProperty(value = "代码项目Id", required = false, example = "", dataType = "Integer")
    private Integer reposId;

    @ApiModelProperty(value = "分支", required = false, example = "", dataType = "String")
    private String codeBranch;

    @ApiModelProperty(value = "分支或标签", required = true, example = "0：分支；1：标签", dataType = "byte")
    private Byte branchOrTag;

    @ApiModelProperty(value = "代码库类型", required = false, example = "0:none 1:git 2:svn", dataType = "byte")
    private Byte codeControlType;

    @ApiModelProperty(value = "认证方式ID", required = false, example = "", dataType = "String")
    private String ciCodeCredentialsId;

    @ApiModelProperty(value = "代码构建dockerfile路径", required = false, example = "", dataType = "String")
    private String dockerfilePath;

    @ApiModelProperty(value = "代码构建编译命令", required = false, example = "", dataType = "String")
    private String compile;

    @ApiModelProperty(value = "构建者", required = false, example = "", dataType = "String")
    private String createdBy;

    /**
     * 模版
     */
    @ApiModelProperty(value = "具体模版ID", required = false, example = "", dataType = "String")
    private String dockerfileTemplateId;

    @ApiModelProperty(value = "模版类型ID", required = false, example = "", dataType = "String")
    private String dockerfileTypeId;

    @ApiModelProperty(value = "是否高级模式", required = true, example = "", dataType = "boolean")
    private Boolean advanced;

    @ApiModelProperty(value = "是否使用hook", required = false, example = "", dataType = "boolean")
    private Boolean hookUsed;
}
