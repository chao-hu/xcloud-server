package com.xxx.xcloud.module.image.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author xjp
 * @Description: 镜像信息
 * @date: 2019年11月7日
 */
@Entity
@Table(name = "`BDOS_IMAGE_VERSION`")
@Data
public class ImageVersion {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 镜像唯一标识
     */
    @Column(name = "`IMAGE_ID`")
    private String imageId;

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
     * 镜像版本
     */
    @Column(name = "`IMAGE_VERSION`")
    private String imageVersion;

    /**
     * 镜像端口
     */
    @Column(name = "`PORTS`")
    private String ports;

    /**
     * 镜像大小
     */
    @Column(name = "`IMAGE_SIZE`")
    private Double imageSize;

    /**
     * 镜像生成方式 : 0:上传镜像 1:代码构建 2:dockerfile构建
     */
    @Column(name = "`CI_TYPE`")
    private Byte ciType;

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;

    @Column(name = "`ENV_VARIABLES`")
    private String envVariables;

    /**
     * 代码库名称(代码构建需)
     */
    @Column(name = "`CODE_BASE_NAME`")
    private String codeBaseName;

    /**
     * 镜像仓库中完整的镜像名称(地址/镜像名称:版本)
     */
    @Transient
    private String registryImageName;

    public ImageVersion() {

    }

    public ImageVersion(String imageId, Date createTime, Byte imageType, String imageVersion, Double imageSize,
            Byte ciType, String ports, String createdBy, String projectId, String envVariables) {
        super();
        this.imageId = imageId;
        this.createTime = createTime;
        this.imageType = imageType;
        this.imageVersion = imageVersion;
        this.imageSize = imageSize;
        this.ciType = ciType;
        this.ports = ports;
        this.createdBy = createdBy;
        this.envVariables = envVariables;
    }

}
