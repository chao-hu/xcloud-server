package com.xxx.xcloud.module.ingress.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

/**
 * Descriptioni:服务和代理关联信息
 * @author  LYJ </br>
 * create time：2018年12月3日 下午3:37:15 </br>
 * @version 1.0
 * @since
 */
@Entity
@Table(name = "`BDOS_SERVICE_INGRESS`")
@Data
public class ServiceIngress implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GenericGenerator(name="uuidGenerator", strategy="uuid")
	@GeneratedValue(generator="uuidGenerator")
	private String id;
	/**
	 * 服务ID
	 */
	@Column(name = "`SERVICE_ID`")
	private String serviceId; 
	
	@Column(name = "`INGRESS_PROXY_ID`")
	private String ingressProxyId;

	@Column(name = "`INGRESS_DOMAIN_ID`")
	private String ingressDomainId;
	
	/**
	 * 服务访问路径和服务端口,Map结构JSON串("visitPath":"servicePort")
	 */
	@Column(name = "`PATH_AND_PORT`")
	private String pathAndPort;

	@Override
	public String toString() {
		return "ServiceIngress [id=" + id + ", serviceId=" + serviceId + ", ingressProxyId=" + ingressProxyId
				+ ", ingressDomainId=" + ingressDomainId + ", pathAndPort=" + pathAndPort + "]";
	}
	
}
