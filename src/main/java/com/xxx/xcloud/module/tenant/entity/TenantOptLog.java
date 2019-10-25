package com.xxx.xcloud.module.tenant.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author ruzz 租户信息
 */
@Entity
@Table(name = "`BDOS_TENANT_OPT_LOG`")
@Data
public class TenantOptLog implements Serializable {

    private static final long serialVersionUID = -2401549414939267038L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`TENANT_NAME`", unique = true)
    private String tenantName;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`LAST_TIME`")
    private Date lastTime;

    @Column(name = "`OPT`")
    private String opt;

    @Column(name = "`RETRIES`")
    private Integer retries;
}
