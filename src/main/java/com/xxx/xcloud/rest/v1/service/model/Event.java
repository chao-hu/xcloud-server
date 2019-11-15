package com.xxx.xcloud.rest.v1.service.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Description:k8s资源触发的事件
 * @author  LYJ </br>
 * create time：2018年12月7日 上午11:15:18 </br>
 * @version 1.0
 * @since
 */
@Data
public class Event implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 信息
	 */
	private String message;

	/**
	 * 状态
	 */
	private String type;

	private String timeStamp;
	
    @Override
    public String toString() {
        return "Event [message=" + message + ", type=" + type + ", timeStamp=" + timeStamp + "]";
    }

}
