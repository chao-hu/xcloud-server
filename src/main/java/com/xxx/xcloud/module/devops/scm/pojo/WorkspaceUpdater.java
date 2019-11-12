package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlAttribute;

public class WorkspaceUpdater {
    private String clazz = "hudson.scm.subversion.UpdateUpdater";

    @XmlAttribute(name = "class")
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
