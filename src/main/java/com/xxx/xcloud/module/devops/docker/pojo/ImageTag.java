package com.xxx.xcloud.module.devops.docker.pojo;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author xujiangpeng
 * @date 2019/3/15
 */
public class ImageTag {

    private String tagString;

    @XmlElement(name = "string")
    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }
}
