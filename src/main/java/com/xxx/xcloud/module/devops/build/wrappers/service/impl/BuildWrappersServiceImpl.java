package com.xxx.xcloud.module.devops.build.wrappers.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.build.wrappers.pojo.AntWrapper;
import com.xxx.xcloud.module.devops.build.wrappers.pojo.BuildWrappers;
import com.xxx.xcloud.module.devops.build.wrappers.pojo.GolangBuildWrapper;
import com.xxx.xcloud.module.devops.build.wrappers.pojo.NodeJsWrapper;
import com.xxx.xcloud.module.devops.build.wrappers.service.BuildWrappersService;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.model.LanguageModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author daien
 * @date 2019年3月15日
 */
@Service
public class BuildWrappersServiceImpl implements BuildWrappersService {

    private static final Logger LOG = LoggerFactory.getLogger(BuildWrappersServiceImpl.class);

    @Override
    public BuildWrappers getBuildWrappers(Job jobModel) throws DevopsException {
        BuildModel buildModel = jobModel.getBuildModel();
        if (buildModel == null) {
            return null;
        }

        BuildWrappers buildWrappers = null;
        LanguageModel languageModel = jobModel.getLanguageModel();
        if (languageModel.getLangType().equals(CiConstant.DEVOPS_LANG_GO)) {
            buildWrappers = new BuildWrappers();

            GolangBuildWrapper golangBuildWrapper = new GolangBuildWrapper();
            golangBuildWrapper.setGoVersion(languageModel.getLangVersion());
            golangBuildWrapper.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_GO));

            buildWrappers.setGolangBuildWrapper(golangBuildWrapper);
        } else if (languageModel.getLangType().equals(CiConstant.DEVOPS_LANG_JAVA)
                && buildModel.getAntModel() != null) {
            buildWrappers = new BuildWrappers();

            AntWrapper antWrapper = new AntWrapper();
            antWrapper.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_ANT));
            antWrapper.setJdk(languageModel.getLangVersion());
            // antWrapper.setInstallation();
            antWrapper.setInstallation(buildModel.getAntModel().getAntName());
            buildWrappers.setAntWrapper(antWrapper);
        }

        //NodeJS wrapper
        String languageType;
        String languageVersion;
        try {
            languageType = jobModel.getLanguageModel().getLangType();
            languageVersion = jobModel.getLanguageModel().getLangVersion();
        } catch (Exception e) {
            LOG.error("Build wrapper get languageType or languageVersion exception!");
            throw e;
        }

        if (CiConstant.DEVOPS_LANG_NODEJS.equalsIgnoreCase(languageType) && StringUtils.isNotEmpty(languageVersion)) {
            NodeJsWrapper nodeJsWrapper = new NodeJsWrapper();
            nodeJsWrapper.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_NODEJS));
            nodeJsWrapper.setInstallationName(languageVersion);

            buildWrappers = new BuildWrappers();
            buildWrappers.setNodeJsWrapper(nodeJsWrapper);
        }

        return buildWrappers;
    }

}
