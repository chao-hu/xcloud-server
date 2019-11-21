package com.xxx.xcloud.module.component.model.kafka;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

import io.fabric8.kubernetes.api.model.Affinity;

/**
 * <p>
 * kafka pod 描述
 * <p>
 * 这里也可以不继承Spec,继承后不赋值相关字段即可。
 *
 * @author xujiangpeng
 * @date 2018/6/12
 */
public class KafkaSpec extends BaseSpec {

    /**
     * 操作属性
     */
    private KafkaOp kafkaop;
    private String version;
    private String image;
    private KafkaConfig config;

    private boolean healthcheck;
    private Map<String, String> nodeSelector;
    private Affinity affinity;
    /**
     * YAML 更新时间
     */
    private String updatetime;
    private String schedulerName;

    public KafkaOp getKafkaop() {
        return kafkaop;
    }

    public void setKafkaop(KafkaOp kafkaop) {
        this.kafkaop = kafkaop;
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

    public KafkaConfig getConfig() {
        return config;
    }

    public void setConfig(KafkaConfig config) {
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
