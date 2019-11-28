package com.xxx.xcloud.module.springcloud.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * Spring Cloud 服务表
 *
 * @author ruzz
 * @date 2019年4月23日
 */
@Entity
@Table(name = "`BDOS_SPRINGCLOUD_CONFIG_FILE`", indexes = {
        @Index(name = "`idx_servicename`", columnList = "`SERVICE_ID`") })
public class SpringCloudConfigFile implements Serializable {

    private static final long serialVersionUID = -7865176457067568717L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id; // ID

    @Column(name = "`SERVICE_ID`")
    private String serviceId; // 服务ID

    @Column(name = "`CONFIG_NAME`")
    private String configName; // 配置文件名称

    @Column(name = "`CONFIG_CONTENT`")
    private String configContent; // 文件内容

    @Column(name = "`ENABLE`")
    private int enable; // 是否启用

    @Column(name = "`CREATE_TIME`")
    private Date createTime; // 创建时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigContent() {
        return configContent;
    }

    public void setConfigContent(String configContent) {
        this.configContent = configContent;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
