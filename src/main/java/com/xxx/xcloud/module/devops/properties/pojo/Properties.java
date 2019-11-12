package com.xxx.xcloud.module.devops.properties.pojo;

import javax.xml.bind.annotation.XmlElement;

public class Properties {

	private GitLabConnectionProperty gitLabConnectionProperty;

	private ParametersDefinitionProperty parametersDefinitionProperty;

	@XmlElement(name = "com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty")
	public GitLabConnectionProperty getGitLabConnectionProperty() {
		return gitLabConnectionProperty;
	}

	public void setGitLabConnectionProperty(GitLabConnectionProperty gitLabConnectionProperty) {
		this.gitLabConnectionProperty = gitLabConnectionProperty;
	}

	@XmlElement(name = "hudson.model.ParametersDefinitionProperty")
    public ParametersDefinitionProperty getParametersDefinitionProperty() {
        return parametersDefinitionProperty;
    }

    public void setParametersDefinitionProperty(ParametersDefinitionProperty parametersDefinitionProperty) {
        this.parametersDefinitionProperty = parametersDefinitionProperty;
    }

}
