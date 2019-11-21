package com.xxx.xcloud.module.component.model.postgresql;

public class PostgresqlInstance {

    private String name;
    private String exterHost;
    private int exterPort;
    private String interHost;
    private int interPort;
    private String role;
    private String instancePhase;
    private String svcName;
    private String nodeName;
    private String lvName;
    private String opt;
    private String needRestart;
    private String configName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExterHost() {
        return exterHost;
    }

    public void setExterHost(String exterHost) {
        this.exterHost = exterHost;
    }

    public int getExterPort() {
        return exterPort;
    }

    public void setExterPort(int exterPort) {
        this.exterPort = exterPort;
    }

    public String getInterHost() {
        return interHost;
    }

    public void setInterHost(String interHost) {
        this.interHost = interHost;
    }

    public int getInterPort() {
        return interPort;
    }

    public void setInterPort(int interPort) {
        this.interPort = interPort;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getInstancePhase() {
        return instancePhase;
    }

    public void setInstancePhase(String instancePhase) {
        this.instancePhase = instancePhase;
    }

    public String getSvcName() {
        return svcName;
    }

    public void setSvcName(String svcName) {
        this.svcName = svcName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getLvName() {
        return lvName;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public String getNeedRestart() {
        return needRestart;
    }

    public void setNeedRestart(String needRestart) {
        this.needRestart = needRestart;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

}
