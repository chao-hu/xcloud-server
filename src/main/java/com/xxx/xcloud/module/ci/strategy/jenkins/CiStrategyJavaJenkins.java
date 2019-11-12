package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.alibaba.fastjson.JSONArray;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.model.BuildModel;
import com.xxx.xcloud.module.devops.model.LanguageModel;
import com.xxx.xcloud.module.ci.strategy.jenkins.strategy.compile.JavaCompileStrategyEnum;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月3日 下午6:06:17
 */
public class CiStrategyJavaJenkins extends AbstractCiStrategyJenkins {

	public CiStrategyJavaJenkins() {
		type = CiConstant.DEVOPS_LANG_JAVA_INT;
	}

    @Override
    public BuildModel generateBuildModel(String compileJsonData) {
        if (compileJsonData == null) {
            return null;
        }

        String compileToolType = getComplieToolType(compileJsonData);
        return JavaCompileStrategyEnum
                .valueOf(compileToolType == null ? JavaCompileStrategyEnum.MAVEN.toString() : compileToolType)
                .generateBuildModel(compileJsonData);
    }

    @Override
    public LanguageModel generateLanguageModel(String compileJsonData) {
        if (compileJsonData == null) {
            return null;
        }

        // 从json串中设置langVersion信息
        LanguageModel languageModel = JSONArray.parseObject(compileJsonData, LanguageModel.class);
        languageModel.setLangType(CiConstant.DEVOPS_LANG_JAVA);

        return languageModel;
    }

}
