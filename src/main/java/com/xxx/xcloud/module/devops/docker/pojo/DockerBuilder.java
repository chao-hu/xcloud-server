package com.xxx.xcloud.module.devops.docker.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xujiangpeng
 * @date 2019/3/15
 */
@XmlType(propOrder = { "dockerFileDirectory", "fromRegistry", "tags", "pushOnSuccess", "pushCredentialsId",
        "cleanImages", "cleanupWithJenkinsJobDelete", "cloud" })
public class DockerBuilder {

    /**
     * The docker-plugin version.
     */
    private String plugin;

    private String dockerFileDirectory;

    /**
     * The docker registry which can pull base images. Empty obj if needless.
     */
    private Registry fromRegistry;

    private ImageTag tags;

    /**
     *
     */
    private boolean pushOnSuccess;

    private String pushCredentialsId;

    private Boolean cleanImages;

    private boolean cleanupWithJenkinsJobDelete;

    /**
     * The cloud name has been configured which you want to use.
     */
    private String cloud;

    @XmlAttribute(name = "plugin")
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getDockerFileDirectory() {
        return dockerFileDirectory;
    }

    public void setDockerFileDirectory(String dockerFileDirectory) {
        this.dockerFileDirectory = dockerFileDirectory;
    }

    public Registry getFromRegistry() {
        return fromRegistry;
    }

    public void setFromRegistry(Registry fromRegistry) {
        this.fromRegistry = fromRegistry;
    }

    public ImageTag getTags() {
        return tags;
    }

    public void setTags(ImageTag tags) {
        this.tags = tags;
    }

    public boolean getPushOnSuccess() {
        return pushOnSuccess;
    }

    public void setPushOnSuccess(boolean pushOnSuccess) {
        this.pushOnSuccess = pushOnSuccess;
    }

    public String getPushCredentialsId() {
        return pushCredentialsId;
    }

    public void setPushCredentialsId(String pushCredentialsId) {
        this.pushCredentialsId = pushCredentialsId;
    }

    public Boolean getCleanImages() {
        return cleanImages;
    }

    public void setCleanImages(Boolean cleanImages) {
        this.cleanImages = cleanImages;
    }

    public boolean isCleanupWithJenkinsJobDelete() {
        return cleanupWithJenkinsJobDelete;
    }

    public void setCleanupWithJenkinsJobDelete(boolean cleanupWithJenkinsJobDelete) {
        this.cleanupWithJenkinsJobDelete = cleanupWithJenkinsJobDelete;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }
}



















