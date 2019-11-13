package com.xxx.xcloud.module.ceph.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;
import com.xxx.xcloud.module.ceph.pojo.FileMountService;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 文件存储实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@Entity
@Table(name = "`ceph_file`", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "`NAME`", "`TENANT_NAME`" }) })
public class CephFile implements Serializable {

    private static final long serialVersionUID = -4115856902340491983L;

    // 单位统一为G
    @Transient
    private double used;

    @Transient
    private List<FileMountService> fileMountService;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`NAME`")
    private String name;

    @Column(name = "`SIZE`")
    private Double size;

    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "`UPDATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Column(name = "`DESCRIPTION`")
    private String description;

    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    @Column(name = "`CREATE_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    public CephFile() {

    }

    @Generated("SparkTools")
    private CephFile(Builder builder) {
        this.used = builder.used;
        this.fileMountService = builder.fileMountService;
        this.id = builder.id;
        this.name = builder.name;
        this.size = builder.size;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
        this.description = builder.description;
        this.tenantName = builder.tenantName;
        this.createdBy = builder.createBy;
        this.projectId = builder.projectId;
    }

    /**
     * Builder to build .
     */
    @Generated("SparkTools")
    public static final class Builder {

        private double used;
        private List<FileMountService> fileMountService;
        private String id;
        private String name;
        private Double size;
        private Date createTime;
        private Date updateTime;
        private String description;
        private String tenantName;
        private String createBy;
        private String projectId;

        private Builder() {}

        public Builder withUsed(double used) {
            this.used = used;
            return this;
        }

        public Builder withFileMountService(List<FileMountService> fileMountService) {
            this.fileMountService = fileMountService;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSize(Double size) {
            this.size = size;
            return this;
        }

        public Builder withCreateTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withTenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public Builder withCreateBy(String createBy) {
            this.createBy = createBy;
            return this;
        }

        public Builder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public CephFile build() {
            return new CephFile(this);
        }
    }

}
