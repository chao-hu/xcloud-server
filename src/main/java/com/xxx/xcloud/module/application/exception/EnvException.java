package com.xxx.xcloud.module.application.exception;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午3:30:29
 */
public class EnvException extends Exception {

	private int code;

	public EnvException(int code, String msg) {
		super(msg);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
