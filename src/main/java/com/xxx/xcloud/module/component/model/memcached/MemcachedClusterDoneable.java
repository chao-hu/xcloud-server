package com.xxx.xcloud.module.component.model.memcached;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class MemcachedClusterDoneable extends CustomResourceDoneable<MemcachedCluster> {
    public MemcachedClusterDoneable(MemcachedCluster resource, Function<MemcachedCluster, MemcachedCluster> function) {
        super(resource, function);
    }
}