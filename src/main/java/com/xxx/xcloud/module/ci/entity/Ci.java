package com.xxx.xcloud.module.ci.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Generated;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author mengaijun
 * @Description: 构建信息
 * @date: 2018年12月7日 下午5:19:26
 */
@Entity
@Table(name = "`BDOS_CI`")
public class Ci implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 3467984339118547779L;

	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid")
	@GeneratedValue(generator = "uuidGenerator")
	private String id;

	/**
	 * 任务名称
	 */
	@Column(name = "`CI_NAME`")
	private String ciName;

	/**
	 * 租户
	 */
	@Column(name = "`TENANT_NAME`")
	private String tenantName;

	/**
	 * 镜像名
	 */
	@Column(name = "`IMAGE_NAME`")
	private String imageName;

	/**
	 * 镜像版本
	 */
	@Column(name = "`IMAGE_VERSION`")
	private String imageVersion;

	/**
	 * 版本前缀
	 */
	@Column(name = "`IMAGE_VERSION_PRE`")
	private String imageVersionPre;

	/**
	 * 任务描述
	 */
	@Column(name = "`CI_DESCRIPTION`")
	private String ciDescription;

	/**
	 * 构建时间
	 */
	@Column(name = "`CONSTRUCTION_TIME`")
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date constructionTime;

	/**
	 * 构建持续时间
	 */
	@Column(name = "`CONSTRUCTION_DURATION`")
	private Integer constructionDuration;

	/**
     * 构建状态: 1未构建2构建中3完成4失败5禁用
     */
	@Column(name = "`CONSTRUCTION_STATUS`")
    private Byte constructionStatus;

	/**
	 * 创建时间
	 */
	@Column(name = "`CREATE_TIME`")
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
     * 镜像环境变量信息
     */
    @Column(name = "`ENV_VARIABLES`")
    private String envVariables;

    /**
     * 构建类型: 1代码构建2DockerFile构建
     */
	@Column(name = "`CI_TYPE`")
    private Byte ciType;

	/**
	 * 时间表达式
	 */
	@Column(name = "`CRON`")
	private String cron;

	/**
	 * 时间表达式描述
	 */
	@Column(name = "`CRON_DESCRIPTION`")
	private String cronDescription;

	/**冗余代码构建字段*/
	/**
	 * dockerfile路径
	 */
	@Column(name = "`DOCKERFILE_PATH`")
	private String dockerfilePath;

	/**
	 * 语言
	 */
	@Column(name = "`LANG`")
	private String lang;

	/**
	 * 编译信息json串
	 */
	@Lob
	@Column(name = "`COMPILE`")
	private String compile;

	/**
	 * 代码信息ID
	 */
	@Column(name = "`CODE_INFO_ID`")
	private String codeInfoId;

    @Column(name = "`CREATED_BY`")
    private String createdBy;

    @Column(name = "`PROJECT_ID`")
    private String projectId;

    @Column(name = "`IMAGE_VERSION_GENERATION_STRATEGY`")
    private String imageVersionGenerationStrategy;

