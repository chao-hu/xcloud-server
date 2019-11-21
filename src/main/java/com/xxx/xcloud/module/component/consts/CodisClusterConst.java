package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: CodisClusterConst
 * @Description: codis常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisClusterConst {
    public static final String KIND_CODIS = "CodisCluster";
    public static final String CODIS_CLUSTER_CRDS = "codisclusters.redis.bonc.com";
    public static final String API_VERSION = "redis.bonc.com/v1beta1";

    public final static String CODIS_CLUSTER_CREATE_WORKER = "CodisClusterCreateWorker";
    public final static String CODIS_CLUSTER_DELETE_WORKER = "CodisClusterDeleteWorker";
    public final static String CODIS_CLUSTER_START_WORKER = "CodisClusterStartWorker";
    public final static String CODIS_CLUSTER_STOP_WORKER = "CodisClusterStopWorker";
    public final static String CODIS_CLUSTER_EXPAND_NODE_WORKER = "CodisClusterExpandNodeWorker";
    public final static String CODIS_CLUSTER_CHANGE_RESOURCE_WORKER = "CodisClusterChangeResourceWorker";

    public static final String CODIS_TYPE = "codis";

    public static final String CODIS_ROLE_MASTER = "master";
    public static final String CODIS_ROLE_DATA = "data";
    public static final String CODIS_ROLE_SERVER = "server";
    public static final String CODIS_ROLE_DASHBOARD = "dashboard";
    public static final String CODIS_ROLE_PROXY = "proxy";
    public static final String CODIS_ROLE_SENTINEL = "sentinel";

    public static final String CODIS_DASHBOARD_CONF_NAME = "dashboard.toml";
    public static final String CODIS_PROXY_CONF_NAME = "proxy.toml";
    public static final String CODIS_SERVER_GROUPS_CONF_NAME = "redis.conf";

    public static final int CODIS_SERVER_GROUPS_SLAVE_REPLICAS = 1;
    public static final int CODIS_SENTINEL_SLAVE_REPLICAS = 3;

    public static final String CODIS_EXPORTER_LOGDIR = "/temp/component";
    public static final String CODIS_STORAGE_CLASS_LVM = "lvm";

    public static final int CODIS_DASHBOARD_REPLICAS = 1;
    public static final double CODIS_DASHBOARD_DEFAULT_CPU = 0.5;
    public static final double CODIS_DASHBOARDL_DEFAULT_MEMORY = 128;

    public static final int CODIS_SENTINEL_REPLICAS = 3;
    public static final double CODIS_SENTINEL_DEFAULT_CPU = 0.5;
    public static final double CODIS_SENTINEL_DEFAULT_MEMORY = 128;

    public static final List<String> CODIS_CLUSTER_WAITING_STATE_LIST = Arrays.asList(CommonConst.STATE_CLUSTER_PENDING,
            CommonConst.STATE_CLUSTER_TERMINATING);

    public static final List<String> CODIS_NODE_WAITING_STATE_LIST = Arrays.asList(CommonConst.STATE_NODE_PENDING,
            CommonConst.STATE_NODE_DELETEING, CommonConst.STATE_NODE_STOPPING);

    public static final List<String> CODIS_BASE_NODE_NAME_LIST = Arrays.asList(CODIS_ROLE_DASHBOARD, CODIS_ROLE_PROXY,
            CODIS_ROLE_SENTINEL);

    public static List<String> getCodisBaseNodeNameList() {
        return CODIS_BASE_NODE_NAME_LIST;
    }

    public static List<String> getCodisClusterWaitingStateList() {
        return CODIS_CLUSTER_WAITING_STATE_LIST;
    }

    public static List<String> getCodisNodeWaitingStateList() {
        return CODIS_NODE_WAITING_STATE_LIST;
    }
}
