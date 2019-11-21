package com.xxx.xcloud.module.component.model.mysql;

public class MysqlPhpMyAdmin {

    private String name;
    private int phpMyAdminExterPort;
    private int phpMyAdminInterPort;
    private String phpMyAdminExterHost;
    private String phpMyAdminInterHost;
    private String nodeName;
    private String serverStatus;
    private String deststatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPhpMyAdminExterPort() {
        return phpMyAdminExterPort;
    }

    public void setPhpMyAdminExterPort(int phpMyAdminExterPort) {
        this.phpMyAdminExterPort = phpMyAdminExterPort;
    }

    public int getPhpMyAdminInterPort() {
        return phpMyAdminInterPort;
    }

    public void setPhpMyAdminInterPort(int phpMyAdminInterPort) {
        this.phpMyAdminInterPort = phpMyAdminInterPort;
    }

    public String getPhpMyAdminExterHost() {
        return phpMyAdminExterHost;
    }

    public void setPhpMyAdminExterHost(String phpMyAdminExterHost) {
        this.phpMyAdminExterHost = phpMyAdminExterHost;
    }

    public String getPhpMyAdminInterHost() {
        return phpMyAdminInterHost;
    }

    public void setPhpMyAdminInterHost(String phpMyAdminInterHost) {
        this.phpMyAdminInterHost = phpMyAdminInterHost;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String serverStatus) {
        this.serverStatus = serverStatus;
    }

    public String getDeststatus() {
        return deststatus;
    }

    public void setDeststatus(String deststatus) {
        this.deststatus = deststatus;
    }

}
