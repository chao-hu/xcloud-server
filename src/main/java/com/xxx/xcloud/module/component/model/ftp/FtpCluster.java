package com.xxx.xcloud.module.component.model.ftp;

import com.xxx.xcloud.module.component.consts.FtpClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

/**
 * @ClassName: FtpCluster
 * @Description: FtpCluster
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpCluster extends CustomResource {

    private static final long serialVersionUID = 6386171895711494509L;
    private FtpSpec spec;
    private FtpStatus status;

    public FtpCluster() {
        super();
        super.setApiVersion(FtpClusterConst.API_VERSION);
        super.setKind(FtpClusterConst.KIND);
    }

    public FtpSpec getSpec() {
        return spec;
    }

    public void setSpec(FtpSpec spec) {
        this.spec = spec;
    }

    public FtpStatus getStatus() {
        return status;
    }

    public void setStatus(FtpStatus status) {
        this.status = status;
    }
}
