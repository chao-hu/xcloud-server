package com.xxx.xcloud.module.component.model.redis;

import com.xxx.xcloud.module.component.consts.RedisClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

public class RedisCluster extends CustomResource {

    private static final long serialVersionUID = -8435824833804885600L;
    private RedisSpec spec;
    private RedisStatus status;

    public RedisCluster() {
        super();
        super.setKind(RedisClusterConst.REDIS_KIND);
        super.setApiVersion(RedisClusterConst.REDIS_API_VERSION);
    }

    public RedisSpec getSpec() {
        return spec;
    }

    public void setSpec(RedisSpec spec) {
        this.spec = spec;
    }

    public RedisStatus getStatus() {
        return status;
    }

    public void setStatus(RedisStatus status) {
        this.status = status;
    }

}
