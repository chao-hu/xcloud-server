package com.xxx.xcloud.client.sonar.exception;

/**
 * 
 * @author mengaijun
 * @date: 2018年12月20日 上午11:30:32
 */
public class SonarException extends Exception{
	private static final long serialVersionUID = 2674323655386180059L;
	protected int code;

    public SonarException(int code, String message) {
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
