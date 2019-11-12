package com.xxx.xcloud.module.cronjob.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 定时任务实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@Entity
@Table(name = "`bdos_cronjob`", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "`NAME`", "`TENANT_NAME`" }) })
public class Cronjob implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3248636678904586160L;

    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 定时任务名称
     */
    @Column(name = "`NAME`")
    private String name;

    /**
     * 租户名称
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * cpu大小(核)
     */
    @Column(name = "`CPU`")
    private Double cpu;

    /**
     * 内存大小(GB)
     */
    @Column(name = "`MEMORY`")
    private Double memory;

    /**
     * 镜像版本id
     */
    @Column(name = "`IMAGE_VERSION_ID`")
    private String imageVerisonId;

    /**
     * 任务状态
     */
    @Column(name = "`STATUS`")
    private Byte status;

    /**
     * 自定义启动命令
     */
    @Column(name = "`CMD`")
    private String cmd;

    /**
     * 定时计划--cron表达式
     */
    @Column(name = "`SCHEDULE`")
    private String schedule;

    /**
     * 定时计划--中文描述
     */
    @Column(name = "`SCHEDULE_CH`")
    private String scheduleCh;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`UPDATE_TIME`")
    private Date updateTime;

    /**
     * 所属项目
     */
    @Column(name = "`PROJECT_ID`")
    private String projectId;

    /**
     * 冗余字段
     */
    @Column(name = "`CREATED_BY`")
    private String createdBy;

}
