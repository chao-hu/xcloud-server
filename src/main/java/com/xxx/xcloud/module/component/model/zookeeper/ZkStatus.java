package com.xxx.xcloud.module.component.model.zookeeper;

import java.util.Map;

public class ZkStatus {
    private String exterAddress;
    private Map<String, ZkInstance> instances;
    private String interAddress;
    private String phase;
    private String resourceupdateneedrestart;
    private String parameterupdateneedrestart;

    public String getExterAddress() {
        return exterAddress;
    }

    public void setExterAddress(String exterAddress) {
        this.exterAddress = exterAddress;
    }

    public Map<String, ZkInstance> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, ZkInstance> instances) {
        this.instances = instances;
    }

    public String getInterAddress() {
        return interAddress;
    }

    public void setInterAddress(String interAddress) {
        this.interAddress = interAddress;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getResourceupdateneedrestart() {
        return resourceupdateneedrestart;
    }

    public void setResourceupdateneedrestart(String resourceupdateneedrestart) {
        this.resourceupdateneedrestart = resourceupdateneedrestart;
    }

    public String getParameterupdateneedrestart() {
        return parameterupdateneedrestart;
    }

    public void setParameterupdateneedrestart(String parameterupdateneedrestart) {
        this.parameterupdateneedrestart = parameterupdateneedrestart;
    }

}
