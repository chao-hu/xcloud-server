package com.xxx.xcloud.module.ceph.entity;

import java.io.Serializable;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 服务和块存储关联实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@Entity
@Table(name = "`service_ceph_rbd`")
public class ServiceCephRbd implements Serializable {

    private static final long serialVersionUID = -7714400331276853708L;

    @Transient
    private CephRbd cephRbd;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`SERVICE_ID`")
    private String serviceId;

    @Column(name = "`CEPH_RBD_ID`")
    private String cephRbdId;

    @Column(name = "`MOUNT_PATH`")
    private String mountPath;

    /**
     * Creates builder to build .
     *
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    public ServiceCephRbd() {

    }

    @Generated("SparkTools")
    private ServiceCephRbd(Builder builder) {
        this.cephRbd = builder.cephRbd;
        this.id = builder.id;
        this.serviceId = builder.serviceId;
        this.cephRbdId = builder.cephRbdId;
        this.mountPath = builder.mountPath;
    }

    /**
     * Builder to build .
     */
    @Generated("SparkTools")
    public static final class Builder {
        private CephRbd cephRbd;
        private String id;
        private String serviceId;
        private String cephRbdId;
        private String mountPath;

        private Builder() {}

        public Builder withCephRbd(CephRbd cephRbd) {
            this.cephRbd = cephRbd;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder withCephRbdId(String cephRbdId) {
            this.cephRbdId = cephRbdId;
            return this;
        }

        public Builder withMountPath(String mountPath) {
            this.mountPath = mountPath;
            return this;
        }

        public ServiceCephRbd build() {
            return new ServiceCephRbd(this);
        }

    }

}
