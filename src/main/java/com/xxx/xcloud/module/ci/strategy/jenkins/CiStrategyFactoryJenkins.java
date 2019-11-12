package com.xxx.xcloud.module.ci.strategy.jenkins;

import com.xxx.xcloud.module.ci.consts.CiConstant;

/**
 * @author mengaijun
 * @date: 2019年1月3日 下午6:05:48
 */
public class CiStrategyFactoryJenkins {
    /**
     * 获取相应构建策略类
     *
     * @param type java|go|python
     * @return CiStrategy
     * @date: 2018年12月20日 下午3:34:10
     */
    public static AbstractCiStrategyJenkins getCiStrategy(String type) {
        switch (type) {
        case CiConstant.DEVOPS_LANG_JAVA:
            return new CiStrategyJavaJenkins();
        case CiConstant.DEVOPS_LANG_GO:
            return new CiStrategyGoJenkins();
        case CiConstant.DEVOPS_LANG_PYTHON:
            return new CiStrategyPythonJenkins();
        case CiConstant.DEVOPS_LANG_PHP:
            return new CiStrategyPhpJenkins();
        case CiConstant.DEVOPS_LANG_NODEJS:
            return new CiStrategyNodeJsJenkins();
        default:
            return new CiStrategyJavaJenkins();
        }
    }

}
