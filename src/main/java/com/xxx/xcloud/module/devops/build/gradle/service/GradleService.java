package com.xxx.xcloud.module.devops.build.gradle.service;

import com.xxx.xcloud.module.devops.build.gradle.pojo.Gradle;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * @author daien
 * @date 2019年7月30日
 */
public interface GradleService {

    Gradle getGradle(Job job);

}
