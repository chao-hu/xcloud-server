package com.xxx.xcloud.module.component.model.zookeeper;

import com.xxx.xcloud.module.component.consts.ZkClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

public class ZkCluster extends CustomResource {

    /**
     * 
     */
    private static final long serialVersionUID = 3731192399871697639L;
    private ZkSpec spec;
    private ZkStatus status;

    public ZkCluster() {
        super();
        super.setKind(ZkClusterConst.KIND);
        super.setApiVersion(ZkClusterConst.API_VERSION);
    }

    public ZkSpec getSpec() {
        return spec;
    }

    public void setSpec(ZkSpec spec) {
        this.spec = spec;
    }

    public ZkStatus getStatus() {
        return status;
    }

    public void setStatus(ZkStatus status) {
        this.status = status;
    }

}
