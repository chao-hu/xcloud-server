package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author xingej
 */
public class SubmoduleCfg {

	private String clazz;

	@XmlAttribute(name = "class")
	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
}
