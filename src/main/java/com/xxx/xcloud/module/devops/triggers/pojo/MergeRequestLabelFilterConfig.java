package com.xxx.xcloud.module.devops.triggers.pojo;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author daien
 * @date 2019年8月1日
 */
public class MergeRequestLabelFilterConfig {

    private String include;

    private String exclude;

    @XmlElement(name = "include")
    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    @XmlElement(name = "exclude")
    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

}
