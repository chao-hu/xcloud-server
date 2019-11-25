package com.xxx.xcloud.module.devops.build.phing.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.build.phing.pojo.PhingBuilder;
import com.xxx.xcloud.module.devops.build.phing.service.PhingService;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.model.PhingModel;
import com.xxx.xcloud.utils.StringUtils;
import org.springframework.stereotype.Service;


/**
 * phing接口
 * 
 * @author mengaijun
 * @date: 2019年4月2日 上午10:34:16
 */
@Service
public class PhingServiceImpl implements PhingService {

    @Override
    public PhingBuilder getPhing(Job job) {
        if (job.getBuildModel() == null || job.getBuildModel().getPhingModel() == null) {
            return null;
        }

        PhingModel phingModel = job.getBuildModel().getPhingModel();
        PhingBuilder phingBuilder = new PhingBuilder();
        phingBuilder.setBuildFile(phingModel.getBuildFile());
        phingBuilder.setName(phingModel.getName());
        phingBuilder.setOptions(phingModel.getOptions());
        phingBuilder.setPlugin(StringUtils.isEmpty(phingModel.getPlugin())
                ? XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_PHING)
                : phingModel.getPlugin());
        phingBuilder.setProperties(phingModel.getProperties());
        phingBuilder.setTargets(phingModel.getTargets());
        phingBuilder.setUseModuleRoot(phingModel.isUseModuleRoot());

        return phingBuilder;
    }

}
