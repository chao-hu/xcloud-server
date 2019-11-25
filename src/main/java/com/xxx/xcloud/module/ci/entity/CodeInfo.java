package com.xxx.xcloud.module.ci.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Generated;
import javax.persistence.*;

/**
 * 代码信息
 *
 * @author mengaijun
 * @date: 2018年12月7日 下午5:19:49
 */
@Entity
@Table(name = "`BDOS_CODE_INFO`")
public class CodeInfo {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 代码项目名
     */
    @Column(name = "`CODE_REPOS_NAME`")
    private String codeReposName;

    /**
     * 代码项目ID
     */
    @Column(name = "`CODE_REPOS_ID`")
    private Integer codeReposId;

    /**
     * 分支还是tag 0分支，1tag
     */
    @Column(name = "`BRANCH_OR_TAG`", columnDefinition = "tinyint default 0")
    private Byte branchOrTag;

    /**
     * 代码分支||代码tag
     */
    @Column(name = "`CODE_BRANCH`")
    private String codeBranch;

    /**
     * 代码认证方式
     */
    @Column(name = "`CI_CODE_CREDENTIALS_ID`")
    private String ciCodeCredentialsId;

    /**
     * 代码类型: 1:gitlab、2:svn  3:github
     */
    @Column(name = "`CODE_CONTROL_TYPE`")
    private Byte codeControlType;

    /**
     * 代码地址
     */
    @Column(name = "`CODE_URL`")
    private String codeUrl;

    @Generated("SparkTools")
    private CodeInfo(Builder builder) {
        this.id = builder.id;
        this.codeReposName = builder.codeReposName;
        this.codeReposId = builder.codeReposId;
        this.branchOrTag = builder.branchOrTag;
        this.codeBranch = builder.codeBranch;
        this.ciCodeCredentialsId = builder.ciCodeCredentialsId;
        this.codeControlType = builder.codeControlType;
        this.codeUrl = builder.codeUrl;
    }

    public CodeInfo() {
        super();
    }

    public CodeInfo(String codeReposName, Integer codeReposId, String codeBranch, String ciCodeCredentialsId,
                    Byte codeControlType, String codeUrl) {
        super();
        this.codeReposName = codeReposName;
        this.codeReposId = codeReposId;
        this.codeBranch = codeBranch;
        this.ciCodeCredentialsId = ciCodeCredentialsId;
        this.codeControlType = codeControlType;
        this.codeUrl = codeUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Byte getBranchOrTag() {
        return branchOrTag;
    }

    public void setBranchOrTag(Byte branchOrTag) {
        this.branchOrTag = branchOrTag;
    }

    public String getCodeBranch() {
        return codeBranch;
    }

    public void setCodeBranch(String codeBranch) {
        this.codeBranch = codeBranch;
    }

    public String getCiCodeCredentialsId() {
        return ciCodeCredentialsId;
    }

    public void setCiCodeCredentialsId(String ciCodeCredentialsId) {
        this.ciCodeCredentialsId = ciCodeCredentialsId;
    }

    public Byte getCodeControlType() {
        return codeControlType;
    }

    public void setCodeControlType(Byte codeControlType) {
        this.codeControlType = codeControlType;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getCodeReposName() {
        return codeReposName;
    }

    public void setCodeReposName(String codeReposName) {
        this.codeReposName = codeReposName;
    }

    public Integer getCodeReposId() {
        return codeReposId;
    }

    public void setCodeReposId(Integer codeReposId) {
        this.codeReposId = codeReposId;
    }

    /**
     * Creates builder to build {@link CodeInfo}.
     *
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link CodeInfo}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private String id;
        private String codeReposName;
        private Integer codeReposId;
        private Byte branchOrTag;
        private String codeBranch;
        private String ciCodeCredentialsId;
        private Byte codeControlType;
        private String codeUrl;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCodeReposName(String codeReposName) {
            this.codeReposName = codeReposName;
            return this;
        }

        public Builder withCodeReposId(Integer codeReposId) {
            this.codeReposId = codeReposId;
            return this;
        }

        public Builder withBranchOrTag(Byte branchOrTag) {
            this.branchOrTag = branchOrTag;
            return this;
        }

        public Builder withCodeBranch(String codeBranch) {
            this.codeBranch = codeBranch;
            return this;
        }

        public Builder withCiCodeCredentialsId(String ciCodeCredentialsId) {
            this.ciCodeCredentialsId = ciCodeCredentialsId;
            return this;
        }

        public Builder withCodeControlType(Byte codeControlType) {
            this.codeControlType = codeControlType;
            return this;
        }

        public Builder withCodeUrl(String codeUrl) {
            this.codeUrl = codeUrl;
            return this;
        }

        public CodeInfo build() {
            return new CodeInfo(this);
        }
    }

}
