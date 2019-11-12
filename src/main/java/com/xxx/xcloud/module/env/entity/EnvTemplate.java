package com.xxx.xcloud.module.env.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * 
 * <p>
 * Description:环境变量模板实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@Entity
@Table(name = "`env_template`", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "`TEMPLATE_NAME`", "`TENANT_NAME`" }) })
public class EnvTemplate implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 688776169152017162L;

    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 租户名称
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * 模板名称
     */
    @Column(name = "`TEMPLATE_NAME`")
    private String templateName;

    /**
     * 环境变量json串[Map<key, value>]
     */
    @Lob
    @Column(columnDefinition = "TEXT", name = "`ENV_DATA`")
    private String envData;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`UPDATE_TIME`")
    private Date updateTime;

    /**
     * 项目id
     */
    @Column(name = "`PROJECT_ID`")
    private String projectId;

    /**
     * 冗余字段
     */
    @Column(name = "`CREATED_BY`")
    private String createdBy;

    /**
     * 变量数量
     */
    @Transient
    private Integer variableNumber;

}
