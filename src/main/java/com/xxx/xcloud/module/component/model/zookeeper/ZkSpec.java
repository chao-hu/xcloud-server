package com.xxx.xcloud.module.component.model.zookeeper;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.Resources;

import io.fabric8.kubernetes.api.model.Affinity;

public class ZkSpec {

    private String image;
    private String version;
    private String opt;
    private String optNodename;

    private int replicas;
    private Resources resources;
    private Map<String, String> config;
    private String vgName;

    private Map<String, String> nodeSelector;
    private ZkExporter exporter;
    private Affinity affinity;
    private String schedulerName;

    public ZkExporter getExporter() {
        return exporter;
    }

    public void setExporter(ZkExporter exporter) {
        this.exporter = exporter;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public String getOptNodename() {
        return optNodename;
    }

    public void setOptNodename(String optNodename) {
        this.optNodename = optNodename;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVgName() {
        return vgName;
    }

    public void setVgName(String vgName) {
        this.vgName = vgName;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

}
