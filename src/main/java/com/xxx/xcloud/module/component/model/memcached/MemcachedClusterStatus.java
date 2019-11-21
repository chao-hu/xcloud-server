package com.xxx.xcloud.module.component.model.memcached;
import java.util.List;
import java.util.Map;

import com.xxx.xcloud.module.component.model.base.StatusCondition;


public class MemcachedClusterStatus {

	private String phase;
	private String reason;
	private String lastAction;
	private boolean needRestart;
	private Map<String, MemcachedClusterGroupInfo> groups;
	private List<StatusCondition> conditions;

	public Map<String, MemcachedClusterGroupInfo> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, MemcachedClusterGroupInfo> groups) {
		this.groups = groups;
	}

	public boolean isNeedRestart() {
		return needRestart;
	}

	public void setNeedRestart(boolean needRestart) {
		this.needRestart = needRestart;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getLastAction() {
		return lastAction;
	}

	public void setLastAction(String lastAction) {
		this.lastAction = lastAction;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public List<StatusCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<StatusCondition> conditions) {
		this.conditions = conditions;
	}

}
