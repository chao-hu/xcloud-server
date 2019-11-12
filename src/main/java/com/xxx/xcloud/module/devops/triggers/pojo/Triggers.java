package com.xxx.xcloud.module.devops.triggers.pojo;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author daien
 * @date 2019年8月1日
 */
public class Triggers {

    private GitLabPushTrigger gitLabPushTrigger;

    @XmlElement(name = "com.dabsquared.gitlabjenkins.GitLabPushTrigger")
    public GitLabPushTrigger getGitLabPushTrigger() {
        return gitLabPushTrigger;
    }

    public void setGitLabPushTrigger(GitLabPushTrigger gitLabPushTrigger) {
        this.gitLabPushTrigger = gitLabPushTrigger;
    }

}
