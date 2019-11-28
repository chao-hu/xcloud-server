package com.xxx.xcloud.module.springcloud.consts;

/**
 * 组件公有的常量
 *
 * @author LiuYue
 * @date 2018年12月12日
 */
public class SpringCloudCommonConst {

    public static final String APPTYPE_APP = "app";
    // 服务类型
    public static final String APPTYPE_EUREKA = "eureka";
    public static final String APPTYPE_CONFIG_BUS = "configbus";
    public static final String APPTYPE_RABBITMQ = "mq";
    public static final String APPTYPE_ZUUL = "zuul";

    // 资源类型
    public static final String RESOURCE_CPU = "cpu";
    public static final String RESOURCE_MEMORY = "memory";
    public static final String RESOURCE_STORAGE = "storage";
    public static final String RESOURCE_NODE_NUM = "nodeNum";

    // 应用操作
    public final static String OPERATOR_CREATE = "create";
    public final static String OPERATOR_STOP = "stop";
    public final static String OPERATOR_START = "satet";
    public final static String OPERATOR_DELETE = "delete";

    // 应用状态
    public final static String STATE_APP_RUNNING = "Running";
    public final static String STATE_APP_STOPPED = "Stopped";
    public final static String STATE_APP_WAITING = "Waiting";
    public final static String STATE_APP_FAILED = "Failed";
    public final static String STATE_APP_WARNING = "Warning";
    public final static String STATE_APP_DELETED = "Deleted";

    // service状态

    public final static String STATE_SERVICE_PENDING = "Pending";
    public final static String STATE_SERVICE_RUNNING = "Running";
    public final static String STATE_SERVICE_SUCCEEDED = "Succeeded";
    public final static String STATE_SERVICE_FAILED = "Failed";
    public final static String STATE_SERVICE_UNKNOWN = "Unknown";

    // 默认端口
    public static final int DEFAULT_EUREKA_INNER_PORT = 8761;
    public static final int DEFAULT_ZUUL_INNER_PORT = 8762;
    public static final int DEFAULT_CONFIGBUS_CONTAINERPORT = 8080;

    public static final int DEFAULT_REPLICAS = 1;
    public static final String DEFAULT_ENV_JAVA_OPTS = "JAVA_OPTS";
    public static final String DEFAULT_HA_EUREKA_ENV_VALUE = "--eureka.client.registerWithEureka=true --eureka.client.fetchRegistry=true";
    public static final String DEFAULT_SINGLE_EUREKA_ENV_VALUE = "--eureka.client.registerWithEureka=false --eureka.client.fetchRegistry=false";

    public static final String DEFAULT_CONFIGBUS_ENV_VALUE = "--server.port=8080  " + "--spring.profiles.active=native "
            + "--management.security.enabled=false " + "--spring.rabbitmq.port=5672 "
            + "--spring.rabbitmq.username=guest " + "--spring.rabbitmq.password=guest "
            + "--spring.cloud.config.server.git.force-pull=true";
    public static final int DEFAULT_RABBITMQ_CONTAINERPORT_ONE = 5672;
    public static final int DEFAULT_RABBITMQ_CONTAINERPORT_TWO = 15672;
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_USER = "guest";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_PASSWORD = "guest";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_CONFIG_URI = "configUri";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_HOST = "rabbitMqHost";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PORT = "rabbitMqPort";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_UI_PORT = "rabbitMqUiPort";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_PASSWORD = "rabbitMqPassword";
    public static final String DEFAULT_RABBITMQ_CONTAINERPORT_RABBITMQ_USER = "rabbitMqUser";
    // Harbor的Secret
    public static final String HARBOR_SECRET = "harbor-secret";
    // 文件挂载到configbus容器内部的地址
    public static final String CEPHFILEIDCONFIGBUS = "cephfileIdConfigbus";
    public static final String CEPHFS_MOUNTPATH = "/opt/files";

    public static final String PORT_PROTOCOL_TCP = "TCP";
    public static final String K8S_SERVICE_TYPE_NODEPORT = "NodePort";
    public static final String K8S_SERVICE_TYPE_CLUSTERIP = "ClusterIP";

    public static final String SERVICE_TYPE_HA = "HA";
    public static final String SERVICE_TYPE_SINGLE = "SINGLE";

    public static final int SERVICE_CONFIG_ENABLE = 1;
    public static final int SERVICE_CONFIG_DISABLE = 0;

    // 新的资源类型
    public static final String RESOURCE_EUREKA_CPU = "eureka-cpu";
    public static final String RESOURCE_EUREKA_MEMORY = "eureka-memory";
    public static final String RESOURCE_EUREKA_NODE_NUM = "eureka-nodeNum";

    public static final String RESOURCE_CONFIGBUS_CPU = "configbus-cpu";
    public static final String RESOURCE_CONFIGBUS_MEMORY = "configbus-memory";
    public static final String RESOURCE_CONFIGBUS_STORAGE = "configbus-storage";

    public static final String RESOURCE_ZUUL_CPU = "zuul-cpu";
    public static final String RESOURCE_ZUUL_MEMORY = "zuul-memory";

}
