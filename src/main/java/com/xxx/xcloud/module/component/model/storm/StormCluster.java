package com.xxx.xcloud.module.component.model.storm;

import io.fabric8.kubernetes.client.CustomResource;

public class StormCluster extends CustomResource {

    /**
     * 
     */
    private static final long serialVersionUID = -262716998997304029L;
    private StormSpec spec;
    private StormStatus status;

    public StormSpec getSpec() {
        return spec;
    }

    public void setSpec(StormSpec spec) {
        this.spec = spec;
    }

    public StormStatus getStatus() {
        return status;
    }

    public void setStatus(StormStatus status) {
        this.status = status;
    }
}
