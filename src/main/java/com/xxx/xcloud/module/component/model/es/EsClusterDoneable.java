package com.xxx.xcloud.module.component.model.es;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

/**
 * @ClassName: EsClusterDoneable
 * @Description: EsClusterDoneable
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsClusterDoneable extends CustomResourceDoneable<EsCluster> {
    public EsClusterDoneable(EsCluster resource, Function<EsCluster, EsCluster> function) {
        super(resource, function);
    }
}