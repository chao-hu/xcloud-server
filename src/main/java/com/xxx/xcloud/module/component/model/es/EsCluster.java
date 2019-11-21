package com.xxx.xcloud.module.component.model.es;

import com.xxx.xcloud.module.component.consts.EsClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

/**
 * @ClassName: EsCluster
 * @Description: EsCluster
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsCluster extends CustomResource {

    private static final long serialVersionUID = -7563622674423081677L;
    private EsSpec spec;
    private EsStatus status;

    public EsCluster() {
        super();
        super.setKind(EsClusterConst.ES_KIND);
        super.setApiVersion(EsClusterConst.ES_API_VERSION);
    }

    public EsSpec getSpec() {
        return spec;
    }

    public void setSpec(EsSpec spec) {
        this.spec = spec;
    }

    public EsStatus getStatus() {
        return status;
    }

    public void setStatus(EsStatus status) {
        this.status = status;
    }

}
