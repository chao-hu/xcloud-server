package com.xxx.xcloud.module.ci.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Generated;
import javax.persistence.*;
import java.io.Serializable;

/**
 * @author mengaijun
 * @Description: Dockerfile构建->上传文件信息
 * @date: 2018年12月7日 下午5:23:14
 */
@Entity
@Table(name = "`BDOS_CI_FILE`")
public class CiFile implements Serializable{
	
	private static final long serialVersionUID = -3817382201274733174L;

	@Id
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid")
	@GeneratedValue(generator = "uuidGenerator")
	private String id;
	
	@Column(name = "`CI_ID`")
	private String ciId;
	
	/**
     * dockerfile构建：上传文件和dockerfile的路径(ftp地址__端口号__用户名__密码__path)
     * 代码构建：dockerfile路径
     */
	@Column(name = "`FILE_PATH`")
	private String filePath;
	
	/**
     * 模版信息
     */
    @Column(name = "`DOCKERFILE_TEMPLATE_ID`")
    private String dockerfileTemplateId;

    @Column(name = "`DOCKERFILE_TYPE_ID`")
    private String dockerfileTypeId;

    /**
     * dockerfile文件内容
     */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "`DOCKERFILE_CONTENT`")
	private String dockerfileContent;
	
	/**
     * 是否高级
     */
    @Column(name = "`ADVANCED`")
    private Boolean advanced;

    /**
     * dockerfile构建：上传文件名称 | 代码构建：包名
     */
	@Column(name = "`FILE_NAME`")
	private String fileName;

    /**
     * 代码构建，dockerfile填写方式字段 0：在线编辑；1：引用代码库
     */
    @Column(name = "`DOCKERFILE_WRITE_TYPE`", columnDefinition = "tinyint default 1")
    private Byte dockerfileWriteType;

    @Column(name = "`UPLOADFILE_SIZE`")
    private String uploadfileSize;

    @Generated("SparkTools")
    private CiFile(Builder builder) {
        this.id = builder.id;
        this.ciId = builder.ciId;
        this.filePath = builder.filePath;
        this.dockerfileTemplateId = builder.dockerfileTemplateId;
        this.dockerfileTypeId = builder.dockerfileTypeId;
        this.dockerfileContent = builder.dockerfileContent;
        this.advanced = builder.advanced;
        this.fileName = builder.fileName;
        this.uploadfileSize = builder.uploadfileSize;
    }
	
	public CiFile() {
		super();
	}

	public CiFile(String filePath, String dockerfileContent, String fileName) {
		super();
		this.filePath = filePath;
		this.dockerfileContent = dockerfileContent;
		this.fileName = fileName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCiId() {
		return ciId;
	}

	public void setCiId(String ciId) {
		this.ciId = ciId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDockerfileContent() {
		return dockerfileContent;
	}

	public void setDockerfileContent(String dockerfileContent) {
		this.dockerfileContent = dockerfileContent;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

    public String getDockerfileTemplateId() {
        return dockerfileTemplateId;
    }

    public void setDockerfileTemplateId(String dockerfileTemplateId) {
        this.dockerfileTemplateId = dockerfileTemplateId;
    }

    public String getDockerfileTypeId() {
        return dockerfileTypeId;
    }

    public void setDockerfileTypeId(String dockerfileTypeId) {
        this.dockerfileTypeId = dockerfileTypeId;
    }

    public Boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(Boolean advanced) {
        this.advanced = advanced;
    }

    public Byte getDockerfileWriteType() {
        return dockerfileWriteType;
    }

    public void setDockerfileWriteType(Byte dockerfileWriteType) {
        this.dockerfileWriteType = dockerfileWriteType;
    }

    public String getUploadfileSize() {
        return uploadfileSize;
    }

    public void setUploadfileSize(String uploadfileSize) {
        this.uploadfileSize = uploadfileSize;
    }

    /**
     * Creates builder to build {@link CiFile}.
     * 
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link CiFile}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private String id;
        private String ciId;
        private String filePath;
        private String dockerfileTemplateId;
        private String dockerfileTypeId;
        private String dockerfileContent;
        private Boolean advanced;
        private String fileName;
        private String uploadfileSize;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCiId(String ciId) {
            this.ciId = ciId;
            return this;
        }

        public Builder withFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder withDockerfileTemplateId(String dockerfileTemplateId) {
            this.dockerfileTemplateId = dockerfileTemplateId;
            return this;
        }

        public Builder withDockerfileTypeId(String dockerfileTypeId) {
            this.dockerfileTypeId = dockerfileTypeId;
            return this;
        }

        public Builder withDockerfileContent(String dockerfileContent) {
            this.dockerfileContent = dockerfileContent;
            return this;
        }

        public Builder withAdvanced(boolean advanced) {
            this.advanced = advanced;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withUploadfileSize(String uploadfileSize) {
            this.uploadfileSize = uploadfileSize;
            return this;
        }

        public CiFile build() {
            return new CiFile(this);
        }
    }
}
