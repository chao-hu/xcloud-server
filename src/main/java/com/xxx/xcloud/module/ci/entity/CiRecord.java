package com.xxx.xcloud.module.ci.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 
 * @author mengaijun
 * @Description: 构建记录信息
 * @date: 2018年12月7日 下午5:24:49
 */
@Entity
@Table(name = "`BDOS_CI_RECORD`")
public class CiRecord {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`CI_ID`")
    private String ciId;

    /**
     * 镜像名
     */
    @Column(name = "`IMAGE_NAME`")
    private String imageName;

    /**
     * 版本
     */
    @Column(name = "`IMAGE_VERSION`")
    private String imageVersion;

    /**
     * 构建时间
     */
    @Column(name = "`CONSTRUCTION_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date constructionTime;

    /**
     * 构建结果: 2构建中3完成4失败
     */
    @Column(name = "`CONSTRUCTION_RESULT`")
    private Byte constructionResult;

    /**
     * 构建持续时间
     */
    @Column(name = "`CONSTRUCTION_DURATION`")
    private Integer constructionDuration;

    /**
     * 当前任务xcloud本次构建的ID
     */
    @Column(name = "`XCLOUD_CONSTRUCTION_ID`")
    private Integer xcloudConstructionId;

    /**
     * 构建日志
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "`LOG_PRINT`")
    private String logPrint = "";

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    public CiRecord() {
        super();
    }

    public CiRecord(String ciId, String imageName, String imageVersion, Date constructionTime, Byte constructionResult,
            String createdBy) {
        super();
        this.ciId = ciId;
        this.imageName = imageName;
        this.imageVersion = imageVersion;
        this.constructionTime = constructionTime;
        this.constructionResult = constructionResult;
        this.createdBy = createdBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCiId() {
        return ciId;
    }

    public void setCiId(String ciId) {
        this.ciId = ciId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public Date getConstructionTime() {
        return constructionTime;
    }

    public void setConstructionTime(Date constructionTime) {
        this.constructionTime = constructionTime;
    }

    public Byte getConstructionResult() {
        return constructionResult;
    }

    public void setConstructionResult(Byte constructionResult) {
        this.constructionResult = constructionResult;
    }

    public Integer getConstructionDuration() {
        return constructionDuration;
    }

    public void setConstructionDuration(Integer constructionDuration) {
        this.constructionDuration = constructionDuration;
    }

    public Integer getSheraConstructionId() {
        return xcloudConstructionId;
    }

    public void setSheraConstructionId(Integer xcloudConstructionId) {
        this.xcloudConstructionId = xcloudConstructionId;
    }

    public String getLogPrint() {
        return logPrint;
    }

    public void setLogPrint(String logPrint) {
        this.logPrint = logPrint;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
