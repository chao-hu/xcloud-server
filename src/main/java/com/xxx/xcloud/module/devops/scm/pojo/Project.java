package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "project")
public class Project {

    private Scm scm;

    @XmlElement(name = "scm")
    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }
}
