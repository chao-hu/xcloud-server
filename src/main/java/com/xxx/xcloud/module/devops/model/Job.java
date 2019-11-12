package com.xxx.xcloud.module.devops.model;

public class Job {

    // 租户名
    private String namespace;
    // 任务名
    private String name;
    // 任务描述
    private String description;
    private LanguageModel languageModel;
    private ScmModel scmModel;
    private SonarModel sonarModel;
    private BuildModel buildModel;
    private DockerModel dockerModel;
    private Boolean hook = Boolean.FALSE;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScmModel getScmModel() {
        return scmModel;
    }

    public void setScmModel(ScmModel scmModel) {
        this.scmModel = scmModel;
    }

    public BuildModel getBuildModel() {
        return buildModel;
    }

    public void setBuildModel(BuildModel buildModel) {
        this.buildModel = buildModel;
    }

    public SonarModel getSonarModel() {
        return sonarModel;
    }

    public void setSonarModel(SonarModel sonarModel) {
        this.sonarModel = sonarModel;
    }

    public DockerModel getDockerModel() {
        return dockerModel;
    }

    public void setDockerModel(DockerModel dockerModel) {
        this.dockerModel = dockerModel;
    }

    public LanguageModel getLanguageModel() {
        return languageModel;
    }

    public void setLanguageModel(LanguageModel languageModel) {
        this.languageModel = languageModel;
    }

    public Boolean getHook() {
        return hook;
    }

    public void setHook(Boolean hook) {
        this.hook = hook;
    }

}
