package com.xxx.xcloud.module.ceph.constant;

import java.util.regex.Pattern;

/**
 * @ClassName: CephConstant
 * @Description: Ceph所用常量
 * @author wangkebiao
 * @date 2019年11月14日
 *
 */
public class CephConstant {

    public static final String OPERATION_FORMAT = "format";
    public static final String OPERATION_EXPAND = "expand";
    public static final String OPERATION_ROLLBACK = "rollback";
    public static final String BUCKET_NAME_SPLIT = "bucket";
    public static Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,15}$");
    
    
    public static final String CEPH_FILE_ALREADY_EXIST = "文件存储卷:%s已经存在";
    public static final String CEPH_FILE_NOT_EXIST = "文件存储卷不存在";
    public static final String CEPH_FILE_NAME_ILLEGAL = "文件存储卷名称不合法，应由5到15位字母数字和下划线组成，以字母开头";
    public static final String CEPH_FILE_SIZE_ILLEGAL = "文件存储卷大小:%s不合法";
    public static final String CEPH_FILE_CREATE_FAILED = "租户:%s创建卷:%s异常";
    public static final String CEPH_FILE_DELETE_FAILED = "文件存储卷:%s删除失败";
    public static final String CEPH_FILE_MOUNTED = "文件存储卷:%s已被挂载，不能删除";
    public static final String CEPH_FILE_UPLOAD_FAILED = "文件上传失败";
    public static final String CEPH_FILE_DOWNLOAD_FAILED = "下载文件异常";
    public static final String CEPH_FILE_DOWNLOAD_NOT_EXIST = "在存储卷内不存在";
    public static final String CEPH_FILE_PATH_EMPTY = "文件路径为空";
    public static final String CEPH_FILE_CLEAR_FAILED = "文件存储卷:%s清空失败";
    public static final String CEPH_FILE_GET_RESOURCE_FAILED = "获取卷:%s已使用容量失败";
    public static final String CEPH_FILE_FOLDER_ILLEGAL = "文件夹前缀不存在或不是文件夹";
    public static final String CEPH_FILE_FOLDER_ALREADY_EXIST = "文件夹已经存在";
    public static final String CEPH_FILE_EMPTY = "上传的文件为空";
    public static final double HEX = 1024d;
    public static final int NAME_MIN_LENGTH = 4;
    public static final int NAME_MAX_LENGTH = 15;
    public static final double CEPH_FILE_MIN_SIZE = 0d;
    
    
    public static final String CEPH_RBD_ALREADY_EXIST = "块:%s已经存在";
    public static final String CEPH_RBD_NOT_EXIST = "指定的块不存在";
    public static final String CEPH_RBD_SIZE_ILLEGAL = "块存储大小:%s不合法";
    public static final String CEPH_RBD_NAME_ILLEGAL = "块存储名称不合法，应由5到15位字母数字和下划线组成，以字母开头";
    public static final String CEPH_RBD_CREATE_FAILED = "块:%s创建失败";
    public static final String CEPH_RBD_MOUNTED = "块存储已经被挂载";
    public static final String CEPH_RBD_MOUNT_FAILED = "id不存在无法修改";
    public static final String CEPH_RBD_DELETE_FAILED = "租户:%s删除块或快照:%s时出现异常";
    public static final String TENANT_NOT_EXIST = "租户:%s不存在";
    public static final String SNAP_ALREADY_EXIST = "指定的快照:%s已经存在";
    public static final String SNAP_CREATE_FAILED = "快照:%s创建失败";
    public static final String CEPH_RBD_RESIZE_ILLEGAL = "扩容大小不合法，应大于之前的容量";
    public static final String CEPH_RBD_RESIZE_FAILED = "块存储扩容失败";
    public static final String SNAP_STRATEGY_RUNNING = "快照策略正在运行中";
    public static final String SNAP_DELETE_FAILED = "快照删除失败";
    public static final String SNAP_NOT_EXIST = "指定的快照不存在";
    public static final String SNAP_ROLLBACK_FAILED = "快照回滚失败";
    public static final String CEPH_RBD_HAS_SNAP = "块存储存在快照";
    public static final String CEPH_RBD_HAS_STRATEGY = "块存储已经存在快照策略";
    public static final String SNAP_STRATEGY_WEEK_ILLEGAL = "week参数不合法，应由0-6的数字组成，逗号隔开";
    public static final String SNAP_STRATEGY_TIME_ILLEGAL = "time参数不合法，应由0-23的数字组成，逗号隔开";
    public static final String SNAP_STRATEGE_ENDDATE_ILLEGAL = "endDate参数不合法，应非空且大于当前日期";
    public static final String SNAP_STRATEGE_STATUS_ILLEGAL = "status参数不合法，应为0或者1";
    public static final String SNAP_STRATEGE_NOT_EXIST = "指定的快照策略不存在";
    public static final long HEXL = 1024L;
    public static final long FEATURES = 1L;
    public static final double CEPH_RBD_MIN_SIZE = 0d;
    

    public static final String BUCKET_NAME_ILLEGAL = "桶名称不合法，应由5到15位小写字母、数字和下划线组成，以字母开头";
    public static final String BUCKET_NOT_EXIST = "桶:%s不存在或不属于租户:%s";
    public static final String BUCKET_ACL_ILLEGAL = "acl参数不合法，应为public/publicread/publicreadwrite";
    public static final String BUCKET_STORAGECLASS_ILLEGAL = "storageClass参数不合法，应为STANDARD/GLACIER/STANDARD_IA";
    public static final String BUCKET_ALREADY_EXIST = "桶:%s已经存在";
    public static final String BUCKET_CREATE_FAILED = "桶:%s创建失败";
    public static final String BUCKET_NOT_EMPTY = "桶:%s非空，删除失败";
    public static final String BUCKET_DELETE_FAILED = "桶不存在或删除失败";
    public static final String UPLOAD_FAILED = "上传文件失败";
    public static final String UPLOAD_FILE_EMPTY = "上传文件为空";
    public static final String UPLOAD_FILE_ALREADY_EXIST = "文件已存在";
    public static final String DOWNLOAD_FAILED = "文件:%s下载失败";
    public static final String TENANT_NAME_ILLEGAL = "租户名为空";
    public static final String FILE_NAME_EMPTY = "文件名为空";
    public static final String DOWNLOAD_FILE_NOT_EXIST = "对象:%s不存在";
    public static final String DOWNLOAD_FILE_NAME_ILLEGAL = "下载文件名为空";
    public static final String CEPH_OBJ_CLIENT = "对象存储客户端异常";
    public static final String CHARSET_UTF = "utf-8";
    public static final String CHARSET_ISO = "ISO8859-1";
}
