package com.xxx.xcloud.module.component.model.prometheus;

import com.xxx.xcloud.module.component.consts.PrometheusClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

public class PrometheusCluster extends CustomResource {

    private static final long serialVersionUID = 6287662085277812117L;
    private PrometheusSpec spec;
    private PrometheusStatus status;

    public PrometheusCluster() {
        super();
        super.setKind(PrometheusClusterConst.PROMETHEUS_KIND);
        super.setApiVersion(PrometheusClusterConst.PROMETHEUS_API_VERSION);
    }

    public PrometheusSpec getSpec() {
        return spec;
    }

    public void setSpec(PrometheusSpec spec) {
        this.spec = spec;
    }

    public PrometheusStatus getStatus() {
        return status;
    }

    public void setStatus(PrometheusStatus status) {
        this.status = status;
    }

}
