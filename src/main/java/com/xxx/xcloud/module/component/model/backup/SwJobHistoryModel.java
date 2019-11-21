package com.xxx.xcloud.module.component.model.backup;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @ClassName: SwJobHistoryModel
 * @Description: 备份历史记录
 * @author lnn
 * @date 2019年11月15日
 *
 */
@ApiModel(value = "备份历史记录模型")
public class SwJobHistoryModel {

    /**
     * @Fields: 集群ID
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
     * @Fields: 任务类型----增量备份、全量备份、恢复
     */
    @ApiModelProperty(value = "任务类型,全量备份 1、 增加备份 2、 恢复任务 3", required = true, example = "2", dataType = "int")
    private Integer jobType;

    /**
     * @Fields: 任务id
     */
    @ApiModelProperty(value = "任务id", required = true, example = "", dataType = "String")
    private String jobId;

    /**
     * @Fields: 任务名称
     */
    @ApiModelProperty(value = "任务名称", required = true, example = "", dataType = "String")
    private String jobName;

    /**
     * @Fields: 全量时---无值; 增量时---上一次备份的历史任务id 恢复时---指定的备份历史任务id.
     */
    @ApiModelProperty(value = "全量时-无值; 增量时-上一次备份的历史任务id,恢复时-指定的备份历史任务id", required = true, example = "", dataType = "String")
    private String lastJobHistoryId;

    /**
     * @Fields: 备份时的相对文件路径；恢复时指定的备份文件路径
     */
    @ApiModelProperty(value = "备份时的相对文件路径；恢复时指定的备份文件路径", required = false, example = "", dataType = "String")
    private String relativePath;

    /**
     * @Fields: 工作状态 1 未开始 2 成功完成 -1 失败完成
     */
    @ApiModelProperty(value = "工作状态 1 未开始 2 成功完成 -1 失败完成", required = false, example = "", dataType = "int")
    private Integer status;

    /**
     * @Fields: 开始执行时间
     */
    @ApiModelProperty(value = "开始执行时间", required = false, example = "", dataType = "Date")
    private Date starttime;

    /**
     * @Fields: 结束时间
     */
    @ApiModelProperty(value = "结束时间", required = false, example = "", dataType = "Date")
    private Date endtime;

    /**
     * @Fields: 执行时间 （秒）
     */
    @ApiModelProperty(value = "执行时间 （秒）", required = false, example = "", dataType = "int")
    private Integer costtime;

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

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getLastJobHistoryId() {
        return lastJobHistoryId;
    }

    public void setLastJobHistoryId(String lastJobHistoryId) {
        this.lastJobHistoryId = lastJobHistoryId;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getCosttime() {
        return costtime;
    }

    public void setCosttime(Integer costtime) {
        this.costtime = costtime;
    }

}
