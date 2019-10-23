package com.xxx.xcloud.client.gitlab.exception;

/**
 * gitlab接口异常
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月3日 下午6:41:20
 */
public class GitlabException extends Exception {
    private static final long serialVersionUID = -3105414187808764217L;
    protected int code;

    public GitlabException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
