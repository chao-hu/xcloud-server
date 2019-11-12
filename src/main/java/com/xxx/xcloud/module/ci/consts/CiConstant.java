package com.xxx.xcloud.module.ci.consts;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月7日 下午5:45:54
 */
public class CiConstant {
    /**
     * 代码类型 1代码构建 2dockerFile构建
     */
    public static final byte TYPE_CODE = 1;
    public static final byte TYPE_DOCKERFILE = 2;

    /**
     * 代码来源 1gitlab 2svn 3github
     */
    public static final byte CODE_TYPE_GITLAB = 1;
    public static final byte CODE_TYPE_SVN = 2;
    public static final byte CODE_TYPE_GITHUB = 3;
    public static final String CODE_TYPE_GITLAB_STR = "gitlab";
    public static final String CODE_TYPE_SVN_STR = "svn";
    public static final String CODE_TYPE_GITHUB_STR = "github";

    /**
     * 构建状态：1未构建2构建中3完成4失败
     */
    public static final byte CONSTRUCTION_STATUS_WAIT = 1;
    public static final byte CONSTRUCTION_STATUS_ING = 2;
    public static final byte CONSTRUCTION_STATUS_SUCCESS = 3;
    public static final byte CONSTRUCTION_STATUS_FAIL = 4;
    public static final byte CONSTRUCTION_STATUS_DISABLED = 5;

    /**
     * 构建语言
     */
    public static final String DEVOPS_LANG_JAVA = "java";
    public static final String DEVOPS_LANG_GO = "go";
    public static final String DEVOPS_LANG_PYTHON = "python";
    public static final String DEVOPS_LANG_NODEJS = "nodejs";
    public static final String DEVOPS_LANG_PHP = "php";

    /**
     * 构建语言
     */
    public static final int DEVOPS_LANG_JAVA_INT = 1;
    public static final int DEVOPS_LANG_GO_INT = 2;
    public static final int DEVOPS_LANG_PYTHON_INT = 3;
    public static final int DEVOPS_LANG_NODEJS_INT = 4;
    public static final int DEVOPS_LANG_PHP_INT = 5;

    /**
     * 编译工具类型0:none、1：ant、2：maven、3：shell、4：gobuild、5、gomake
     */
    public static final int COMPILE_TOOL_TYPE_NONE = 0;
    public static final int COMPILE_TOOL_TYPE_ANT = 1;
    public static final int COMPILE_TOOL_TYPE_MAVEN = 2;
    public static final int COMPILE_TOOL_TYPE_SHELL = 3;
    public static final int COMPILE_TOOL_TYPE_GO_BUILD = 4;
    public static final int COMPILE_TOOL_TYPE_GO_MAKE = 5;

    /**
     * xcloud Job 默认值
     */
    public static final Integer XCLOUD_JOB_MAX_EXECUTION_RECORDS_DEFAULT = 2;
    public static final Integer XCLOUD_JOB_MAX_KEEP_DAYS_DEFAULT = 2;

    /**
     * giblab获取token认证方式: 用户名密码
     */
    public static final String GITLAB_GRANTTYPE_PASSWORD = "password";

    /**
     * 认证方式: 1：http认证、2：ssh认证
     */
    public static final byte AUTH_TYPE_HTTP = 1;
    public static final byte AUTH_TYPE_SSH = 2;

    /**
     * admin字符串
     */
    public static final String USER_ADMIN_NAMESPACE = "admin";

    /**
     * xcloud配置工具, ExecConfig类型 1:maven 2:ant 3:sonar
     */
    public final static Integer EXEC_CONFIG_MAVEN = 1;
    public final static Integer EXEC_CONFIG_ANT = 2;
    public final static Integer EXEC_CONFIG_SONAR = 3;
    public final static Integer EXEC_CONFIG_GRADLE = 4;

    /**
     * 代码构建完成标志
     */
    public final static Integer CODE_CI_FINISHED_FLAG = 1;
    public final static Integer CODE_CI_FINISHED_SUCCESS_FLAG = 0;
    public final static Integer CODE_CI_FINISHED_FAILED_FLAG = 1;

    /**
     * 代码构建定时任务组
     */
    public final static String CODE_CI_QUARTZ_GROUP = "code_ci_quartz_group_";

    /**
     * 认证类型: 1: (gitlab||github)&&http 2:gitlab&&ssh 3:svn&http
     */
    public final static int CREDENTIALS_HTTP = 1;
    public final static int CREDENTIALS_SSH = 2;
    public final static int CREDENTIALS_SVN = 3;

    /**
     * 构建任务PUT接口操作类型
     */
    public final static String CI_OPERATOR_START = "start";
    public final static String CI_OPERATOR_STOP = "stop";
    public final static String CI_OPERATOR_MODIFY = "modify";
    public final static String CI_OPERATOR_DISABLE = "disable";
    public final static String CI_OPERATOR_ENABLE = "enable";

    /**
     * 凭据范围
     */
    public static final String CREDENTIALS_SCOPE = "GLOBAL";

    /**
     * 代码分支标签类型
     */
    public final static byte CODE_BRANCH_TYPE = 0;
    public final static byte CODE_TAG_TYPE = 1;

    /**
     * dockerfile填写方式
     */
    public final static byte DOCKERFILE_WRITE_TYPE_WRITE_ONLINE = 0;
    public final static byte DOCKERFILE_WRITE_TYPE_CODE_BASE = 1;

    public static final String COMPILE_TOOL_TYPE = "compileToolType";

    public static final String IMG_VER_GENERATE_STRATEBY_AUTOMATIC = "1";
    public static final String IMG_VER_GENERATE_STRATEBY_MANUAL = "2";
}
