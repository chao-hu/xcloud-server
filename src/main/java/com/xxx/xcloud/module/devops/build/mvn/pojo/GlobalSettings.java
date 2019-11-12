package com.xxx.xcloud.module.devops.build.mvn.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author xingej
 * maven模块 bean
 * maven global settings file
 */
public class GlobalSettings {
	private String clazz;
	private String path;

	@XmlAttribute(name = "class")
	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	@XmlElement(name = "path")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
