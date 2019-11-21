package com.xxx.xcloud.module.component.model.lvm;

import io.fabric8.kubernetes.client.CustomResource;

public class Lvm extends CustomResource {

    private static final long serialVersionUID = 3266782640294373362L;
    private LvmSpec spec;

    public Lvm() {
        super();
        super.setApiVersion("bonc.com/v1");
        super.setKind("LVM");
    }

    public LvmSpec getSpec() {
        return spec;
    }

    public void setSpec(LvmSpec spec) {
        this.spec = spec;
    }

}
