package com.xxx.xcloud.module.application.entity;

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
 * Description: 外部服务对象
 * </p>
 * 
 * @author wangkebiao
 * @date 2019年7月3日
 */
@Entity
@Table(name = "`EXTERNAL_SERVICE`", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "`SERVICE_NAME`", "`TENANT_NAME`" }) })
@Data
public class ExternalService implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7538204246928018879L;

    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 外部服务名称
     */
    @Column(name = "`SERVICE_NAME`")
    private String serviceName;

    /**
     * 外部服务IP地址
     */
    @Column(name = "`IP`")
    private String ip;

    /**
     * 外部服务端口
     */
    @Column(name = "`PORT`")
    private int port;

    /**
     * 租户名称
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * 外部服务描述
     */
    @Column(name = "`DESCRIPTION`")
    private String description;

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
     * 冗余字段-供前端使用
     */
    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Override
    public String toString() {
        return "ExternalService [id=" + id + ", serviceName=" + serviceName + ", ip=" + ip + ", port=" + port
                + ", tenantName=" + tenantName + ", description=" + description + ", createTime=" + createTime
                + ", updateTime=" + updateTime + ", projectId=" + projectId + ", createdBy=" + createdBy + "]";
    }

}
