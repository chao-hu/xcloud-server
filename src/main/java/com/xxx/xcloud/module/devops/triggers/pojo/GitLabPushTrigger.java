package com.xxx.xcloud.module.devops.triggers.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author daien
 * @date 2019年8月1日
 */
public class GitLabPushTrigger {

    private String plugin;

    private String spec;

    private Boolean triggerOnPush = Boolean.TRUE;

    private Boolean triggerOnMergeRequest = Boolean.TRUE;

    private Boolean triggerOnPipelineEvent = Boolean.FALSE;

    private Boolean triggerOnAcceptedMergeRequest = Boolean.FALSE;

    private Boolean triggerOnClosedMergeRequest = Boolean.FALSE;

    private Boolean triggerOnApprovedMergeRequest = Boolean.TRUE;

    private String triggerOpenMergeRequestOnPush;

    private Boolean triggerOnNoteRequest = Boolean.TRUE;

    private String noteRegex = "Jenkins please retry a build";

    private Boolean ciSkip = Boolean.TRUE;

    private Boolean skipWorkInProgressMergeRequest = Boolean.TRUE;

    private Boolean setBuildDescription = Boolean.TRUE;

    private String branchFilterType = "All";

    private String includeBranchesSpec;

    private String excludeBranchesSpec;

    private String sourceBranchRegex;

    private String targetBranchRegex;

    private MergeRequestLabelFilterConfig mergeRequestLabelFilterConfig;

    private String secretToken;

    private String pendingBuildName;

    private Boolean cancelPendingBuildsOnUpdate = Boolean.FALSE;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XmlElement(name = "spec")
    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    @XmlElement(name = "triggerOnPush")
    public Boolean getTriggerOnPush() {
        return triggerOnPush;
    }

    public void setTriggerOnPush(Boolean triggerOnPush) {
        this.triggerOnPush = triggerOnPush;
    }

    @XmlElement(name = "triggerOnMergeRequest")
    public Boolean getTriggerOnMergeRequest() {
        return triggerOnMergeRequest;
    }

    public void setTriggerOnMergeRequest(Boolean triggerOnMergeRequest) {
        this.triggerOnMergeRequest = triggerOnMergeRequest;
    }

    @XmlElement(name = "triggerOnPipelineEvent")
    public Boolean getTriggerOnPipelineEvent() {
        return triggerOnPipelineEvent;
    }

    public void setTriggerOnPipelineEvent(Boolean triggerOnPipelineEvent) {
        this.triggerOnPipelineEvent = triggerOnPipelineEvent;
    }

    @XmlElement(name = "triggerOnAcceptedMergeRequest")
    public Boolean getTriggerOnAcceptedMergeRequest() {
        return triggerOnAcceptedMergeRequest;
    }

    public void setTriggerOnAcceptedMergeRequest(Boolean triggerOnAcceptedMergeRequest) {
        this.triggerOnAcceptedMergeRequest = triggerOnAcceptedMergeRequest;
    }

    @XmlElement(name = "triggerOnClosedMergeRequest")
    public Boolean getTriggerOnClosedMergeRequest() {
        return triggerOnClosedMergeRequest;
    }

    public void setTriggerOnClosedMergeRequest(Boolean triggerOnClosedMergeRequest) {
        this.triggerOnClosedMergeRequest = triggerOnClosedMergeRequest;
    }

    @XmlElement(name = "triggerOnApprovedMergeRequest")
    public Boolean getTriggerOnApprovedMergeRequest() {
        return triggerOnApprovedMergeRequest;
    }

    public void setTriggerOnApprovedMergeRequest(Boolean triggerOnApprovedMergeRequest) {
        this.triggerOnApprovedMergeRequest = triggerOnApprovedMergeRequest;
    }

    @XmlElement(name = "triggerOpenMergeRequestOnPush")
    public String getTriggerOpenMergeRequestOnPush() {
        return triggerOpenMergeRequestOnPush;
    }

