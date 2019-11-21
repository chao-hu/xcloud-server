package com.xxx.xcloud.module.component.consts;

/**
 * @ClassName: CommonConst
 * @Description: 组件公共常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CommonConst {

    /**
     * @Fields: APPTYPE
     */
    public static final String APPTYPE_MYSQL = "mysql";
    public static final String APPTYPE_ES = "es";
    public static final String APPTYPE_ZK = "zookeeper";
    public static final String APPTYPE_FTP = "ftp";
    public static final String APPTYPE_KAFKA = "kafka";
    public static final String APPTYPE_STORM = "storm";
    public static final String APPTYPE_PROMETHEUS = "prometheus";
    public static final String APPTYPE_MEMCACHED = "memcached";
    public static final String APPTYPE_REDIS = "redis";
    public static final String APPTYPE_CODIS = "codis";
    public static final String APPTYPE_POSTGRESQL = "postgresql";
    public static final String APPTYPE_YARN = "yarn";

    /**
     * 集群、节点状态
     */
    public final static String STATE_CLUSTER_RUNNING = "Running";
    public final static String STATE_CLUSTER_STOPPED = "Stopped";
    public final static String STATE_CLUSTER_WAITING = "Waiting";
    public final static String STATE_CLUSTER_FAILED = "Failed";
    public final static String STATE_CLUSTER_DELETED = "Deleted";
    public final static String STATE_CLUSTER_WARNING = "Warning";
    public final static String STATE_CLUSTER_PENDING = "Pending";
    public final static String STATE_CLUSTER_SCALING = "Scaling";
    public final static String STATE_CLUSTER_TERMINATING = "Terminating";
    public final static String STATE_CLUSTER_STARTING = "Starting";
    public final static String STATE_CLUSTER_STOPPING = "Stopping";

    public final static String STATE_NODE_RUNNING = "Running";
    public final static String STATE_NODE_STOPPED = "Stopped";
    public final static String STATE_NODE_WAITING = "Waiting";
    public final static String STATE_NODE_FAILED = "Failed";
    public final static String STATE_NODE_DELETED = "Deleted";
    public final static String STATE_NODE_INITIATED = "Initiated";
    public final static String STATE_NODE_WARNING = "Warning";
    public final static String STATE_NODE_INIT = "Init";
    public final static String STATE_NODE_UNKNOWN = "Unknow";
    public final static String STATE_NODE_ONLINE = "Online";
    public final static String STATE_NODE_PENDING = "Pending";
    public final static String STATE_NODE_DELETEING = "Deleteing";
    public final static String STATE_NODE_STOPPING = "Stopping";
    public final static String STATE_NODE_STARTING = "Starting";
    public final static String STATE_NODE_TERMINATING = "Terminating";

    /**
     * redis、codis、memcached使用的操作类型
     */
    public static final String OPT_CLUSTER_STOP = "Stop";
    public static final String OPT_CLUSTER_START = "Start";

    /**
     * 定义statefulService的latestAction
     */
    public static final String ACTION_CLUSTER_CREATE = "ClusterCreate";
    public static final String ACTION_CLUSTER_STOP = "ClusterStop";
    public static final String ACTION_CLUSTER_START = "ClusterStart";
    public static final String ACTION_CLUSTER_DELETE = "ClusterDelete";
    public static final String ACTION_CLUSTER_EXPAND = "ClusterExpand";
    public static final String ACTION_CLUSTER_CHANGERESOURCE = "ClusterChangeResource";
    public static final String ACTION_CLUSTER_CHANGECONFIG = "ClusterChangeConfig";
    public static final String ACTION_NODE_START = "NodeStart";
    public static final String ACTION_NODE_STOP = "NodeStop";
    public static final String ACTION_NODE_DELETE = "NodeDelete";
    public static final String ACTION_USER_ADD = "UserAdd";
    public static final String ACTION_USER_UPDATE = "UserUpdate";
    public static final String ACTION_USER_DELETE = "UserDelete";

    /**
     * 字段校验
     */
    public static final String CHECK_CLUSTER_NAME = "^[a-zA-Z][a-zA-Z0-9]{4,14}[a-zA-Z0-9]$";
    public static final String CHECK_USER_PASSSWORD = "^[a-zA-Z0-9]{6,16}$";
    public static final String CHECK_USER_NAME = "^[a-zA-Z0-9]{6,16}$";
    public static final String CHECK_CLUSTER_REPLICAS = "^[1-9]*[1-9][0-9]*$";

    public static final String CHECK_HEALTH_CONFIG = "^[1-9]\\d*$";

    public static final String CHECK_RESOURCE_CPU = "^[0-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_RESOURCE_MEMORY = "^[1-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_RESOURCE_CAPACITY = "^[1-9]\\d*(\\.\\d+)?$";

    public static final String CHECK_REDIS_SENTINEL_CPU = "^[0-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_REDIS_SENTINEL_MEMORY = "^[0-9]\\d*(\\.\\d+)?$";

    /**
     * 资源、角色
     */
    public static final String CPU = "cpu"; 
    public static final String MEMORY = "memory";

    public static final String UNIT_GI = "Gi";
    public static final String UNIT_MI = "Mi";

    public static final String ROLE_MASTER = "master";
    public static final String ROLE_SLAVE = "slave";

    public static final String NODESELECTOR_PERFORMANCE = "performance";

    public static final String EXPORTER = "exporter";

    public static final int SHOW_LEVEL_UNABLE = 0;
    public static final int SHOW_LEVEL_ORDINARY = 1;
    public static final int SHOW_LEVEL_SENIOR = 2;

    /**
     * lvm相关
     */
    public static final String LVM_API_VERSION = "bonc.com/v1";
    public static final String LVM_KIND = "LVM";
    public static final String LVM_CRDS = "lvms.bonc.com";
    public static final String LVM_SCHEDULER = "lvm-scheduler";

    /**
     * 线程休眠时间、超时时间
     */
    public static final int THREAD_SLEEP_TIME = 5000;
    public static final int COMPONENT_OPERATION_TIMEOUT = 300000;

    public static final Long SECOND = 1000L;
    public static final Long MINUTE = SECOND * 60;
    public static final Long HOUR = MINUTE * 60;
    public static final Long DAY = HOUR * 24;

    /**
     * 参数显示等级
     */
    public static final String LEVEL_ORDINARY = "ordinary";
    public static final String LEVEL_SENIOR = "senior";
    public static final String EXPORTER_STRING = "exporter";

    /**
     * @Fields: 数字类
     */
    public static final int NUMBER_ZERO = 0;
    public static final int NUMBER_ONE = 1;
    public static final int NUMBER_TWO = 2;
    public static final int NUMBER_FIVE = 5;
    public static final int NUMBER_TEN = 10;
    public static final int NUMBER_TWELVE = 12;
    public static final int NUMBER_THIRTY = 30;
    public static final int NUMBER_SIXTY_FOUR = 64;

    /**
     * 项目id和订购id
     */
    public static final String LABELS_MONITOR_PROJCETID = "monitor_projcet";
    public static final String LABELS_MONITOR_ORDER = "monitor_order";
    public static final String CONFIG_EFFECTIVE = "configEffective";
    public static final String RESOURCE_EFFECTIVE = "resourceEffective";
    public static final String RESOURCE_OR_CONFIG_EFFECTIVE = "resourceOrConfigEffective";
    public static final String EFFECTIVE_TRUE = "true";
    public static final String EFFECTIVE_FALSE = "false";

    public static final String[] FRAMEWORK_TYPE = { "hftp", "hive", "es", "prometheus", "spark", "postgresql",
            "configbus", "eureka", "zuul" };
    public static final String[] FRAMEWORK_TYPE_SERVER = { "storm", "kafka", "redis", "codis", "memcached", "ftp",
            "mysql" };

}
