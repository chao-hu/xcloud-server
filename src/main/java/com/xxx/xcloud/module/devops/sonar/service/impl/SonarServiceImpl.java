package com.xxx.xcloud.module.devops.sonar.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.SonarModel;
import com.xxx.xcloud.module.devops.sonar.pojo.SonarRunnerBuilder;
import com.xxx.xcloud.module.devops.sonar.service.SonarService;
import com.xxx.xcloud.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service("devops_sonarServiceImpl")
public class SonarServiceImpl implements SonarService {

    @Override
    public SonarRunnerBuilder getSonar(SonarModel sonarModel) throws DevopsException {
        if (sonarModel == null) {
            return null;
        }

        String properties = getProperties(sonarModel);
        SonarRunnerBuilder sonarRunnerBuilder = new SonarRunnerBuilder();
        sonarRunnerBuilder.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_SONAR));
        sonarRunnerBuilder.setTask("scan");
        sonarRunnerBuilder.setJdk("(Inherit From Job)");
        sonarRunnerBuilder.setProperties(properties);
        /**
         * jenkins系统配置中，配置的sonar服务的名称
         */
        sonarRunnerBuilder.setInstallationName("localsonar");

        return sonarRunnerBuilder;
    }

    private String getProperties(SonarModel sonarModel) throws DevopsException {
        if (StringUtils.isEmpty(sonarModel.getProjectName())) {
            throw new DevopsException(500, "projectName为空");
        }

        if (StringUtils.isEmpty(sonarModel.getProjectKey())) {
            throw new DevopsException(500, "projectKey为空");
        }

        if (StringUtils.isEmpty(sonarModel.getProjectVersion())) {
            throw new DevopsException(500, "projectVersion为空");
        }

        if (StringUtils.isEmpty(sonarModel.getLanguage())) {
            throw new DevopsException(500, "language为空");
        }

        if (StringUtils.isEmpty(sonarModel.getSources())) {
            throw new DevopsException(500, "sources为空");
        }

        if (StringUtils.isEmpty(sonarModel.getProfile())) {
            throw new DevopsException(500, "profile为空");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sonar.projectName=");
        stringBuilder.append(sonarModel.getProjectName());
        stringBuilder.append("\r\n");

        stringBuilder.append("sonar.projectKey=");
        stringBuilder.append(sonarModel.getProjectKey());
        stringBuilder.append("\r\n");

        stringBuilder.append("sonar.projectVersion=");
        stringBuilder.append(sonarModel.getProjectVersion());
        stringBuilder.append("\r\n");

        stringBuilder.append("sonar.language=");
        stringBuilder.append(sonarModel.getLanguage());
        stringBuilder.append("\r\n");

        stringBuilder.append("sonar.sources=");
        stringBuilder.append(sonarModel.getSources());
        stringBuilder.append("\r\n");

        if (sonarModel.getLanguage().equals(CiConstant.DEVOPS_LANG_JAVA)) {
            stringBuilder.append("sonar.java.binaries=.");
            stringBuilder.append("\r\n");
        }

        stringBuilder.append("sonar.profile=");
        stringBuilder.append(sonarModel.getProfile());

        return stringBuilder.toString();
    }

}
