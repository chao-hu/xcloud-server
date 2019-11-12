package com.xxx.xcloud.module.devops.build.ant.service.impl;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.devops.build.ant.pojo.Ant;
import com.xxx.xcloud.module.devops.build.ant.service.AntService;
import com.xxx.xcloud.module.devops.model.AntModel;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AntServiceImpl implements AntService {

    @Override
    public Ant getAnt(Job jobModel) {
        AntModel antModel = null;
        if (jobModel.getBuildModel() != null) {
            antModel = jobModel.getBuildModel().getAntModel();
        }

        Ant ant = null;
        if (antModel != null) {
            ant = new Ant();
            ant.setAntName(antModel.getAntName());
            ant.setAntOpts(antModel.getAntOpts());
            ant.setBuildFile(antModel.getBuildFile());
            ant.setPlugin(StringUtils.isEmpty(antModel.getPlugin())
                    ? XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_ANT)
                    : antModel.getPlugin());
            ant.setProperties(antModel.getProperties());
            ant.setTargets(antModel.getTargets());
        }
        return ant;
    }

}
