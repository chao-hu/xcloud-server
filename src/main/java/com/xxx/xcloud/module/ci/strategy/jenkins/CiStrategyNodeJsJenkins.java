package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.LanguageModel;
import com.xxx.xcloud.module.devops.model.ShellModel;
import com.xxx.xcloud.module.ci.model.CiInvokeNodeJs;

/**
 * com.xxx.xcloud.module.ci.strategy.jenkins.CiStrategyNodeJsJenkins
 *
 * @author xujiangpeng
 * @date 2019/6/4
 */
public class CiStrategyNodeJsJenkins extends AbstractCiStrategyJenkins {

    public CiStrategyNodeJsJenkins() {
        type = CiConstant.DEVOPS_LANG_NODEJS_INT;
    }

    @Override
    public BuildModel generateBuildModel(String compileJsonData) {
        BuildModel buildModel = null;
        if (compileJsonData != null) {
            CiInvokeNodeJs ciInvokeNodeJs = JSONArray.parseObject(compileJsonData, CiInvokeNodeJs.class);
            buildModel = new BuildModel();

            ShellModel shellModel = new ShellModel();
            shellModel.setCmd(ciInvokeNodeJs.getBuildcmd());

            buildModel.setShellModel(shellModel);
        }

        return buildModel;
    }

    @Override
    public LanguageModel generateLanguageModel(String compileJsonData) {
        LanguageModel languageModel = null;
        if (compileJsonData != null) {
            CiInvokeNodeJs ciInvokeNodeJs = JSONArray.parseObject(compileJsonData, CiInvokeNodeJs.class);
            languageModel = new LanguageModel();
            languageModel.setLangType(CiConstant.DEVOPS_LANG_NODEJS);
            languageModel.setLangVersion(ciInvokeNodeJs.getLangVersion());
            return languageModel;
        }
        return languageModel;
    }

}
