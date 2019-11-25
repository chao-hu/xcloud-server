package com.xxx.xcloud.rest.v1.code.check.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: com.xxx.xcloud.rest.v1.code.check.model.SwModifyRulesetModel
 * @author: xjp
 * @date: 2019年5月28日 上午11:58:38
 */
@Data
public class ModifyRulesetDTO {

    @ApiModelProperty(value = "规则集所属语言", required = true, example = "java", dataType = "String")
    private String language;

    @ApiModelProperty(value = "租户名称", required = true, example = "testTenant", dataType = "String")
    private String tenantName;

    @ApiModelProperty(value = "新规则集名称", required = false, example = "testName", dataType = "String")
    private String newName;

}

