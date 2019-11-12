package com.xxx.xcloud.module.devops.build.pojo;

import com.xxx.xcloud.module.devops.build.ant.pojo.Ant;
import com.xxx.xcloud.module.devops.build.gradle.pojo.Gradle;
import com.xxx.xcloud.module.devops.build.mvn.pojo.Maven;
import com.xxx.xcloud.module.devops.build.phing.pojo.PhingBuilder;
import com.xxx.xcloud.module.devops.build.python.pojo.PythonBuilder;
import com.xxx.xcloud.module.devops.build.shell.pojo.Shell;
import com.xxx.xcloud.module.devops.docker.pojo.DockerBuilder;
import com.xxx.xcloud.module.devops.sonar.pojo.SonarRunnerBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author daien
 * @date 2019年3月15日
 */
@XmlType(propOrder = { "sonarRunnerBuilder", "maven", "ant", "gradle", "shell", "dockerBuilder", "phingBuilder",
        "pythonBuilder" })
public class Builders {
    private SonarRunnerBuilder sonarRunnerBuilder;
    private Maven maven;
    private Ant ant;
    private Gradle gradle;
    private List<Shell> shell;
    private DockerBuilder dockerBuilder;
    private PhingBuilder phingBuilder;
    private PythonBuilder pythonBuilder;

    @XmlElement(name = "hudson.tasks.Maven")
    public Maven getMaven() {
        return maven;
    }

    public void setMaven(Maven maven) {
        this.maven = maven;
    }

    @XmlElement(name = "hudson.tasks.Ant")
    public Ant getAnt() {
        return ant;
    }

    public void setAnt(Ant ant) {
        this.ant = ant;
    }

    @XmlElement(name = "hudson.plugins.gradle.Gradle")
    public Gradle getGradle() {
        return gradle;
    }

    public void setGradle(Gradle gradle) {
        this.gradle = gradle;
    }

    @XmlElement(name = "com.nirima.jenkins.plugins.docker.builder.DockerBuilderPublisher")
    public DockerBuilder getDockerBuilder() {
        return dockerBuilder;
    }

    public void setDockerBuilder(DockerBuilder dockerBuilder) {
        this.dockerBuilder = dockerBuilder;
    }

    @XmlElement(name = "hudson.tasks.Shell")
    public List<Shell> getShell() {
        return shell;
    }

    public void setShell(List<Shell> shell) {
        this.shell = shell;
    }

    @XmlElement(name = "hudson.plugins.sonar.SonarRunnerBuilder")
    public SonarRunnerBuilder getSonarRunnerBuilder() {
        return sonarRunnerBuilder;
    }

    public void setSonarRunnerBuilder(SonarRunnerBuilder sonarRunnerBuilder) {
        this.sonarRunnerBuilder = sonarRunnerBuilder;
    }

    @XmlElement(name = "hudson.plugins.phing.PhingBuilder")
    public PhingBuilder getPhingBuilder() {
        return phingBuilder;
    }

    public void setPhingBuilder(PhingBuilder phingBuilder) {
        this.phingBuilder = phingBuilder;
    }

    @XmlElement(name = "jenkins.plugins.shiningpanda.builders.PythonBuilder")
    public PythonBuilder getPythonBuilder() {
        return pythonBuilder;
    }

    public void setPythonBuilder(PythonBuilder pythonBuilder) {
        this.pythonBuilder = pythonBuilder;
    }

}
