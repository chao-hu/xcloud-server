package com.xxx.xcloud.module.ceph.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

import lombok.Data;

/**
 * 
 * <p>
 * Description: 块存储实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@Entity
@Table(name = "`ceph_rbd`", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "`NAME`", "`TENANT_NAME`" }) })
public class CephRbd implements Serializable {

    private static final long serialVersionUID = 5522319007341046209L;

    /*
     * @Transient private Service service;
     */

    @Transient
    private SnapStrategy snapStrategy;

    @Transient
    private List<CephSnap> cephSnaps;

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

    @Override
    public int hashCode() {
        return Objects.hash(id, name, tenantName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CephRbd)) {
            return false;
        }
        CephRbd o = (CephRbd) obj;
        return (this.id == o.getId() && this.name.equals(o.getName()) && this.tenantName.equals(o.getTenantName()));
    }

    public CephRbd() {

    }

    @Generated("SparkTools")
    private CephRbd(Builder builder) {
        // this.service = builder.service;
        this.snapStrategy = builder.snapStrategy;
        this.cephSnaps = builder.cephSnaps;
        this.id = builder.id;
        this.name = builder.name;
        this.size = builder.size;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
        this.description = builder.description;
        this.tenantName = builder.tenantName;
        this.createdBy = builder.createdBy;
        this.projectId = builder.projectId;
    }

    /**
     * Creates builder to build .
     *
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build .
     */
    @Generated("SparkTools")
    public static final class Builder {
        // private Service service;
        private SnapStrategy snapStrategy;
        private List<CephSnap> cephSnaps;
        private String id;
        private String name;
        private Double size;
        private Date createTime;
        private Date updateTime;
        private String description;
        private String tenantName;
        private String createdBy;
        private String projectId;

        private Builder() {}

        /*
         * public Builder withService(Service service) { this.service = service;
         * return this; }
         */

        public Builder withSnapStrategy(SnapStrategy snapStrategy) {
            this.snapStrategy = snapStrategy;
            return this;
        }

        public Builder withCephSnaps(List<CephSnap> cephSnaps) {
            this.cephSnaps = cephSnaps;
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

        public Builder withCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public CephRbd build() {
            return new CephRbd(this);
        }
    }
}
