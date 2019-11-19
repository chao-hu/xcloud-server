package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceCephRbdAddDTO
 * @Description: 新增ceph块存储模版
 * @author zyh
 * @date 2019年11月14日
 *
 */
@Data
@ApiModel(value = "新增ceph块存储模版")
public class ServiceCephRbdAddDTO {

    @ApiModelProperty(value = "块存储的id", required = true, example = "", dataType = "String")
    @NotBlank(message = "关联块存储id为空")
    private String cephRbdId;

    @ApiModelProperty(value = "挂载路径", required = true, example = "/test", dataType = "String")
    @NotBlank(message = "挂载路径为空")
    private String mountPath;

}
