package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: EsClusterConst
 * @Description: es常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsClusterConst {

    public static final String ES_KIND = "ESCluster";
    public static final String ES_API_VERSION = "es.bonc.com/v1beta1";
    public static final String ES_CLUSTER_CRDS = "esclusters.es.bonc.com";

    public static final String ES_CONFIG_FILE_NAME = "elasticsearch.yml";

    public static final String ES_ROLE_MASTER = "master";
    public static final String ES_ROLE_DATA = "data";
    public static final String ES_ROLE_WORKER = "worker";
    
    public static final String ES_HAS_KIBANA_VERSION = "5.5.2";
    
    public static final String ES_MASTER_SEPARATE_FLAG_TRUE = "true";
    public static final String ES_MASTER_SEPARATE_FLAG_FALSE = "false";
    
    public static final String ES_CLUSTER_OPT_CREATE = "ClusterCreate";
    public static final String ES_CLUSTER_OPT_STOP = "ClusterStop";
    public static final String ES_CLUSTER_OPT_START = "ClusterStart";
    public static final String ES_CLUSTER_OPT_CHANGE_RESOURCE = "ClusterChangeResource";
    public static final String ES_CLUSTER_OPT_ADD_NODE = "ClusterAddNode";
    public static final String ES_CLUSTER_OPT_DELETE = "ClusterDelete";
    public static final String ES_CLUSTER_OPT_CHANGE_CONFIG = "ClusterChangeConfig";
    
    public static final String ES_NODE_OPT_START = "NodeStart";
    public static final String ES_NODE_OPT_STOP = "NodeStop";
    public static final String ES_NODE_OPT_DELETE = "NodeDelete";
    

    public final static String ES_CLUSTER_CREATE_WORKER = "EsClusterCreateWorker";
    public final static String ES_CLUSTER_DELETE_WORKER = "EsClusterDeleteWorker";
    public final static String ES_CLUSTER_START_WORKER = "EsClusterStartWorker";
    public final static String ES_CLUSTER_STOP_WORKER = "EsClusterStopWorker";
    public static final String ES_CLUSTER_EXPAND_NODE_WORKER = "EsClusterExpandNodeWorker";

    public final static String ES_NODE_START_WORKER = "EsNodeStartWorker";
    public final static String ES_NODE_STOP_WORKER = "EsNodeStopWorker";
    public final static String ES_NODE_DELETE_WORKER = "EsNodeDeleteWorker";

    private static final List<String> ES_NODE_STATE_WAITING_LIST = Arrays.asList(CommonConst.STATE_NODE_PENDING,
            CommonConst.STATE_NODE_STARTING, CommonConst.STATE_NODE_STOPPING, CommonConst.STATE_NODE_TERMINATING);

    private static final List<String> ES_CLUSTER_STATE_WAITING_LIST = Arrays.asList(CommonConst.STATE_CLUSTER_PENDING,
            CommonConst.STATE_CLUSTER_STARTING, CommonConst.STATE_CLUSTER_STOPPING,
            CommonConst.STATE_CLUSTER_TERMINATING, CommonConst.STATE_CLUSTER_SCALING,
            CommonConst.STATE_CLUSTER_WAITING);

    private static final List<String> ES_ROLES = Arrays.asList(ES_ROLE_MASTER, ES_ROLE_DATA, ES_ROLE_WORKER);

    public static List<String> getEsNodeStateWaitingList() {
        return ES_NODE_STATE_WAITING_LIST;
    }

    public static List<String> getEsClusterStateWaitingList() {
        return ES_CLUSTER_STATE_WAITING_LIST;
    }

    public static List<String> getRoles() {
        return ES_ROLES;
    }

}
