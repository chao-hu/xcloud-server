package com.xxx.xcloud.module.component.model.prometheus;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.Resources;

import io.fabric8.kubernetes.api.model.Affinity;

public class PrometheusSpec {

    private String opt;
    private int replicas;
    private String vgName;
    private String version;
    private String storage;
    private String configOpt;
    private String optNodename;
    private Resources resources;
    private String prometheusImage;
    private Map<String, String> nodeSelector;
    private Map<String, String> targetConfig;
    private Map<String, String> prometheusConfig;
    private String schedulerName;
    private Affinity affinity;

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

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
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

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getConfigOpt() {
        return configOpt;
    }

    public void setConfigOpt(String configOpt) {
        this.configOpt = configOpt;
    }

    public String getPrometheusImage() {
        return prometheusImage;
    }

    public void setPrometheusImage(String prometheusImage) {
        this.prometheusImage = prometheusImage;
    }

    public Map<String, String> getTargetConfig() {
        return targetConfig;
    }

    public void setTargetConfig(Map<String, String> targetConfig) {
        this.targetConfig = targetConfig;
    }

    public Map<String, String> getPrometheusConfig() {
        return prometheusConfig;
    }

    public void setPrometheusConfig(Map<String, String> prometheusConfig) {
        this.prometheusConfig = prometheusConfig;
    }

}
