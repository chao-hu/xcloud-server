/**
 * Project Name:EPM_PAAS_CLOUD
 * File Name:Rule.java
 * Package Name:com.bonc.epm.paas.sonar.model
 * Date:2018年2月9日下午2:34:48
 * Copyright (c) 2018, longkaixiang@bonc.com.cn All Rights Reserved.
 *
 */

package com.xxx.xcloud.client.sonar.model;

import java.util.List;

/**
 * 
 * @author mengaijun
 * @date: 2018年12月10日 上午10:13:05
 */
public class Rule {
    /**
     * 规则key
     */
    private String key;
    private String repo;
    /**
     * 规则名称
     */
    private String name;
    private String htmlDesc;
    /**
     * 严重程度
     */
    private String severity;

    private String status;
    private String internalKey;
    /**
     * 为true的话，不允许激活
     */
    private Boolean isTemplate;
    private List<String> tags;
    private List<String> sysTags;
    /**
     * 语言
     */
    private String lang;
    /**
     * 语言名称
     */
    private String langName;
    /**
     * 规则类型
     */
    private String type;

    private List<Param> params;
    private String createdAt;
    private String mdNote;
    private String htmlNote;
    private String noteLogin;
    private String qProfile;
    private String inherit;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getRepo() {
        return repo;
    }
    public void setRepo(String repo) {
        this.repo = repo;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHtmlDesc() {
        return htmlDesc;
    }
    public void setHtmlDesc(String htmlDesc) {
        this.htmlDesc = htmlDesc;
    }
    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getInternalKey() {
        return internalKey;
    }
    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }
    public Boolean getIsTemplate() {
        return isTemplate;
    }
    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public List<String> getSysTags() {
        return sysTags;
    }
    public void setSysTags(List<String> sysTags) {
        this.sysTags = sysTags;
    }
    public String getLang() {
        return lang;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }
    public String getLangName() {
        return langName;
    }
    public void setLangName(String langName) {
        this.langName = langName;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public List<Param> getParams() {
        return params;
    }
    public void setParams(List<Param> params) {
        this.params = params;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public String getMdNote() {
        return mdNote;
    }
    public void setMdNote(String mdNote) {
        this.mdNote = mdNote;
    }
    public String getHtmlNote() {
        return htmlNote;
    }
    public void setHtmlNote(String htmlNote) {
        this.htmlNote = htmlNote;
    }
    public String getNoteLogin() {
        return noteLogin;
    }
    public void setNoteLogin(String noteLogin) {
        this.noteLogin = noteLogin;
    }
    public String getqProfile() {
        return qProfile;
    }
    public void setqProfile(String qProfile) {
        this.qProfile = qProfile;
    }
    public String getInherit() {
        return inherit;
    }
    public void setInherit(String inherit) {
        this.inherit = inherit;
    }
}
