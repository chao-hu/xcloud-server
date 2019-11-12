package com.xxx.xcloud.module.devops.build.wrappers.pojo;

import javax.xml.bind.annotation.XmlElement;

public class BuildWrappers {

    private AntWrapper antWrapper;

    private GolangBuildWrapper golangBuildWrapper;

    private NodeJsWrapper nodeJsWrapper;

    @XmlElement(name = "hudson.tasks.AntWrapper")
    public AntWrapper getAntWrapper() {
        return antWrapper;
    }

    public void setAntWrapper(AntWrapper antWrapper) {
        this.antWrapper = antWrapper;
    }

    @XmlElement(name = "org.jenkinsci.plugins.golang.GolangBuildWrapper")
    public GolangBuildWrapper getGolangBuildWrapper() {
        return golangBuildWrapper;
    }

    public void setGolangBuildWrapper(GolangBuildWrapper golangBuildWrapper) {
        this.golangBuildWrapper = golangBuildWrapper;
    }

    @XmlElement(name = "jenkins.plugins.nodejs.NodeJSBuildWrapper")
    public NodeJsWrapper getNodeJsWrapper() {
        return nodeJsWrapper;
    }

    public void setNodeJsWrapper(NodeJsWrapper nodeJsWrapper) {
        this.nodeJsWrapper = nodeJsWrapper;
    }
}
