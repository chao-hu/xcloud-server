package com.xxx.xcloud.module.component.model.memcached;
public class MemcachedClusterService {

	private int nodePort;
	private int port;
	private String svcIp;
	private String svcName;

	public int getNodePort() {
		return nodePort;
	}

	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSvcIp() {
		return svcIp;
	}

	public void setSvcIp(String svcIp) {
		this.svcIp = svcIp;
	}

	public String getSvcName() {
		return svcName;
	}

	public void setSvcName(String svcName) {
		this.svcName = svcName;
	}

}
