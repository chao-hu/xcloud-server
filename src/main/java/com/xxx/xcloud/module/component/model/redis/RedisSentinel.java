package com.xxx.xcloud.module.component.model.redis;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

public class RedisSentinel extends BaseSpec {

    private int quorum;
    private Map<String, String> configMap;

    private String storageClass;

    public int getQuorum() {
        return quorum;
    }

    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

}
