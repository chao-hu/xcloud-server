/**
 * Project Name:EPM_PAAS_CLOUD
 * File Name:Param.java
 * Package Name:com.bonc.epm.paas.sonar.model
 * Date:2018年2月9日下午2:41:48
 * Copyright (c) 2018, longkaixiang@bonc.com.cn All Rights Reserved.
 *
 */

package com.xxx.xcloud.client.sonar.model;

/**
 * 
 * @author mengaijun
 * @date: 2018年12月10日 上午10:12:29
 */
public class Param {
    private String key;
    private String desc;
    private String defaultValue;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
