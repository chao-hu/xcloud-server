package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

/**
 * @ClassName: CodisStatus
 * @Description: CodisStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisStatus {

    private CodisDashboardStatus dashboard;
    private CodisProxyStatus proxy;
    private Map<Integer, CodisGroupStatus> group;
    private CodisSentinelStatus sentinel;

    private boolean needRestart;
    private String phase;
    private String reason;

    public CodisDashboardStatus getDashboard() {
        return dashboard;
    }

    public void setDashboard(CodisDashboardStatus dashboard) {
        this.dashboard = dashboard;
    }

    public CodisProxyStatus getProxy() {
        return proxy;
    }

    public void setProxy(CodisProxyStatus proxy) {
        this.proxy = proxy;
    }

    public Map<Integer, CodisGroupStatus> getGroup() {
        return group;
    }

    public void setGroup(Map<Integer, CodisGroupStatus> group) {
        this.group = group;
    }

    public CodisSentinelStatus getSentinel() {
        return sentinel;
    }

    public void setSentinel(CodisSentinelStatus sentinel) {
        this.sentinel = sentinel;
    }

    public boolean isNeedRestart() {
        return needRestart;
    }

    public void setNeedRestart(boolean needRestart) {
        this.needRestart = needRestart;
    }

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

}
