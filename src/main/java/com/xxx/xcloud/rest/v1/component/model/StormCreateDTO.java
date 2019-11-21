package  com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.component.consts.CommonConst;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: StormCreateDTO
 * @Description: storm创建DTO
 * @author lnn
 * @date 2019年11月14日
 *
 */

@Data
@ApiModel(value = "Storm创建请求模型")
public class StormCreateDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "服务名称", required = true, example = "teststorm", dataType = "String")
    @Pattern(regexp = Global.CHECK_SERVICE_NAME, message = "服务名称不符合规范")
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    @ApiModelProperty(value = "组件类型", required = true, example = "storm", dataType = "String")
    @NotBlank(message = "组件类型不能为空")
    private String appType;

    @ApiModelProperty(value = "zk服务名称", required = true, example = "testzk", dataType = "String")
    @Pattern(regexp = Global.CHECK_SERVICE_NAME, message = "zk服务名称不符合规范")
    @NotBlank(message = "zk服务名称不能为空")
    private String zkServiceName;

    @ApiModelProperty(value = "nimbusCPU", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "nimbusCpu不符合规范")
    @NotNull(message = "CPU不能为空")
    private Double nimbusCpu;
    
    @ApiModelProperty(value = "nimbus内存", required = true, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "nimbusMemory不符合规范")
    @NotNull(message = "内存不能为空")
    private Double nimbusMemory;

    @ApiModelProperty(value = "supervisorCPU", required = false, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "supervisorCPU不符合规范")
    @NotNull(message = "supervisorCPU不能为空")
    private Double supervisorCpu;

    @ApiModelProperty(value = "supervisor个数", required = false, example = "2", dataType = "int")
    @Pattern(regexp = CommonConst.CHECK_CLUSTER_REPLICAS, message = "supervisor个数不符合规范")
    @NotNull(message = "supervisor个数不能为空")
    private Integer supervisorNum;

    @ApiModelProperty(value = "supervisor内存", required = false, example = "2", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "supervisor内存不符合规范")
    @NotNull(message = "supervisor内存不能为空")
    private Double supervisorMemory;

    @ApiModelProperty(value = "所属项目ID", required = true, example = "projectId", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "版本", required = true, example = "1.2.2", dataType = "String")
    @NotBlank(message = "版本不能为空")
    private String version;

    @ApiModelProperty(value = "配置", required = false, example = "", dataType = "JSONObject")
    private JSONObject configuration;

    @ApiModelProperty(value = "订购ID", required = true, example = "", dataType = "String")
    private String orderId;

    @ApiModelProperty(value = "创建人", required = false, example = "Tom", dataType = "String")
    private String createdBy;

}
