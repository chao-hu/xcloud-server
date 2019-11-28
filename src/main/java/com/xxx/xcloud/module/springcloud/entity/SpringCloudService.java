package com.xxx.xcloud.module.springcloud.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Spring Cloud 服务表
 *
 * @author ruzz
 * @date 2019年4月23日
 */
@Entity
@Table(name = "`BDOS_SPRINGCLOUD_SERVICE`", indexes = {
        @Index(name = "`idx_tenantname`", columnList = "`TENANT_NAME`") })
public class SpringCloudService implements Serializable {

    private static final long serialVersionUID = -7865176457067568717L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id; // 服务ID

    @Column(name = "`APP_TYPE`")
    private String appType; // 框架类型

    @Column(name = "`TENANT_NAME`")
    private String tenantName; // 命名空间

    @Column(name = "`VERSION`")
    private String version; // 部署服务版本

    @Column(name = "`SERVICE_NAME`")
    private String serviceName; // 服务名称 规则： ^[a-z][a-z0-9-]{4,62}[a-z0-9]$

    @Column(name = "`app_id`")
    private String appId; // 应用Id

    @Column(name = "`app_name`")
    private String appName; // 服务名称 规则： ^[a-z][a-z0-9-]{4,62}[a-z0-9]$

    @Column(name = "`NODENUM`")
    private int nodeNum; // 节点个数

    @Column(name = "`SERVICE_STATE`")
    private String serviceState; // 服务状态

    @Column(name = "`CPU`")
    private Double cpu; // 服务总的cpu

    @Column(name = "`MEMORY`")
    private Double memory; // 服务总的memory

    /*@Column(name = "`STORAGE`")
    private Double storage; // 服务总的存储
*/
    @Column(name = "`SERVICE_TYPE`")
    private String serviceType; // 服务模式

    @Column(name = "`NODE_NAMES`")
    private String nodeNames; // 服务模式

    @Column(name = "`PROJECT_ID`")
    private String projectId; // 项目id

    @Column(name = "`ORDER_ID`")
    private String orderId; // 订购id

    @Column(name = "`EXTENDED_FIELD`", length = 1000)
    private String extendedField; // 扩展字段

    @Column(name = "`CONFIG`", length = 1000)
    private String config; // 服务配置

    @Column(name = "`EXTERNAL_URL`", length = 1000)
    private String externalUrl; // 外部使用地址

    @Column(name = "`INTERIOR_URL`", length = 1000)
    private String interiorUrl; // 内部使用地址

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime; // 创建时间

    @Column(name = "`CREATED_BY`")
    private String createdBy; // 创建人

    @Column(name = "`CEPH_FILE_ID`")
    private String cephfileId; // 文件存储Id

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
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

    /*public Double getStorage() {
        return storage;
    }

    public void setStorage(Double storage) {
        this.storage = storage;
    }*/

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getInteriorUrl() {
        return interiorUrl;
    }

    public void setInteriorUrl(String interiorUrl) {
        this.interiorUrl = interiorUrl;
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

    public String getNodeNames() {
        return nodeNames;
    }

    public void setNodeNames(String nodeNames) {
        this.nodeNames = nodeNames;
    }

    public String getCephfileId() {
        return cephfileId;
    }

    public void setCephfileId(String cephfileId) {
        this.cephfileId = cephfileId;
    }

}