//    @Transient
    @Column(name = "`HOOK_USED`")
    private Boolean hookUsed = Boolean.FALSE;

    @Generated("SparkTools")
    private Ci(Builder builder) {
        this.id = builder.id;
        this.ciName = builder.ciName;
        this.tenantName = builder.tenantName;
        this.imageName = builder.imageName;
        this.imageVersion = builder.imageVersion;
        this.imageVersionPre = builder.imageVersionPre;
        this.ciDescription = builder.ciDescription;
        this.constructionTime = builder.constructionTime;
        this.constructionDuration = builder.constructionDuration;
        this.constructionStatus = builder.constructionStatus;
        this.createTime = builder.createTime;
        this.envVariables = builder.envVariables;
        this.ciType = builder.ciType;
        this.cron = builder.cron;
        this.cronDescription = builder.cronDescription;
        this.dockerfilePath = builder.dockerfilePath;
        this.lang = builder.lang;
        this.compile = builder.compile;
        this.codeInfoId = builder.codeInfoId;
        this.createdBy = builder.createdBy;
        this.projectId = builder.projectId;
        this.imageVersionGenerationStrategy = builder.imageVersionGenerationStrategy;
        this.hookUsed = builder.hookUsed;
    }


    // public static class Builder {
    // private String id;
    // private String ciName;
    // private String tenantName;
    // private String imageName;
    // private String imageVersion;
    // private String imageVersionPre;
    // private String ciDescription;
    // private Date constructionTime;
    // private Integer constructionDuration;
    // private Byte constructionStatus;
    // private Date createTime;
    // private String envVariables;
    // private Byte ciType;
    // private String cron;
    // private String cronDescription;
    // private String dockerfilePath;
    // private String lang;
    // private String compile;
    // private String codeInfoId;
    // private String createdBy;
    // private String projectId;
    // }

	public Ci() {
		super();
	}

    public Ci(String ciName, String tenantName, String imageName, String imageVersion, String imageVersionPre,
            String envVariables,
            String ciDescription, Date constructionTime, Integer constructionDuration, Byte constructionStatus,
            Date createTime, Byte ciType, String cron, String cronDescription,
			String dockerfilePath, String lang, String compile, String createdBy, String projectId,Boolean hookUsed) {
		super();
		this.ciName = ciName;
		this.tenantName = tenantName;
		this.imageName = imageName;
		this.imageVersion = imageVersion;
		this.imageVersionPre = imageVersionPre;
        this.envVariables = envVariables;
		this.ciDescription = ciDescription;
		this.constructionTime = constructionTime;
		this.constructionDuration = constructionDuration;
		this.constructionStatus = constructionStatus;
		this.createTime = createTime;
		this.ciType = ciType;
		this.cron = cron;
		this.cronDescription = cronDescription;
		// 代码构建字段
		this.dockerfilePath = dockerfilePath;
		this.lang = lang;
		this.compile = compile;

		this.createdBy = createdBy;
		this.projectId = projectId;
		this.hookUsed = hookUsed;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCiName() {
		return ciName;
	}

	public void setCiName(String ciName) {
		this.ciName = ciName;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImageVersion() {
		return imageVersion;
	}

	public void setImageVersion(String imageVersion) {
		this.imageVersion = imageVersion;
	}

	public String getImageVersionPre() {
		return imageVersionPre;
	}

	public void setImageVersionPre(String imageVersionPre) {
		this.imageVersionPre = imageVersionPre;
	}

	public String getCiDescription() {
		return ciDescription;
	}

	public void setCiDescription(String ciDescription) {
		this.ciDescription = ciDescription;
	}

	public Date getConstructionTime() {
		return constructionTime;
	}

	public void setConstructionTime(Date constructionTime) {
		this.constructionTime = constructionTime;
	}

	public Integer getConstructionDuration() {
		return constructionDuration;
	}

	public void setConstructionDuration(Integer constructionDuration) {
		this.constructionDuration = constructionDuration;
	}

    public Byte getConstructionStatus() {
		return constructionStatus;
	}

    public void setConstructionStatus(Byte constructionStatus) {
		this.constructionStatus = constructionStatus;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

    public Byte getCiType() {
		return ciType;
	}

    public void setCiType(Byte ciType) {
		this.ciType = ciType;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getCronDescription() {
		return cronDescription;
	}

	public void setCronDescription(String cronDescription) {
		this.cronDescription = cronDescription;
	}

	public String getDockerfilePath() {
		return dockerfilePath;
	}

	public void setDockerfilePath(String dockerfilePath) {
		this.dockerfilePath = dockerfilePath;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getCompile() {
		return compile;
	}

	public void setCompile(String compile) {
		this.compile = compile;
	}

	public String getCodeInfoId() {
		return codeInfoId;
	}

	public void setCodeInfoId(String codeInfoId) {
		this.codeInfoId = codeInfoId;
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

    public String getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(String envVariables) {
        this.envVariables = envVariables;
    }

    public Boolean getHookUsed() {
        return hookUsed;
    }

    public void setHookUsed(Boolean hookUsed) {
        this.hookUsed = hookUsed;
    }

    public String getImageVersionGenerationStrategy() {
        return imageVersionGenerationStrategy;
    }

    public void setImageVersionGenerationStrategy(String imageVersionGenerationStrategy) {
        this.imageVersionGenerationStrategy = imageVersionGenerationStrategy;
    }

    /**
     * Creates builder to build {@link Ci}.
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link Ci}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private String id;
        private String ciName;
        private String tenantName;
        private String imageName;
        private String imageVersion;
        private String imageVersionPre;
        private String ciDescription;
        private Date constructionTime;
        private Integer constructionDuration;
        private Byte constructionStatus;
        private Date createTime;
        private String envVariables;
        private Byte ciType;
        private String cron;
        private String cronDescription;
        private String dockerfilePath;
        private String lang;
        private String compile;
        private String codeInfoId;
        private String createdBy;
        private String projectId;
        private String imageVersionGenerationStrategy;
        private Boolean hookUsed;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCiName(String ciName) {
            this.ciName = ciName;
            return this;
        }

        public Builder withTenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public Builder withImageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder withImageVersion(String imageVersion) {
            this.imageVersion = imageVersion;
            return this;
        }

        public Builder withImageVersionPre(String imageVersionPre) {
            this.imageVersionPre = imageVersionPre;
            return this;
        }

        public Builder withCiDescription(String ciDescription) {
            this.ciDescription = ciDescription;
            return this;
        }

        public Builder withConstructionTime(Date constructionTime) {
            this.constructionTime = constructionTime;
            return this;
        }

        public Builder withConstructionDuration(Integer constructionDuration) {
            this.constructionDuration = constructionDuration;
            return this;
        }

        public Builder withConstructionStatus(Byte constructionStatus) {
            this.constructionStatus = constructionStatus;
            return this;
        }

        public Builder withCreateTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withEnvVariables(String envVariables) {
            this.envVariables = envVariables;
            return this;
        }

        public Builder withCiType(Byte ciType) {
            this.ciType = ciType;
            return this;
        }

        public Builder withCron(String cron) {
            this.cron = cron;
            return this;
        }

        public Builder withCronDescription(String cronDescription) {
            this.cronDescription = cronDescription;
            return this;
        }

        public Builder withDockerfilePath(String dockerfilePath) {
            this.dockerfilePath = dockerfilePath;
            return this;
        }

        public Builder withLang(String lang) {
            this.lang = lang;
            return this;
        }

        public Builder withCompile(String compile) {
            this.compile = compile;
            return this;
        }

        public Builder withCodeInfoId(String codeInfoId) {
            this.codeInfoId = codeInfoId;
            return this;
        }

        public Builder withCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder withImageVersionGenerationStrategy(String imageVersionGenerationStrategy) {
            this.imageVersionGenerationStrategy = imageVersionGenerationStrategy;
            return this;
        }

        public Builder withHookUsed(Boolean hookUsed) {
            this.hookUsed = hookUsed;
            return this;
        }

        public Ci build() {
            return new Ci(this);
        }
    }

}
