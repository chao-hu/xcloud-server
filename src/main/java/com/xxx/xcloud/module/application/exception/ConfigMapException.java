package com.xxx.xcloud.module.application.exception;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午3:30:14
 */
public class ConfigMapException extends Exception {
	private static final long serialVersionUID = 846868739728293487L;

	private int code;

	public ConfigMapException(int code, String msg) {
		super(msg);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
