/**
 * Project Name:EPM_PAAS_CLOUD
 * File Name:Facet.java
 * Package Name:com.bonc.epm.paas.sonar.model
 * Date:2018年2月9日下午3:43:40
 * Copyright (c) 2018, longkaixiang@bonc.com.cn All Rights Reserved.
 *
 */

package com.xxx.xcloud.client.sonar.model;

import java.util.List;

/**
 * 
 * @author mengaijun
 * @date: 2018年12月10日 上午10:12:17
 */
public class Facet {
    private String name;
    private String property;
    private List<FacetValue> values;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<FacetValue> getValues() {
        return values;
    }
    public void setValues(List<FacetValue> values) {
        this.values = values;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

}
