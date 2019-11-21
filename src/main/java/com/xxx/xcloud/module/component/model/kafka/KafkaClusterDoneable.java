package com.xxx.xcloud.module.component.model.kafka;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class KafkaClusterDoneable extends CustomResourceDoneable<KafkaCluster> {

    public KafkaClusterDoneable(KafkaCluster resource, Function<KafkaCluster, KafkaCluster> function) {
        super(resource, function);
    }

}
