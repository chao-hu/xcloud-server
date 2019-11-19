/**
 *
 */
package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ruzz
 *
 */
@ApiModel(value = "创建服务请求模型")
@Data
public class ServiceDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范")
    private String tenantName;

    @ApiModelProperty(value = "CPU", required = true, example = "1", dataType = "Double")
    private Double cpu;

    @ApiModelProperty(value = "镜像版本id", required = true, example = "1", dataType = "String")
    @NotBlank(message = "镜像版本ID不能为空")
    private String imageVersionId;

    @ApiModelProperty(value = "实例个数", required = true, example = "1", dataType = "int")
    private Integer instance;

    @ApiModelProperty(value = "内存", required = true, example = "1", dataType = "Double")
    private Double memory;

    @ApiModelProperty(value = "GPU", required = false, example = "1", dataType = "Double")
    private Integer gpu;

    @ApiModelProperty(value = "服务名称", required = true, example = "testservice", dataType = "String")
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    @ApiModelProperty(value = "会话黏连", required = false, example = "1", dataType = "String")
    private String sessionAffinity;

    @ApiModelProperty(value = "自定义启动命令", required = false, example = "bash", dataType = "String")
    private String cmd;

    @ApiModelProperty(value = "环境变量", required = false, dataType = "JSONObject")
    private JSONObject envData;

    @ApiModelProperty(value = "配置文件", required = false, dataType = "JSONObject")
    private List<ServiceConfigAddDTO> config;

    @ApiModelProperty(value = "描述", required = false, example = "", dataType = "String")
    private String description;

    @ApiModelProperty(value = "pod是否互斥 0:非互斥，1:互斥", required = false, example = "", dataType = "Boolean")
    private Boolean ispodmutex;

    @ApiModelProperty(value = "服务亲和类型 0:非亲和，1:亲和", required = false, example = "0", dataType = "Byte")
    private Byte serviceAffinityType;

    @ApiModelProperty(value = "亲和服务名称", required = false, example = "", dataType = "String")
    private String serviceAffinity;

    @ApiModelProperty(value = "节点亲和类型 0:非亲和，1:亲和", required = false, example = "0", dataType = "Byte")
    private Byte nodeAffinityType;

    @ApiModelProperty(value = "节点名称", required = false, example = "", dataType = "String")
    private String nodeAffinity;

    @ApiModelProperty(value = "文件存储", required = false, dataType = "JSONObject")
    private List<ServiceCephFileAddDTO> storageFile;

    @ApiModelProperty(value = "块存储", required = false, dataType = "JSONObject")
    private List<ServiceCephRbdAddDTO> storageRbd;

    @ApiModelProperty(value = "本地存储", required = false, dataType = "JSONObject")
    private List<ServiceLocalAddDTO> storageLocal;

    @ApiModelProperty(value = "项目ID", required = false, example = "testproject", dataType = "String")
    private String projectId;

    @ApiModelProperty(value = "创建人", required = false, example = "testuser", dataType = "String")
    private String createdBy;

    @ApiModelProperty(value = "容器端口和协议", required = true, dataType = "JSONObject")
    private JSONObject portAndProtocol;

    @ApiModelProperty(value = "健康检查", required = false)
    private List<ServiceHealthUpdateDTO> healthCheck;

    @ApiModelProperty(value = "List<HostAliases>对象", required = false)
    private List<ServiceHostAliasesModelDTO> hostAliases;

    @ApiModelProperty(value = "InitContainer对象", required = false)
    private ServiceInitContainerModelDTO initContainer;

    @ApiModelProperty(value = "是否使用APM监控 0:不使用，1:使用", required = true, example = "json", dataType = "Boolean")
    private Boolean isUsedApm;

    public void build(Service service) {
        setServiceName(service.getServiceName());
        setCpu(service.getCpu());
        setMemory(service.getMemory());
        setInstance(service.getInstance());
        setTenantName(service.getTenantName());
        setImageVersionId(service.getImageVersionId());
        setCmd(service.getCmd());
        setPortAndProtocol(JSON.parseObject(service.getPortAndProtocol()));
        setEnvData(JSON.parseObject(service.getEnv()));
        setIspodmutex(service.getIsPodMutex());
        setDescription(service.getDescription());
        setProjectId(service.getProjectId());
        setCreatedBy(service.getCreatedBy());
        setIsUsedApm(service.getIsUsedApm());
        setGpu(service.getGpu());
        if (StringUtils.isNotEmpty(service.getHostAliases())) {
            List<ServiceHostAliasesModelDTO> hostAliasJsonList = JSON.parseObject(service.getHostAliases(),
                    new TypeReference<List<ServiceHostAliasesModelDTO>>() {});
            setHostAliases(hostAliasJsonList);
        }
        if (StringUtils.isNotEmpty(service.getInitContainer())) {
            ServiceInitContainerModelDTO serviceInitContainerModel = JSON.parseObject(service.getInitContainer(),
                    new TypeReference<ServiceInitContainerModelDTO>() {});
            setInitContainer(serviceInitContainerModel);
        }
    }

}
