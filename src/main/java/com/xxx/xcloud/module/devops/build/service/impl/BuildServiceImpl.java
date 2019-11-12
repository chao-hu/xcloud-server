package com.xxx.xcloud.module.devops.build.service.impl;

import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.build.ant.service.AntService;
import com.xxx.xcloud.module.devops.build.gradle.service.GradleService;
import com.xxx.xcloud.module.devops.build.mvn.service.MvnService;
import com.xxx.xcloud.module.devops.build.phing.service.PhingService;
import com.xxx.xcloud.module.devops.build.pojo.Builders;
import com.xxx.xcloud.module.devops.build.python.service.PythonService;
import com.xxx.xcloud.module.devops.build.service.BuildService;
import com.xxx.xcloud.module.devops.build.shell.service.ShellService;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.docker.service.DockerService;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.sonar.service.SonarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author daien
 * @date 2019年3月15日
 */
@Service
public class BuildServiceImpl implements BuildService {

    @Autowired
    MvnService mvnService;

    @Autowired
    AntService antService;

    @Autowired
    GradleService gradleService;

    @Autowired
    PhingService phingService;

    @Autowired
    PythonService pythonService;

    @Autowired
    SonarService sonarService;

    @Autowired
    ShellService shellService;

    @Autowired
    DockerService dockerService;

    @Override
    public Builders getBuilders(Job jobModel, String jobType) throws DevopsException {
        Builders builders = new Builders();

        builders.setMaven(mvnService.getMvn(jobModel));
        builders.setAnt(antService.getAnt(jobModel));
        builders.setGradle(gradleService.getGradle(jobModel));
        builders.setShell(shellService.getShellModel(jobModel, jobType));
        builders.setPhingBuilder(phingService.getPhing(jobModel));
        builders.setPythonBuilder(pythonService.getPythonBuilder(jobModel));

        builders.setSonarRunnerBuilder(sonarService.getSonar(jobModel.getSonarModel()));

        if (jobModel.getDockerModel() != null) {
            builders.setDockerBuilder(dockerService.getDocker(jobModel.getDockerModel()));
        }

        return builders;
    }

    @Override
    public String getJdkVersion(Job jobModel) {
        if (jobModel.getLanguageModel() != null
                && CiConstant.DEVOPS_LANG_JAVA.equals(jobModel.getLanguageModel().getLangType())) {
            return jobModel.getLanguageModel().getLangVersion();
        }
        return null;
    }

}
