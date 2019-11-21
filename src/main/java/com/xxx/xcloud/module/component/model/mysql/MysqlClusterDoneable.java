package com.xxx.xcloud.module.component.model.mysql;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class MysqlClusterDoneable extends CustomResourceDoneable<MysqlCluster>{

	public MysqlClusterDoneable(MysqlCluster resource, Function<MysqlCluster, MysqlCluster> function) {
		super(resource, function);
	}

}