    public void setTriggerOpenMergeRequestOnPush(String triggerOpenMergeRequestOnPush) {
        this.triggerOpenMergeRequestOnPush = triggerOpenMergeRequestOnPush;
    }

    @XmlElement(name = "triggerOnNoteRequest")
    public Boolean getTriggerOnNoteRequest() {
        return triggerOnNoteRequest;
    }

    public void setTriggerOnNoteRequest(Boolean triggerOnNoteRequest) {
        this.triggerOnNoteRequest = triggerOnNoteRequest;
    }

    @XmlElement(name = "noteRegex")
    public String getNoteRegex() {
        return noteRegex;
    }

    public void setNoteRegex(String noteRegex) {
        this.noteRegex = noteRegex;
    }

    @XmlElement(name = "ciSkip")
    public Boolean getCiSkip() {
        return ciSkip;
    }

    public void setCiSkip(Boolean ciSkip) {
        this.ciSkip = ciSkip;
    }

    @XmlElement(name = "skipWorkInProgressMergeRequest")
    public Boolean getSkipWorkInProgressMergeRequest() {
        return skipWorkInProgressMergeRequest;
    }

    public void setSkipWorkInProgressMergeRequest(Boolean skipWorkInProgressMergeRequest) {
        this.skipWorkInProgressMergeRequest = skipWorkInProgressMergeRequest;
    }

    @XmlElement(name = "setBuildDescription")
    public Boolean getSetBuildDescription() {
        return setBuildDescription;
    }

    public void setSetBuildDescription(Boolean setBuildDescription) {
        this.setBuildDescription = setBuildDescription;
    }

    @XmlElement(name = "branchFilterType")
    public String getBranchFilterType() {
        return branchFilterType;
    }

    public void setBranchFilterType(String branchFilterType) {
        this.branchFilterType = branchFilterType;
    }

    @XmlElement(name = "includeBranchesSpec")
    public String getIncludeBranchesSpec() {
        return includeBranchesSpec;
    }

    public void setIncludeBranchesSpec(String includeBranchesSpec) {
        this.includeBranchesSpec = includeBranchesSpec;
    }

    @XmlElement(name = "excludeBranchesSpec")
    public String getExcludeBranchesSpec() {
        return excludeBranchesSpec;
    }

    public void setExcludeBranchesSpec(String excludeBranchesSpec) {
        this.excludeBranchesSpec = excludeBranchesSpec;
    }

    @XmlElement(name = "sourceBranchRegex")
    public String getSourceBranchRegex() {
        return sourceBranchRegex;
    }

    public void setSourceBranchRegex(String sourceBranchRegex) {
        this.sourceBranchRegex = sourceBranchRegex;
    }

    @XmlElement(name = "targetBranchRegex")
    public String getTargetBranchRegex() {
        return targetBranchRegex;
    }

    public void setTargetBranchRegex(String targetBranchRegex) {
        this.targetBranchRegex = targetBranchRegex;
    }

    @XmlElement(name = "mergeRequestLabelFilterConfig")
    public MergeRequestLabelFilterConfig getMergeRequestLabelFilterConfig() {
        return mergeRequestLabelFilterConfig;
    }

    public void setMergeRequestLabelFilterConfig(MergeRequestLabelFilterConfig mergeRequestLabelFilterConfig) {
        this.mergeRequestLabelFilterConfig = mergeRequestLabelFilterConfig;
    }

    @XmlElement(name = "secretToken")
    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    @XmlElement(name = "pendingBuildName")
    public String getPendingBuildName() {
        return pendingBuildName;
    }

    public void setPendingBuildName(String pendingBuildName) {
        this.pendingBuildName = pendingBuildName;
    }

    @XmlElement(name = "cancelPendingBuildsOnUpdate")
    public Boolean getCancelPendingBuildsOnUpdate() {
        return cancelPendingBuildsOnUpdate;
    }

    public void setCancelPendingBuildsOnUpdate(Boolean cancelPendingBuildsOnUpdate) {
        this.cancelPendingBuildsOnUpdate = cancelPendingBuildsOnUpdate;
    }

}
