package com.xxx.xcloud.module.component.model.es;

import com.xxx.xcloud.module.component.model.base.Resources;

/**
 * @ClassName: EsInstanceGroup
 * @Description: EsInstanceGroup
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsInstanceGroup {

    private String role;
    private int replicas;
    private String storage;
    private Resources resources;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
