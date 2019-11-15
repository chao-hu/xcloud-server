package com.xxx.xcloud.module.application.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

/**
 * 
 * <p>
 * Description:本地存储
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月5日
 */
@Entity
@Table(name = "`BDOS_SERVICE_HOSTPATH`")
@Data
public class ServiceHostpath implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8931377531407098094L;

	/**
	 * 主键
	 */
	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid")
	@GeneratedValue(generator = "uuidGenerator")
	private String id;

	/**
	 * 关联服务id
	 */
	@Column(name = "`SERVICE_ID`")
	private String serviceId;

	/**
	 * 主机路径
	 */
	@Column(name = "`HOST_PATH`")
	private String hostPath;

	/**
	 * 挂载地址
	 */
	@Column(name = "`MOUNT_PATH`")
	private String mountPath;

	@Override
	public String toString() {
		return "ServiceHostpath [id=" + id + ", serviceId=" + serviceId + ", hostPath=" + hostPath + ", mountPath="
				+ mountPath + "]";
	}

}
