package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlElement;

public class Locations {

    private ModuleLocation moduleLocation;

    @XmlElement(name = "hudson.scm.SubversionSCM_-ModuleLocation")
    public ModuleLocation getModuleLocation() {
        return moduleLocation;
    }

    public void setModuleLocation(ModuleLocation moduleLocation) {
        this.moduleLocation = moduleLocation;
    }

}
