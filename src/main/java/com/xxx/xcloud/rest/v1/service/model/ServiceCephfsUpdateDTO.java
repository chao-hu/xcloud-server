package com.xxx.xcloud.rest.v1.service.model;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ServiceCephfsUpdateDTO
 * @Description: 修改ceph文件存储模版
 * @author zyh
 * @date 2019年10月26日
 *
 */
@Data
@ApiModel(value = "修改ceph文件存储模版")
public class ServiceCephfsUpdateDTO {

    @ApiModelProperty(value = "service和文件存储关联ID", required = true, example = "id", dataType = "String")
    private String id;

    @ApiModelProperty(value = "关联存储卷id", required = true, example = "cephFileId", dataType = "String")
    @NotBlank(message = "关联存储卷id不能为空")
    private String cephFileId;

    @ApiModelProperty(value = "挂载路径", required = true, example = "/test", dataType = "String")
    @NotBlank(message = "挂载路径不能为空")
    private String mountPath;

}
