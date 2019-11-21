package com.xxx.xcloud.module.component.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * @ClassName: StatefulServiceDependence
 * @Description: 不同组件集群之间的依赖关系
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_SERVICE_DEPENDENCE`", indexes = {
        @Index(name = "`dependence_serviceId`", columnList = "`DEPENDENCE_SERVICE_ID`") })
public class StatefulServiceDependence implements Serializable {

    private static final long serialVersionUID = -5511524463354875013L;

    /**
     * @Fields: id
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * @Fields: 被依赖的serviceId，如zookeeper
     */
    @Column(name = "`DEPENDENCE_SERVICE_ID`")
    private String dependenceServiceId;

    /**
     * @Fields: 依赖于dependenceServiceId的serviceId，如storm
     */
    @Column(name = "`SERVICE_ID`")
    private String serviceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDependenceServiceId() {
        return dependenceServiceId;
    }

    public void setDependenceServiceId(String dependenceServiceId) {
        this.dependenceServiceId = dependenceServiceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "StatefulServiceDependence [id=" + id + ", dependenceServiceId=" + dependenceServiceId + ", serviceId="
                + serviceId + "]";
    }

}
