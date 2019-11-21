package com.xxx.xcloud.module.component.model.mysql;

import com.xxx.xcloud.module.component.consts.MysqlClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

public class MysqlCluster extends CustomResource {

    private static final long serialVersionUID = -7016405138280256116L;

    private MysqlSpec spec;
    private MysqlStatus status;

    public MysqlCluster() {
        super();
        super.setApiVersion(MysqlClusterConst.API_VERSION);
        super.setKind(MysqlClusterConst.KIND);
    }

    public MysqlSpec getSpec() {
        return spec;
    }

    public void setSpec(MysqlSpec spec) {
        this.spec = spec;
    }

    public MysqlStatus getStatus() {
        return status;
    }

    public void setStatus(MysqlStatus status) {
        this.status = status;
    }

}
