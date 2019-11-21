package com.xxx.xcloud.module.component.consts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: MysqlBackupConst
 * @Description: mysql备份常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class MysqlBackupConst {

    private static Logger logger = LoggerFactory.getLogger(MysqlBackupConst.class);

    /**
     * 任务类型：全量备份 1、 增加备份 2、 恢复任务 3
     */
    public static final int JOB_TYPE_FULL_AMOUNT_BACKUP = 1;
    public static final int JOB_TYPE_INCREMENTAL_BACKUP = 2;
    public static final int JOB_TYPE_RECOVER = 3;

    public static final String JOB_TYPE_FULL_AMOUNT_BACKUP_NAME = "全量备份";
    public static final String JOB_TYPE_INCREMENTAL_BACKUP_NAME = "增量备份";
    public static final String JOB_TYPE_RECOVER_NAME = "增量备份";

    private static final Integer[] BACKUP_TASK_TYPES = new Integer[] { MysqlBackupConst.JOB_TYPE_FULL_AMOUNT_BACKUP,
            MysqlBackupConst.JOB_TYPE_INCREMENTAL_BACKUP };

    /**
     * 任务调度类型：now 现在 1、 once 一次性 2、day 每天 3、 week 每周 4、 month 每月 5、year 每年
     * 6、repeat 重复任务 7；
     */
    public static final int SCHEDULE_TYPE_NOW = 1;
    public static final int SCHEDULE_TYPE_ONCE = 2;
    public static final int SCHEDULE_TYPE_DAY = 3;
    public static final int SCHEDULE_TYPE_WEEK = 4;
    public static final int SCHEDULE_TYPE_MONTH = 5;
    public static final int SCHEDULE_TYPE_YEAR = 6;
    public static final int SCHEDULE_TYPE_REPEAT = 7;

    /**
     * 任务状态：启用 1、 不启用 -1；
     */
    public static final int JOB_STATUS_ENABLED = 1;
    public static final int JOB_STATUS_DISABLED = -1;

    /**
     * 任务执行状态: 执行中 1、成功 2、失败 -1;
     */
    public static final int JOB_HISTORY_STATUS_DOING = 1;
    public static final int JOB_HISTORY_STATUS_SUCCEED = 2;
    public static final int JOB_HISTORY_STATUS_FAILED = -1;

    /**
     * 任务记录删除状态 -100
     */
    public static final int JOB_STATUS_DETETED = -100;
    public static final int JOB_HISTORY_STATUS_DETETED = -100;

    public static final int RETURN_RESULT_SUCCEED = 1;
    public static final int RETURN_RESULT_FAILED = 0;

    /**
     * 拼恢复yaml时需要的常量
     */
    public static final String MYSQL_RECOVER_APIVERSION = "batch/v1";
    public static final String MYSQL_RECOVER_CONTAINER_NAME = "mysql-pxb";

    public static final String MYSQL_RECOVER_MY_NODE_NAME = "MY_NODE_NAME";
    public static final String MYSQL_RECOVER_MY_POD_NAME = "MY_POD_NAME";
    public static final String MYSQL_RECOVER_MY_POD_NAMESPACE = "MY_POD_NAMESPACE";

    public static final String MYSQL_RECOVER_NODE_NAME = "spec.nodeName";
    public static final String MYSQL_RECOVER_POD_NAME = "metadata.name";
    public static final String MYSQL_RECOVER_NAME_SPACE = "metadata.namespace";

    public static final String MYSQL_RECOVER_FTP_ADDR = "FTP_ADDRESS";
    public static final String MYSQL_RECOVER_FTP_USERNAME = "FTP_USER";
    public static final String MYSQL_RECOVER_FTP_PASSWORD = "FTP_PASSWORD";

    public static final String MYSQL_RECOVER_JOB_HISTORY_LIST = "JOB_HISTORY_LIST";
    public static final String MYSQL_RECOVER_DEFAULT_RESTART_POLICY = "Never";

    public static final String MYSQL_RECOVER_VOLUM_MOUNT_CONF_PATH = "/var/lib/mysql.cnf";
    public static final String MYSQL_RECOVER_VOLUM_MOUNT_PATH = "/var/lib/mysql";
    public static final String MYSQL_RECOVER_VOLUM_MOUNT_OPTIONS = "relatime,nobarrier";
    public static final String MYSQL_RECOVER_VOLUM_MOUNT_CONF_SUBPATH = "mysql.cnf";
    public static final String MYSQL_RECOVER_VOLUM_MOUNT_SUBPATH = "data";

    public static final String MYSQL_RECOVER_CONFIG_DIR_NAME = "configdir";

    public static final String MYSQL_RECOVER_FLEXVOLUME_DRIVER = "sysoperator.pl/lvm";
    public static final String MYSQL_RECOVER_FSTYPE = "ext4";

    public static final String MYSQL_RECOVER_CONTAINER_DEFAULT_CPU = "1";
    public static final String MYSQL_RECOVER_CONTAINER_DEFAULT_MEMORY = "1";

    public static final String MYSQL_RECOVER_ETCD_NAME = "ETCD";
    public static final String MYSQL_RECOVER_COMMAND_NAME = "python";
    public static final String MYSQL_RECOVER_COMMAND_PATH = "/backup/restore.py";

    public static final String JOB_CONDITION_TYPE_FAILED = "Failed";
    public static final String JOB_CONDITION_TYPE_COMPLETE = "Complete";

    private MysqlBackupConst() {
    }

    public static Integer[] getBackUpTaskTypes() {
        return BACKUP_TASK_TYPES;
    }

    /**
     * 通过任务类型获取任务类型名称
     *
     * @param jobType
     *            任务类型
     * @return 任务类型名称
     */
    public static String getJobTypeName(Integer jobType) {

        String jobTypeName = "";
        if (null == jobType) {
            return jobTypeName;
        }

        switch (jobType) {
        // 全量备份
        case MysqlBackupConst.JOB_TYPE_FULL_AMOUNT_BACKUP:
            jobTypeName = MysqlBackupConst.JOB_TYPE_FULL_AMOUNT_BACKUP_NAME;
            break;
        // 增量备份
        case MysqlBackupConst.JOB_TYPE_INCREMENTAL_BACKUP:
            jobTypeName = MysqlBackupConst.JOB_TYPE_INCREMENTAL_BACKUP_NAME;
            break;

        // 恢复
        case MysqlBackupConst.JOB_TYPE_RECOVER:
            jobTypeName = MysqlBackupConst.JOB_TYPE_RECOVER_NAME;
            break;

        default:
            logger.warn("存在未知任务类型！jobType={}", jobType);
            break;
        }

        return jobTypeName;
    }
}
