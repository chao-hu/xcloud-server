package com.xxx.xcloud.rest.v1.code.check.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @ClassName: com.xxx.xcloud.module.sonar.model.SwOperatorRuleStatus
 * @Description: TODO
 * @author: lizhen
 * @date: 2019年5月23日 上午10:50:05
 */
@Data
public class OperatorRuleStatusDTO {
    @ApiModelProperty(value = "激活状态", required = true, example = "true", dataType = "boolean")
    private Boolean active;

    @ApiModelProperty(value = "规则key", required = true, example = "5", dataType = "String")
    private String ruleKey;

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the ruleKey
     */
    public String getRuleKey() {
        return ruleKey;
    }

    /**
     * @param ruleKey the ruleKey to set
     */
    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

}
