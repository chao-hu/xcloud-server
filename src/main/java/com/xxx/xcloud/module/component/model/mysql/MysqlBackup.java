package com.xxx.xcloud.module.component.model.mysql;

import com.xxx.xcloud.module.component.model.base.Resources;

public class MysqlBackup {

    private String backupimage;
    private Resources resources;

    public String getBackupimage() {
        return backupimage;
    }

    public void setBackupimage(String backupimage) {
        this.backupimage = backupimage;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
