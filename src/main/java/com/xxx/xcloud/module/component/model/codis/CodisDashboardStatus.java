package com.xxx.xcloud.module.component.model.codis;


/**
 * @ClassName: CodisDashboardStatus
 * @Description: CodisDashboardStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisDashboardStatus {

	private int nodePort;
	private String svcIp;
	private String svcName;
	private int port;
	private String status;

	public int getNodePort() {
		return nodePort;
	}

	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
