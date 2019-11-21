package com.xxx.xcloud.module.component.model.backup;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @ClassName: SwJobModel
 * @Description: 备份任务模型
 * @author lnn
 * @date 2019年11月15日
 *
 */
@ApiModel(value = "备份任务模型")
public class SwJobModel {

    /**
     * @Fields: 任务名称
     */
    @ApiModelProperty(value = "租户名称", required = true, example = "testtenant", dataType = "String")
    private String tenantName;

    /**
     * @Fields: 任务名称
     */
    @ApiModelProperty(value = "任务名称", required = true, example = "", dataType = "String")
    private String name;

    /**
     * @Fields: 集群id
     */
    @ApiModelProperty(value = "集群id", required = true, example = "", dataType = "String")
    private String serviceId;

    /**
     * @Fields: 节点id
     */
    @ApiModelProperty(value = "节点id", required = true, example = "", dataType = "String")
    private String nodeId;

    /**
     * @Fields: 节点名称
     */
    @ApiModelProperty(value = "节点名称", required = true, example = "", dataType = "String")
    private String nodeName;

    /**
     * @Fields: 服务类型
     */
    @ApiModelProperty(value = "服务类型", required = true, example = "", dataType = "String")
    private String appType;

    /**
     * @Fields: 任务类型----增量备份、全量备份、恢复
     */
    @ApiModelProperty(value = "任务类型,全量备份 1、 增加备份 2、 恢复任务 3", required = true, example = "2", dataType = "String")
    private Integer jobType; 

    /**
     * @Fields: 相关联的任务id 只有在增量时使用
     */
    @ApiModelProperty(value = "相关联的任务id 只有在增量时使用", required = false, example = "", dataType = "String")
    private String relationJobId;

    /**
     * @Fields: 调度类型 now 现在 once 一次性 day 每天 week 每周 month 每月 year 每年 repeat 重复任务
     */
    @ApiModelProperty(value = "调度类型 now 现在 once 一次性 day 每天 week 每周 month 每月 year 每年 repeat 重复任务", required = false, example = "", dataType = "int")
    private Integer scheduleType; 

    /**
     * @Fields: 年 4位数字 2018
     */
    @ApiModelProperty(value = "年 4位数字", required = false, example = "2019", dataType = "String")
    private String year;

    /**
     * @Fields: 月 1-12
     */
    @ApiModelProperty(value = "月 1-12", required = false, example = "1", dataType = "String")
    private String month;

    /**
     * @Fields: 周几1-7
     */
    @ApiModelProperty(value = "周几 1-7", required = false, example = "1", dataType = "String")
    private String week; 

    /**
     * @Fields: 天 1-31
     */
    @ApiModelProperty(value = "天 1-31", required = false, example = "1", dataType = "String")
    private String day;

    /**
     * @Fields: 小时 00-24
     */
    @ApiModelProperty(value = "小时 00-24", required = false, example = "00", dataType = "String")
    private String hour;

    /**
     * @Fields: 分钟 00-60
     */
    @ApiModelProperty(value = "分钟 00-60", required = false, example = "00", dataType = "String")
    private String minute;

    /**
     * @Fields: 秒 00-60
     */
    @ApiModelProperty(value = "秒 00-60", required = false, example = "00", dataType = "String")
    private String second;

    /**
     * @Fields: 定时表达式
     */
    @ApiModelProperty(value = "定时表达式", required = false, example = "", dataType = "String")
    private String cron;

    /**
     * @Fields: 计划开始时间
     */
    @ApiModelProperty(value = "计划开始时间", required = false, example = "", dataType = "String")
    private Date starttime;

    /**
     * @Fields: 计划结束时间
     */
    @ApiModelProperty(value = "计划结束时间", required = false, example = "", dataType = "String")
    private Date endtime;

    /**
     * @Fields: 任务状态 1 默认 启用 -1 不启用
     */
    @ApiModelProperty(value = "任务状态 1 默认 启用 -1 不启用", required = false, example = "1", dataType = "int")
    private Integer status;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public String getRelationJobId() {
        return relationJobId;
    }

    public void setRelationJobId(String relationJobId) {
        this.relationJobId = relationJobId;
    }

    public Integer getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(Integer scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Job [name=" + name + ", serviceId=" + serviceId + ", nodeId=" + nodeId + ", nodeName=" + nodeName
                + ", appType=" + appType + ", jobType=" + jobType + ", relationJobId=" + relationJobId
                + ", scheduleType=" + scheduleType + ", year=" + year + ", month=" + month + ", week=" + week + ", day="
                + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + ", cron=" + cron
                + ", starttime=" + starttime + ", endtime=" + endtime + ", status=" + status + "]";
    }

}
