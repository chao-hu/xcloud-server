package com.xxx.xcloud.module.system.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "`sys_cluster_info`")
public class SysClusterInfo implements Serializable {

    private static final long serialVersionUID = 1819099555348375258L;

    @Id
    @Column(name = "`cfg_key`", length = 64)
    private String cfgKey;

    @Column(name = "`cfg_value`")
    private String cfgValue;

    @Column(name = "`cfg_type`")
    private char cfgType;

    @Column(name = "`memo`")
    private String memo;

    public String getCfgKey() {
        return cfgKey;
    }

    public void setCfgKey(String cfgKey) {
        this.cfgKey = cfgKey;
    }

    public String getCfgValue() {
        return cfgValue;
    }

    public void setCfgValue(String cfgValue) {
        this.cfgValue = cfgValue;
    }

    public char getCfgType() {
        return cfgType;
    }

    public void setCfgType(char cfgType) {
        this.cfgType = cfgType;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
