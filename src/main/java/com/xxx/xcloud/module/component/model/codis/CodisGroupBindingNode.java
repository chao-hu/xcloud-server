package com.xxx.xcloud.module.component.model.codis;


/**
 * @ClassName: CodisGroupBindingNode
 * @Description: CodisGroupBindingNode
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisGroupBindingNode {
    private int groupId;
    private String name;
    private String address;
    private String ipAddress;
    private String bindNode;
    private String bindIp;
    private long downTime;
    private String status;
    private boolean needRestart;
    private boolean onDashboard;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public boolean isNeedRestart() {
        return needRestart;
    }

    public void setNeedRestart(boolean needRestart) {
        this.needRestart = needRestart;
    }

    public boolean isOnDashboard() {
        return onDashboard;
    }

    public void setOnDashboard(boolean onDashboard) {
        this.onDashboard = onDashboard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    public long getDownTime() {
        return downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
