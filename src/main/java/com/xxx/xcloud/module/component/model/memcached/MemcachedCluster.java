package com.xxx.xcloud.module.component.model.memcached;

import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

public class MemcachedCluster extends CustomResource {

    private static final long serialVersionUID = -7054767890364320633L;
    private MemcachedClusterSpec spec;
    private MemcachedClusterStatus status;

    public MemcachedCluster() {
        super();
        super.setApiVersion(MemcachedClusterConst.MEMCACHED_API_VERSION);
        super.setKind(MemcachedClusterConst.MEMCACHED_KIND);
    }

    public MemcachedClusterSpec getSpec() {
        return spec;
    }

    public void setSpec(MemcachedClusterSpec spec) {
        this.spec = spec;
    }

    public MemcachedClusterStatus getStatus() {
        return status;
    }

    public void setStatus(MemcachedClusterStatus status) {
        this.status = status;
    }

}
