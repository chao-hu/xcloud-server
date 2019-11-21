/**
 *
 */
package com.xxx.xcloud.rest.v1.component.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.xxx.xcloud.module.component.consts.CommonConst;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: CodisResourceDTO
 * @Description: codis修改资源DTO
 * @author lnn
 * @date 2019年11月12日
 *
 */

@Data
@ApiModel(value = "Codis修改资源请求模型")
public class CodisResourceDTO {

    @ApiModelProperty(value = "租户名称", required = true, example = "", dataType = "String")
    private String tenantName;
    
    @ApiModelProperty(value = "CPU", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "CPU不符合规范")
    @NotNull(message = "CPU不能为空")
    private Double cpu;
    
    @ApiModelProperty(value = "内存", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "memory不符合规范")
    @NotNull(message = "memory不能为空")
    private Double memory;

    @ApiModelProperty(value = "存储", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CAPACITY, message = "存储不符合规范")
    @NotNull(message = "存储不能为空")
    private Double capacity;

    @ApiModelProperty(value = "代理节点CPU", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_CPU, message = "代理节点CPU不符合规范")
    @NotNull(message = "代理节点CPU不能为空")
    private Double proxyCpu;

    @ApiModelProperty(value = "代理节点内存", required = true, example = "1", dataType = "Double")
    @Pattern(regexp = CommonConst.CHECK_RESOURCE_MEMORY, message = "代理节点内存不符合规范")
    @NotNull(message = "代理节点内存不能为空")
    private Double proxyMemory;
}
