/**
 *
 */
package com.xxx.xcloud.common;

import java.util.Arrays;
import java.util.List;

/**
 * @author ruzz
 *
 */
public class Global {

    /**
     *  @Fields: 本项目默认创建的系统租户
     */
    public static final String DEFAULT_SYSTEM_TENANT = "bdos-admin";
    /**
     *  @Fields: 租户名称校验规则
     */
    public static final String CHECK_TENANT_NAME = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    /**
     *  @Fields: 镜像名称校验规则
     */
    public static final String CHECK_IMAGE_NAME = "^[a-z][a-z0-9-_]*$";
    /**
     *  @Fields: 服务名称校验规则
     */
    public static final String CHECK_SERVICE_NAME = "[a-z]([-a-z0-9]*[a-z0-9])?";
    /**
     *  @Fields: 存储卷名称校验规则
     */
    public static final String CHECK_STORAGE_NAME = "[a-z]([-a-z0-9]*[a-z0-9])?";
    /**
     *  @Fields: 定时任务名称校验规则
     */
    public static final String CHECK_CRONJOB_NAME = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    /**
     *  @Fields: 配置文件模板名称校验规则
     */
    public static final String CHECK_CONFIGMAP_NAME = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    /**
     *  @Fields: dockerfile模版名称验证
     */
    public static final String CHECK_DOCKERFILE_TEMPLATE_NAME = "^[a-zA-Z][a-zA-Z0-9-/.]*$";
    /**
     *  @Fields: 构建语言验证
     */
    public static final String CHECK_CI_LANG = "^java|go|python$";
    /**
     *  @Fields: 用于指定服务创建于集群内特定label的节点上的type类别
     */
    public static final String SELECTOR_LABEL_SERVICE = "service";

    /**
     *  @Fields: 域名校验规则
     */
    public static final String CHECK_DOMAIN_NAME = "^[a-zA-Z0-9][a-zA-Z0-9]{0,62}(.[a-zA-Z0-9][a-zA-Z0-9]{0,62}){1,64}$";

    /**
     *  @Fields: harbor 用户在project中的角色 admin
     */
    public static final int HARBOR_PROJECT_ROLE_ADMIN = 1;
    /**
     * @Fields: harbor 用户在project中的角色 developer
     */
    public static final int HARBOR_PROJECT_ROLE_DEVELOPER = 2;
    /**
     * @Fields: harbor 用户在project中的角色 visitor
     */
    public static final int HARBOR_PROJECT_ROLE_VISITOR = 3;

    /**
     *  @Fields: 更新配额信息操作类型 总的
     */
    public static final int TENANT_CHANGE_TOTAL_RESOURCE = 1;
    /**
     *  @Fields: 更新配额信息操作类型 可用的
     */
    public static final int TENANT_CHANGE_USED_RESOURCE = 2;

    /**
     *  服务状态码和定时任务状态码统一定义
     */
    /**
     * @Fields: 未启动
     */
    public static final byte OPERATION_UNSTART = 1;
    /**
     * @Fields: 启动中
     */
    public static final byte OPERATION_STARTING = 2;
    /**
     * @Fields: 运行中
     */
    public static final byte OPERATION_RUNNING = 3;
    /**
     * @Fields: 已停止
     */
    public static final byte OPERATION_STOPPED = 4;
    /**
     * @Fields: 升级中
     */
    public static final byte OPERATION_UPDATING = 5;
    /**
     * @Fields: 启动失败
     */
    public static final byte OPERATION_START_FAILED = 6;
    /**
     * @Fields: 升级失败
     */
    public static final byte OPERATION_UPDATE_FAILED = 7;

    public static final String DEVOPS_PLUGIN_GRADLE = "DEVOPS_PLUGIN_GRADLE";// DEVOPSGRADLE插件版本

    /**
     *  服务端口协议
     */
    /**
     * @Fields: TCP
     */
    public static final String SERVICE_PORT_TCP = "TCP";
    /**
     * @Fields: UDP
     */
    public static final String SERVICE_PORT_UDP = "UDP";

    /**
     *  服务亲和状态
     */
    /**
     * @Fields: 不使用亲和性
     */
    public static final byte NOT_USE_AFFINITY = 0;
    /**
     * @Fields: 亲和
     */
    public static final byte AFFINITY = 1;
    /**
     * @Fields: 反亲和
     */
    public static final byte ANTI_AFFINITY = 2;

