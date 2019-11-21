package com.xxx.xcloud.module.component.model.zookeeper;

import java.util.List;

public class ZkInstance {

    private String name;
    private int myid;
    private String exterHost;
    private int exterport;
    private String interHost;
    private int interPort;
    private String role;
    private String instancePhase;
    private List<String> svcList;
    private String nodeName;
    private String lvName;
    private String opt;
    private String needRestart;

    public String getExterHost() {
        return exterHost;
    }

    public void setExterHost(String exterHost) {
        this.exterHost = exterHost;
    }

    public int getExterport() {
        return exterport;
    }

    public void setExterport(int exterport) {
        this.exterport = exterport;
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

    public int getInterPort() {
        return interPort;
    }

    public void setInterPort(int interPort) {
        this.interPort = interPort;
    }

    public String getLvName() {
        return lvName;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public int getMyid() {
        return myid;
    }

    public void setMyid(int myid) {
        this.myid = myid;
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

    public List<String> getSvcList() {
        return svcList;
    }

    public void setSvcList(List<String> svcList) {
        this.svcList = svcList;
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

}
