package com.xxx.xcloud.module.sonar.model;

/**
 * 检查结果
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年5月9日 下午3:43:20
 */
public class SonarCheckResultSummary {
    private Component component;

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return "SonarCheckResult [component=" + component + "]";
    }

}


