package com.xxx.xcloud.module.springcloud.model;

/**
 * 
 * <p>
 * Description: spring clould service信息描述
 * </p>
 *
 * @author lnn
 * @date 2019年8月8日
 */
public class ServiceInfo {

    private String appId; // service所属APPid
    private String serviceId; // 单个pod所属的serviceId
    private String nodeName; // 容器实际名称
    private String exterUrl; // 外部url
    private String innerUrl; // 内部url
    private String nodeState; // 运行状态
    private String createTime; // 创建时间
    private String type; // 服务类型
    private Double cpu; // cpu
    private Double memory; // memory
    private Double storage; // storage

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    public String getExterUrl() {
        return exterUrl;
    }

    public void setExterUrl(String exterUrl) {
        this.exterUrl = exterUrl;
    }

    public String getInnerUrl() {
        return innerUrl;
    }

    public void setInnerUrl(String innerUrl) {
        this.innerUrl = innerUrl;
    }

    public String getNodeState() {
        return nodeState;
    }

    public void setNodeState(String nodeState) {
        this.nodeState = nodeState;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

}
