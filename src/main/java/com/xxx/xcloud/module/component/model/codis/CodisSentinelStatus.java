package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

/**
 * @ClassName: CodisSentinelStatus
 * @Description: CodisSentinelStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisSentinelStatus {

	private boolean needRestart;
	private Map<String, Boolean> sentinels;
	private String status;

	public Map<String, Boolean> getSentinels() {
		return sentinels;
	}

	public void setSentinels(Map<String, Boolean> sentinels) {
		this.sentinels = sentinels;
	}

	public boolean isNeedRestart() {
		return needRestart;
	}

	public void setNeedRestart(boolean needRestart) {
		this.needRestart = needRestart;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
