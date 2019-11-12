/**
 *
 */
package com.xxx.xcloud.module.harbor.entity;

import com.xxx.xcloud.utils.Base64Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author ruzz 租户信息
 */
@Entity
@Table(name = "`BDOS_HARBOR_USER`")
public class HarborUser {

    @Id
    @Column(name = "`HARBOR_ID`") // harbor用户ID
    private int harborId;

    @Column(name = "`TENANT_NAME`") // 租户名称
    private String tenantName;

    @Column(name = "`username`") // 用户名称
    private String username;

    @Column(name = "`password`") // 密码
    private String password;

    @Column(name = "`email`") // 邮件
    private String email;

    @Column(name = "`PROJECT_ID`") // 项目ID
    private int projectId;

    public int getHarborId() {
        return harborId;
    }

    public void setHarborId(int harborId) {
        this.harborId = harborId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return Base64Util.decrypt(password);
    }

    public void setPassword(String password) {
        this.password = Base64Util.encrypt(password);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

}
