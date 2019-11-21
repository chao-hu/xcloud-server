package com.xxx.xcloud.module.component.model.redis;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class RedisClusterDoneable extends CustomResourceDoneable<RedisCluster> {
	public RedisClusterDoneable(RedisCluster resource, Function<RedisCluster, RedisCluster> function) {
		super(resource, function);
	}
}
