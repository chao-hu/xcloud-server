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
 * Description：服务亲和属性
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月3日
 */
@Entity
@Table(name = "`SERVICE_AFFINITY`")
@Data
public class ServiceAffinity implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5606891127493569493L;

	/**
	 * 主键
	 */
	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid")
	@GeneratedValue(generator = "uuidGenerator")
	private String id;

	/**
	 * 所属服务
	 */
	@Column(name = "`SERVICE_ID`")
	private String serviceId;

	/**
	 * 节点亲和状态，请参见 {@link com.xxx.xcloud.consts.Global}
	 */
	@Column(name = "`NODE_AFFINITY_TYPE`")
	private Byte nodeAffinityType;

	/**
	 * 节点列表，以半英逗号分隔
	 */
	@Column(name = "`NODE_AFFINITY`")
	private String nodeAffinity;

	/**
	 * 服务亲和状态，请参见 {@link com.xxx.xcloud.consts.Global}
	 */
	@Column(name = "`SERVICE_AFFINITY_TYPE`")
	private Byte serviceAffinityType;

	/**
	 * 亲和/反亲和的服务名称
	 */
	@Column(name = "`SERVICE_AFFINITY`")
	private String serviceAffinity;

	@Override
	public String toString() {
		return "ServiceAffinity [id=" + id + ", serviceId=" + serviceId + ", nodeAffinityType=" + nodeAffinityType
				+ ", nodeAffinity=" + nodeAffinity + ", serviceAffinityType=" + serviceAffinityType
				+ ", serviceAffinity=" + serviceAffinity + "]";
	}

}
