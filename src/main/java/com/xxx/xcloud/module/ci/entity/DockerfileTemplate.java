package com.xxx.xcloud.module.ci.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * @author mengaijun
 * @Description: dockerfile模版
 * @date: 2018年12月7日 下午5:25:56
 */
@Entity
@Table(name = "`BDOS_DOCKERFILE_TEMPLATE`")
public class DockerfileTemplate {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 模版类型ID
     */
    @Column(name = "`TYPE_ID`")
    private String typeId;

    /**
     * 租户名
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * dockerfile内容
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "`DOCKERFILE_CONTENT`")
    private String dockerfileContent;

    /**
     * dockerfile模版名称
     */
    @Column(name = "`DOCKERFILE_NAME`")
    private String dockerfileName;

    /**
     * 创建时间
     */
    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;

    public DockerfileTemplate() {
        super();
    }

    public DockerfileTemplate(String tenantName, String dockerfileContent, String dockerfileName, Date createTime,
            String createdBy, String projectId) {
        super();
        this.tenantName = tenantName;
        this.dockerfileContent = dockerfileContent;
        this.dockerfileName = dockerfileName;
        this.createTime = createTime;
        this.createdBy = createdBy;
        this.projectId = projectId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getDockerfileContent() {
        return dockerfileContent;
    }

    public void setDockerfileContent(String dockerfileContent) {
        this.dockerfileContent = dockerfileContent;
    }

    public String getDockerfileName() {
        return dockerfileName;
    }

    public void setDockerfileName(String dockerfileName) {
        this.dockerfileName = dockerfileName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

}
