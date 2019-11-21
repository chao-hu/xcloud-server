package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: MysqlClusterConst
 * @Description: MysqlCluster常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class MysqlClusterConst {

    /**
     *  mysqlcluster
     */
    public static final String KIND = "MysqlCluster";
    public static final String API_VERSION = "mysql.bonc.com/v1beta1";
    public static final String MYSQL_CLUSTER_CRDS = "mysqlclusters.mysql.bonc.com";

    /**
     *  type
     */
    public static final String TYPE_MS = "ms";
    public static final String TYPE_MM = "mm";
    public static final String TYPE_SINGLE = "single";

    /**
     *  opt
     */
    public static final String OPERATOR_CLUSTER_CREATE = "Create";
    public static final String OPERATOR_CLUSTER_STOP = "Stop";
    public static final String OPERATOR_CLUSTER_START = "Start";

    public static final String OPERATOR_CLUSTER_EXPAND = "AddNode";
    public static final String OPERATOR_CHANGE_CONFIG = "ChangeMycnf";
    public static final String OPERATOR_CHANGE_RESOURCE = "ChangeResource";

    public static final String OPERATOR_NODE_START = "Start";
    public static final String OPERATOR_NODE_STOP = "Stop";
    public static final String OPERATOR_NODE_DELETE = "Delete";

    public static final String MYSQL_CONFIG_FILE_NAME_MYCNF = "my.cnf";

    /**
     *  worker
     */
    public final static String CLUSTER_CREATE_WORKER = "MysqlClusterCreateWorker";
    public final static String CLUSTER_DELETE_WORKER = "MysqlClusterDeleteWorker";
    public final static String CLUSTER_START_WORKER = "MysqlClusterStartWorker";
    public final static String CLUSTER_STOP_WORKER = "MysqlClusterStopWorker";
    public final static String CLUSTER_EXPAND_NODE_WORKER = "MysqlClusterExpandNodeWorker";
    public final static String CLUSTER_CHANGE_RESOURCE_WORKER = "MysqlClusterChangeResourceWorker";
    public final static String NODE_DELETE_WORKER = "MysqlNodeDeleteWorker";
    public final static String NODE_START_WORKER = "MysqlNodeStartWorker";
    public final static String NODE_STOP_WORKER = "MysqlNodeStopWorker";

    /**
     *  healthcheckKey
     */
    public final static String LIVENESS_DELAY_TIMEOUT = "LivenessDelayTimeout";
    public final static String LIVENESS_FAILURE_THRESHOLD = "LivenessFailureThreshold";
    public final static String READINESS_DELAY_TIMEOUT = "ReadinessDelayTimeout";
    public final static String READINESS_FAILURE_THRESHOLD = "ReadinessFailureThreshold";
    
    /**
     *  healthcheck
     */
    public static final int HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT = 100;
    public static final int HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD = 100;
    public static final int HEALTH_CHECK_READINESS_DELAY_TIMEOUT = 100;
    public static final int HEALTH_CHECK_READINESS_FAILURE_THRESHOLD = 100;

    /**
     *  backup
     */
    public static final Double MYSQL_BACKUP_CONTAINER_DEFAULT_CPU = 1.0;
    public static final Double MYSQL_BACKUP_CONTAINER_DEFAULT_MEMORY = 1.0;

    public static final List<String> MYSQL_TYPE_LIST = Arrays.asList(TYPE_MS, TYPE_MM, TYPE_SINGLE);

    public static List<String> getMysqlTypeList() {
        return MYSQL_TYPE_LIST;
    }
    
    public static final List<String> HEALTH_CHECK_CONFIG_LIST = Arrays.asList(LIVENESS_DELAY_TIMEOUT, LIVENESS_FAILURE_THRESHOLD, READINESS_DELAY_TIMEOUT, READINESS_FAILURE_THRESHOLD);

    public static List<String> getHealthCheckConfigList() {
        return HEALTH_CHECK_CONFIG_LIST;
    }
    
}
