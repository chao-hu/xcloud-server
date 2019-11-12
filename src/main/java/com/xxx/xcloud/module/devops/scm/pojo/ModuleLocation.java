package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "remote", "credentialsId", "local", "depthOption", "ignoreExternalsOption",
        "cancelProcessOnExternalsFail" })
public class ModuleLocation {

    private String remote;
    private String credentialsId;
    private String local;
    private String depthOption;
    private Boolean ignoreExternalsOption = true;
    private Boolean cancelProcessOnExternalsFail = true;

    @XmlElement(name = "remote")
    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    @XmlElement(name = "credentialsId")
    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @XmlElement(name = "local")
    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    @XmlElement(name = "depthOption")
    public String getDepthOption() {
        return depthOption;
    }

    public void setDepthOption(String depthOption) {
        this.depthOption = depthOption;
    }

    @XmlElement(name = "ignoreExternalsOption")
    public Boolean getIgnoreExternalsOption() {
        return ignoreExternalsOption;
    }

    public void setIgnoreExternalsOption(Boolean ignoreExternalsOption) {
        this.ignoreExternalsOption = ignoreExternalsOption;
    }

    @XmlElement(name = "cancelProcessOnExternalsFail")
    public Boolean getCancelProcessOnExternalsFail() {
        return cancelProcessOnExternalsFail;
    }

    public void setCancelProcessOnExternalsFail(Boolean cancelProcessOnExternalsFail) {
        this.cancelProcessOnExternalsFail = cancelProcessOnExternalsFail;
    }

}
