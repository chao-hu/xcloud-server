package com.xxx.xcloud.module.component.model.ftp;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

/**
 * @ClassName: FtpClusterDoneable
 * @Description: FtpClusterDoneable
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpClusterDoneable extends CustomResourceDoneable<FtpCluster> {

    public FtpClusterDoneable(FtpCluster resource, Function<FtpCluster, FtpCluster> function) {
        super(resource, function);
    }

}
