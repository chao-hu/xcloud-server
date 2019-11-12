package com.xxx.xcloud.module.devops.credentials.pojo;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author xujiangpeng
 * @date 2019/3/21
 */
public class PrivateKeySource {
    private String clazz = "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource";

    private String privateKey;

    @XmlAttribute(name = "class")
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}

