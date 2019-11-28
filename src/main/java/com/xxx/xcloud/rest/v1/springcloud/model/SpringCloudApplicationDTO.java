package com.xxx.xcloud.rest.v1.springcloud.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: SpringCloudApplicationDTO
 * @Description: SpringCloudApplicationDTO
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Data
@ApiModel(value = "SpringCloud应用请求模型")
public class SpringCloudApplicationDTO {

    @ApiModelProperty(value = "应用名称", required = true, example = "springcloudtest", dataType = "String")
    @Pattern(regexp = CommonConst.CHECK_CLUSTER_NAME, message = "应用名称不符合规范")
    @NotBlank(message = "应用名称不能为空")
    private String appName;

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "项目ID", required = true, example = "123455623", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "订购ID", required = true, example = "123455623", dataType = "String")
    private String orderId;

    // @ApiModelProperty(value = "服务类型及资源", required = true, example =
    // "{\"eureka\":{\"cpu\":2,\"memory\":4,\"nodeNum\":1},\"zuul\":{\"cpu\":2,\"memory\":4,\"nodeNum\":1},\"configbus\":{\"cpu\":2,\"memory\":3,\"storage\":1,\"nodeNum\":1}}",
    // dataType = "String")

    // @ApiModelProperty(value = "服务类型及资源", required = true, example =
    // "{\"eureka-cpu\":2,\"eureka-memory\":4,\"eureka-nodeNum\":1,\"configbus-cpu\":2,\"configbus-memory\":4,\"configbus-storage\":4,\"zuul-cpu\":2,\"zuul-memory\":4,}",
    // dataType = "String")
    // private String resource;

    @ApiModelProperty(value = "eureka-cpu", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "eureka-cpu不符合规范")
    @NotNull(message = "eureka-cpu不能为空")
    private Double eurekaCpu;

    @ApiModelProperty(value = "eureka-memory", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "eureka-memory不符合规范")
    @NotNull(message = "eureka-memory不能为空")
    private Double eurekaMemory;

    @ApiModelProperty(value = "eureka-nodeNum", required = true, example = "1", dataType = "int")
    @Pattern(regexp = CommonConst.CHECK_CLUSTER_REPLICAS, message = "eureka-nodeNum不符合规范")
    @NotNull(message = "eureka-nodeNum不能为空")
    private int eurekaNodeNum;

    @ApiModelProperty(value = "configbus-cpu", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "configbus-cpu不符合规范")
    @NotNull(message = "configbus-cpu不能为空")
    private Double configbusCpu;

    @ApiModelProperty(value = "configbus-memory", required = true, example = "4", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "configbus-memory不符合规范")
    @NotNull(message = "configbus-memory不能为空")
    private Double configbusMemory;

    @ApiModelProperty(value = "cephfileId-configbus", required = true, example = "402881f", dataType = "String")
    @NotBlank(message = "cephfileId不能为空")
    private String cephfileIdConfigbus;

    @ApiModelProperty(value = "zuul-cpu", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "zuul-cpu不符合规范")
    @NotNull(message = "zuul-cpu不能为空")
    private Double zuulCpu;

    @ApiModelProperty(value = "zuul-memory", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "zuul-memory不符合规范")
    @NotNull(message = "zuul-memory不能为空")
    private Double zuulMemory;

    @ApiModelProperty(value = "版本", required = true, example = "Edgware.SR4", dataType = "String")
    @NotBlank(message = "版本不能为空")
    private String version;
}
