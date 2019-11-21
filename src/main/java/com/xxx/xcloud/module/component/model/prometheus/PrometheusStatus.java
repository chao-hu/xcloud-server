package com.xxx.xcloud.module.component.model.prometheus;

import java.util.Map;

public class PrometheusStatus {
    private String phase;
    private String configName;
    private String resourceupdateneedrestart;
    private String parameterupdateneedrestart;
    private Map<String, PrometheusInstances> instances;

    public Map<String, PrometheusInstances> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, PrometheusInstances> instances) {
        this.instances = instances;
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

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

}
