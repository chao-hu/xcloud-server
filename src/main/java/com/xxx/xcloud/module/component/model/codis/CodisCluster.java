package com.xxx.xcloud.module.component.model.codis;

import com.xxx.xcloud.module.component.consts.CodisClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

/**
 * @ClassName: CodisCluster
 * @Description: CodisCluster
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisCluster extends CustomResource {

    private static final long serialVersionUID = 1816482532890470529L;
    private CodisSpec spec;
    private CodisStatus status;

    public CodisCluster() {
        super();
        super.setKind(CodisClusterConst.KIND_CODIS);
        super.setApiVersion(CodisClusterConst.API_VERSION);
    }

    public CodisSpec getSpec() {
        return spec;
    }

    public void setSpec(CodisSpec spec) {
        this.spec = spec;
    }

    public CodisStatus getStatus() {
        return status;
    }

    public void setStatus(CodisStatus status) {
        this.status = status;
    }

}
