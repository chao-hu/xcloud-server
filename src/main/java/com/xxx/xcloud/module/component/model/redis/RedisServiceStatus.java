package com.xxx.xcloud.module.component.model.redis;

public class RedisServiceStatus {
	private String role;
	private String svcIp;
	private int nodePort;
	private String svcName;
	private int port;
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getSvcIp() {
		return svcIp;
	}
	public void setSvcIp(String svcIp) {
		this.svcIp = svcIp;
	}
	public int getNodePort() {
		return nodePort;
	}
	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}
	public String getSvcName() {
		return svcName;
	}
	public void setSvcName(String svcName) {
		this.svcName = svcName;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	
}