package com.xxx.xcloud.module.component.model.mysql;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

import io.fabric8.kubernetes.api.model.Affinity;

public class MysqlSpec extends BaseSpec {

    private MysqlNodeOp nodeop;
    private MysqlClusterOp clusterop;
    private String type;
    private String version;
    private String image;
    private String exporterimage;
    private MysqlBackup mysqlbackup;
    private MysqlConfig config;
    private boolean healthcheck;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    private String schedulerName;
    private String phpMyAdminimage;

    public MysqlNodeOp getNodeop() {
        return nodeop;
    }

    public void setNodeop(MysqlNodeOp nodeop) {
        this.nodeop = nodeop;
    }

    public MysqlClusterOp getClusterop() {
        return clusterop;
    }

    public void setClusterop(MysqlClusterOp clusterop) {
        this.clusterop = clusterop;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getExporterimage() {
        return exporterimage;
    }

    public void setExporterimage(String exporterimage) {
        this.exporterimage = exporterimage;
    }

    public MysqlBackup getMysqlbackup() {
        return mysqlbackup;
    }

    public void setMysqlbackup(MysqlBackup mysqlbackup) {
        this.mysqlbackup = mysqlbackup;
    }

    public MysqlConfig getConfig() {
        return config;
    }

    public void setConfig(MysqlConfig config) {
        this.config = config;
    }

    public boolean isHealthcheck() {
        return healthcheck;
    }

    public void setHealthcheck(boolean healthcheck) {
        this.healthcheck = healthcheck;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public Object getAffinity() {
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

    public String getPhpMyAdminimage() {
        return phpMyAdminimage;
    }

    public void setPhpMyAdminimage(String phpMyAdminimage) {
        this.phpMyAdminimage = phpMyAdminimage;
    }

}