    /**
     *  服务健康检查探针类型
     */
    /**
     * @Fields: livenessProbe(运行时)
     */
    public static final byte HEALTHCHECK_PROBE_LIVENESS = 1;
    /**
     * @Fields: readinessProbe(启动时)
     */
    public static final byte HEALTHCHECK_PROBE_READINESS = 2;

    /**
     *  容器生命周期钩子类型
     */
    /**
     * @Fields: postStart(容器创建后运行前)
     */
    public static final byte CONTAINER_LIFECYCLE_POSTSTART = 1;
    /**
     * @Fields: preStop(容器终止前)
     */
    public static final byte CONTAINER_LIFECYCLE_PRESTOP = 2;

    /**
     *  域名创建类型
     */
    /**
     * @Fields: 一级域名
     */
    public static final String DOMAIN_TLD = "TLD";
    /**
     * @Fields: 二级域名
     */
    public static final String DOMAIN_SLD = "SLD";

    /**
     * 是否使用域名高级参数
     */
    /**
     * @Fields: 不使用域名高级参数
     */
    public static final byte DOMAIN_NOT_USE_CONFIG = 0;
    /**
     * @Fields: 使用域名高级参数
     */
    public static final byte DOMAIN_USE_CONFIG = 1;

    /**
     *  是否使用https
     */
    /**
     * @Fields: 不使用https
     */
    public static final byte DOMAIN_NOT_USE_HTTPS = 0;
    /**
     * @Fields: 使用https
     */
    public static final byte DOMAIN_USE_HTTPS = 1;

    /**
     *  service 操作类型
     */
    /**
     * @Fields: 服务停止
     */
    public static final String SERVICE_STOP = "stop";
    /**
     * @Fields: 服务启动
     */
    public static final String SERVICE_START = "start";
    /**
     * @Fields: 更新镜像
     */
    public static final String SERVICE_UPGRADE = "upgrade";
    /**
     * @Fields: 资源变更
     */
    public static final String SERVICE_MODIFY = "modify";
    /**
     * @Fields: 自动伸缩
     */
    public static final String SERVICE_HPA = "hpa";
    /**
     * @Fields: 弹性伸缩
     */
    public static final String SERVICE_SCALE = "scale";

    /**
     * @Fields: cron 操作 停止
     */
    public static final String CRON_STOP = "stop";
    /**
     * @Fields: cron 操作 启动
     */
    public static final String CRON_START = "start";
    /**
     * @Fields: cron 操作 修改
     */
    public static final String CRON_MODIFY = "modify";

    /**
     * @Fields: cron操作列表
     */
    private static final List<String> CRON_TYPE_CODES = Arrays.asList(CRON_STOP, CRON_START, CRON_MODIFY);

    public static final String INITCONTAINER_SERVICE_DEPENDENCY = "/library/service-dependency:v1.0";

    /**
     * @Title: getCronTypeCodes
     * @Description: 获取 cron操作类型列表
     * @param @return 参数
     * @return List<String> 返回类型
     * @throws
     */
    public static List<String> getCronTypeCodes() {
        return CRON_TYPE_CODES;
    }

    /**
     * @Fields: ingress域
     */
    private static final List<String> INGRESS_DOMAIN_TYPE = Arrays.asList("cn", "ren", "wang", "citic", "top", "sohu",
            "xin", "com", "net", "xyz", "vip", "work", "law", "beer", "club", "shop", "site", "ink", "info", "mobi",
            "red", "pro", "kim", "ltd", "group", "auto", "link", "biz", "fun", "online", "store", "tech", "art",
            "design", "wiki", "love", "center", "video", "social", "team", "show", "cool", "zone", "world", "today",
            "city", "chat", "company", "live", "fund", "gold", "plus", "guru", "run", "pub", "email", "life");

    /**
     * @Title: getIngressDomainType
     * @Description: 获取ingress域名类型列表
     * @param @return 参数
     * @return List<String> 返回类型
     * @throws
     */
    public static List<String> getIngressDomainType() {
        return INGRESS_DOMAIN_TYPE;
    }

    /**
     * @Fields: 检查pod运行状态时，租户名称和服务名称的连接符
     */
    public static final String CONCATENATE = "_";

