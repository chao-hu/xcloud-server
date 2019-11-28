package com.xxx.xcloud.module.springcloud.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Spring Cloud 应用表
 *
 * @author ruzz
 * @date 2019年4月23日
 */
@Entity
@Table(name = "`BDOS_SPRINGCLOUD_APPLICATION`", indexes = {
        @Index(name = "`idx_tenantname`", columnList = "`TENANT_NAME`") })
public class SpringCloudApplication implements Serializable {

    private static final long serialVersionUID = -7865176457067568717L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id; // 应用ID

    @Column(name = "`APP_NAME`")
    private String appName; // 应用名称 规则： ^[a-z][a-z0-9-]{4,62}[a-z0-9]$

    @Column(name = "`TENANT_NAME`")
    private String tenantName; // 命名空间

    @Column(name = "`STATE`")
    private String state; // 应用状态

    @Column(name = "`CPU`")
    private Double cpu; // 应用总的cpu

    @Column(name = "`MEMORY`")
    private Double memory; // 应用总的memory

    @Column(name = "`STORAGE`")
    private Double storage; // 应用总的存储

    @Column(name = "`PROJECT_ID`")
    private String projectId; // 项目id

    @Column(name = "`ORDER_ID`")
    private String orderId; // 订购id

    @Column(name = "`EXTENDED_FIELD`", length = 1000)
    private String extendedField; // 扩展字段

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime; // 创建时间

    @Column(name = "`CREATED_BY`")
    private String createdBy; // 创建人

    @Column(name = "`VERSION`")
    private String version; // 部署服务版本

    @Transient
    private String infoJson; // 不持久字段，用于返回不持久化的信息给页面\

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public Double getStorage() {
        return storage;
    }

    public void setStorage(Double storage) {
        this.storage = storage;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExtendedField() {
        return extendedField;
    }

    public void setExtendedField(String extendedField) {
        this.extendedField = extendedField;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInfoJson() {
        return infoJson;
    }

    public void setInfoJson(String infoJson) {
        this.infoJson = infoJson;
    }

}
