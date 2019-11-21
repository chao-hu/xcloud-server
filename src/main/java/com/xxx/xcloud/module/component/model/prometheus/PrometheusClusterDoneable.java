package com.xxx.xcloud.module.component.model.prometheus;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class PrometheusClusterDoneable extends CustomResourceDoneable<PrometheusCluster> {
    public PrometheusClusterDoneable(PrometheusCluster resource,
            Function<PrometheusCluster, PrometheusCluster> function) {
        super(resource, function);
    }
}
