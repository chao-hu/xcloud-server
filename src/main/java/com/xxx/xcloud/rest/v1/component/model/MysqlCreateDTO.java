package  com.xxx.xcloud.rest.v1.component.model;

import java.util.Map;

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
 * @ClassName: MysqlCreateDTO
 * @Description: mysql创建DTO
 * @author lnn
 * @date 2019年11月13日
 *
 */
@Data
@ApiModel(value = "mysql创建请求模型")
public class MysqlCreateDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称不符合规范")
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    @ApiModelProperty(value = "服务名称", required = true, example = "testmysql", dataType = "String")
    @Pattern(regexp = Global.CHECK_SERVICE_NAME, message = "服务名称不符合规范")
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    @ApiModelProperty(value = "组件类型", required = true, example = "mysql", dataType = "String")
    @NotBlank(message = "组件类型不能为空")
    private String appType;
    
    @ApiModelProperty(value = "CPU", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "CPU不符合规范")
    @NotNull(message = "CPU不能为空")
    private Double cpu;
    
    @ApiModelProperty(value = "内存", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "内存不符合规范")
    @NotNull(message = "内存不能为空")
    private Double memory;
    
    @ApiModelProperty(value = "存储", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CAPACITY, message = "存储不符合规范")
    @NotNull(message = "存储不能为空")
    private Double capacity;

    @ApiModelProperty(value = "所属项目ID", required = true, example = "projectId", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "类型", required = true, example = "MS", dataType = "String")
    @NotBlank(message = "类型不能为空")
    private String type;

    @ApiModelProperty(value = "版本", required = true, example = "5.7", dataType = "String")
    @NotBlank(message = "版本不能为空")
    private String version;

    @ApiModelProperty(value = "密码", required = true, example = "123456", dataType = "String")
    @Pattern(regexp = CommonConst.CHECK_USER_PASSSWORD, message = "密码不符合规范")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "实例个数", required = true, example = "2", dataType = "int")
    @Pattern(regexp = CommonConst.CHECK_CLUSTER_REPLICAS, message = "实例个数不符合规范")
    @NotNull(message = "实例个数不能为空")
    private Integer replicas;

    @ApiModelProperty(value = "配置", required = false, example = "{'mysqld':{'ordinary':{'max_connections':'1024'}}}", dataType = "JSONObject")
    private JSONObject configuration;

    @ApiModelProperty(value = "订购ID", required = true, example = "", dataType = "String")
    private String orderId;

    @ApiModelProperty(value = "是否开启健康检查", required = false, example = "false", dataType = "String")
    @NotBlank(message = "是否开启健康检查不能为空")
    private String healthCheck;
    
    @ApiModelProperty(value = "创建人", required = false, example = "Tom", dataType = "String")
    private String createdBy;
    
    @ApiModelProperty(value = "健康检查参数", required = false, example = "{'LivenessDelayTimeout':'100','LivenessFailureThreshold':'100','ReadinessDelayTimeout':'100','ReadinessFailureThreshold':'100'}", dataType = "Map")
    private Map<String, String> healthCheckConfiguration;
    
}
