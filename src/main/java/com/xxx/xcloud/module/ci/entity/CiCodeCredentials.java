package com.xxx.xcloud.module.ci.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.utils.Base64Util;
import com.xxx.xcloud.utils.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author mengaijun
 * @Description: 代码认证信息
 * @date: 2018年12月7日 下午5:18:59
 */
@Entity
@Table(name = "`BDOS_CI_CODE_CREDENTIALS`")
public class CiCodeCredentials {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 证书类型  1：http认证、2：ssh认证
     */
    @Column(name = "`TYPE`")
    private Byte type;

    /**
     * 代码托管类型：1gitlab 2svn 3github
     */
    @Column(name = "`CODE_CONTROL_TYPE`")
    private Byte codeControlType;

    /**
     * 租户名
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * 创建时间
     */
    @Column(name = "`CREATE_TIME`")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 用户名
     */
    @Column(name = "`USER_NAME`")
    private String userName;

    /**
     * 密码
     */
    @Column(name = "`PASSWORD`")
    private String password;

    /**
     * ssh方式生成的key
     */
    @Column(name = "`PRIVATE_KEY`")
    private String privateKey;

    /**
     * gitlab认证的token
     */
    @Column(name = "`GITLAB_TOKEN`")
    private String gitlabToken;

    /**
     * 用户名密码方式的key
     */
    @Column(name = "`UNIQUE_KEY`")
    private String uniqueKey;

    /**
     * 描述
     */
    @Column(name = "`REMARK`")
    private String remark;

    /**
     * 0:公有    1:私有
     */
    @Column(name = "`IS_PUBLIC`")
    private byte isPublic;

    /**
     * gitlab仓库地址
     */
    @Column(name = "`REGISTORY_ADDRESS`")
    private String registoryAddress;

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Byte getCodeControlType() {
        return codeControlType;
    }

    public void setCodeControlType(Byte codeControlType) {
        this.codeControlType = codeControlType;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return Base64Util.decrypt(password);
    }

    /**
     * gitlab方式, 去掉password的@字符, 换成%40; svn和github方式不变
     *
     * @return String
     * @date: 2019年1月11日 下午2:38:34
     */
    public String getPasswordReplaceSpecialChar() {
        String passwordReal = getPassword();
        // 如果是gitlab方式, 如果password包含特殊字符@, 将@替换为%40
        if (codeControlType != null && codeControlType == CiConstant.CODE_TYPE_GITLAB && passwordReal != null
                && passwordReal.contains("@")) {
            return passwordReal.replace("@", "%40");
        }
        return passwordReal;
    }

    /**
     * gitlab方式, 去掉userName的@字符, 换成%40; svn和github方式不变
     *
     * @return String
     * @date: 2019年1月11日 下午2:38:34
     */
    public String getUserNameReplaceSpecialChar() {
        // 如果是gitlab方式, 如果userName包含特殊字符@, 将@替换为%40
        if (codeControlType != null && codeControlType == CiConstant.CODE_TYPE_GITLAB && userName != null && userName
                .contains("@")) {
            return userName.replace("@", "%40");
        }
        return userName;
    }

    public void setPassword(String password) {
        this.password = Base64Util.encrypt(password);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRegistoryAddress() {
        return registoryAddress;
    }

    public void setRegistoryAddress(String registoryAddress) {
        this.registoryAddress = registoryAddress;
    }

    public byte getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(byte isPublic) {
        this.isPublic = isPublic;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGitlabToken() {
        if (!StringUtils.isEmpty(gitlabToken)) {
            return Base64Util.decrypt(gitlabToken);
        }
        return null;
    }

    public void setGitlabToken(String gitlabToken) {
        this.gitlabToken = Base64Util.encrypt(gitlabToken);
    }
}
