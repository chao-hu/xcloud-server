package com.xxx.xcloud.module.component.model.ftp;

/**
 * @ClassName: FtpOp
 * @Description: FtpOp
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpOp {

    /**
     * 操作类型
     */
    private String operator;

    /**
     * 操作对象
     */
    private String name;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

