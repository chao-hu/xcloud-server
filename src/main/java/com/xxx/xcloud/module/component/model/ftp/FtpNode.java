package com.xxx.xcloud.module.component.model.ftp;

import java.util.Map;

/**
 * @ClassName: FtpNode
 * @Description: FtpNode
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpNode {

    private String id;
    /**
     * 实际上时HostIp
     */
    private String hostip;
    private String role;
    private String name;
    private Map<String, String> nodeport;
    private String svcname;
    private Map<String, String> configmapname;

    private String address;
    private String nodeName;
    private String status;
    private String restartaction;

    private String volumeid;
    private String operator;

    private Map<String, String> userconfig;

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

    public Map<String, String> getNodeport() {
        return nodeport;
    }

    public void setNodeport(Map<String, String> nodeport) {
        this.nodeport = nodeport;
    }

    public String getSvcname() {
        return svcname;
    }

    public void setSvcname(String svcname) {
        this.svcname = svcname;
    }

    public Map<String, String> getConfigmapname() {
        return configmapname;
    }

    public void setConfigmapname(Map<String, String> configmapname) {
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

    public Map<String, String> getUserconfig() {
        return userconfig;
    }

    public void setUserconfig(Map<String, String> userconfig) {
        this.userconfig = userconfig;
    }

}
