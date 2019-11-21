package com.xxx.xcloud.module.component.model.storm;

import java.util.List;
import java.util.Map;

import com.xxx.xcloud.module.component.model.base.StatusCondition;

/**
 * @author xujiangpeng
 * @date 2018/7/29
 */
public class StormStatus {

    private Map<String, String> currentConfig;
    private boolean parameterUpdateNeedRestart;
    private String phase;
    private boolean resourceUpdateNeedRestart;

    /**
     * key : nodeName
     */
    private Map<String, StormNode> serverNodes;
    private int waitStormTimeout;

    private List<StatusCondition> conditions;
    private String reason;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public boolean isParameterUpdateNeedRestart() {
        return parameterUpdateNeedRestart;
    }

    public void setParameterUpdateNeedRestart(boolean parameterUpdateNeedRestart) {
        this.parameterUpdateNeedRestart = parameterUpdateNeedRestart;
    }

    public boolean isResourceUpdateNeedRestart() {
        return resourceUpdateNeedRestart;
    }

    public void setResourceUpdateNeedRestart(boolean resourceUpdateNeedRestart) {
        this.resourceUpdateNeedRestart = resourceUpdateNeedRestart;
    }

    public Map<String, StormNode> getServerNodes() {
        return serverNodes;
    }

    public void setServerNodes(Map<String, StormNode> serverNodes) {
        this.serverNodes = serverNodes;
    }

    public int getWaitStormTimeout() {
        return waitStormTimeout;
    }

    public void setWaitStormTimeout(int waitStormTimeout) {
        this.waitStormTimeout = waitStormTimeout;
    }

    public Map<String, String> getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(Map<String, String> currentConfig) {
        this.currentConfig = currentConfig;
    }

    public List<StatusCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<StatusCondition> conditions) {
        this.conditions = conditions;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
