package com.xxx.xcloud.module.component.model.redis;

public class RedisBindingNode {
	private String name;
	private String role;
	private String address;
	private String bindNode;
	private String bindIp;
	private String status;
	private String opAction;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getBindNode() {
		return bindNode;
	}

	public void setBindNode(String bindNode) {
		this.bindNode = bindNode;
	}

	public String getBindIp() {
		return bindIp;
	}

	public void setBindIp(String bindIp) {
		this.bindIp = bindIp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOpAction() {
		return opAction;
	}

	public void setOpAction(String opAction) {
		this.opAction = opAction;
	}

}