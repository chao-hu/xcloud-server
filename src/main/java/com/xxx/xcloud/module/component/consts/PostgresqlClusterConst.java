package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: PostgresqlClusterConst
 * @Description: Postgresql常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class PostgresqlClusterConst {

    public static final String POSTGRESQL_KIND = "PostgreSQLCluster";
    public static final String POSTGRESQL_API_VERSION = "postgresqlcluster.bonc.com/v1beta1";
    public static final String POSTGRESQL_CLUSTER_CRDS = "postgresqlclusters.postgresqlcluster.bonc.com";
    
    /**
     * type
     */
    public static final String TYPE_SINGLE = "single";
    public static final String TYPE_SYNC = "sync";
    public static final String TYPE_ASYNC = "async";
 
    /**
     * postgresql集群处理Worker
     */
    public static final String CLUSTER_CREATE_WORKER = "PostgresqlClusterCreateWorker";
    public static final String CLUSTER_STOP_WORKER = "PostgresqlClusterStopWorker";
    public static final String CLUSTER_START_WORKER = "PostgresqlClusterStartWorker";
    public static final String CLUSTER_DELETE_WORKER = "PostgresqlClusterDeleteWorker";
    public static final String CLUSTER_EXPAND_NODE_WORKER = "PostgresqlClusterExpandNodeWorker";

    public static final String NODE_START_WORKER = "PostgresqlNodeStartWorker";
    public static final String NODE_STOP_WORKER = "PostgresqlNodeStopWorker";
    public static final String NODE_DELETE_WORKER = "PostgresqlNodeDeleteWorker";

    /**
     * cluster and node opt
     */
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

    /**
     * cluster and node state
     */
    public static final String POSTGRESQL_CLUSTER_STATE_SCALING = "Scaling";
    public static final String POSTGRESQL_CLUSTER_STATE_TERMINATING = "Terminating";
    public static final String POSTGRESQL_CLUSTER_STATE_WAITING = "Waiting";
    public static final String POSTGRESQL_CLUSTER_STATE_STARTING = "Starting";
    public static final String POSTGRESQL_CLUSTER_STATE_STOPPING = "Stopping";

    public static final String POSTGRESQL_NODE_STATE_PENDING = "Pending";
    public static final String POSTGRESQL_NODE_STATE_TERMINATING = "Terminating";
    public static final String POSTGRESQL_NODE_STATE_STARTING = "Starting";
    public static final String POSTGRESQL_NODE_STATE_STOPPING = "Stopping";

    public static final String POSTGRESQL_ISHEALTHCHECK = "yes";

    public static final List<String> POSTGRESQL_TYPE_LIST = Arrays.asList(TYPE_SINGLE, TYPE_SYNC, TYPE_ASYNC);

    public static final List<String> POSTGRESQL_CLUSTER_STATE_WAITING_LIST = Arrays.asList(
            POSTGRESQL_CLUSTER_STATE_SCALING, POSTGRESQL_CLUSTER_STATE_TERMINATING, POSTGRESQL_CLUSTER_STATE_WAITING,
            POSTGRESQL_CLUSTER_STATE_STARTING, POSTGRESQL_CLUSTER_STATE_STOPPING);

    public static final List<String> POSTGRESQL_NODE_STATE_WAITING_LIST = Arrays.asList(POSTGRESQL_NODE_STATE_PENDING,
            POSTGRESQL_NODE_STATE_TERMINATING, POSTGRESQL_NODE_STATE_STARTING, POSTGRESQL_NODE_STATE_STOPPING);

    public static List<String> getClusterStateWaitingList() {
        return POSTGRESQL_CLUSTER_STATE_WAITING_LIST;
    }

    public static List<String> getNodeStateWaitingList() {
        return POSTGRESQL_NODE_STATE_WAITING_LIST;
    }

    public static List<String> getPostgresqlTypeList() {
        return POSTGRESQL_TYPE_LIST;
    }

}
