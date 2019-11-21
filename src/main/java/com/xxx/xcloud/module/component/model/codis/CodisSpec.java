package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

import io.fabric8.kubernetes.api.model.Affinity;

/**
 * @ClassName: CodisSpec
 * @Description: CodisSpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisSpec {

    private String version;
    private boolean stopped;
    private boolean paused;
    private String image;
    private String exporterImage;
    private String logDir;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    private CodisDashboardSpec dashboard;
    private CodisProxySpec proxy;
    private CodisGroupsSpec serverGroups;
    private CodisSentinelSpec sentinel;
    private String schedulerName;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getExporterImage() {
        return exporterImage;
    }

    public void setExporterImage(String exporterImage) {
        this.exporterImage = exporterImage;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public CodisDashboardSpec getDashboard() {
        return dashboard;
    }

    public void setDashboard(CodisDashboardSpec dashboard) {
        this.dashboard = dashboard;
    }

    public CodisProxySpec getProxy() {
        return proxy;
    }

    public void setProxy(CodisProxySpec proxy) {
        this.proxy = proxy;
    }

    public CodisGroupsSpec getServerGroups() {
        return serverGroups;
    }

    public void setServerGroups(CodisGroupsSpec serverGroups) {
        this.serverGroups = serverGroups;
    }

    public CodisSentinelSpec getSentinel() {
        return sentinel;
    }

    public void setSentinel(CodisSentinelSpec sentinel) {
        this.sentinel = sentinel;
    }

}
