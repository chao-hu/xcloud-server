package com.xxx.xcloud.module.component.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * @ClassName: SelectorLabel
 * @Description: 用于指定服务和组件创建于集群内特定label的节点上
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`SELECTOR_LABEL`")
public class SelectorLabel implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5978739986097893235L;

	/**
	 * 主键
	 */
	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid")
	@GeneratedValue(generator = "uuidGenerator")
	private String id;

	/**
	 * label的key值
	 */
	@Column(name = "`LABEL_KEY`")
	private String labelKey;

	/**
	 * label的value值
	 */
	@Column(name = "`LABEL_VALUE`")
	private String labelValue;

	/**
	 * 服务&组件的类别
	 * 组件类别   {@link com.xxx.xcloud.module.component.consts.CommonConst}
	 * 服务类别   {@See com.xxx.xcloud.consts.Global.SELECTOR_LABEL_SERVICE }
	 */
	@Column(name = "`TYPE`")
	private String type;

	/**
	 * 是否启用当前label
	 */
	@Column(name = "`ENABLE`")
	private String enable;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabelKey() {
		return labelKey;
	}

	public void setLabelKey(String labelKey) {
		this.labelKey = labelKey;
	}

	public String getLabelValue() {
		return labelValue;
	}

	public void setLabelValue(String labelValue) {
		this.labelValue = labelValue;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	@Override
	public String toString() {
		return "SelectorLabel [id=" + id + ", labelKey=" + labelKey + ", labelValue=" + labelValue + ", type=" + type
				+ ", enable=" + enable + "]";
	}

}
