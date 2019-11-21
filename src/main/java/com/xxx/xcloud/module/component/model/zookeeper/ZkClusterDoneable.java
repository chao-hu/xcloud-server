package com.xxx.xcloud.module.component.model.zookeeper;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class ZkClusterDoneable extends CustomResourceDoneable<ZkCluster> {

    public ZkClusterDoneable(ZkCluster resource, Function<ZkCluster, ZkCluster> function) {
        super(resource, function);
    }

}
