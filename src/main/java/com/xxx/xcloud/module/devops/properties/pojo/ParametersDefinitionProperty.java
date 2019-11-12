package com.xxx.xcloud.module.devops.properties.pojo;

import javax.xml.bind.annotation.XmlElement;

public class ParametersDefinitionProperty {

    private ParameterDefinitions parameterDefinitions;

    @XmlElement(name = "parameterDefinitions")
    public ParameterDefinitions getParameterDefinitions() {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(ParameterDefinitions parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

}
