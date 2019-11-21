package com.xxx.xcloud.module.component.model.codis;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

/**
 * @ClassName: CodisClusterDoneable
 * @Description: CodisClusterDoneable
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisClusterDoneable extends CustomResourceDoneable<CodisCluster> {
    public CodisClusterDoneable(CodisCluster resource, Function<CodisCluster, CodisCluster> function) {
        super(resource, function);
    }
}
