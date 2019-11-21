package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

/**
 * @ClassName: CodisDashboardSpec
 * @Description: CodisDashboardSpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisDashboardSpec extends BaseSpec {

    private Map<String, String> config;
    private String storageClass;

    private Map<String, String> coordinator;
    private String productAuth;

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

    public Map<String, String> getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(Map<String, String> coordinator) {
        this.coordinator = coordinator;
    }

    public String getProductAuth() {
        return productAuth;
    }

    public void setProductAuth(String productAuth) {
        this.productAuth = productAuth;
    }

}
