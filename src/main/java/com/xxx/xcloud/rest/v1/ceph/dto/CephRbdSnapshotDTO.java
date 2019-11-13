/**
 *
 */
package com.xxx.xcloud.rest.v1.ceph.dto;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 块存储快照策略请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@ApiModel(value = "块存储快照策略请求模型")
public class CephRbdSnapshotDTO {

    @ApiModelProperty(value = "时间（0点至23点，例：7点8点22点23点0点由字符串“7,8,22,23,0”表示）", required = true, example = "", dataType = "String")
    @NotEmpty(message = "具体时间信息不能为空")
    private String time;

    @ApiModelProperty(value = "周几字符串（周一至周日分别由0,1.....6代表，例：周三周四与周日由字符串\"2,3,6\"表示）", required = true, example = "", dataType = "String")
    @NotEmpty(message = "星期信息不能为空")
    private String week;

    @ApiModelProperty(value = "截止日期", required = true, example = "2045-01-20 00:00:00", dataType = "Date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    @ApiModelProperty(value = "运行状态,0：停止状态,1：运行状态", required = true, example = "1", dataType = "int")
    private Integer status;

}
