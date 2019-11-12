package com.xxx.xcloud.module.devops.common;

public class DevopsException extends Exception {

    private static final long serialVersionUID = 1190567297472008216L;
    protected int code;

    public DevopsException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
