package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;


/**
 * @ClassName: MemcachedClusterConst
 * @Description: Memcached常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class MemcachedClusterConst {

    public static final String MEMCACHED_KIND = "Memcached";
    public static final String MEMCACHED_API_VERSION = "memcached.bonc.com/v1beta1";
    public static final String MEMCACHED_CLUSTER_CRDS = "memcacheds.memcached.bonc.com";

    public static final String MEMCACHED_CONFIG_NAME = "memconf";
    public static final String MEMCACHED_TYPE_SINGLE = "single";
    public static final String MEMCACHED_TYPE_MM = "mm";

    public static final String MEMCACHED_ROLE_SERVER = "server";

    public final static String MEMCACHED_CLUSTER_CREATE_WOEKER = "MemcachedClusterCreateWorker";
    public final static String MEMCACHED_CLUSTER_DELETE_WOEKER = "MemcachedClusterDeleteWorker";
    public final static String MEMCACHED_CLUSTER_START_WOEKER = "MemcachedClusterStartWorker";
    public final static String MEMCACHED_CLUSTER_STOP_WOEKER = "MemcachedClusterStopWorker";
    public final static String MEMCACHED_CLUSTER_EXPAND_NODE_WOEKER = "MemcachedClusterExpandNodeWorker";

    public static final String MEMCACHED_EXPORTER_LOGDIR = "/temp/component";

    public static final List<String> MEMCACHED_TYPE_LIST = Arrays.asList(MEMCACHED_TYPE_SINGLE, MEMCACHED_TYPE_MM);

    public static final List<String> MEMCACHED_CLUSTER_WAITING_STATE_LIST = Arrays.asList(
            CommonConst.STATE_CLUSTER_PENDING, CommonConst.STATE_CLUSTER_SCALING,
            CommonConst.STATE_CLUSTER_TERMINATING);

    public static final List<String> MEMCACHED_NODE_WAITING_STATE_LIST = Arrays.asList(CommonConst.STATE_NODE_PENDING,
            CommonConst.STATE_NODE_DELETEING, CommonConst.STATE_NODE_STOPPING);

    public static List<String> getMemcachedTypeList() {
        return MEMCACHED_TYPE_LIST;
    }

    public static List<String> getMemcachedClusterWaitingStateList() {
        return MEMCACHED_CLUSTER_WAITING_STATE_LIST;
    }

    public static List<String> getMemcachedNodeWaitingStateList() {
        return MEMCACHED_NODE_WAITING_STATE_LIST;
    }

}
