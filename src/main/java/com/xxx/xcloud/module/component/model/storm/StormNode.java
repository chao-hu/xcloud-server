package com.xxx.xcloud.module.component.model.storm;

/**
 * @author xujiangpeng
 * @date 2018/7/29
 */
public class StormNode {

    private String id;
    private String name;
    private String address;
    private String configMapName;
    /**
     * 实际上是HostName
     */
    private String nodeName;
    private int nodePort;
    private int nodeInnerport;
    private int nodeExterport;
    private String operator;
    private String restartAction;
    private String role;
    private String status;
    private String svcName;
    private String volumeid;

    /**
     * 实际上是HostIp
     */
    private String hostip;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getConfigMapName() {
        return configMapName;
    }

    public void setConfigMapName(String configMapName) {
        this.configMapName = configMapName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }
    
    public int getNodeInnerport() {
        return nodeInnerport;
    }

    public void setNodeInnerport(int nodeInnerport) {
        this.nodeInnerport = nodeInnerport;
    }

    public int getNodeExterport() {
        return nodeExterport;
    }

    public void setNodeExterport(int nodeExterport) {
        this.nodeExterport = nodeExterport;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRestartAction() {
        return restartAction;
    }

    public void setRestartAction(String restartAction) {
        this.restartAction = restartAction;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSvcName() {
        return svcName;
    }

    public void setSvcName(String svcName) {
        this.svcName = svcName;
    }

    public String getVolumeid() {
        return volumeid;
    }

    public void setVolumeid(String volumeid) {
        this.volumeid = volumeid;
    }

    public String getHostip() {
        return hostip;
    }

    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

}
