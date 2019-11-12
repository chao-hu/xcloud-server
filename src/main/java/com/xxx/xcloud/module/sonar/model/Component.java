package com.xxx.xcloud.module.sonar.model;

import java.util.List;

/**
 * 拼接检查结果类
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年5月9日 下午3:51:37
 */
public class Component {
    private List<Measure> measures;

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    @Override
    public String toString() {
        return "Component [measures=" + measures + "]";
    }

}