    /**
     * @Fields: 是否启用ceph
     */
    public static final String CEPH_ENABLE = "CEPH_ENABLE";
    /**
     * @Fields: ceph对象访问密钥
     */
    public static final String CEPH_RGW_ADMIN_ACCESSKEY = "CEPH_RGW_ADMIN_ACCESSKEY";
    /**
     * @Fields: ceph对象密钥
     */
    public static final String CEPH_RGW_ADMIN_SECRETKEY = "CEPH_RGW_ADMIN_SECRETKEY";
    /**
     * @Fields: LVM名称
     */
    public static final String LVM_VGNAME = "LVM_VGNAME";
    /**
     * @Fields: 节点筛选是否开启
     */
    public static final String NODESELECTOR_COMPONENT = "NODESELECTOR_COMPONENT";
    /**
     * @Fields: 节点高低性能筛选是否开启
     */
    public static final String NODESELECTOR_PERFORMANCE = "NODESELECTOR_PERFORMANCE";
    /**
     * @Fields: lvm调度是否开启
     */
    public static final String COMPONENT_SCHEDULER_LVM = "COMPONENT_SCHEDULER_LVM";
    /**
     * @Fields: CPU最小和需求比例
     */
    public static final String RATIO_LIMITTOREQUESTCPU = "RATIO_LIMITTOREQUESTCPU";
    /**
     * @Fields: 内存最小和需求比例
     */
    public static final String RATIO_LIMITTOREQUESTMEMORY = "RATIO_LIMITTOREQUESTMEMORY";
    /**
     * @Fields: ETCDAPI服务地址
     */
    public static final String ETCD_API_ADDRESS = "ETCD_API_ADDRESS";
    /**
     * @Fields: FTPIP地址
     */
    public static final String FTP_HOST = "FTP_HOST";
    /**
     * @Fields: ceph挂载命令
     */
    public static final String CEPH_SSH_MOUNTEXEC = "CEPH_SSH_MOUNTEXEC";
    /**
     * @Fields: FTP端口
     */
    public static final String FTP_PORT = "FTP_PORT";
    /**
     * @Fields: FTP用户名称
     */
    public static final String FTP_USERNAME = "FTP_USERNAME";
    /**
     * @Fields: FTP密码
     */
    public static final String FTP_PASSWORD = "FTP_PASSWORD";
    /**
     * @Fields: FTP主路径
     */
    public static final String FTP_PATH = "FTP_PATH";
    /**
     * @Fields: BDOS名称
     */
    public static final String BDOS_NAME = "BDOS_NAME";
    /**
     * @Fields: BDOS路径
     */
    public static final String BDOS_ADDRESS = "BDOS_ADDRESS";
    /**
     * @Fields: 组件是否启用项目ID
     */
    public static final String COMPONENT_LABELS_PROJECTID = "COMPONENT_LABELS_PROJECTID";
    /**
     * @Fields: 组件是否启用订购ID
     */
    public static final String COMPONENT_LABELS_ORDERID = "COMPONENT_LABELS_ORDERID";
    /**
     * @Fields: DOCKER终端版本
     */
    public static final String DOCKER_DAEMON_APIVERSION = "DOCKER_DAEMON_APIVERSION";
    /**
     * @Fields: DOCKER端口
     */
    public static final String DOCKER_DAEMON_PORT = "DOCKER_DAEMON_PORT";
    /**
     * @Fields: ceph挂载路径
     */
    public static final String CEPH_SSH_MOUNTPOINT = "CEPH_SSH_MOUNTPOINT";
    /**
     * @Fields: DOCKER地址
     */
    public static final String DOCKER_URL = "DOCKER_URL";
    /**
     * @Fields: DOCKERAPI版本
     */
    public static final String DOCKER_API_VERSION = "DOCKER_API_VERSION";
    /**
     * @Fields: HARBOR管理员
     */
    public static final String HARBOR_USERNAME = "HARBOR_USERNAME";
    /**
     * @Fields: HARBOR密码
     */
    public static final String HARBOR_PASSWORD = "HARBOR_PASSWORD";
    /**
     * @Fields: HARBORemail地址
     */
    public static final String HARBOR_EMAIL = "HARBOR_EMAIL";
    /**
     * @Fields: HARBOR证书地址
     */
    public static final String HARBOR_CRT_PATH = "HARBOR_CRT_PATH";
    /**
     * @Fields: HARBOR公共镜像仓库名称
     */
    public static final String HARBOR_PUBLIC_PROJECT_NAME = "HARBOR_PUBLIC_PROJECT_NAME";
    /**
     * @Fields: HARBOR访问地址
     */
    public static final String HARBOR_REGISTRY_ADDRESS = "HARBOR_REGISTRY_ADDRESS";
    /**
     * @Fields: 构建镜像缓存存放地址
     */
    public static final String CI_IMAGE_TEMP_PATH = "CI_IMAGE_TEMP_PATH";
    /**
     * @Fields: ceph配置地址
     */
    public static final String CEPH_SSH_CEPHDIR = "CEPH_SSH_CEPHDIR";
    /**
     * @Fields: pinpoint服务IP
     */
    public static final String PINPOINT_SERVER_IP = "PINPOINT_SERVER_IP";
    /**
     * @Fields: pinpoint节点元数据上报端口
     */
    public static final String PINPOINT_TCP_PORT = "PINPOINT_TCP_PORT";
    /**
     * @Fields: pinpoint节点汇总数据上报端口
     */
    public static final String PINPOINT_STAT_PORT = "PINPOINT_STAT_PORT";
    /**
     * @Fields: pinpoint节点明细数据上报端口
     */
    public static final String PINPOINT_SPAN_PORT = "PINPOINT_SPAN_PORT";

