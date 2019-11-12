package com.xxx.xcloud.module.sonar.model;

/**
 * 拼接检查结果类
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年5月9日 下午3:51:47
 */
public class Measure {
    private String metric;
    private String value;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Measure [metric=" + metric + ", value=" + value + "]";
    }
}
