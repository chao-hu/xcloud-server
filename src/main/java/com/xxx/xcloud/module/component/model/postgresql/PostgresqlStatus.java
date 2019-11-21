package com.xxx.xcloud.module.component.model.postgresql;

import java.util.Map;

public class PostgresqlStatus {

    private String resourceupdateneedrestart;
    private String parameterupdateneedrestart;
    private String phase;
    private Map<String, PostgresqlInstance> instances;

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

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public Map<String, PostgresqlInstance> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, PostgresqlInstance> instances) {
        this.instances = instances;
    }

}
