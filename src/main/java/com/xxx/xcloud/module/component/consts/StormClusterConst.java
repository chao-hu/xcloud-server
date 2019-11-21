package com.xxx.xcloud.module.component.consts;

/**
 * @ClassName: StormClusterConst
 * @Description: storm常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class StormClusterConst {

    /**
     * 资源类型
     */
    public static final String KIND = "StormCluster";

    /**
     * 资源版本
     */
    public static final String API_VERSION = "storm.bonc.com/v1beta1";
    public static final String STORM_CLUSTER_CRDS = "stormclusters.storm.bonc.com";

    /**
     * 健康检查
     */
    public final static String HEALTH_CHECK_IS_ENABLE = "isEnable";
    public final static String HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT = "livenessDelayTimeout";
    public final static String HEALTH_CHECK_READINESS_DELAY_TIMEOUT = "readinessDelayTimeout";
    public final static String HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD = "livenessFailureThreshold";
    public final static String HEALTH_CHECK_READINESS_FAILURE_THRESHOLD = "readinessFailureThreshold";

    /**
     * storm集群处理Worker
     */
    public final static String CLUSTER_CREATE_WORKER = "StormClusterCreateWorker";
    public final static String CLUSTER_STOP_WORKER = "StormClusterStopWorker";
    public final static String CLUSTER_START_WORKER = "StormClusterStartWorker";
    public final static String CLUSTER_DELETE_WORKER = "StormClusterDeleteWorker";
    public final static String CLUSTER_EXPAND_NODE_WORKER = "StormClusterExpandNodeWorker";
    public final static String CLUSTER_CHANGE_RESOURCE_WORKER = "StormClusterChangeResourceWorker";

    public final static String NODE_START_WORKER = "StormNodeStartWorker";
    public final static String NODE_STOP_WORKER = "StormNodeStopWorker";
    public final static String NODE_DELETE_WORKER = "StormNodeDeleteWorker";

    /**
     * Storm操作类型
     */
    public static final String OPERATOR_CLUSTER_CREATE = "ClusterCreate";
    public static final String OPERATOR_CLUSTER_STOP = "ClusterStop";
    public static final String OPERATOR_CLUSTER_START = "ClusterStart";
    public static final String OPERATOR_CLUSTER_EXPAND = "AddNode";
    public static final String OPERATOR_CLUSTER_DELETE = "ClusterDelete";

    public static final String OPERATOR_CHANGE_CONFIG = "ChangeConfig";
    public static final String OPERATOR_CHANGE_RESOURCE = "ChangeResource";

    public static final String OPERATOR_NODE_STOP = "NodeStop";
    public static final String OPERATOR_NODE_START = "NodeStart";
    public static final String OPERATOR_NODE_DELETE = "NodeDelete";

    /**
     * Storm常用
     */
    public static final String ROLE_NIMBUS = "nimbus";

    public static final Double NIMBUS_CAPACITY_DEFAULT_MI = 1024.0;

    public static final Double NIMBUS_CAPACITY_DEFAULT_GI = 1.0;

    public static final String ROLE_SUPERVISOR = "supervisor";

    public static final String ZOOKEEPER_SERVERS = "storm.zookeeper.servers";
    
    public static final String NIMBUS_UI_URL = "nimbusUiUrl";
    public static final String NIMBUS_TCP_PORT_VALUE = "6627";
    
    public static final String JAR_UPLOAD_PATH_KEY = "jarUploadPath";
    public static final String JAR_UPLOAD_PATH_VALUE = "/opt/storm/topologyData";
}
