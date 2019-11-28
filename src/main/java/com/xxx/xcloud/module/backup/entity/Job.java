package com.xxx.xcloud.module.backup.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ClassName: Job
 * @Description: mysql备份任务job表
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_SERVICE_OPERATIONS_JOB`", indexes = {
        @Index(name = "`idx_serviceId`", columnList = "`SERVICE_ID`") })
public class Job {

    /**
     * @Fields: 任务id
     */
    @Id
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @Column(name = "`ID`")
    private String id;

    /**
     * @Fields: 任务名称
     */
    @NotEmpty(message = "任务名称不能为空！")
    @Column(name = "`NAME`")
    private String name;

    /**
     * @Fields: 集群id
     */
    @NotEmpty(message = "集群ID不能为空！")
    @Column(name = "`SERVICE_ID`")
    private String serviceId;

    /**
     * @Fields: 节点id
     */
    @NotEmpty(message = "节点ID不能为空！")
    @Column(name = "`NODE_ID`")
    private String nodeId;
    
    /**
     * @Fields: 节点名称
     */
    @NotEmpty(message = "节点名称不能为空！")
    @Column(name = "`NODE_NAME`")
    private String nodeName;

    /**
     * @Fields: 服务类型
     */
    @NotEmpty(message = "服务类型不能为空！")
    @Column(name = "`APP_TYPE`")
    private String appType;

    /**
     * @Fields: 任务类型----增量备份、全量备份、恢复
     */
    @NotNull(message = "任务类型不能为空")
    @Column(name = "`JOB_TYPE`")
    private Integer jobType;

    /**
     * @Fields: 相关联的任务id 只有在增量时使用
     */
    @Column(name = "`RELATION_JOB_ID`")
    private String relationJobId;

    /**
     * @Fields: 调度类型 now 现在 once 一次性 day 每天 week 每周 month 每月 year 每年 repeat 重复任务
     */
    @Column(name = "`SCHEDULE_TYPE`")
    private Integer scheduleType;

    /**
     * @Fields: 年 4位数字 2018
     */
    @Column(name = "`YEAR`")
    private String year;

    /**
     * @Fields: 月 1-12
     */
    @Column(name = "`MONTH`")
    private String month; 

    /**
     * @Fields: 周几 1-7
     */
    @Column(name = "`WEEK`")
    private String week;

    /**
     * @Fields: 天 1-31
     */
    @Column(name = "`DAY`")
    private String day;

    /**
     * @Fields: 小时 00-24
     */
    @Column(name = "`HOUR`")
    private String hour; 

    /**
     * @Fields: 分钟 00-60
     */
    @Column(name = "`MINUTE`")
    private String minute;

    /**
     * @Fields: 秒 00-60
     */
    @Column(name = "`SECOND`")
    private String second;

    /**
     * @Fields: 定时表达式
     */
    @Column(name = "`CRON`")
    private String cron;

    /**
     * @Fields: 计划开始时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`START_TIME`")
    private Date starttime;

    /**
     * @Fields: 计划结束时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`END_TIME`")
    private Date endtime;

    /**
     * @Fields: 任务状态 1 默认 启用 -1 不启用
     */
    @Column(name = "`STATUS`")
    private Integer status;

    /**
     * @Fields: 任务创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createtime;

    /**
     * @Fields: 任务更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`UPDATE_TIME`")
    private Date updatetime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public String toString() {
        return "Job [id=" + id + ", name=" + name + ", serviceId=" + serviceId + ", nodeId=" + nodeId + ", nodeName="
                + nodeName + ", appType=" + appType + ", jobType=" + jobType + ", relationJobId=" + relationJobId
                + ", scheduleType=" + scheduleType + ", year=" + year + ", month=" + month + ", week=" + week + ", day="
                + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + ", cron=" + cron
                + ", starttime=" + starttime + ", endtime=" + endtime + ", status=" + status + ", createtime="
                + createtime + ", updatetime=" + updatetime + "]";
    }

}
