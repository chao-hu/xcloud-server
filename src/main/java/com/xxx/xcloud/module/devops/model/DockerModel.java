package com.xxx.xcloud.module.devops.model;

/**
 * The model contains information to init docker job.
 *
 * @author xujiangpeng
 * @date 2019/3/26
 */
public class DockerModel {

    /**
     * True if you want to use user-defined dockerfile.
     */
    private boolean isUserDefined;

    /**
     * The user-defined dockerfile context.
     */
    private String dockerFileContext;

    /**
     * The dockerfile dir and name.
     */
    private String dockerFileDirectory;

    /**
     * True if you want use external registry to pull base images.
     */
    private boolean externalRegistry;
    private String externalRegistryUrl;
    private String externalRegistryCredentialsId;

    /**
     * The imageName must contains registry url(port)
     * such as 192.168.128.1:8443/project/{ImageName}:{Tag}
     */
    private String imageName;

    /**
     * If true and the docker image builds successfully,the resulting docker image
     * will be pushed to the registry specified within the "ImageName" field.
     */
    private boolean pushOnSuccess;

    /**
     * Credentials to push to a private registry.
     */
    private String pushCredentialsId;

    /**
     * If true clean local images which current job generated.
     */
    private boolean cleanImages;

    public boolean isUserDefined() {
        return isUserDefined;
    }

    public void setUserDefined(boolean userDefined) {
        isUserDefined = userDefined;
    }

    public String getDockerFileDirectory() {
        return dockerFileDirectory;
    }

    public void setDockerFileDirectory(String dockerFileDirectory) {
        this.dockerFileDirectory = dockerFileDirectory;
    }

    public boolean isExternalRegistry() {
        return externalRegistry;
    }

    public void setExternalRegistry(boolean externalRegistry) {
        this.externalRegistry = externalRegistry;
    }

    public String getExternalRegistryUrl() {
        return externalRegistryUrl;
    }

    public void setExternalRegistryUrl(String externalRegistryUrl) {
        this.externalRegistryUrl = externalRegistryUrl;
    }

    public String getExternalRegistryCredentialsId() {
        return externalRegistryCredentialsId;
    }

    public void setExternalRegistryCredentialsId(String externalRegistryCredentialsId) {
        this.externalRegistryCredentialsId = externalRegistryCredentialsId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean isPushOnSuccess() {
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

    public boolean getCleanImages() {
        return cleanImages;
    }

    public void setCleanImages(boolean cleanImages) {
        this.cleanImages = cleanImages;
    }

    public String getDockerFileContext() {
        return dockerFileContext;
    }

    public void setDockerFileContext(String dockerFileContext) {
        this.dockerFileContext = dockerFileContext;
    }

    public boolean isCleanImages() {
        return cleanImages;
    }

}