    /**
     * @Fields: SONAR用户名
     */
    public static final String SONAR_USER_NAME = "SONAR_USER_NAME";
    /**
     * @Fields: SONAR地址
     */
    public static final String SONAR_URL = "SONAR_URL";
    /**
     * @Fields: SONAR 用户密码
     */
    public static final String SONAR_USER_PWD = "SONAR_USER_PWD";
    /**
     * @Fields: SONAR系统规则集
     */
    public static final String SONAR_QUALITYFILE_NAME_SYSTEM = "SONAR_QUALITYFILE_NAME_SYSTEM";
    /**
     * @Fields: GITHUBAPI地址
     */
    public static final String GITHUB_API_URL = "GITHUB_API_URL";
    /**
     * @Fields: ceph配置名称
     */
    public static final String CEPH_CONF = "CEPH_CONF";
    /**
     * @Fields: GITHUB地址
     */
    public static final String GITHUB_CODE_URL = "GITHUB_CODE_URL";
    /**
     * @Fields: k8s主节点URL
     */
    public static final String KUBENETES_MASTER_URL = "KUBENETES_MASTER_URL";
    /**
     * @Fields: DEVOPSGOPATH
     */
    public static final String DEVOPS_GO_SHELL_CMD_PRE = "DEVOPS_GO_SHELL_CMD_PRE";
    /**
     * @Fields: DEVOPS参数
     */
    public static final String DEVOPS_PARAM = "DEVOPS_PARAM";
    /**
     * @Fields: DEVOPS地址
     */
    public static final String DEVOPS_URL = "DEVOPS_URL";
    /**
     * @Fields: DEVOPS管理员
     */
    public static final String DEVOPS_ADMIN = "DEVOPS_ADMIN";
    /**
     * @Fields: DEVOPSTOKEN
     */
    public static final String DEVOPS_TOKEN = "DEVOPS_TOKEN";
    /**
     * @Fields: DEVOPSJDK配置
     */
    public static final String DEVOPS_GLOBAL_CONFIGURE_JDK = "DEVOPS_GLOBAL_CONFIGURE_JDK";
    /**
     * @Fields: DEVOPSMAVEN配置
     */
    public static final String DEVOPS_GLOBAL_CONFIGURE_MAVEN = "DEVOPS_GLOBAL_CONFIGURE_MAVEN";
    /**
     * @Fields: DEVOPSGO配置
     */
    public static final String DEVOPS_GLOBAL_CONFIGURE_GO = "DEVOPS_GLOBAL_CONFIGURE_GO";
    /**
     * @Fields: ceph名称
     */
    public static final String CEPH_NAME = "CEPH_NAME";
    /**
     * @Fields: DEVOPSANT配置
     */
    public static final String DEVOPS_GLOBAL_CONFIGURE_ANT = "DEVOPS_GLOBAL_CONFIGURE_ANT";
    /**
     * @Fields: DEVOPSGIT插件版本
     */
    public static final String DEVOPS_PLUGIN_GIT = "DEVOPS_PLUGIN_GIT";
    /**
     * @Fields: DEVOPSSVN插件版本
     */
    public static final String DEVOPS_PLUGIN_SVN = "DEVOPS_PLUGIN_SVN";
    /**
     * @Fields: DEVOPSSONAR插件版本
     */
    public static final String DEVOPS_PLUGIN_SONAR = "DEVOPS_PLUGIN_SONAR";
    /**
     * @Fields: DEVOPSGO插件版本
     */
    public static final String DEVOPS_PLUGIN_GO = "DEVOPS_PLUGIN_GO";
    /**
     * @Fields: DEVOPSGITLAB插件版本
     */
    public static final String DEVOPS_PLUGIN_GITLAB = "DEVOPS_PLUGIN_GITLAB";
    /**
     * @Fields: DEVOPSANT插件版本
     */
    public static final String DEVOPS_PLUGIN_ANT = "DEVOPS_PLUGIN_ANT";
    /**
     * @Fields: DEVOPSPHING插件版本
     */
    public static final String DEVOPS_PLUGIN_PHING = "DEVOPS_PLUGIN_PHING";
    /**
     * @Fields: DEVOPSPYTHON插件版本
     */
    public static final String DEVOPS_PLUGIN_PYTHON = "DEVOPS_PLUGIN_PYTHON";
    /**
     * @Fields: DEVOPSSSH插件版本
     */
    public static final String DEVOPS_PLUGIN_SSH_CREDENTIAL = "DEVOPS_PLUGIN_SSH_CREDENTIAL";

