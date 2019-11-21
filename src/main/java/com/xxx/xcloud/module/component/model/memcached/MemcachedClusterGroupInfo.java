package com.xxx.xcloud.module.component.model.memcached;

import java.util.Map;

public class MemcachedClusterGroupInfo {

    private int id;
    private String status;
    private Map<String, MemcachedClusterServer> server;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, MemcachedClusterServer> getServer() {
        return server;
    }

    public void setServer(Map<String, MemcachedClusterServer> server) {
        this.server = server;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
