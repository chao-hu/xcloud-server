package com.xxx.xcloud.module.ceph.entity;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 快照策略实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@Entity
@Table(name = "`snap_strategy`")
public class SnapStrategy implements Serializable {

    private static final long serialVersionUID = -7850107045678538741L;

    @Transient
    public static final int STATUS_RUNNING = 1;

    @Transient
    public static final int STATUS_STOP = 0;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`CEPH_RBD_ID`")
    private String cephRbdId;

    @Column(name = "`TIME`")
    private String time;

    @Column(name = "`WEEK`")
    private String week;

    @Column(name = "`STATUS`")
    private int status;

    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "`END_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * Creates builder to build .
     *
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    public SnapStrategy() {

    }

    @Generated("SparkTools")
    private SnapStrategy(Builder builder) {
        this.id = builder.id;
        this.cephRbdId = builder.cephRbdId;
        this.time = builder.time;
        this.week = builder.week;
        this.status = builder.status;
        this.createTime = builder.createTime;
        this.endTime = builder.endTime;
    }

    /**
     * Builder to build .
     */
    @Generated("SparkTools")
    public static final class Builder {
        private String id;
        private String cephRbdId;
        private String time;
        private String week;
        private int status;
        private Date createTime;
        private Date endTime;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCephRbdId(String cephRbdId) {
            this.cephRbdId = cephRbdId;
            return this;
        }

        public Builder withTime(String time) {
            this.time = time;
            return this;
        }

        public Builder withWeek(String week) {
            this.week = week;
            return this;
        }

        public Builder withStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder withCreateTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withEndTime(Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public SnapStrategy build() {
            return new SnapStrategy(this);
        }
    }
}
