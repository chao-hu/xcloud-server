package com.xxx.xcloud.module.component.consts;

/**
 * @ClassName: KafkaClusterConst
 * @Description: kafka常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class KafkaClusterConst {

    /**
     * 资源类型
     */
    public static final String KIND = "KafkaCluster";

    /**
     * 资源版本
     */
    public static final String API_VERSION = "kafka.bonc.com/v1beta1";
    public static final String KAFKA_CLUSTER_CRDS = "kafkaclusters.kafka.bonc.com";

    /**
     * kafka集群处理Worker
     */
    public final static String CLUSTER_CREATE_WORKER = "KafkaClusterCreateWorker";
    public final static String CLUSTER_START_WORKER = "KafkaClusterStartWorker";
    public final static String CLUSTER_STOP_WORKER = "KafkaClusterStopWorker";
    public final static String CLUSTER_DELETE_WORKER = "KafkaClusterDeleteWorker";
    public final static String CLUSTER_EXPAND_NODE_WORKER = "KafkaClusterExpandNodeWorker";
    public final static String CLUSTER_CHANGE_RESOURCE_WORKER = "KafkaClusterChangeResourceWorker";

    public final static String NODE_START_WORKER = "KafkaNodeStartWorker";
    public final static String NODE_STOP_WORKER = "KafkaNodeStopWorker";
    public final static String NODE_DELETE_WORKER = "KafkaNodeDeleteWorker";

    /**
     * Kafka操作类型
     */
    public static final String OPERATOR_CLUSTER_CREATE = "ClusterCreate";
    public static final String OPERATOR_CLUSTER_STOP = "ClusterStop";
    public static final String OPERATOR_CLUSTER_START = "ClusterStart";
    public static final String OPERATOR_CLUSTER_EXPAND = "AddNode";
    public static final String OPERATOR_CHANGE_CONFIG = "ChangeConfig";
    public static final String OPERATOR_CHANGE_RESOURCE = "ChangeResource";

    public static final String OPERATOR_NODE_STOP = "NodeStop";
    public static final String OPERATOR_NODE_START = "NodeStart";
    public static final String OPERATOR_NODE_DELETE = "NodeDelete";

    /**
     * zk连接串
     */
    public static final String ZOOKEEPER_SERVERS = "zookeeper.connect";
}
