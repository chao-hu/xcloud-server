/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 块存储快照回滚请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "块存储快照回滚请求模型")
public class CephRbdSnapRollbackDTO {

    @ApiModelProperty(value = "快照ID", required = true, example = "", dataType = "String")
    @NotBlank(message = "快照ID不能为空")
    private String snapshotId;

    @ApiModelProperty(value = "操作", required = true, example = "rollback", dataType = "String")
    @NotBlank(message = "操作不能为空")
    private String operation;

}
