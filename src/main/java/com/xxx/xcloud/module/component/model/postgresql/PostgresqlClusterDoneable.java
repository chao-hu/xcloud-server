package com.xxx.xcloud.module.component.model.postgresql;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class PostgresqlClusterDoneable extends CustomResourceDoneable<PostgresqlCluster> {

    public PostgresqlClusterDoneable(PostgresqlCluster resource,
            Function<PostgresqlCluster, PostgresqlCluster> function) {
        super(resource, function);
    }

}
