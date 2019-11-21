package com.xxx.xcloud.module.component.model.storm;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class StormClusterDoneable extends CustomResourceDoneable<StormCluster> {

    public StormClusterDoneable(StormCluster resource, Function<StormCluster, StormCluster> function) {
        super(resource, function);
    }

}
