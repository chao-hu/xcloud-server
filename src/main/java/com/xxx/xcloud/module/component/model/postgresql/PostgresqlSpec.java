package com.xxx.xcloud.module.component.model.postgresql;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.Resources;

import io.fabric8.kubernetes.api.model.Affinity;

public class PostgresqlSpec {

    private String postgresqlClusterImage;
    private String postgresqlExporterImage;
    private String version;
    private String type;
    private String password;
    private String replUser;
    private String replPassword;
    private String opt;
    private String optNodename;
    private Resources resources;
    private int replicas;
    private String storage;
    private String vgName;
    private Map<String, String> postgresqlClusterConfig;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    private String schedulerName;
    private String isHealthCheck;

    public String getPostgresqlClusterImage() {
        return postgresqlClusterImage;
    }

    public void setPostgresqlClusterImage(String postgresqlClusterImage) {
        this.postgresqlClusterImage = postgresqlClusterImage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReplUser() {
        return replUser;
    }

    public void setReplUser(String replUser) {
        this.replUser = replUser;
    }

    public String getReplPassword() {
        return replPassword;
    }

    public void setReplPassword(String replPassword) {
        this.replPassword = replPassword;
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

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getVgName() {
        return vgName;
    }

    public void setVgName(String vgName) {
        this.vgName = vgName;
    }

    public Map<String, String> getPostgresqlClusterConfig() {
        return postgresqlClusterConfig;
    }

    public void setPostgresqlClusterConfig(Map<String, String> postgresqlClusterConfig) {
        this.postgresqlClusterConfig = postgresqlClusterConfig;
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

    public String getPostgresqlExporterImage() {
        return postgresqlExporterImage;
    }

    public void setPostgresqlExporterImage(String postgresqlExporterImage) {
        this.postgresqlExporterImage = postgresqlExporterImage;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getIsHealthCheck() {
        return isHealthCheck;
    }

    public void setIsHealthCheck(String isHealthCheck) {
        this.isHealthCheck = isHealthCheck;
    }

}
