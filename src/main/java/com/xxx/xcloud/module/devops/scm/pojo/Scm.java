package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "scm")
public class Scm {

	/*scm标签*/
    private String clazz;
    private String plugin;

    /*git标签配置*/
    private Integer configVersion;
    private UserRemoteConfigs userRemoteConfigs;
    private Branches branches;
    private Boolean doGenerateSubmoduleConfigurations;
    private SubmoduleCfg submoduleCfg;
    private String extensions;

    /*svn标签配置*/
    private Locations locations;
    private ExcludedRegions excludedRegions;
    private IncludedRegions includedRegions;
    private ExcludedUsers excludedUsers;
    private ExcludedRevprop excludedRevprop;
    private ExcludedCommitMessages excludedCommitMessages;
    private WorkspaceUpdater  workspaceUpdater;
    private Boolean ignoreDirPropChanges;
    private Boolean filterChangelog;
    private Boolean quietOperation;

    @XmlAttribute(name = "class")
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "configVersion")
    public Integer getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }

    @XmlElement(name = "userRemoteConfigs")
    public UserRemoteConfigs getUserRemoteConfigs() {
        return userRemoteConfigs;
    }

    public void setUserRemoteConfigs(UserRemoteConfigs userRemoteConfigs) {
        this.userRemoteConfigs = userRemoteConfigs;
    }

    @XmlElement(name = "branches")
    public Branches getBranches() {
        return branches;
    }

    public void setBranches(Branches branches) {
        this.branches = branches;
    }

    @XmlElement(name = "doGenerateSubmoduleConfigurations")
    public Boolean getDoGenerateSubmoduleConfigurations() {
        return doGenerateSubmoduleConfigurations;
    }

    public void setDoGenerateSubmoduleConfigurations(Boolean doGenerateSubmoduleConfigurations) {
        this.doGenerateSubmoduleConfigurations = doGenerateSubmoduleConfigurations;
    }

    @XmlElement(name = "submoduleCfg")
    public SubmoduleCfg getSubmoduleCfg() {
        return submoduleCfg;
    }

    public void setSubmoduleCfg(SubmoduleCfg submoduleCfg) {
        this.submoduleCfg = submoduleCfg;
    }

    @XmlElement(name = "extensions")
    public String getExtensions() {
        return extensions;
    }

    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    @XmlElement(name = "locations")
    public Locations getLocations() {
        return locations;
    }

    public void setLocations(Locations locations) {
        this.locations = locations;
    }

    @XmlElement(name = "excludedRegions")
    public ExcludedRegions getExcludedRegions() {
        return excludedRegions;
    }

    public void setExcludedRegions(ExcludedRegions excludedRegions) {
        this.excludedRegions = excludedRegions;
    }

    @XmlElement(name = "includedRegions")
    public IncludedRegions getIncludedRegions() {
        return includedRegions;
    }

    public void setIncludedRegions(IncludedRegions includedRegions) {
        this.includedRegions = includedRegions;
    }

    @XmlElement(name = "excludedUsers")
    public ExcludedUsers getExcludedUsers() {
        return excludedUsers;
    }

    public void setExcludedUsers(ExcludedUsers excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    @XmlElement(name = "excludedRevprop")
    public ExcludedRevprop getExcludedRevprop() {
        return excludedRevprop;
    }

    public void setExcludedRevprop(ExcludedRevprop excludedRevprop) {
        this.excludedRevprop = excludedRevprop;
    }

    @XmlElement(name = "excludedCommitMessages")
    public ExcludedCommitMessages getExcludedCommitMessages() {
        return excludedCommitMessages;
    }

    public void setExcludedCommitMessages(ExcludedCommitMessages excludedCommitMessages) {
        this.excludedCommitMessages = excludedCommitMessages;
    }

    @XmlElement(name = "workspaceUpdater")
    public WorkspaceUpdater getWorkspaceUpdater() {
        return workspaceUpdater;
    }

    public void setWorkspaceUpdater(WorkspaceUpdater workspaceUpdater) {
        this.workspaceUpdater = workspaceUpdater;
    }

    @XmlElement(name = "ignoreDirPropChanges")
    public Boolean getIgnoreDirPropChanges() {
        return ignoreDirPropChanges;
    }

    public void setIgnoreDirPropChanges(Boolean ignoreDirPropChanges) {
        this.ignoreDirPropChanges = ignoreDirPropChanges;
    }

    @XmlElement(name = "filterChangelog")
    public Boolean getFilterChangelog() {
        return filterChangelog;
    }

    public void setFilterChangelog(Boolean filterChangelog) {
        this.filterChangelog = filterChangelog;
    }

    @XmlElement(name = "quietOperation")
    public Boolean getQuietOperation() {
        return quietOperation;
    }

    public void setQuietOperation(Boolean quietOperation) {
        this.quietOperation = quietOperation;
    }

}
