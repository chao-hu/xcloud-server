package com.xxx.xcloud.module.image.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author xjp
 * @Description: 镜像信息
 * @date: 2019年11月17日
 */
@Entity
@Table(name = "`BDOS_IMAGE`")
@Data
public class Image {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 租户
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * 创建时间
     */
    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 1公用2私有
     */
    @Column(name = "`IMAGE_TYPE`")
    private Byte imageType;

    /**
     * 镜像名称
     */
    @Column(name = "`IMAGE_NAME`")
    private String imageName;

    /**
     * 描述
     */
    @Column(name = "`DESCRIPTION`", length = 6000)
    private String description;

    /**
     * 镜像版本
     */
    @Column(name = "`IMAGE_VERSION`")
    private String imageVersion;

    /**
     * 镜像端口, 多端口用逗号分隔
     */
    @Column(name = "`PORTS`")
    private String ports;

    /**
     * 镜像大小
     */
    @Column(name = "`IMAGE_SIZE`")
    private Double imageSize;

    /**
     * 版本数
     */
    @Column(name = "`VERSION_NUM`")
    private Integer versionNum;

    /**
     * 镜像生成方式 : 0:上传镜像 1:代码构建 2:dockerfile构建
     */
    @Column(name = "`CI_TYPE`")
    private Byte ciType;

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;

    /**
     * 镜像仓库中完整的镜像名称(地址/镜像名称)
     */
    @Transient
    private String registryImageName;

    /**
     * 公共镜像版本数量
     */
    @Transient
    private Integer versionNumPublic;

    public Image() {
        super();
    }

    public Image(String tenantName, Date createTime, Byte imageType, String imageName, String description,
            String imageVersion, Double imageSize, Byte ciType, String createdBy, String projectId) {
        super();
        this.tenantName = tenantName;
        this.createTime = createTime;
        this.imageType = imageType;
        this.imageName = imageName;
        this.description = description;
        this.imageVersion = imageVersion;
        this.imageSize = imageSize;
        this.ciType = ciType;
        this.versionNum = 0;
        this.createdBy = createdBy;
        this.projectId = projectId;
    }

}
