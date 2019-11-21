package com.xxx.xcloud.module.component.model.kafka;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.HealthCheck;

/**
 * 
 * 
 * @author LiuYue
 * @date 2018年12月24日
 */
public class KafkaConfig extends HealthCheck {

    /**
     * 配置项
     */
    private Map<String, String> kafkacnf;

    public Map<String, String> getKafkacnf() {
        return kafkacnf;
    }

    public void setKafkacnf(Map<String, String> kafkacnf) {
        this.kafkacnf = kafkacnf;
    }
}
