package com.xxx.xcloud.module.component.entity;

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
 * @ClassName: StatefulNode
 * @Description: 组件节点表
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_NODE`", indexes = { @Index(name = "`idx_serviceId`", columnList = "`SERVICE_ID`") })
public class StatefulNode implements Serializable {

    private static final long serialVersionUID = 3602272807569295712L;

    /**
     * @Fields: 节点ID
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * @Fields: 集群名称
     */
    @Column(name = "`SERVICE_NAME`")
    private String serviceName;

    /**
     * @Fields: 集群id
     */
    @Column(name = "`SERVICE_ID`")
    private String serviceId;

    /**
     * @Fields: 节点名称
     */
    @Column(name = "`NODE_NAME`")
    private String nodeName;

    /**
     * @Fields: lvm名称
     */
    @Column(name = "`LVM_NAME`")
    private String lvmName;

    /**
     * @Fields: 框架类型
     */
    @Column(name = "`APP_TYPE`")
    private String appType;

    /**
     * @Fields: 集群状态
     */
    @Column(name = "`ROLE`")
    private String role;

    /**
     * @Fields: ip
     */
    @Column(name = "`IP`")
    private String ip;

    /**
     * @Fields: port
     */
    @Column(name = "`PORT`")
    private int port;

    /**
     * @Fields: cpu
     */
    @Column(name = "`CPU`")
    private Double cpu;

    /**
     * @Fields: memory
     */
    @Column(name = "`MEMORY`")
    private Double memory;

    /**
     * @Fields: 存储
     */
    @Column(name = "`STORAGE`")
    private Double storage; 

    /**
     * @Fields: nodeState
     */
    @Column(name = "`NODE_STATE`")
    private String nodeState;

    /**
     * @Fields: createTime
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime; 

    /**
     * @Fields: 扩展字段
     */
    @Column(name = "`EXTENDED_FIELD`")
    private String extendedField; 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getLvmName() {
        return lvmName;
    }

    public void setLvmName(String lvmName) {
        this.lvmName = lvmName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public String getNodeState() {
        return nodeState;
    }

    public void setNodeState(String nodeState) {
        this.nodeState = nodeState;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getExtendedField() {
        return extendedField;
    }

    public void setExtendedField(String extendedField) {
        this.extendedField = extendedField;
    }

    @Override
    public String toString() {
        return "StatefulNode [id=" + id + ", serviceName=" + serviceName + ", serviceId=" + serviceId + ", nodeName="
                + nodeName + ", lvmName=" + lvmName + ", appType=" + appType + ", role=" + role + ", ip=" + ip
                + ", port=" + port + ", cpu=" + cpu + ", memory=" + memory + ", storage=" + storage + ", nodeState="
                + nodeState + ", createTime=" + createTime + ", extendedField=" + extendedField + "]";
    }

}
