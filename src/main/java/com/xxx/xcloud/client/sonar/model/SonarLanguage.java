package com.xxx.xcloud.client.sonar.model;

/**
 * @author mengaijun
 * @Description: sonar支持的语言的一种
 * @date: 2018年12月24日 下午4:24:26
 */
public class SonarLanguage {
	
	private String key;
	private String name;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "SonarLanguage [key=" + key + ", name=" + name + "]";
	}
}
