package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

/**
 * @ClassName: CodisSentinelSpec
 * @Description: CodisSentinelSpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisSentinelSpec extends BaseSpec {

    private Map<String, String> config;
    private String storageClass;

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

}
