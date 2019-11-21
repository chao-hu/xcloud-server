package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: ZkClusterConst
 * @Description: zk常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class ZkClusterConst {

    public static final String KIND = "ZKCluster";
    public static final String API_VERSION = "zk.bonc.com/v1beta1";
    public static final String ZK_CLUSTER_CRDS = "zkclusters.zk.bonc.com";

    public static final String ZK_CONFIG_FILE_NAME = "zoo.cfg";

    public static final String OPERATOR_CLUSTER_CREATE = "ClusterCreate";
    public static final String OPERATOR_CLUSTER_STOP = "ClusterStop";
    public static final String OPERATOR_CLUSTER_START = "ClusterStart";
    public static final String OPERATOR_CLUSTER_EXPAND = "ClusterAddNode";
    public static final String OPERATOR_CLUSTER_DELETE = "ClusterDelete";

    public static final String OPERATOR_CHANGE_CONFIG = "ClusterChangeConfig";
    public static final String OPERATOR_CHANGE_RESOURCE = "ClusterChangeResource";

    public static final String OPERATOR_NODE_STOP = "NodeStop";
    public static final String OPERATOR_NODE_START = "NodeStart";
    public static final String OPERATOR_NODE_DELETE = "NodeDelete";

    public final static String CLUSTER_CREATE_WORKER = "ZkClusterCreateWorker";
    public final static String CLUSTER_DELETE_WORKER = "ZkClusterDeleteWorker";
    public final static String CLUSTER_START_WORKER = "ZkClusterStartWorker";
    public final static String CLUSTER_STOP_WORKER = "ZkClusterStopWorker";
    public final static String CLUSTER_EXPAND_NODE_WORKER = "ZkClusterExpandNodeWorker";
    public final static String CLUSTER_CHANGE_RESOURCE_WORKER = "ZkClusterChangeResourceWorker";

    public final static String NODE_START_WORKER = "ZkNodeStartWorker";
    public final static String NODE_STOP_WORKER = "ZkNodeStopWorker";
    public final static String NODE_DELETE_WORKER = "ZkNodeDeleteWorker";

    public final static String ZK_NODE_STATE_PENDING = "Pending";
    public final static String ZK_NODE_STATE_STARTING = "Starting";
    public final static String ZK_NODE_STATE_STOPPING = "Stopping";
    public final static String ZK_NODE_STATE_TERMINATING = "Terminating";

    public final static String ZK_CLUSTER_STATE_PENDING = "Pending";
    public final static String ZK_CLUSTER_STATE_STARTING = "Starting";
    public final static String ZK_CLUSTER_STATE_STOPPING = "Stopping";
    public final static String ZK_CLUSTER_STATE_TERMINATING = "Terminating";
    public final static String ZK_CLUSTER_STATE_SCALING = "Scaling";
    public final static String ZK_CLUSTER_STATE_WAITING = "Waiting";

    public static final List<String> ZK_NODE_STATE_WAITING_LIST = Arrays.asList(ZK_NODE_STATE_PENDING,
            ZK_NODE_STATE_STARTING, ZK_NODE_STATE_STOPPING, ZK_NODE_STATE_TERMINATING);

    public static final List<String> ZK_CLUSTER_STATE_WAITING_LIST = Arrays.asList(ZK_CLUSTER_STATE_PENDING,
            ZK_CLUSTER_STATE_STARTING, ZK_CLUSTER_STATE_STOPPING, ZK_CLUSTER_STATE_TERMINATING,
            ZK_CLUSTER_STATE_SCALING, ZK_CLUSTER_STATE_WAITING);

    public static final String ZK_CAPACITY_DEFAULT = "1024";

    public static final int ZK_CLUSTER_CREATE_TIMEOUT = 300000;

    public static List<String> getZKNodeStateWaitingList() {
        return ZK_NODE_STATE_WAITING_LIST;
    }

    public static List<String> getZKClusterStateWaitingList() {
        return ZK_CLUSTER_STATE_WAITING_LIST;
    }

}
