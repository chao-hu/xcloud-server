package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.model.CiInvokePhp;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.LanguageModel;
import com.xxx.xcloud.module.devops.model.PhingModel;

/**
 * php
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年5月30日 上午10:44:13
 */
public class CiStrategyPhpJenkins extends AbstractCiStrategyJenkins {
    public CiStrategyPhpJenkins() {
        type = CiConstant.DEVOPS_LANG_PHP_INT;
    }

    @Override
    public BuildModel generateBuildModel(String compileJsonData) {
        BuildModel buildModel = null;
        if (compileJsonData != null) {
            CiInvokePhp ciInvokePhp = JSONArray.parseObject(compileJsonData, CiInvokePhp.class);

            buildModel = new BuildModel();
            PhingModel phingModel = new PhingModel();
            buildModel.setPhingModel(phingModel);

            phingModel.setTargets(ciInvokePhp.getTargets());
            phingModel.setName(ciInvokePhp.getPhingVersion());
            phingModel.setBuildFile(ciInvokePhp.getPhingBuildFile());
            // 默认为true
            phingModel.setUseModuleRoot(true);
        }

        return buildModel;
    }

    @Override
    public LanguageModel generateLanguageModel(String compileJsonData) {
        LanguageModel languageModel = null;
        if (compileJsonData != null) {
            CiInvokePhp ciInvokePhp = JSONArray.parseObject(compileJsonData, CiInvokePhp.class);
            languageModel = new LanguageModel();
            languageModel.setLangType(CiConstant.DEVOPS_LANG_PHP);
            languageModel.setLangVersion(ciInvokePhp.getPhingVersion());
            return languageModel;
        }
        return languageModel;
    }
}
