package com.xxx.xcloud.module.component.model.es;

import java.util.Map;

import io.fabric8.kubernetes.api.model.Affinity;

/**
 * @ClassName: EsSpec
 * @Description: EsSpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsSpec {

    private String opt;
    private String vgName;
    private String version;
    private String esImage;
    private String optNodename;
    private boolean kibanaFlag;
    private String kibanaImage;
    private String esExporterImage;
    private Map<String, String> config;
    private Map<String, String> nodeSelector;
    private Map<String, EsInstanceGroup> instanceGroup;
    private String schedulerName;
    private Affinity affinity;

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public String getOptNodename() {
        return optNodename;
    }

    public void setOptNodename(String optNodename) {
        this.optNodename = optNodename;
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

    public Map<String, EsInstanceGroup> getInstanceGroup() {
        return instanceGroup;
    }

    public void setInstanceGroup(Map<String, EsInstanceGroup> instanceGroup) {
        this.instanceGroup = instanceGroup;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getKibanaImage() {
        return kibanaImage;
    }

    public void setKibanaImage(String kibanaImage) {
        this.kibanaImage = kibanaImage;
    }

    public String getEsImage() {
        return esImage;
    }

    public void setEsImage(String esImage) {
        this.esImage = esImage;
    }

    public boolean isKibanaFlag() {
        return kibanaFlag;
    }

    public void setKibanaFlag(boolean kibanaFlag) {
        this.kibanaFlag = kibanaFlag;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public String getEsExporterImage() {
        return esExporterImage;
    }

    public void setEsExporterImage(String esExporterImage) {
        this.esExporterImage = esExporterImage;
    }

}
