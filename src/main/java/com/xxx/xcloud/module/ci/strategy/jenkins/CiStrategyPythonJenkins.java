package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.model.CiInvokePython;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.LanguageModel;
import com.xxx.xcloud.module.devops.model.PythonModel;

/**
 * @author mengaijun
 * @date: 2019年1月3日 下午6:06:25
 */
public class CiStrategyPythonJenkins extends AbstractCiStrategyJenkins {

    public CiStrategyPythonJenkins() {
        type = CiConstant.DEVOPS_LANG_PYTHON_INT;
    }

    @Override
    public BuildModel generateBuildModel(String compileJsonData) {
        BuildModel buildModel = null;
        if (compileJsonData != null) {
            CiInvokePython ciInvokePython = JSONArray.parseObject(compileJsonData, CiInvokePython.class);

            buildModel = new BuildModel();
            PythonModel pythonModel = new PythonModel();
            buildModel.setPythonModel(pythonModel);

            // 未完
            pythonModel.setCommand(ciInvokePython.getBuildcmd());
            pythonModel.setPythonName(ciInvokePython.getLangVersion());
            // 默认为Shell，不暴露页面
            pythonModel.setNature("Shell");
        }

        return buildModel;
    }

    @Override
    public LanguageModel generateLanguageModel(String compileJsonData) {
        LanguageModel languageModel = null;
        if (compileJsonData != null) {
            CiInvokePython ciInvokePython = JSONArray.parseObject(compileJsonData, CiInvokePython.class);
            languageModel = new LanguageModel();
            languageModel.setLangType(CiConstant.DEVOPS_LANG_PYTHON);
            languageModel.setLangVersion(ciInvokePython.getLangVersion());
            return languageModel;
        }
        return languageModel;
    }
}
