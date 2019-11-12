package com.xxx.xcloud.module.devops.model;

public class BuildModel {

    private MvnModel mvnModel;

    private AntModel antModel;

    private GradleModel gradleModel;

    private ShellModel shellModel;

    private PhingModel phingModel;

    private PythonModel pythonModel;

    public MvnModel getMvnModel() {
        return mvnModel;
    }

    public void setMvnModel(MvnModel mvnModel) {
        this.mvnModel = mvnModel;
    }

    public AntModel getAntModel() {
        return antModel;
    }

    public void setAntModel(AntModel antModel) {
        this.antModel = antModel;
    }

    public GradleModel getGradleModel() {
        return gradleModel;
    }

    public void setGradleModel(GradleModel gradleModel) {
        this.gradleModel = gradleModel;
    }

    public ShellModel getShellModel() {
        return shellModel;
    }

    public void setShellModel(ShellModel shellModel) {
        this.shellModel = shellModel;
    }

    public PhingModel getPhingModel() {
        return phingModel;
    }

    public void setPhingModel(PhingModel phingModel) {
        this.phingModel = phingModel;
    }

    public PythonModel getPythonModel() {
        return pythonModel;
    }

    public void setPythonModel(PythonModel pythonModel) {
        this.pythonModel = pythonModel;
    }

}
