package com.xxx.xcloud.module.component.model.ftp;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

import io.fabric8.kubernetes.api.model.Affinity;

/**
 * @ClassName: FtpSpec
 * @Description: FtpSpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpSpec extends BaseSpec {

    /**
     * 操作属性
     */
    private FtpOp ftpop;
    private String version;
    private String image;
    private FtpConfig config;
    private boolean healthcheck;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    /**
     * YAML 更新时间
     */
    private String updatetime;
    private String schedulerName;

    public FtpOp getFtpop() {
        return ftpop;
    }

    public void setFtpop(FtpOp ftpop) {
        this.ftpop = ftpop;
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

    public FtpConfig getConfig() {
        return config;
    }

    public void setConfig(FtpConfig config) {
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

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

}
