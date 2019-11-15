/**
 *
 */
package com.xxx.xcloud.module.ingress.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;


/**
 * Descriptioni: 域名相关信息
 * @author  LYJ </br>
 * create time：2018年12月5日 上午10:02:04 </br>
 * @version 1.0
 * @since
 */
@Entity
@Table(name = "`BDOS_INGRESS_DOMAIN`")
@Data
public class IngressDomain implements Serializable {

    private static final long serialVersionUID = -6385584576766732579L;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "`UPDATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
    /**
     * 租户名称
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;
    
    /**
     * 一级域名
     */
    @Column(name = "`DOMAIN`")
    private String domain;
    
    /**
     * 生成的ingress资源名称,也是真正使用的域名(项目编码.一级域名)
     */
    @Column(name = "`INGRESS_DOMAIN_NAME`")
    private String ingressDomainName;
    
    /**
     * 是否使用高级参数 0:不使用,1:使用
     */
    @Column(name = "`CONFIG_STATUS`", columnDefinition="tinyint default 0")
    private Byte configStatus = 0;
    
    @Column(name = "`ADD_BASE_URL`")
    private String addBaseUrl;
    
    @Column(name = "`BASE_URL_SCHEME`")
    private String baseUrlScheme;
    
    @Column(name = "`X_FORWARDED_PREFIX`")
    private String xForwardedPrefix;
    
    @Column(name = "`PROXY_PASS_PARAMS`")
    private String proxyPassParams;
    
    @Column(name = "`SERVER_ALIAS`")
    private String serverAlias;
    
    @Column(name = "`LIMIT_RATE`")
    private String limitRate;
    
    @Column(name = "`LIMIT_RATE_AFTER`")
    private String limitRateAfter;
    
    /**
     * 是否使用Https配置 0:不使用,1:使用
     */
    @Column(name = "`HTTPS_STATUS`", columnDefinition="tinyint default 0")
    private Byte httpsStatus = 0;
    
    @Column(name = "`HTTPS_SECRET_NAME`")
    private String httpsSecretName;
    
    @Type(type = "text")
    @Column(name = "`HTTPS_TLS_CRT`")
    private String httpsTlsCrt;
    
    @Type(type = "text")
    @Column(name = "`HTTPS_TLS_KEY`")
    private String httpsTlsKey;
    
    /**
     * TLD:一级域名,SLD:二级域名 
     */
    @Column(name = "`TYPE`")
    private String type;
    
    /**
     * 一级域名对应的ip地址 
     */
    @Column(name = "`IP`")
    private String ip;
    
    /**
     * 项目编码
     */
    @Column(name = "`PROJECT_CODE`")
    private String projectCode;
    
    /**
     * 数据来源  1:保存     2：查询
     */
    @Transient
    private int saveFlag; 
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIngressDomainName() {
        return ingressDomainName;
    }

    public void setIngressDomainName(String ingressDomainName) {
        this.ingressDomainName = ingressDomainName;
    }

    public Byte getConfigStatus() {
        return configStatus;
    }

    public void setConfigStatus(Byte configStatus) {
        this.configStatus = configStatus;
    }

    public String getAddBaseUrl() {
        return addBaseUrl;
    }

    public void setAddBaseUrl(String addBaseUrl) {
        this.addBaseUrl = addBaseUrl;
    }

    public String getBaseUrlScheme() {
        return baseUrlScheme;
    }

    public void setBaseUrlScheme(String baseUrlScheme) {
        this.baseUrlScheme = baseUrlScheme;
    }

    public String getxForwardedPrefix() {
        return xForwardedPrefix;
    }

    public void setxForwardedPrefix(String xForwardedPrefix) {
        this.xForwardedPrefix = xForwardedPrefix;
    }

    public String getProxyPassParams() {
        return proxyPassParams;
    }

    public void setProxyPassParams(String proxyPassParams) {
        this.proxyPassParams = proxyPassParams;
    }

    public String getServerAlias() {
        return serverAlias;
    }

    public void setServerAlias(String serverAlias) {
        this.serverAlias = serverAlias;
    }

    public String getLimitRate() {
        return limitRate;
    }

    public void setLimitRate(String limitRate) {
        this.limitRate = limitRate;
    }

    public String getLimitRateAfter() {
        return limitRateAfter;
    }

    public void setLimitRateAfter(String limitRateAfter) {
        this.limitRateAfter = limitRateAfter;
    }

    public Byte getHttpsStatus() {
        return httpsStatus;
    }

    public void setHttpsStatus(Byte httpsStatus) {
        this.httpsStatus = httpsStatus;
    }

    public String getHttpsSecretName() {
        return httpsSecretName;
    }

    public void setHttpsSecretName(String httpsSecretName) {
        this.httpsSecretName = httpsSecretName;
    }

    public String getHttpsTlsCrt() {
        return httpsTlsCrt;
    }

    public void setHttpsTlsCrt(String httpsTlsCrt) {
        this.httpsTlsCrt = httpsTlsCrt;
    }

    public String getHttpsTlsKey() {
        return httpsTlsKey;
    }

    public void setHttpsTlsKey(String httpsTlsKey) {
        this.httpsTlsKey = httpsTlsKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public int getSaveFlag() {
        return saveFlag;
    }

    public void setSaveFlag(int saveFlag) {
        this.saveFlag = saveFlag;
    }

    @Override
    public String toString() {
        return "IngressDomain [id=" + id + ", createTime=" + createTime + ", updateTime=" + updateTime + ", tenantName="
                + tenantName + ", domain=" + domain + ", ingressDomainName=" + ingressDomainName + ", configStatus="
                + configStatus + ", addBaseUrl=" + addBaseUrl + ", baseUrlScheme=" + baseUrlScheme
                + ", xForwardedPrefix=" + xForwardedPrefix + ", proxyPassParams=" + proxyPassParams + ", serverAlias="
                + serverAlias + ", limitRate=" + limitRate + ", limitRateAfter=" + limitRateAfter + ", httpsStatus="
                + httpsStatus + ", httpsSecretName=" + httpsSecretName + ", httpsTlsCrt=" + httpsTlsCrt
                + ", httpsTlsKey=" + httpsTlsKey + ", type=" + type + ", ip=" + ip + ", projectCode=" + projectCode
                + "]";
    }

}
