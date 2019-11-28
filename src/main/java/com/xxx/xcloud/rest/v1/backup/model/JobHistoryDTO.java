package com.xxx.xcloud.rest.v1.backup.model;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: JobHistoryDTO
 * @Description: mysql备份历史记录表
 * @author lnn
 * @date 2019年11月21日
 *
 */
@Data
@ApiModel(value = "备份历史记录模型")
public class JobHistoryDTO {

    /**
     * 集群ID
     */
    @ApiModelProperty(value = "集群id", required = true, example = "", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String serviceId;

    /**
     * 节点id
     */
    @ApiModelProperty(value = "节点id", required = true, example = "", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String nodeId;

    /**
     * 节点名称
     */
    @ApiModelProperty(value = "节点名称", required = true, example = "", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String nodeName;
    
    /**
     * jobId
     */
    @ApiModelProperty(value = "jobId", required = true, example = "", dataType = "String")
    @NotBlank(message = "jobId不能为空")
    private String jobId;
    
    /**
     * 任务类型----增量备份、全量备份、恢复
     */
    @ApiModelProperty(value = "任务类型,全量备份 1、 增加备份 2、 恢复任务 3", required = true, example = "1", dataType = "int")
    @NotNull(message = "任务类型不能为空")
    private Integer jobType;

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称", required = true, example = "", dataType = "String")
    @NotBlank(message = "租户名称不能为空")
    private String jobName;

    /**
     * 全量时---无值; 增量时---上一次备份的历史任务id 恢复时---指定的备份历史任务id.
     */
    @ApiModelProperty(value = "全量时-无值; 增量时-上一次备份的历史任务id,恢复时-指定的备份历史任务id", required = true, example = "", dataType = "String")
    private String lastJobHistoryId;
    
    /**
     * 备份时的相对文件路径；恢复时指定的备份文件路径
     */
    @ApiModelProperty(value = "备份时的相对文件路径；恢复时指定的备份文件路径", required = false, example = "", dataType = "String")
    private String relativePath;

    /**
     * 工作状态 1 未开始 2 成功完成 -1 失败完成
     */
    @ApiModelProperty(value = "工作状态 1 未开始 2 成功完成 -1 失败完成", required = false, example = "", dataType = "int")
    private Integer status;

    /**
     * 开始执行时间
     */
    @ApiModelProperty(value = "开始执行时间", required = false, example = "", dataType = "Date")
    private Date starttime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间", required = false, example = "", dataType = "Date")
    private Date endtime;

    /**
     * 执行时间 （秒）
     */
    @ApiModelProperty(value = "执行时间 （秒）", required = false, example = "", dataType = "int")
    private Integer costtime;
    
    @Override
    public String toString() {
        return super.toString();
    }

}
