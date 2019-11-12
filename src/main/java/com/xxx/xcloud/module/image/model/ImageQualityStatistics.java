package com.xxx.xcloud.module.image.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 镜像质量统计
 *
 * @author xjp
 * @Description:
 * @date: 2019年8月15日
 */
@Data
public class ImageQualityStatistics {
    /**
     * 租户
     */
    String tenantName;
    /**
     * 镜像名
     */
    String imageName;
    /**
     * 服务信息，一个镜像可能被多个服务使用；key包括serviceId, serviceName, projectId, createTime
     */
    List<Map<String, Object>> services;

    /**
     * 代码检查最新一条记录；一个镜像有多个版本，每个版本可能对应一个检查任务
     * taskName,taskId,lang,healthDegree,codeLineNumbers,questionNumbers
     * infoQuestionNumbers,minorQuestionNumbers,majorQuestionNumbers,criticalQuestionNumbers,blockerQuestionNumbers
     */
    List<Map<String, Object>> codeCheckInfos;

    /**
     * 构建总次数
     */
    private Integer constructionTotal = 0;
    /**
     * 构建成功次数
     */
    private Integer constructionOkTotal = 0;
    /**
     * 构建失败次数
     */
    private Integer constructionFailTotal = 0;
    /**
     * 构建成功率
     */
    private String constructionOkRate = "0";

    /**
     * 构建次数（总）
     */
    private Integer constructionTotalAll = 0;

    /**
     * 构建成功次数（总）
     */
    private Integer constructionOkTotalAll = 0;
    /**
     * 构建失败次数（总）
     */
    private Integer constructionFailTotalAll = 0;
    /**
     * 构建成功率（总）
     */
    private String constructionOkRateAll = "0";

    public ImageQualityStatistics(String tenantName, String imageName, List<Map<String, Object>> services) {
        super();
        this.tenantName = tenantName;
        this.imageName = imageName;
        this.services = services;
    }

    public ImageQualityStatistics(String tenantName, String imageName) {
        super();
        this.tenantName = tenantName;
        this.imageName = imageName;
    }

}
