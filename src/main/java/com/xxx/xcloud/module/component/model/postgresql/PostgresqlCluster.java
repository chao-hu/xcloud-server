package com.xxx.xcloud.module.component.model.postgresql;

import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

public class PostgresqlCluster extends CustomResource {

    private static final long serialVersionUID = -750341823279991998L;

    private PostgresqlSpec spec;
    private PostgresqlStatus status;

    public PostgresqlCluster() {
        super();
        super.setKind(PostgresqlClusterConst.POSTGRESQL_KIND);
        super.setApiVersion(PostgresqlClusterConst.POSTGRESQL_API_VERSION);
    }

    public PostgresqlSpec getSpec() {
        return spec;
    }

    public void setSpec(PostgresqlSpec spec) {
        this.spec = spec;
    }

    public PostgresqlStatus getStatus() {
        return status;
    }

    public void setStatus(PostgresqlStatus status) {
        this.status = status;
    }

}
