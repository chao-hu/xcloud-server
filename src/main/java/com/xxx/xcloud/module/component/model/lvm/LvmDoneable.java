package com.xxx.xcloud.module.component.model.lvm;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class LvmDoneable extends CustomResourceDoneable<Lvm> {
    public LvmDoneable(Lvm resource, Function<Lvm, Lvm> function) {
        super(resource, function);
    }
}
