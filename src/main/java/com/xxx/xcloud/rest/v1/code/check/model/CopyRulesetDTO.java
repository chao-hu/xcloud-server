package com.xxx.xcloud.rest.v1.code.check.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @ClassName: com.xxx.xcloud.module.sonar.model.SwOperatorRuleset
 * @Description: TODO
 * @author: xjp
 * @date: 2019年5月23日 上午10:19:15
 */
@Data
public class CopyRulesetDTO {

    @ApiModelProperty(value = "规则集所属语言", required = true, example = "java", dataType = "String")
    private String language;

    @ApiModelProperty(value = "租户名称", required = true, example = "testTenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "规则集key", required = true, example = "5", dataType = "String")
    private String rulesetKey;

    @ApiModelProperty(value = "新规则集名称", required = false, example = "testName", dataType = "String")
    private String newName;

    /**
     * @return the tenantName
     */
    public String getTenantName() {
        return tenantName;
    }

    /**
     * @param tenantName the tenantName to set
     */
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    /**
     * @return the rulesetKey
     */
    public String getRulesetKey() {
        return rulesetKey;
    }

    /**
     * @param rulesetKey the rulesetKey to set
     */
    public void setRulesetKey(String rulesetKey) {
        this.rulesetKey = rulesetKey;
    }

    /**
     * @return the newName
     */
    public String getNewName() {
        return newName;
    }

    /**
     * @param newName the newName to set
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

}
