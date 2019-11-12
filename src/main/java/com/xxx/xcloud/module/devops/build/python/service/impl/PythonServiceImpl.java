package com.xxx.xcloud.module.devops.build.python.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.build.python.pojo.PythonBuilder;
import com.xxx.xcloud.module.devops.build.python.service.PythonService;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.model.PythonModel;
import org.springframework.stereotype.Service;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年4月3日 上午10:21:54
 */
@Service
public class PythonServiceImpl implements PythonService {

    @Override
    public PythonBuilder getPythonBuilder(Job job) {
        if (job.getBuildModel() == null || job.getBuildModel().getPythonModel() == null) {
            return null;
        }

        PythonBuilder pythonBuilder = new PythonBuilder();
        PythonModel pythonModel = job.getBuildModel().getPythonModel();
        pythonBuilder.setCommand(pythonModel.getCommand());
        pythonBuilder.setIgnoreExitCode(pythonModel.isIgnoreExitCode());
        pythonBuilder.setNature(pythonModel.getNature());
        pythonBuilder.setPlugin(
                pythonModel.getPlugin() == null ? XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_PYTHON)
                        : pythonModel.getPlugin());
        pythonBuilder.setPythonName(pythonModel.getPythonName());

        return pythonBuilder;
    }

}
