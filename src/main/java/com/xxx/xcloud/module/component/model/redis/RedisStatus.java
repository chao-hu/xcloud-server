package com.xxx.xcloud.module.component.model.redis;

import java.util.List;
import java.util.Map;

import com.xxx.xcloud.module.component.model.base.StatusCondition;

public class RedisStatus {
    private String phase;
    private String reason;
    private boolean needRestart;

    private Map<String, RedisServiceStatus> services;
    private Map<String, RedisBindingNode> bindings;

    private List<StatusCondition> conditions;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isNeedRestart() {
        return needRestart;
    }

    public void setNeedRestart(boolean needRestart) {
        this.needRestart = needRestart;
    }

    public Map<String, RedisServiceStatus> getServices() {
        return services;
    }

    public void setServices(Map<String, RedisServiceStatus> services) {
        this.services = services;
    }

    public Map<String, RedisBindingNode> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, RedisBindingNode> bindings) {
        this.bindings = bindings;
    }

    public List<StatusCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<StatusCondition> conditions) {
        this.conditions = conditions;
    }

}
