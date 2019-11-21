package com.xxx.xcloud.module.component.model.prometheus;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrometheusInstances {

    private String name;
    private String role;
    private String lvName;
    private String svcName;
    private String nodeName;
    private String exterHost;
    private String interHost;
    private int exterHttpport;
    private int interHttpPort;
    @JsonProperty(value = "NeedRestart")
    private String needRestart;
    private String instancePhase;
    private String opt;

    public String getExterHost() {
        return exterHost;
    }

    public void setExterHost(String exterHost) {
        this.exterHost = exterHost;
    }

    public String getInstancePhase() {
        return instancePhase;
    }

    public void setInstancePhase(String instancePhase) {
        this.instancePhase = instancePhase;
    }

    public String getInterHost() {
        return interHost;
    }

    public void setInterHost(String interHost) {
        this.interHost = interHost;
    }

    public String getLvName() {
        return lvName;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNeedRestart() {
        return needRestart;
    }

    public void setNeedRestart(String needRestart) {
        this.needRestart = needRestart;
    }

    public String getSvcName() {
        return svcName;
    }

    public void setSvcName(String svcName) {
        this.svcName = svcName;
    }

    public int getExterHttpport() {
        return exterHttpport;
    }

    public void setExterHttpport(int exterHttpport) {
        this.exterHttpport = exterHttpport;
    }

    public int getInterHttpPort() {
        return interHttpPort;
    }

    public void setInterHttpPort(int interHttpPort) {
        this.interHttpPort = interHttpPort;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

}
