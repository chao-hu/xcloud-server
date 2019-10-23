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

    // 本项目默认创建的系统租户
    public static final String DEFAULT_SYSTEM_TENANT = "bdos-admin";
    // 租户名称校验规则
    public static final String CHECK_TENANT_NAME = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    // 镜像名称校验规则
    public static final String CHECK_IMAGE_NAME = "^[a-z][a-z0-9-_]*$";
    // 服务名称校验规则
    public static final String CHECK_SERVICE_NAME = "[a-z]([-a-z0-9]*[a-z0-9])?";
    // 存储卷名称校验规则
    public static final String CHECK_STORAGE_NAME = "[a-z]([-a-z0-9]*[a-z0-9])?";
    // 定时任务名称校验规则
    public static final String CHECK_CRONJOB_NAME = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    // 配置文件模板名称校验规则
    public static final String CHECK_CONFIGMAP_NAME = "[a-z0-9]([-a-z0-9]*[a-z0-9])?";
    // dockerfile模版名称验证
    public static final String CHECK_DOCKERFILE_TEMPLATE_NAME = "^[a-zA-Z][a-zA-Z0-9-/.]*$";
    // 构建语言验证
    public static final String CHECK_CI_LANG = "^java|go|python$";
    // 用于指定服务创建于集群内特定label的节点上的type类别
    public static final String SELECTOR_LABEL_SERVICE = "service";

    // 域名校验规则
    public static final String CHECK_DOMAIN_NAME = "^[a-zA-Z0-9][a-zA-Z0-9]{0,62}(.[a-zA-Z0-9][a-zA-Z0-9]{0,62}){1,64}$";

    // harbor 用户在project中的角色
    public static final int HARBOR_PROJECT_ROLE_ADMIN = 1;
    public static final int HARBOR_PROJECT_ROLE_DEVELOPER = 2;
    public static final int HARBOR_PROJECT_ROLE_VISITOR = 3;

    // 更新配额信息操作类型
    public static final int TENANT_CHANGE_TOTAL_RESOURCE = 1; // 修改总配额
    public static final int TENANT_CHANGE_USED_RESOURCE = 2; // 修改可用配额

    // 服务状态码和定时任务状态码统一定义
    public static final byte OPERATION_UNSTART = 1; // 未启动
    public static final byte OPERATION_STARTING = 2; // 启动中
    public static final byte OPERATION_RUNNING = 3; // 运行中
    public static final byte OPERATION_STOPPED = 4; // 已停止
    public static final byte OPERATION_UPDATING = 5; // 升级中
    public static final byte OPERATION_START_FAILED = 6; // 启动失败
    public static final byte OPERATION_UPDATE_FAILED = 7; // 升级失败

    // 服务亲和状态
    public static final byte NOT_USE_AFFINITY = 0; // 不使用亲和性
    public static final byte AFFINITY = 1; // 亲和
    public static final byte ANTI_AFFINITY = 2; // 反亲和

    // 服务健康检查探针类型
    public static final byte HEALTHCHECK_PROBE_LIVENESS = 1; // livenessProbe(运行时)
    public static final byte HEALTHCHECK_PROBE_READINESS = 2; // readinessProbe(启动时)

    // 容器生命周期钩子类型
    public static final byte CONTAINER_LIFECYCLE_POSTSTART = 1; // postStart(容器创建后运行前)
    public static final byte CONTAINER_LIFECYCLE_PRESTOP = 2; // preStop(容器终止前)

    // 域名创建类型
    public static final String DOMAIN_TLD = "TLD"; // 一级域名
    public static final String DOMAIN_SLD = "SLD"; // 二级域名

    // 是否使用域名高级参数
    public static final byte DOMAIN_NOT_USE_CONFIG = 0; // 不使用
    public static final byte DOMAIN_USE_CONFIG = 1; // 使用

    // 是否使用https
    public static final byte DOMAIN_NOT_USE_HTTPS = 0; // 不使用
    public static final byte DOMAIN_USE_HTTPS = 1; // 使用

    // service 操作类型
    public static final String SERVICE_STOP = "stop"; // 服务停止
    public static final String SERVICE_START = "start"; // 服务启动
    public static final String SERVICE_UPGRADE = "upgrade"; // 更新镜像
    public static final String SERVICE_MODIFY = "modify"; // 资源变更
    public static final String SERVICE_HPA = "hpa"; // 自动伸缩
    public static final String SERVICE_SCALE = "scale"; // 弹性伸缩

    // cron 操作类型
    public static final String CRON_STOP = "stop"; // 停止
    public static final String CRON_START = "start"; // 启动
    public static final String CRON_MODIFY = "modify"; // 修改

    private static final List<String> CRON_TYPE_CODES = Arrays.asList(CRON_STOP, CRON_START, CRON_MODIFY);

    public static List<String> getCronTypeCodes() {
        return CRON_TYPE_CODES;
    }

    //
    private static final List<String> INGRESS_DOMAIN_TYPE = Arrays.asList("cn", "ren", "wang", "citic", "top", "sohu",
            "xin", "com", "net", "xyz", "vip", "work", "law", "beer", "club", "shop", "site", "ink", "info", "mobi",
            "red", "pro", "kim", "ltd", "group", "auto", "link", "biz", "fun", "online", "store", "tech", "art",
            "design", "wiki", "love", "center", "video", "social", "team", "show", "cool", "zone", "world", "today",
            "city", "chat", "company", "live", "fund", "gold", "plus", "guru", "run", "pub", "email", "life");

    public static List<String> getIngressDomainType() {
        return INGRESS_DOMAIN_TYPE;
    }

    // 检查pod运行状态时，租户名称和服务名称的连接符
    public static final String CONCATENATE = "_";

    public static final String CEPH_ENABLE = "CEPH_ENABLE";// 是否启用ceph
    public static final String CEPH_RGW_ADMIN_ACCESSKEY = "CEPH_RGW_ADMIN_ACCESSKEY";// ceph对象访问密钥
    public static final String CEPH_RGW_ADMIN_SECRETKEY = "CEPH_RGW_ADMIN_SECRETKEY";// ceph对象密钥
    public static final String LVM_VGNAME = "LVM_VGNAME";// LVM名称
    public static final String NODESELECTOR_COMPONENT = "NODESELECTOR_COMPONENT";// 节点筛选是否开启
    public static final String NODESELECTOR_PERFORMANCE = "NODESELECTOR_PERFORMANCE";// 节点高低性能筛选是否开启
    public static final String COMPONENT_SCHEDULER_LVM = "COMPONENT_SCHEDULER_LVM";// lvm调度是否开启
    public static final String RATIO_LIMITTOREQUESTCPU = "RATIO_LIMITTOREQUESTCPU";// CPU最小和需求比例
    public static final String RATIO_LIMITTOREQUESTMEMORY = "RATIO_LIMITTOREQUESTMEMORY";// 内存最小和需求比例
    public static final String ETCD_API_ADDRESS = "ETCD_API_ADDRESS";// ETCDAPI服务地址
    public static final String FTP_HOST = "FTP_HOST";// FTPIP地址
    public static final String CEPH_SSH_MOUNTEXEC = "CEPH_SSH_MOUNTEXEC";// ceph挂载命令
    public static final String FTP_PORT = "FTP_PORT";// FTP端口
    public static final String FTP_USERNAME = "FTP_USERNAME";// FTP用户名称
    public static final String FTP_PASSWORD = "FTP_PASSWORD";// FTP密码
    public static final String FTP_PATH = "FTP_PATH";// FTP主路径
    public static final String BDOS_NAME = "BDOS_NAME";// BDOS名称
    public static final String BDOS_ADDRESS = "BDOS_ADDRESS";// BDOS路径
    public static final String COMPONENT_LABELS_PROJECTID = "COMPONENT_LABELS_PROJECTID";// 组件是否启用项目ID
    public static final String COMPONENT_LABELS_ORDERID = "COMPONENT_LABELS_ORDERID";// 组件是否启用订购ID
    public static final String DOCKER_DAEMON_APIVERSION = "DOCKER_DAEMON_APIVERSION";// DOCKER终端版本
    public static final String DOCKER_DAEMON_PORT = "DOCKER_DAEMON_PORT";// DOCKER端口
    public static final String CEPH_SSH_MOUNTPOINT = "CEPH_SSH_MOUNTPOINT";// ceph挂载路径
    public static final String DOCKER_URL = "DOCKER_URL";// DOCKER地址
    public static final String DOCKER_API_VERSION = "DOCKER_API_VERSION";// DOCKERAPI版本
    public static final String HARBOR_USERNAME = "HARBOR_USERNAME";// HARBOR管理员
    public static final String HARBOR_PASSWORD = "HARBOR_PASSWORD";// HARBOR密码
    public static final String HARBOR_EMAIL = "HARBOR_EMAIL";// HARBORemail地址
    public static final String HARBOR_CRT_PATH = "HARBOR_CRT_PATH";// HARBOR证书地址
    public static final String HARBOR_PUBLIC_PROJECT_NAME = "HARBOR_PUBLIC_PROJECT_NAME";// HARBOR公共镜像仓库名称
    public static final String HARBOR_REGISTRY_ADDRESS = "HARBOR_REGISTRY_ADDRESS";// HARBOR访问地址
    public static final String CI_IMAGE_TEMP_PATH = "CI_IMAGE_TEMP_PATH";// 构建镜像缓存存放地址
    public static final String CEPH_SSH_CEPHDIR = "CEPH_SSH_CEPHDIR";// ceph配置地址
    public static final String PINPOINT_SERVER_IP = "PINPOINT_SERVER_IP";// pinpoint服务IP
    public static final String PINPOINT_TCP_PORT = "PINPOINT_TCP_PORT";// pinpoint节点元数据上报端口
    public static final String PINPOINT_STAT_PORT = "PINPOINT_STAT_PORT";// pinpoint节点汇总数据上报端口
    public static final String PINPOINT_SPAN_PORT = "PINPOINT_SPAN_PORT";// pinpoint节点明细数据上报端口
    public static final String SHERA_URL = "SHERA_URL";// SHERA地址
    public static final String SHERA_CI_CHECK_RESULT_INTERVAL_TIME = "SHERA_CI_CHECK_RESULT_INTERVAL_TIME";// SHERA结果校验间隔
    public static final String SHERA_CI_CHECK_RESULT_TIMEOUT_COUNT = "SHERA_CI_CHECK_RESULT_TIMEOUT_COUNT";// SHERA超时次数

    public static final String SONAR_USER_NAME = "SONAR_USER_NAME";// SONAR用户名
    public static final String SONAR_URL = "SONAR_URL";// SONAR地址
    public static final String SONAR_USER_PWD = "SONAR_USER_PWD";// SONAR 用户密码
    public static final String SONAR_QUALITYFILE_NAME_SYSTEM = "SONAR_QUALITYFILE_NAME_SYSTEM";// SONAR系统规则集
    public static final String GITHUB_API_URL = "GITHUB_API_URL";// GITHUBAPI地址
    public static final String CEPH_CONF = "CEPH_CONF";// ceph配置名称
    public static final String GITHUB_CODE_URL = "GITHUB_CODE_URL";// GITHUB地址
    public static final String KUBENETES_MASTER_URL = "KUBENETES_MASTER_URL";// k8s主节点URL
    public static final String DEVOPS_GO_SHELL_CMD_PRE = "DEVOPS_GO_SHELL_CMD_PRE";// DEVOPSGOPATH
    public static final String DEVOPS_PARAM = "DEVOPS_PARAM";// DEVOPS参数
    public static final String DEVOPS_URL = "DEVOPS_URL";// DEVOPS地址
    public static final String DEVOPS_ADMIN = "DEVOPS_ADMIN";// DEVOPS管理员
    public static final String DEVOPS_TOKEN = "DEVOPS_TOKEN";// DEVOPSTOKEN
    public static final String DEVOPS_GLOBAL_CONFIGURE_JDK = "DEVOPS_GLOBAL_CONFIGURE_JDK";// DEVOPSJDK配置
    public static final String DEVOPS_GLOBAL_CONFIGURE_MAVEN = "DEVOPS_GLOBAL_CONFIGURE_MAVEN";// DEVOPSMAVEN配置
    public static final String DEVOPS_GLOBAL_CONFIGURE_GO = "DEVOPS_GLOBAL_CONFIGURE_GO";// DEVOPSGO配置
    public static final String CEPH_NAME = "CEPH_NAME";// ceph名称
    public static final String DEVOPS_GLOBAL_CONFIGURE_ANT = "DEVOPS_GLOBAL_CONFIGURE_ANT";// DEVOPSANT配置
    public static final String DEVOPS_PLUGIN_GIT = "DEVOPS_PLUGIN_GIT";// DEVOPSGIT插件版本
    public static final String DEVOPS_PLUGIN_SVN = "DEVOPS_PLUGIN_SVN";// DEVOPSSVN插件版本
    public static final String DEVOPS_PLUGIN_SONAR = "DEVOPS_PLUGIN_SONAR";// DEVOPSSONAR插件版本
    public static final String DEVOPS_PLUGIN_GO = "DEVOPS_PLUGIN_GO";// DEVOPSGO插件版本
    public static final String DEVOPS_PLUGIN_GITLAB = "DEVOPS_PLUGIN_GITLAB";// DEVOPSGITLAB插件版本
    public static final String DEVOPS_PLUGIN_ANT = "DEVOPS_PLUGIN_ANT";// DEVOPSANT插件版本
    public static final String DEVOPS_PLUGIN_PHING = "DEVOPS_PLUGIN_PHING";// DEVOPSPHING插件版本
    public static final String DEVOPS_PLUGIN_PYTHON = "DEVOPS_PLUGIN_PYTHON";// DEVOPSPYTHON插件版本
    public static final String DEVOPS_PLUGIN_SSH_CREDENTIAL = "DEVOPS_PLUGIN_SSH_CREDENTIAL";// DEVOPSSSH插件版本
    public static final String CEPH_MONITOR = "CEPH_MONITOR";// ceph监听地址
    public static final String DEVOPS_PLUGIN_DOCKER = "DEVOPS_PLUGIN_DOCKER";// DEVOPSDOCKER插件版本
    public static final String DEVOPS_PLUGIN_DOCKER_COMMONS = "DEVOPS_PLUGIN_DOCKER_COMMONS";// DEVOPSDOCKERCOMMONS插件版本
    public static final String CEPH_KEY = "CEPH_KEY";// ceph密钥
    public static final String CEPH_RGW_ENDPOINT = "CEPH_RGW_ENDPOINT";// ceph对象访问地址
    public static final String CI_THREAD_POOL_CORE_SIZE = "CI_THREAD_POOL_CORE_SIZE"; // 构建线程池核心线程数
    public static final String CI_THREAD_POOL_MAX_SIZE = "CI_THREAD_POOL_MAX_SIZE"; // 构建线程池最大线程数
    public static final String DOCKER_WEBSOCKET_INTERCEPTURL = "/ws/container/exec"; // DOCKER终端URL

    public static final String DEFAULT_SYSTEM_IP = "127.0.0.1";

}
