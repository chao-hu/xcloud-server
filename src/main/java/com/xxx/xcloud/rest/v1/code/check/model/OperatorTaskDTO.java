package com.xxx.xcloud.rest.v1.code.check.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @ClassName: com.xxx.xcloud.module.ci.model.SwOperatorTask
 * @Description: TODO
 * @author: lizhen
 * @date: 2019年5月22日 下午7:44:39
 */

@ApiModel(value = "代码检查任务操作模型")
@Data
public class OperatorTaskDTO {

    @ApiModelProperty(value = "操作,执行：start，禁用：disable，启用：enable", required = true, example = "start", dataType = "String")
    private String operator;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

}
