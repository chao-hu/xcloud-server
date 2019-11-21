package com.xxx.xcloud.module.component.model.kafka;

import java.util.List;
import java.util.Map;

import com.xxx.xcloud.module.component.model.base.StatusCondition;

/**
 * @author xujiangpeng
 * @date 2018/6/19
 */
public class KafkaStatus {

    private String phase;
    private boolean resourceupdateneedrestart;
    private boolean parameterupdateneedrestart;
    /**
     * key : nodeName
     */
    private Map<String, KafkaNode> serverNodes;
    private List<StatusCondition> conditions;
    private String reason;

    private int waitkafkatimeout;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public boolean isResourceupdateneedrestart() {
        return resourceupdateneedrestart;
    }

    public void setResourceupdateneedrestart(boolean resourceupdateneedrestart) {
        this.resourceupdateneedrestart = resourceupdateneedrestart;
    }

    public boolean isParameterupdateneedrestart() {
        return parameterupdateneedrestart;
    }

    public void setParameterupdateneedrestart(boolean parameterupdateneedrestart) {
        this.parameterupdateneedrestart = parameterupdateneedrestart;
    }

    public Map<String, KafkaNode> getServerNodes() {
        return serverNodes;
    }

    public void setServerNodes(Map<String, KafkaNode> serverNodes) {
        this.serverNodes = serverNodes;
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

    public int getWaitkafkatimeout() {
        return waitkafkatimeout;
    }

    public void setWaitkafkatimeout(int waitkafkatimeout) {
        this.waitkafkatimeout = waitkafkatimeout;
    }

}
