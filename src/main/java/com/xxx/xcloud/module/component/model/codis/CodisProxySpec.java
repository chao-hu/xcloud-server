package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.BaseSpec;

/**
 * @ClassName: CodisProxySpec
 * @Description: CodisProxySpec
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisProxySpec extends BaseSpec {

	private Map<String, String> config;
	private String storageClass;

	private String password;

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
