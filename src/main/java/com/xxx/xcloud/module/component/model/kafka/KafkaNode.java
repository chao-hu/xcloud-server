package com.xxx.xcloud.module.component.model.kafka;

import java.util.Map;

/**
 * @author xujiangpeng
 * @date 2018/6/19
 */
public class KafkaNode {

    private String id;
    private String hostip;
    private String role;
    private String name;
    private int nodeport;
    private String svcname;
    private String configmapname;

    private String address;
    private String nodeName;
    private String status;
    private int downTime;
    private int count;
    private String restartaction;
    private String volumeid;

    private String operator;
    private int brokerid;
    private Map<String, String> nodeconfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostip() {
        return hostip;
    }

    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNodeport() {
        return nodeport;
    }

    public void setNodeport(int nodeport) {
        this.nodeport = nodeport;
    }

    public String getSvcname() {
        return svcname;
    }

    public void setSvcname(String svcname) {
        this.svcname = svcname;
    }

    public String getConfigmapname() {
        return configmapname;
    }

    public void setConfigmapname(String configmapname) {
        this.configmapname = configmapname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRestartaction() {
        return restartaction;
    }

    public void setRestartaction(String restartaction) {
        this.restartaction = restartaction;
    }

    public String getVolumeid() {
        return volumeid;
    }

    public void setVolumeid(String volumeid) {
        this.volumeid = volumeid;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getDownTime() {
        return downTime;
    }

    public void setDownTime(int downTime) {
        this.downTime = downTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBrokerid() {
        return brokerid;
    }

    public void setBrokerid(int brokerid) {
        this.brokerid = brokerid;
    }

    public Map<String, String> getNodeconfig() {
        return nodeconfig;
    }

    public void setNodeconfig(Map<String, String> nodeconfig) {
        this.nodeconfig = nodeconfig;
    }

}
