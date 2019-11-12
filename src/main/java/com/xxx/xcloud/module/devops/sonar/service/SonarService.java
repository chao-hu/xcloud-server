package com.xxx.xcloud.module.devops.sonar.service;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.SonarModel;
import com.xxx.xcloud.module.devops.sonar.pojo.SonarRunnerBuilder;

public interface SonarService {

    SonarRunnerBuilder getSonar(SonarModel sonarModel) throws DevopsException;

}
