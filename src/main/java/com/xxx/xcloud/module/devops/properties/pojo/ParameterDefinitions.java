package com.xxx.xcloud.module.devops.properties.pojo;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class ParameterDefinitions {

    private List<TextParameterDefinition> textParameterDefinitions;

    @XmlElement(name = "hudson.model.TextParameterDefinition")
    public List<TextParameterDefinition> getTextParameterDefinitions() {
        return textParameterDefinitions;
    }

    public void setTextParameterDefinitions(List<TextParameterDefinition> textParameterDefinitions) {
        this.textParameterDefinitions = textParameterDefinitions;
    }

}
