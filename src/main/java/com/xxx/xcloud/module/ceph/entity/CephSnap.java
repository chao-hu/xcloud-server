package com.xxx.xcloud.module.ceph.entity;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 快照实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@Entity
@Table(name = "`ceph_snap`")
public class CephSnap implements Serializable {

    private static final long serialVersionUID = -7564911909142435064L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`CEPH_RBD_ID`")
    private String cephRbdId;

    @Column(name = "`NAME`")
    private String name;

    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "`DESCRIPTION`")
    private String description;

    /**
     * Creates builder to build .
     *
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    public CephSnap() {}

    @Generated("SparkTools")
    private CephSnap(Builder builder) {
        this.id = builder.id;
        this.cephRbdId = builder.cephRbdId;
        this.name = builder.name;
        this.createTime = builder.createTime;
        this.description = builder.description;
    }

    /**
     * Builder to build .
     */
    @Generated("SparkTools")
    public static final class Builder {

        private String id;
        private String cephRbdId;
        private String name;
        private Date createTime;
        private String description;

        private Builder() {}

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCephRbdId(String cephRbdId) {
            this.cephRbdId = cephRbdId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCreateTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CephSnap build() {
            return new CephSnap(this);
        }
    }

}
