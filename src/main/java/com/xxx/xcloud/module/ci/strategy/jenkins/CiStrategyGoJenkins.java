package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.model.LanguageModel;
import com.xxx.xcloud.module.devops.model.ShellModel;
import com.xxx.xcloud.module.ci.model.CiInvokeGo;

/**
 * 
 * @author mengaijun
 * @date: 2019年1月3日 下午6:06:02
 */
public class CiStrategyGoJenkins extends AbstractCiStrategyJenkins {

	public CiStrategyGoJenkins() {
		type = CiConstant.DEVOPS_LANG_GO_INT;
	}

    @Override
    public void addLanguageModelAndBuildModel(Job job, String compileJsonData) {
        if (compileJsonData != null) {
            CiInvokeGo ciInvokeGo = JSONArray.parseObject(compileJsonData, CiInvokeGo.class);
            LanguageModel languageModel = new LanguageModel();
            languageModel.setLangType(CiConstant.DEVOPS_LANG_GO);
            languageModel.setLangVersion(ciInvokeGo.getLangVersion());
            job.setLanguageModel(languageModel);

            // go语言使用shell构建方式
            BuildModel buildModel = new BuildModel();
            ShellModel shellModel = new ShellModel();
            buildModel.setShellModel(shellModel);
            shellModel.setCmd(ciInvokeGo.getBuildcmd());
            job.setBuildModel(buildModel);
        }
    }

    @Override
    public BuildModel generateBuildModel(String compileJsonData) {
        BuildModel buildModel = null;
        if (compileJsonData != null) {
            CiInvokeGo ciInvokeGo = JSONArray.parseObject(compileJsonData, CiInvokeGo.class);
            // go语言使用shell构建方式
            buildModel = new BuildModel();
            ShellModel shellModel = new ShellModel();
            buildModel.setShellModel(shellModel);
            shellModel.setCmd(ciInvokeGo.getBuildcmd());
        }

        return buildModel;
    }

    @Override
    public LanguageModel generateLanguageModel(String compileJsonData) {
        LanguageModel languageModel = null;
        if (compileJsonData != null) {
            CiInvokeGo ciInvokeGo = JSONArray.parseObject(compileJsonData, CiInvokeGo.class);
            languageModel = new LanguageModel();
            languageModel.setLangType(CiConstant.DEVOPS_LANG_GO);
            languageModel.setLangVersion(ciInvokeGo.getLangVersion());
            return languageModel;
        }
        return languageModel;
    }

}
