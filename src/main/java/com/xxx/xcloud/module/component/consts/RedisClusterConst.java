package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: RedisClusterConst
 * @Description: redis常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class RedisClusterConst {

    public static final String REDIS_KIND = "Redis";
    public static final String REDIS_API_VERSION = "redis.bonc.com/v1beta1";
    public static final String REDIS_CLUSTER_CRDS = "redises.redis.bonc.com";

    public final static String REDIS_CLUSTER_CREATE_WORKER = "RedisClusterCreateWorker";
    public final static String REDIS_CLUSTER_DELETE_WORKER = "RedisClusterDeleteWorker";
    public final static String REDIS_CLUSTER_START_WORKER = "RedisClusterStartWorker";
    public final static String REDIS_CLUSTER_STOP_WORKER = "RedisClusterStopWorker";
    public final static String REDIS_CLUSTER_EXPAND_NODE_WORKER = "RedisClusterExpandNodeWorker";
    public final static String REDIS_CLUSTER_CHANGERESOURCE_WORKER = "RedisClusterChangeResourceWorker";

    public static final String REDIS_CONF_NAME = "redis.conf";

    public static final int REDIS_SENTINEL_REPLICAS = 3;
    public static final double REDIS_SENTINEL_DEFAULT_CPU = 0.5;
    public static final double REDIS_SENTINEL_DEFAULT_MEMORY = 128;

    public static final String REDIS_TYPE_SINGLE = "single";
    public static final String REDIS_TYPE_MS = "ms";
    public static final String REDIS_TYPE_MS_SENTINEL = "ms-sentinel";

    public static final String REDIS_EXPORTER_LOGDIR = "/temp/component";
    public static final String REDIS_STORAGE_CLASS_LVM = "lvm";

    public static final List<String> REDIS_TYPE_LIST = Arrays.asList(REDIS_TYPE_MS, REDIS_TYPE_MS_SENTINEL,
            REDIS_TYPE_SINGLE);

    public static List<String> getRedisTypeList() {
        return REDIS_TYPE_LIST;
    }

    public static final List<String> REDIS_CLUSTER_WAITING_STATE_LIST = Arrays.asList(CommonConst.STATE_CLUSTER_PENDING,
            CommonConst.STATE_CLUSTER_SCALING, CommonConst.STATE_CLUSTER_TERMINATING);

    public static final List<String> REDIS_NODE_WAITING_STATE_LIST = Arrays.asList(CommonConst.STATE_NODE_PENDING,
            CommonConst.STATE_NODE_DELETEING, CommonConst.STATE_NODE_STOPPING);

    public static List<String> getRedisClusterWaitingStateList() {
        return REDIS_CLUSTER_WAITING_STATE_LIST;
    }

    public static List<String> getRedisNodeWaitingStateList() {
        return REDIS_NODE_WAITING_STATE_LIST;
    }

}
