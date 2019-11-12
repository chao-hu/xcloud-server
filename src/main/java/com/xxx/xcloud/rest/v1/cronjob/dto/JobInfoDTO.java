package com.xxx.xcloud.rest.v1.cronjob.dto;

import java.util.Date;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 定时任务对应的job信息
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
public class JobInfoDTO {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 开启时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 任务状态
     */
    private String status;

}
