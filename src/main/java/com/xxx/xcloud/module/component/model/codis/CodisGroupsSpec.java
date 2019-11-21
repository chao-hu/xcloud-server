package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

/**
 * @ClassName: CodisGroupsSpec
 * @Description: CodisGroupsSpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisGroupsSpec extends BaseSpec {

    private Map<String, String> config;
    private String storageClass;

    private int slaves;

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

    public int getSlaves() {
        return slaves;
    }

    public void setSlaves(int slaves) {
        this.slaves = slaves;
    }

}
