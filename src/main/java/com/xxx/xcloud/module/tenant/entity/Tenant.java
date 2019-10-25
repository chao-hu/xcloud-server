package com.xxx.xcloud.module.tenant.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author ruzz 租户信息
 */
@Entity
@Table(name = "`BDOS_TENANT`")
public class Tenant implements Serializable {

    private static final long serialVersionUID = -2401549414939267038L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`TENANT_NAME`", unique = true)
    private String tenantName;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`UPDATE_TIME`")
    private Date updateTime;

    @Column(name = "`OPERATOR_INFO`")
    private String operatorInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
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

    public String getOperatorInfo() {
        return operatorInfo;
    }

    public void setOperatorInfo(String operatorInfo) {
        this.operatorInfo = operatorInfo;
    }

    @JSONField(serialize = false)
    public JSONObject getOperatorInfoJson() {
        return JSON.parseObject(operatorInfo);
    }

    public void addOrReplaceOperatorInfo(String key, Object obj) {

        JSONObject json = JSON.parseObject(operatorInfo);
        json.put(key, obj);

        operatorInfo = json.toJSONString();
    }
}
