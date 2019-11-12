package com.xxx.xcloud.module.devops.job.pojo;

import com.xxx.xcloud.module.devops.actions.pojo.Actions;
import com.xxx.xcloud.module.devops.build.pojo.Builders;
import com.xxx.xcloud.module.devops.build.wrappers.pojo.BuildWrappers;
import com.xxx.xcloud.module.devops.properties.pojo.Properties;
import com.xxx.xcloud.module.devops.publishers.pojo.Publishers;
import com.xxx.xcloud.module.devops.scm.pojo.Scm;
import com.xxx.xcloud.module.devops.triggers.pojo.Triggers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author daien
 * @date 2019年3月11日
 */
@XmlRootElement(name = "project")
public class Project {

    private Actions actions;  //post-build Actions
    private String description;
    private Boolean keepDependencies; //default
    private Properties properties;
    private Scm scm;
    private Boolean canRoam; //default
    private Boolean disabled; //default
    private Boolean blockBuildWhenDownstreamBuilding; //default
    private Boolean blockBuildWhenUpstreamBuilding; //default
    private String jdk;
    private Triggers triggers;
    private Boolean concurrentBuild; //default
    private Builders builders;  //构建包括 mvn,ant,shell,sonar,docker
    private Publishers publishers;
    private BuildWrappers buildWrappers;
    private String customWorkspace;

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "keepDependencies")
    public Boolean getKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(Boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    @XmlElement(name = "properties")
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @XmlElement(name = "scm")
    public Scm getScm() {
        return scm;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    @XmlElement(name = "canRoam")
    public Boolean getCanRoam() {
        return canRoam;
    }

    public void setCanRoam(Boolean canRoam) {
        this.canRoam = canRoam;
    }

    @XmlElement(name = "disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @XmlElement(name = "blockBuildWhenDownstreamBuilding")
    public Boolean getBlockBuildWhenDownstreamBuilding() {
        return blockBuildWhenDownstreamBuilding;
    }

    public void setBlockBuildWhenDownstreamBuilding(Boolean blockBuildWhenDownstreamBuilding) {
        this.blockBuildWhenDownstreamBuilding = blockBuildWhenDownstreamBuilding;
    }

    @XmlElement(name = "blockBuildWhenUpstreamBuilding")
    public Boolean getBlockBuildWhenUpstreamBuilding() {
        return blockBuildWhenUpstreamBuilding;
    }

    public void setBlockBuildWhenUpstreamBuilding(Boolean blockBuildWhenUpstreamBuilding) {
        this.blockBuildWhenUpstreamBuilding = blockBuildWhenUpstreamBuilding;
    }

    @XmlElement(name = "jdk")
    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    @XmlElement(name = "concurrentBuild")
    public Boolean getConcurrentBuild() {
        return concurrentBuild;
    }

    public void setConcurrentBuild(Boolean concurrentBuild) {
        this.concurrentBuild = concurrentBuild;
    }

    @XmlElement(name = "builders")
    public Builders getBuilders() {
        return builders;
    }

    public void setBuilders(Builders builders) {
        this.builders = builders;
    }

    @XmlElement(name = "actions")
    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @XmlElement(name = "triggers")
    public Triggers getTriggers() {
        return triggers;
    }

    public void setTriggers(Triggers triggers) {
        this.triggers = triggers;
    }

    @XmlElement(name = "publishers")
    public Publishers getPublishers() {
        return publishers;
    }

    public void setPublishers(Publishers publishers) {
        this.publishers = publishers;
    }

    @XmlElement(name = "buildWrappers")
    public BuildWrappers getBuildWrappers() {
        return buildWrappers;
    }

    public void setBuildWrappers(BuildWrappers buildWrappers) {
        this.buildWrappers = buildWrappers;
    }

    @XmlElement(name = "customWorkspace")
    public String getCustomWorkspace() {
        return customWorkspace;
    }

    public void setCustomWorkspace(String customWorkspace) {
        this.customWorkspace = customWorkspace;
    }

}
