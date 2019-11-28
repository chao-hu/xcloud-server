package com.xxx.xcloud.module.backup.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.jdbc.core.RowMapper;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @ClassName: JobHistory
 * @Description: mysql备份历史记录表
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_SERVICE_OPERATIONS_JOBHISTORY`", indexes = {
        @Index(name = "`idx_serviceId`", columnList = "`SERVICE_ID`") })
public class JobHistory implements RowMapper<JobHistory> {

    
    /**
     * @Fields: 历史任务ID
     */
    @Id
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @Column(name = "`ID`")
    private String id;

    /**
     * @Fields: 集群ID
     */
    @Column(name = "`SERVICE_ID`")
    private String serviceId; 

    /**
     * @Fields: 节点ID
     */
    @Column(name = "`NODE_ID`")
    private String nodeId;

    /**
     * @Fields: 节点Name
     */
    @Column(name = "`NODE_NAME`")
    private String nodeName;

    /**
     * @Fields: 任务id
     */
    @Column(name = "`JOB_ID`")
    private String jobId;

    /**
     * @Fields: 任务名称
     */
    @Column(name = "`JOB_NAME`")
    private String jobName;

    /**
     * @Fields: 任务类型
     */
    @Column(name = "`JOB_TYPE`")
    private Integer jobType;

    /**
     * @Fields: 全量时---无值; 增量时---上一次备份的历史任务id 恢复时---指定的备份历史任务id.
     */
    @Column(name = "`LAST_JOB_HISTORY_ID`")
    private String lastJobHistoryId;

    /**
     * @Fields: 备份时的相对文件路径；恢复时指定的备份文件路径
     */
    @Column(name = "`RELATIVE_PATH`")
    private String relativePath;

    /**
     * @Fields: 工作状态 1 未开始 2 成功完成 -1 失败完成
     */
    @Column(name = "`STATUS`")
    private Integer status;

    /**
     * @Fields: 开始执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`START_TIME`")
    private Date starttime;

    /**
     * @Fields: 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`END_TIME`")
    private Date endtime;

    /**
     * @Fields: 执行时间 （秒）
     */
    @Column(name = "`COST_TIME`")
    private Integer costtime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
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

    @Override
    public String toString() {
        return "JobHistory [id=" + id + ", serviceId=" + serviceId + ", nodeId=" + nodeId + ", nodeName=" + nodeName
                + ", jobId=" + jobId + ", jobName=" + jobName + ", jobType=" + jobType + ", lastJobHistoryId="
                + lastJobHistoryId + ", relativePath=" + relativePath + ", status=" + status + ", starttime="
                + starttime + ", endtime=" + endtime + ", costtime=" + costtime + "]";
    }

    @Override
    public JobHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        JobHistory ret = new JobHistory();
        ret.setId(rs.getString("id"));
        ret.setServiceId(rs.getString("service_id"));
        ret.setNodeId(rs.getString("node_id"));
        ret.setNodeName(rs.getString("node_name"));
        ret.setJobId(rs.getString("job_id"));
        ret.setJobType(rs.getInt("job_type"));
        ret.setJobName(rs.getString("job_name"));
        ret.setLastJobHistoryId(rs.getString("last_job_history_id"));
        ret.setRelativePath(rs.getString("relative_path"));
        ret.setStatus(rs.getInt("status"));
        ret.setStarttime(rs.getDate("start_time"));
        ret.setEndtime(rs.getDate("end_time"));
        ret.setCosttime(rs.getInt("cost_time"));
        return ret;
    }
}
