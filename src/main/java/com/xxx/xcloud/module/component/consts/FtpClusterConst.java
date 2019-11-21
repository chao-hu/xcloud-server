package com.xxx.xcloud.module.component.consts;

/**
 * @ClassName: FtpClusterConst
 * @Description: ftp常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpClusterConst {

    /**
     * 资源类型
     */
    public static final String KIND = "FtpCluster";

    /**
     * 用户配置前缀
     */
    public static final String USER_CONFIG_PREFIX = "ftpserver.user";

    /**
     * 用户配置后缀
     */

    public static final String USER_CONFIG_SUFFIX_USR = "username";
    public static final String USER_CONFIG_SUFFIX_PWD = "userpassword";

    public static final String USER_CONFIG_SUFFIX_DIR = "homedirectory";
    public static final String USER_CONFIG_SUFFIX_ENABLE = "enableflag";
    public static final String USER_CONFIG_SUFFIX_ISWRITE = "writepermission";

    public static final String USER_CONFIG_SUFFIX_MAXLOGINNUMBER = "maxloginnumber";
    public static final String USER_CONFIG_SUFFIX_MAXLOGINPERIP = "maxloginperip";
    public static final String USER_CONFIG_SUFFIX_IDLETIME = "idletime";
    public static final String USER_CONFIG_SUFFIX_UPLOADRATE = "uploadrate";
    public static final String USER_CONFIG_SUFFIX_DOWNLOADRATE = "downloadrate";

    public static final String FTP_USER_CONFIG_FILE = "users.properties";

    /**
     * 资源版本
     */
    public static final String API_VERSION = "ftp.bonc.com/v1beta1";
    public static final String FTP_CLUSTER_CRDS = "ftpclusters.ftp.bonc.com";

    /**
     * ftp集群处理Worker
     */
    public final static String CLUSTER_CREATE_WORKER = "FtpClusterCreateWorker";
    public final static String CLUSTER_START_WORKER = "FtpClusterStartWorker";
    public final static String CLUSTER_STOP_WORKER = "FtpClusterStopWorker";
    public final static String CLUSTER_DELETE_WORKER = "FtpClusterDeleteWorker";
    public final static String CLUSTER_CHANGE_RESOURCE_WORKER = "FtpClusterChangeResourceWorker";

    public final static String NODE_START_WORKER = "FtpNodeStartWorker";
    public final static String NODE_STOP_WORKER = "FtpNodeStopWorker";
    public final static String NODE_DELETE_WORKER = "FtpNodeDeleteWorker";

    /**
     * Ftp操作类型
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

    public static final String OPERATOR_USER_ADD = "UserAdd";
    public static final String OPERATOR_USER_UPDATE = "UserUpdate";
    public static final String OPERATOR_USER_DELETE = "UserDelete";

    /**
     * 符号点
     */
    public static final String DOT_STRING = ".";

    /**
     * 用户属性名称对应的字符串
     */
    public static final String USER_USERNAME = "userName";
    public static final String USER_PWD = "password";
    public static final String USER_STATUS = "status";
    public static final String USER_PERMISSION = "permission";
    public static final String USER_DIRECTORY = "directory";
    public static final int FTP_REPLICAS_DEFAULT = 1;

    public static final String USER_PERMISSION_DEFAULT = "RW";
    public static final int USER_STATUS_DEFAULT = 1;
    public static final String EXTENDED_FIELD_FTPUSER = "ftpUser";

    /**
     * ftp节点端口
     */
    public static final String ACCESS_PORT = "accessPort";
    public static final String PASSIVE_PORT = "passivePort";

}