    /**
     * @Fields: ceph监听地址
     */
    public static final String CEPH_MONITOR = "CEPH_MONITOR";
    /**
     * @Fields: DEVOPSDOCKER插件版本
     */
    public static final String DEVOPS_PLUGIN_DOCKER = "DEVOPS_PLUGIN_DOCKER";
    /**
     * @Fields: DEVOPSDOCKERCOMMONS插件版本
     */
    public static final String DEVOPS_PLUGIN_DOCKER_COMMONS = "DEVOPS_PLUGIN_DOCKER_COMMONS";
    /**
     * @Fields: ceph密钥
     */
    public static final String CEPH_KEY = "CEPH_KEY";
    /**
     * @Fields: ceph对象访问地址
     */
    public static final String CEPH_RGW_ENDPOINT = "CEPH_RGW_ENDPOINT";
    /**
     * @Fields: 构建线程池核心线程数
     */
    public static final String CI_THREAD_POOL_CORE_SIZE = "CI_THREAD_POOL_CORE_SIZE";
    /**
     * @Fields: 构建线程池最大线程数
     */
    public static final String CI_THREAD_POOL_MAX_SIZE = "CI_THREAD_POOL_MAX_SIZE";
    /**
     * @Fields: DOCKER终端URL
     */
    public static final String DOCKER_WEBSOCKET_INTERCEPTURL = "/ws/container/exec";

    /**
     * @Fields: 默认系统ip
     */
    public static final String DEFAULT_SYSTEM_IP = "127.0.0.1";

    /**
     * NodeJS plugin version not language version.
     */
    public static final String DEVOPS_PLUGIN_NODEJS = "DEVOPS_PLUGIN_NODEJS";

    /**
     * / XCLOUD结果校验间隔
     * 不仅仅是SheRa
     */
    public static final String XCLOUD_CI_CHECK_RESULT_INTERVAL_TIME = "XCLOUD_CI_CHECK_RESULT_INTERVAL_TIME";

    /**
     * XCLOUD超时次数
     */
    public static final String XCLOUD_CI_CHECK_RESULT_TIMEOUT_COUNT = "XCLOUD_CI_CHECK_RESULT_TIMEOUT_COUNT";

    public static final String QUERY_SONAR_RESULT_INTERVAL_TIME_AFTER_JENKINS_FINISH = "QUERY_SONAR_RESULT_INTERVAL_TIME_AFTER_JENKINS_FINISH";

    // 是否使用灰度发布
    /**
     * 不使用
     */
    public static final byte DOMAIN_NOT_USE_CANARY = 0;
    /**
     * 使用
     */
    public static final byte DOMAIN_USE_CANARY = 1;

    /**
     * 挂载存储卷的服务类型
     */
    public static final String MOUNTCEPHFILE_SERVICE = "service";
    public static final String MOUNTCEPHFILE_CONFIGBUS = "springcloud-configbus";


    public static final String DEVOPS_GLOBAL_CONFIGURE_NODEJS = "DEVOPS_GLOBAL_CONFIGURE_NODEJS";
    public static final String DEVOPS_GLOBAL_CONFIGURE_PYTHON = "DEVOPS_GLOBAL_CONFIGURE_PYTHON";
}
