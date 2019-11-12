package com.xxx.xcloud.module.configmap.entity;

import java.io.Serializable;

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
 * Description: 服务与配置文件模板关联表
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Data
@Entity
@Table(name = "`service_config`")
public class ServiceConfig implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4853752738180565282L;

    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 关联服务id
     */
    @Column(name = "`SERVICE_ID`")
    private String serviceId;

    /**
     * 关联配置文件模板id
     */
    @Column(name = "`CONFIG_TEMPLATE_ID`")
    private String configTemplateId;

    /**
     * 挂载路径
     */
    @Column(name = "`PATH`")
    private String path;

    @Transient
    private ConfigTemplate configTemplate;

}
