package com.xxx.xcloud.module.component.model.mysql;

/**
 * @author LYX
 *
 */
public class MysqlServer {

	private String id;
	private String role;
	private String name;
	private int nodeport;
	private int exporternodeport;
	private String svcname;
	private String configmapname;
	private String address;
	private String nodeName;
	private String nodeIP;
	private String status;
	private String deststatus;
	private long downTime;

	private String restartaction;
	private String volumeid;

	private String master;
	private int serverID;
	private boolean mmreplstatus;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNodeport() {
		return nodeport;
	}

	public void setNodeport(int nodeport) {
		this.nodeport = nodeport;
	}

	public int getExporternodeport() {
		return exporternodeport;
	}

	public void setExporternodeport(int exporternodeport) {
		this.exporternodeport = exporternodeport;
	}

	public String getSvcname() {
		return svcname;
	}

	public void setSvcname(String svcname) {
		this.svcname = svcname;
	}

	public String getConfigmapname() {
		return configmapname;
	}

	public void setConfigmapname(String configmapname) {
		this.configmapname = configmapname;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeIP() {
		return nodeIP;
	}

	public void setNodeIP(String nodeIP) {
		this.nodeIP = nodeIP;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDeststatus() {
		return deststatus;
	}

	public void setDeststatus(String deststatus) {
		this.deststatus = deststatus;
	}

	public long getDownTime() {
		return downTime;
	}

	public void setDownTime(long downTime) {
		this.downTime = downTime;
	}

	public String getRestartaction() {
		return restartaction;
	}

	public void setRestartaction(String restartaction) {
		this.restartaction = restartaction;
	}

	public String getVolumeid() {
		return volumeid;
	}

	public void setVolumeid(String volumeid) {
		this.volumeid = volumeid;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public boolean isMmreplstatus() {
		return mmreplstatus;
	}

	public void setMmreplstatus(boolean mmreplstatus) {
		this.mmreplstatus = mmreplstatus;
	}

}
