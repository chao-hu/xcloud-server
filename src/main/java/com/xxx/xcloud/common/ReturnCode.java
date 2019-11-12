package com.xxx.xcloud.common;

/**
 * @ClassName: ReturnCode
 * @Description: Api 返回的码表
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class ReturnCode {

    /**
     * @Fields field:field:接口正常返回码
     */
    public static final int CODE_SUCCESS = 200;

    /**
     * JSON转换异常，等其他形式异常， 范围：500-599
     */

    /**
     *  公共错误（校验错误）， 范围 ：600-699
     */

    /**
     * @Fields: xx为空
     */
    public static final int CODE_CHECK_PARAM_IS_NULL = 600;
    /**
     * @Fields: xx不符合规范
     */
    public static final int CODE_CHECK_PARAM_IS_NOT_FORMAT = 601;

    /**
     * @Fields: xx不存在
     */
    public static final int CODE_CHECK_PARAM_IS_NOT_EXIST = 602;
    /**
     * @Fields: 修改CPU总配额小于等于已使用配额
     */
    public static final int CODE_CHECK_PARAM_CPU_LESSTHAN_USED = 603;
    /**
     * @Fields: 修改Memory总配额小于等于已使用配额
     */
    public static final int CODE_CHECK_PARAM_MEMORY_LESSTHAN_USED = 604;
    /**
     * @Fields: 修改Storage总配额小于等于已使用配额
     */
    public static final int CODE_CHECK_PARAM_STORAGE_LESSTHAN_USED = 605;
    /**
     * @Fields: xx已存在
     */
    public static final int CODE_CHECK_PARAM_IS_EXIST = 606;
    /**
     * @Fields: xx不能修改
     */
    public static final int CODE_CHECK_PARAM_NOT_UPDATE = 607;
    /**
     * @Fields: xx删除失败
     */
    public static final int CODE_CHECK_PARAM_DELETE_FAILED = 608;
    /**
     * @Fields: CPU或Memory资源不足
     */
    public static final int CODE_CHECK_PARAM_QUOTA_INSUFFICIENT = 609;
    /**
     * @Fields: 链接FTP失败
     */
    public static final int CODE_CHECK_PARAM_CONNECT_FTP_FAILED = 610;
    /**
     * @Fields: xx新增失败
     */
    public static final int CODE_CHECK_PARAM_ADD_FAILED = 611;
    /**
     * @Fields: 定时任务移除失败
     */
    public static final int CODE_CHECK_QUARTZ_DELETE_FAILED = 631;
    /**
     * @Fields: 定时任务添加失败
     */
    public static final int CODE_CHECK_QUARTZ_ADD_FAILED = 632;

    /**
     *  SQL异常 ，范围：700-799
     */
    /**
     * @Fields: 数据库连接失败
     */
    public static final int CODE_SQL_CONNECT_FAILED = 700;
    /**
     * @Fields: 查询单条数据，多条数据返回错误
     */
    public static final int CODE_SQL_FIND_ONE_RETURN_MORE_FAILED = 701;
    /**
     * @Fields: 保存信息失败
     */
    public static final int CODE_SQL_SAVE_INFO_FAILED = 702;
    /**
     * @Fields: 查询数据列表失败
     */
    public static final int CODE_SQL_FIND_LIST_FAILED = 703;
    /**
     * @Fields: 查询单条数据失败
     */
    public static final int CODE_SQL_FIND_ONE_FAILED = 704;
    /**
     * @Fields: 删除失败
     */
    public static final int CODE_SQL_DELETE_FAILED = 705;

    /**
     *  ceph异常 ，范围：800-899
     */
    /**
     * @Fields: Ceph客户端连接异常
     */
    public static final int CODE_CEPH_CLIENT = 801;
    /**
     * @Fields: 资源已经存在
     */
    public static final int CODE_CEPH_EXIST = 802;
    /**
     * @Fields: 指定参数不合法
     */
    public static final int CODE_CEPH_INVALID_PARAM = 803;
    /**
     * @Fields: 指定的资源不存在
     */
    public static final int CODE_CEPH_NOT_FOUND = 804;
    /**
     * @Fields: 资源创建异常
     */
    public static final int CODE_CEPH_CREATE = 805;
    /**
     * @Fields: 资源删除异常
     */
    public static final int CODE_CEPH_DELETE = 806;
    /**
     * @Fields: 资源正在被占用
     */
    public static final int CODE_CEPH_OCCUPIED = 807;
    /**
     * @Fields: 资源缺乏异常
     */
    public static final int CODE_CEPH_RESOURCE_LACK = 808;
    /**
     * @Fields: 文件上传异常
     */
    public static final int CODE_CEPH_UPLOAD = 809;
    /**
     * @Fields: 文件下载异常
     */
    public static final int CODE_CEPH_DOWNLOAD = 810;
    /**
     * @Fields: 存储挂载异常
     */
    public static final int CODE_CEPH_MOUNT = 812;
    /**
     * @Fields: 扩容异常
     */
    public static final int CODE_CEPH_RESIZE = 813;
    /**
     * @Fields: 回滚操作异常
     */
    public static final int CODE_CEPH_ROLLBACK = 814;
    /**
     * @Fields: 初始化异常
     */
    public static final int CODE_CEPH_INIT = 815;
    /**
     * @Fields: 清空异常
     */
    public static final int CODE_CEPH_DESTROY = 816;
    /**
     * @Fields: 块设备重复挂载错误
     */
    public static final int CODE_CEPH_RDB_MOUNT_REPEAT = 817;

    /**
     *  k8s异常，范围:900-999
     */
    /**
     * @Fields: k8s初始化客户端失败
     */
    public static final int CODE_K8S_INIT_FAILED = 900;
    /**
     * @Fields: k8s创建namespace失败
     */
    public static final int CODE_K8S_CREATE_NS_FAILED = 901;
    /**
     * @Fields: k8s创建配额失败
     */
    public static final int CODE_K8S_CREATE_QUATE_FAILED = 902;
    /**
     * @Fields: k8s创建ceph证书密钥失败
     */
    public static final int CODE_K8S_CREATE_CEPH_SECRET_FAILED = 903;
    /**
     * @Fields: k8s删除namespace失败
     */
    public static final int CODE_K8S_DELETE_NS_FAILED = 904;
    /**
     * @Fields: k8s创建ingress失败
     */
    public static final int CODE_K8S_CREATE_INGRESS_FAILED = 905;
    /**
     * @Fields: k8s修改ingress失败
     */
    public static final int CODE_K8S_UPDATE_INGRESS_FAILED = 906;
    /**
     * @Fields: k8s删除ingress失败
     */
    public static final int CODE_K8S_DELETE_INGRESS_FAILED = 907;
    /**
     * @Fields: k8s创建secret失败
     */
    public static final int CODE_K8S_CREATE_SECRET_FAILED = 908;
    /**
     * @Fields: k8s修改secret失败
     */
    public static final int CODE_K8S_UPDATE_SECRET_FAILED = 909;
    /**
     * @Fields: k8s删除secret失败
     */
    public static final int CODE_K8S_DELETE_SECRET_FAILED = 910;
    /**
     * @Fields: k8s创建cronjob失败
     */
    public static final int CODE_K8S_CREATE_CRONJOB_FAILED = 911;
    /**
     * @Fields: k8s修改cronjob失败
     */
    public static final int CODE_K8S_UPDATE_CRONJOB_FAILED = 912;
    /**
     * @Fields:  k8s删除cronjob失败
     */
    public static final int CODE_K8S_DELETE_CRONJOB_FAILED = 913;
    /**
     * @Fields: k8s创建job失败
     */
    public static final int CODE_K8S_CREATE_JOB_FAILED = 914;
    /**
     * @Fields: k8s获取job失败
     */
    public static final int CODE_K8S_GET_JOB_FAILED = 915;
    /**
     * @Fields: k8s修改job失败
     */
    public static final int CODE_K8S_UPDATE_JOB_FAILED = 916;
    /**
     * @Fields: k8s删除job失败
     */
    public static final int CODE_K8S_DELETE_JOB_FAILED = 917;
    /**
     * @Fields: k8s创建组件yaml失败
     */
    public static final int CODE_K8S_CREATE_COMPONENT_YAML_FAILED = 918;
    /**
     * @Fields: k8s获取组件yaml失败
     */
    public static final int CODE_K8S_GET_COMPONENT_YAML_FAILED = 919;
    /**
     * @Fields: k8s修改组件yaml失败
     */
    public static final int CODE_K8S_UPDATE_COMPONENT_YAML_FAILED = 920;
    /**
     * @Fields: k8s删除组件yaml失败
     */
    public static final int CODE_K8S_DELETE_COMPONENT_YAML_FAILED = 921;
    /**
     * @Fields: k8s获取pod失败
     */
    public static final int CODE_K8S_GET_POD_FAILED = 922;
    /**
     * @Fields: k8s删除pod失败
     */
    public static final int CODE_K8S_DELETE_POD_FAILED = 923;
    /**
     * @Fields: k8s创建、修改或删除HPA失败
     */
    public static final int CODE_K8S_HPA_FAILED = 924;
    /**
     * @Fields: k8s创建、修改或删除deployment失败
     */
    public static final int CODE_K8S_DEPLOYMENT_FAILED = 925;
    /**
     * @Fields: k8s创建configmap失败
     */
    public static final int CODE_K8S_CREATE_CONFIGMAP_FAILED = 926;
    /**
     * @Fields: k8s删除configmap失败
     */
    public static final int CODE_K8S_DELETE_CONFIGMAP_FAILED = 927;
    /**
     * @Fields: k8s修改configmap失败
     */
    public static final int CODE_K8S_UPDATE_CONFIGMAP_FAILED = 928;
    /**
     * @Fields: k8s创建service失败
     */
    public static final int CODE_K8S_CREATE_SERVICE_FAILED = 929;
    /**
     * @Fields: k8s修改service失败
     */
    public static final int CODE_K8S_UPDATE_SERVICE_FAILED = 930;
    /**
     * @Fields: k8s删除service失败
     */
    public static final int CODE_K8S_DELETE_SERVICE_FAILED = 931;
    /**
     * @Fields: k8s获取node失败
     */
    public static final int CODE_K8S_GET_NODE_FAILED = 932;
    /**
     * @Fields: k8s创建、修改或删除Endpoints失败
     */
    public static final int CODE_K8S_ENDPOINTS_FAILED = 933;

    /**
     *  harbor异常，范围1000-1099
     */
    /**
     * @Fields: harbor创建用户失败
     */
    public static final int CODE_HARBOR_CREATE_USER_FAILED = 1001;
    /**
     * @Fields: 没有所需要的project信息
     */
    public static final int CODE_HARBOR_PROJECT_IS_NULL_FAILED = 1002;
    /**
     * @Fields: 查询harbor-project失败
     */
    public static final int CODE_HARBOR_FIND_PROJECT_FAILED = 1003;
    /**
     * @Fields: 删除harbor-project失败
     */
    public static final int CODE_HARBOR_DELETE_PROJECT_FAILED = 1004;
    /**
     * @Fields: 删除harbor-user失败
     */
    public static final int CODE_HARBOR_DELETE_USER_FAILED = 1005;
    /**
     * @Fields: 查询harbor-user失败
     */
    public static final int CODE_HARBOR_FIND_USER_FAILED = 1006;
    /**
     * @Fields: harbor创建project失败
     */
    public static final int CODE_HARBOR_CREATE_PROJECT_FAILED = 1007;
    /**
     * @Fields: project新增成员失败
     */
    public static final int CODE_HARBOR_ADD_PROJECT_MEMBER_FAILED = 1008;
    /**
     * @Fields: 删除仓库镜像失败
     */
    public static final int CODE_HARBOR_DELETE_IMAGE_FAILED = 1009;
    /**
     * @Fields: harbor连接失败
     */
    public static final int CODE_HARBOR_CONNECT_FAILED = 1010;
    /**
     * @Fields:  查询镜像失败
     */
    public static final int CODE_HARBOR_FIND_IMAGE_FAILED = 1011;
    /**
     * @Fields: 启动扫描镜像失败
     */
    public static final int CODE_HARBOR_SCAN_IMAGE_FAILED = 1012;
    /**
     * @Fields: 获取镜像扫描结果失败
     */
    public static final int CODE_HARBOR_GET_IMAGE_SCAN_RESULT_FAILED = 1013;

    /**
     *  docker异常，范围：1100-1199
     */
    /**
     * @Fields: 初始化客户端异常
     */
    public static final int CODE_DOCKER_INIT_CLIENT_FAILED = 1101;
    /**
     * @Fields: 加载镜像失败
     */
    public static final int CODE_DOCKER_LOAD_IMAGE_FAILED = 1102;
    /**
     * @Fields: 修改镜像名失败
     */
    public static final int CODE_DOCKER_TAG_IMAGE_FAILED = 1103;
    /**
     * @Fields: 推送镜像失败
     */
    public static final int CODE_DOCKER_PUSH_IMAGE_FAILED = 1104;
    /**
     * @Fields: 创建镜像失败
     */
    public static final int CODE_DOCKER_CREATE_IMAGE_FAILED = 1105;
    /**
     * @Fields: 删除镜像失败
     */
    public static final int CODE_DOCKER_DELETE_IMAGE_FAILED = 1106;
    /**
     * @Fields: 查询镜像详情失败
     */
    public static final int CODE_DOCKER_INSPECT_IMAGE_FAILED = 1107;
    /**
     * @Fields: 镜像tar包操作失败
     */
    public static final int CODE_IMAGE_TAR_OPERATION_FAILED = 1108;
    /**
     * @Fields: 执行命令失败
     */
    public static final int CODE_DOCKER_EXEC_OPERATION_FAILED = 1109;

    /**
     *  操作限制异常，范围：1200-1299
     */
    /**
     * @Fields: 不允许进行服务操作
     */
    public static final int CODE_OPT_SERVICE_NOT_ALLOWED_FAILED = 1201;
    /**
     * @Fields: 不允许进行节点操作
     */
    public static final int CODE_OPT_NODE_NOT_ALLOWED_FAILED = 1202;
    /**
     * @Fields: 不允许进行定时任务操作
     */
    public static final int CODE_OPT_CRONJOBE_NOT_ALLOWED_FAILED = 1203;
    /**
     * @Fields: 操作超时
     */
    public static final int CODE_OPT_OVERTIME_FAILED = 1204;
    /**
     * @Fields: 不允许两服务之间同时存在亲和与反亲和的操作
     */
    public static final int CODE_OPT_SERVICEAFFINITY_MUTUAL = 1205;
    /**
     * @Fields: 当前操作不满足pod互斥的前提条件
     */
    public static final int CODE_OPT_POD_PODMUTEX = 1206;
    /**
     * @Fields: 当前资源对象或行为动作不允许跨租户操作
     */
    public static final int CODE_OPT_CROSS_TENANT_NOT_ALLOWED = 1207;

    /**
     *  etcd 异常，范围：1300-1399
     */
    /**
     * @Fields: etcd client调用失败
     */
    public static final int CODE_ETCD_CLIENT_FAILED = 1301;

    /**
     * @Fields:  jenkins创建Job失败
     */
    public static final int CODE_JENKINS_CREATE_JOB_FAILED = 1451;
    /**
     * @Fields: jenkins获取语言信息失败
     */
    public static final int CODE_JENKINS_GET_LANG_INFO_FAILED = 1452;
    /**
     * @Fields: jenkins获取配置信息失败
     */
    public static final int CODE_JENKINS_GET_EXEC_CONFIG_FAILED = 1453;
    /**
     * @Fields: jenkins更新Job失败
     */
    public static final int CODE_JENKINS_UPDATE_JOB_FAILED = 1454;
    /**
     * @Fields: jenkins删除Job失败
     */
    public static final int CODE_JENKINS_DELETE_JOB_FAILED = 1455;
    /**
     * @Fields: jenkins停止job失败
     */
    public static final int CODE_JENKINS_STOP_JOB_FAILED = 1456;
    /**
     * @Fields: jenkins注册代码信息失败
     */
    public static final int CODE_JENKINS_REGISTER_CREDENTIALS_FAILED = 1457;
    /**
     * @Fields: jenkins删除代码信息失败
     */
    public static final int CODE_JENKINS_DELETE_CREDENTIALS_FAILED = 1458;
    /**
     * @Fields: jenkins获取Job失败
     */
    public static final int CODE_JENKINS_GET_JOB_FAILED = 1459;
    /**
     * @Fields: jenkins Job不存在
     */
    public static final int CODE_JENKINS_JOB_NOE_EXIST = 1460;

    /**
     * @Fields: 删除Jenkins服务端凭据失败。
     */
    public static final int CODE_JENKINS_CREDENTIAL_DELETE_FAILED = 1421;

    /**
     * @Fields: 代码认证失败
     */
    public static final int CODE_CODE_AUTH_FAILED = 1411;
    /**
     * @Fields: gitlab获取project失败
     */
    public static final int CODE_GITLAB_GET_PROJECTS_FAILED = 1412;
    /**
     * @Fields: gitlab获取项目分支失败
     */
    public static final int CODE_GITLAB_GET_BRANCHES_FAILED = 1413;
    /**
     * @Fields: 添加sonar端规则集失败
     */
    public static final int CODE_SONAR_ADD_QUALITYPROFILE_FAILED = 1414;
    /**
     * @Fields: 删除sonar端规则集失败
     */
    public static final int CODE_SONAR_DELETE_QUALITYPROFILE_FAILED = 1415;
    /**
     * @Fields: 查询sonar端规则集对应规则失败
     */
    public static final int CODE_SONAR_GET_QUALITYPROFILE_RULES_FAILED = 1416;
    /**
     * @Fields: 激活sonar端规则集规则失败
     */
    public static final int CODE_SONAR_ACTIVATE_QUALITYPROFILE_RULE_FAILED = 1417;
    /**
     * @Fields: 停用sonar端规则集规则失败
     */
    public static final int CODE_SONAR_DEACTIVATE_QUALITYPROFILE_RULE_FAILED = 1418;
    /**
     * @Fields: 获取规则集失败
     */
    public static final int CODE_SONAR_GET_QUALITYPROFILE_FAILED = 1419;
    /**
     * @Fields: 获取检查结果失败
     */
    public static final int CODE_SONAR_GET_CHECK_RESULT_FAILED = 1420;

    /**
     * 集群管理异常编码 1500-1599
     */
    /**
     * @Fields: 主机已经被锁住
     */
    public static final int CODE_CLUSTER_HOST_CHECK = 1501;
    /**
     * @Fields: 主机上有安装的角色
     */
    public static final int CODE_CLUSTER_ROLE_INSTALLED = 1502;
    /**
     * @Fields: 设备ID不存在
     */
    public static final int CODE_CLUSTER_DEV_NOT_EXIST = 1503;
    /**
     * @Fields: 存在任务正在安装
     */
    public static final int CODE_CLUSTER_PLAY_RUNNING = 1504;
    /**
     * @Fields: 请求参数为空
     */
    public static final int CODE_CLUSTER_PARAM_IS_EMPTY = 1505;
    /**
     * @Fields: 任务不存在
     */
    public static final int CODE_CLUSTER_PLAY_NOT_EXIST = 1506;
    /**
     * @Fields:  任务不具备执行条件
     */
    public static final int CODE_CLUSTER_PLAY_CANT_EXEC = 1507;
    /**
     * @Fields: 执行任务调度失败
     */
    public static final int CODE_CLUSTER_BOOTSTRAP_CALL_FAIL = 1508;
    /**
     * @Fields: 任务不存在
     */
    public static final int CODE_CLUSTER_TASK_NOT_EXIST = 1509;
    /**
     * @Fields: 任务状态不是failed
     */
    public static final int CODE_CLUSTER_TASK_NOT_FAILED = 1510;
    /**
     * @Fields: 任务token错误
     */
    public static final int CODE_CLUSTER_TASK_TOKEN_ERROR = 1511;

    /**
     * @Fields: 任务错误状态
     */
    public static final int CODE_CLUSTER_TASK_ERROR_STATUS = 1512;
    /**
     * @Fields: 设备空间不足
     */
    public static final int CODE_CLUSTER_DEV_NOT_ENOUGH = 1513;
    /**
     * @Fields: 日志状态错误
     */
    public static final int CODE_CLUSTER_LOG_ERROR_STATUS = 1514;
    /**
     * @Fields: 日志不存在
     */
    public static final int CODE_CLUSTER_LOG_NOE_EXIST = 1515;
    /**
     * @Fields: 初始化主机host文件失败
     */
    public static final int CODE_CLUSTER_HOST_INV_FAILED = 1516;
    /**
     * @Fields: 初始化主机hosts文件夹失败
     */
    public static final int CODE_CLUSTER_HOST_DIR_FAILED = 1517;
    /**
     * @Fields: 设备已被使用
     */
    public static final int CODE_CLUSTER_DEV_IN_USERD = 1518;
    /**
     * @Fields: 系统错误
     */
    public static final int CODE_CLUSTER_SYSTEM_ERROR = 1519;

    /**
     * // xcloud删除Job失败
     */
    public static final int CODE_XCLOUD_DELETE_JOB_FAILED = 1405;

    /**
     * // xcloud停止job失败
     */
    public static final int CODE_XCLOUD_STOP_JOB_FAILED = 1406;

    /**
     * // xcloud注册代码信息失败
     */
    public static final int CODE_XCLOUD_REGISTER_CREDENTIALS_FAILED = 1407;
}
