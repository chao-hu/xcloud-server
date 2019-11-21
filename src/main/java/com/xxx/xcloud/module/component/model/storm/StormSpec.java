package com.xxx.xcloud.module.component.model.storm;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.Resources;

import io.fabric8.kubernetes.api.model.Affinity;

/**
 * @author xujiangpeng
 * @date 2018/7/29
 */
public class StormSpec {

    private Map<String, Resources> resources;
    private Map<String, Integer> replicas;
    private String volume;
    private String volumeMount;
    private String capacity;
    private StormOp stormOp;
    /**
     * Failed 状态的节点或集群进行启动时，需要该属性为true
     */
    private boolean opFailed;
    private String version;
    private String image;
    private Map<String, String> config;
    private StormHealthCheck healthCheck;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    private String schedulerName;
    /**
     * 线程超时时间
     */
    private int waitStormComponentAvailableTimeout;
    private StormExporter exporter;

    public Map<String, Integer> getReplicas() {
        return replicas;
    }

    public void setReplicas(Map<String, Integer> replicas) {
        this.replicas = replicas;
    }

    public StormOp getStormOp() {
        return stormOp;
    }

    public void setStormOp(StormOp stormOp) {
        this.stormOp = stormOp;
    }

    public boolean isOpFailed() {
        return opFailed;
    }

    public void setOpFailed(boolean opFailed) {
        this.opFailed = opFailed;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public StormHealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(StormHealthCheck healthCheck) {
        this.healthCheck = healthCheck;
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

    public int getWaitStormComponentAvailableTimeout() {
        return waitStormComponentAvailableTimeout;
    }

    public void setWaitStormComponentAvailableTimeout(int waitStormComponentAvailableTimeout) {
        this.waitStormComponentAvailableTimeout = waitStormComponentAvailableTimeout;
    }

    public StormExporter getExporter() {
        return exporter;
    }

    public void setExporter(StormExporter exporter) {
        this.exporter = exporter;
    }

    public Map<String, Resources> getResources() {
        return resources;
    }

    public void setResources(Map<String, Resources> resources) {
        this.resources = resources;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getVolumeMount() {
        return volumeMount;
    }

    public void setVolumeMount(String volumeMount) {
        this.volumeMount = volumeMount;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

}
