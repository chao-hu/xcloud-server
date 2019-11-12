package com.xxx.xcloud.module.devops.build.gradle.service.impl;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.devops.build.gradle.pojo.Gradle;
import com.xxx.xcloud.module.devops.build.gradle.service.GradleService;
import com.xxx.xcloud.module.devops.model.GradleModel;
import com.xxx.xcloud.module.devops.model.Job;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author daien
 * @date 2019年7月30日
 */
@Service
public class GradleServiceImpl implements GradleService {

    @Override
    public Gradle getGradle(Job job) {
        GradleModel gradleModel = null;
        if (job != null && job.getBuildModel() != null) {
            gradleModel = job.getBuildModel().getGradleModel();
        }

        Gradle gradle = null;
        if (gradleModel != null) {
            gradle = new Gradle();
            gradle.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_GRADLE));

            BeanUtils.copyProperties(gradleModel, gradle);
            // if (gradleModel.getUseWrapper()) {
            // gradle.setBuildFile(gradleModel.getBuildFile());
            // gradle.setMakeExecutable(gradleModel.getMakeExecutable());
            // gradle.setPassAllAsProjectProperties(gradleModel.getPassAllAsProjectProperties());
            // gradle.setPassAllAsSystemProperties(gradleModel.getPassAllAsSystemProperties());
            // gradle.setProjectProperties(gradleModel.getProjectProperties());
            // gradle.setRootBuildScriptDir(gradleModel.getRootBuildScriptDir());
            // gradle.setSwitchs(gradleModel.getSwitchs());
            // gradle.setTasks(gradleModel.getTasks());
            // gradle.setUseWorkspaceAsHome(gradleModel.getUseWorkspaceAsHome());
            // gradle.setWrapperLocation(gradleModel.getWrapperLocation());
            // gradle.setSystemProperties(gradleModel.getSystemProperties());
            // } else {
            // gradle.setGradleName(gradleModel.getGradleName());
            // }
        }

        return gradle;
    }

}
