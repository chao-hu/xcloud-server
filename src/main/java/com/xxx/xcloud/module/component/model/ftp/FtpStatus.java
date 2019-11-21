package com.xxx.xcloud.module.component.model.ftp;

import java.util.List;
import java.util.Map;

import com.xxx.xcloud.module.component.model.base.StatusCondition;

/**
 * @ClassName: FtpStatus
 * @Description: FtpStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpStatus {

    private String phase;
    private boolean resourceupdateneedrestart;
    private boolean parameterupdateneedrestart;
    private Map<String, FtpNode> serverNodes;
    private List<StatusCondition> conditions;
    private String reason;

    private int waitftptimeout;

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

    public Map<String, FtpNode> getServerNodes() {
        return serverNodes;
    }

    public void setServerNodes(Map<String, FtpNode> serverNodes) {
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

    public int getWaitftptimeout() {
        return waitftptimeout;
    }

    public void setWaitftptimeout(int waitftptimeout) {
        this.waitftptimeout = waitftptimeout;
    }

}
