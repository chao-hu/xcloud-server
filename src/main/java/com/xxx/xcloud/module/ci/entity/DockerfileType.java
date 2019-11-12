package com.xxx.xcloud.module.ci.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * @author mengaijun
 * @Description: dockerfile模版
 * @date: 2018年12月7日 下午5:25:56
 */
@Entity
@Table(name = "`BDOS_DOCKERFILE_TYPE`")
public class DockerfileType {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 类型名
     */
    @Column(name = "`TYPE_NAME`")
    private String typeName;

    /**
     * 描述
     */
    @Column(name = "`DESCRIPTION`")
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 创建时间
     */
    @Column(name = "`UPDATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
