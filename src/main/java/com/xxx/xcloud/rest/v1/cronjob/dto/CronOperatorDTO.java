/**
 *
 */
package com.xxx.xcloud.rest.v1.cronjob.dto;

import java.util.Date;

import javax.validation.constraints.NotBlank;

import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.cronjob.entity.Cronjob;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * <p>
 * Description: 定时任务操作请求模型
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@ApiModel(value = "定时任务操作请求模型")
public class CronOperatorDTO {

    @ApiModelProperty(value = "操作,启动:start,停止:stop,修改:modify", required = true, example = "", dataType = "String")
    @NotBlank(message = "操作内容不能为空")
    private String operation;

    @ApiModelProperty(value = "CPU", required = false, example = "", dataType = "Double")
    private Double cpu;

    @ApiModelProperty(value = "内存", required = false, example = "", dataType = "Double")
    private Double memory;

    @ApiModelProperty(value = "启动命令", required = false, example = "", dataType = "String")
    private String cmd;
    
    @ApiModelProperty(value = "定时计划--cron表达式", required = true, example = "*/1 * * * *", dataType = "String")
    private String schedule;

    @ApiModelProperty(value = "任务时间  X月X日X时X分", required = false, example = "每分钟", dataType = "String")
    private String scheduleCh;

    
    /**
     * 构造Cronjob对象
     * @Title: getCronjob
     * @Description: 构造Cronjob对象
     * @return Cronjob 
     * @throws
     */
    public Cronjob getUpdatesCronjob(Cronjob cronjob){
        
        if (StringUtils.isEmpty(getSchedule())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "任务时间不能为空");
        }

        if (Double.doubleToLongBits(getMemory()) < 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "内存应该大于0");
        }

        if (Double.doubleToLongBits(getMemory()) < 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "CPU应该大于0");
        }
        
        cronjob.setSchedule(getSchedule());
        cronjob.setMemory(getMemory());
        cronjob.setCpu(getCpu());
        if (StringUtils.isNotEmpty(getScheduleCh())) {
            cronjob.setScheduleCh(getScheduleCh());
        }
        cronjob.setUpdateTime(new Date());
        
        return cronjob;
    }

}
