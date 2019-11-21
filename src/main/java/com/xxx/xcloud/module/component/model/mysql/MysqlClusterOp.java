package com.xxx.xcloud.module.component.model.mysql;

/**
 * @author LYX
 *
 */
public class MysqlClusterOp {
    private String operator;
    private String master;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

}
