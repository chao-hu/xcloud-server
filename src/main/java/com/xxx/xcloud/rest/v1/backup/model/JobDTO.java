package com.xxx.xcloud.rest.v1.backup.model;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: JobDTO
 * @Description: mysql备份任务job表
 * @author lnn
 * @date 2019年11月22日
 *
 */
@Data
@ApiModel(value = "备份任务模型")
public class JobDTO {

    
    /**
     * 租户名称
     */
    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    private String tenantName;

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称", required = true, example = "", dataType = "String")
    private String name;

    /**
     * 集群id
     */
    @ApiModelProperty(value = "集群id", required = true, example = "", dataType = "String")
    private String serviceId;

    /**
     * 节点id
     */
    @ApiModelProperty(value = "节点id", required = true, example = "", dataType = "String")
    private String nodeId;

    /**
     * 节点名称
     */
    @ApiModelProperty(value = "节点名称", required = true, example = "", dataType = "String")
    private String nodeName;

    /**
     * 服务类型
     */
    @ApiModelProperty(value = "服务类型", required = true, example = "mysql", dataType = "String")
    private String appType;

    /**
     * 任务类型----增量备份、全量备份、恢复
     */
    @ApiModelProperty(value = "任务类型,全量备份 1、 增加备份 2、 恢复任务 3", required = true, example = "2", dataType = "String")
    private Integer jobType;

    /**
     * 相关联的任务id 只有在增量时使用
     */
    @ApiModelProperty(value = "相关联的任务id 只有在增量时使用", required = false, example = "", dataType = "String")
    private String relationJobId;

    /**
     * 调度类型 now 现在 once 一次性 day 每天 week 每周 month 每月 year 每年 repeat 重复任务
     */
    @ApiModelProperty(value = "调度类型 now 现在 once 一次性 day 每天 week 每周 month 每月 year 每年 repeat 重复任务", required = false, example = "", dataType = "int")
    private Integer scheduleType; 

    /**
     * 年 4位数字 2018
     */
    @ApiModelProperty(value = "年 4位数字", required = false, example = "2019", dataType = "String")
    private String year;

    /**
     * 月 1-12
     */
    @ApiModelProperty(value = "月 1-12", required = false, example = "1", dataType = "String")
    private String month;

    /**
     * 周几 1-7
     */
    @ApiModelProperty(value = "周几 1-7", required = false, example = "1", dataType = "String")
    private String week;

    /**
     * 天 1-31
     */
    @ApiModelProperty(value = "天 1-31", required = false, example = "1", dataType = "String")
    private String day;

    /**
     * 小时 00-24
     */
    @ApiModelProperty(value = "小时 00-24", required = false, example = "00", dataType = "String")
    private String hour;

    /**
     * 分钟 00-60
     */
    @ApiModelProperty(value = "分钟 00-60", required = false, example = "00", dataType = "String")
    private String minute;

    /**
     * 秒 00-60
     */
    @ApiModelProperty(value = "秒 00-60", required = false, example = "00", dataType = "String")
    private String second;

    /**
     * 定时表达式
     */
    @ApiModelProperty(value = "定时表达式", required = false, example = "", dataType = "String")
    private String cron;

    /**
     * 计划开始时间
     */
    @ApiModelProperty(value = "计划开始时间", required = false, example = "", dataType = "String")
    private Date starttime;

    /**
     * 计划结束时间
     */
    @ApiModelProperty(value = "计划结束时间", required = false, example = "", dataType = "String")
    private Date endtime;

    /**
     * 任务状态 1 默认 启用 -1 不启用
     */
    @ApiModelProperty(value = "任务状态 1 默认 启用 -1 不启用", required = false, example = "1", dataType = "int")
    private Integer status;

    @Override
    public String toString() {
        return "Job [name=" + name + ", serviceId=" + serviceId + ", nodeId=" + nodeId + ", nodeName=" + nodeName
                + ", appType=" + appType + ", jobType=" + jobType + ", relationJobId=" + relationJobId
                + ", scheduleType=" + scheduleType + ", year=" + year + ", month=" + month + ", week=" + week + ", day="
                + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + ", cron=" + cron
                + ", starttime=" + starttime + ", endtime=" + endtime + ", status=" + status + "]";
    }

}
