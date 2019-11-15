package com.xxx.xcloud.module.application.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 *
 * <p>
 * Description: service对象
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月3日
 */
@Entity
@Table(name = "`SERVICE`")
@Data
public class Service implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2276760636278756860L;

    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 服务名称
     */
    @Column(name = "`SERVICE_NAME`")
    private String serviceName;

    /**
     * 服务状态，请参见 {@link com.xxx.xcloud.consts.Global}
     */
    @Column(name = "`STATUS`")
    private Byte status;

    /**
     * cpu大小
     */
    @Column(name = "`CPU`")
    private Double cpu;

    /**
     * 内存大小
     */
    @Column(name = "`MEMORY`")
    private Double memory;

    /**
     * GPU大小
     */
    @Column(name = "`GPU`", columnDefinition = "INT default 0")
    private int gpu = 0;

    /**
     * 实例个数
     */
    @Column(name = "`INSTANCE`")
    private Integer instance;

    /**
     * 租户名称
     */
    @Column(name = "`TENANT_NAME`")
    private String tenantName;

    /**
     * 镜像版本id
     */
    @Column(name = "`IMAGE_VERSION_ID`")
    private String imageVersionId;

    /**
     * 自定义启动命令
     */
    @Column(name = "`CMD`")
    private String cmd;

    /**
     * 端口信息json串[Map<containerPort, protocol>]
     * containerPort:容器端口,protocol:协议(TCP,UDP)
     */
    @Column(name = "`PORT_AND_PROTOCOL`")
    private String portAndProtocol;

    /**
     * 环境变量信息json串[Map<key, value>]
     */
    @Lob
    @Column(columnDefinition = "TEXT", name = "`ENV`")
    private String env;

    /**
     * pod是否互斥(0：非互斥；1：互斥)
     */
    @Column(name = "`IS_POD_MUTEX`")
    private Boolean isPodMutex = false;

    /**
     * 自动伸缩json串
     */
    @Lob
    @Column(columnDefinition = "TEXT", name = "`HPA`")
    private String hpa;

    /**
     * 服务描述
     */
    @Column(name = "`DESCRIPTION`")
    private String description;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`UPDATE_TIME`")
    private Date updateTime;

    /**
     * 所属项目
     */
    @Column(name = "`PROJECT_ID`")
    private String projectId;

    /**
     * 冗余字段-供前端使用
     */
    @Column(name = "`CREATED_BY`")
    private String createdBy;

    /**
     * 是否使用APM监控
     */
    @Column(name = "`IS_USED_APM`")
    private Boolean isUsedApm = false;;

    /**
     * 更改内容是否需要重启生效(true：表示需要重启才能生效)
     */
    @Column(name = "`IS_RESTART_EFFECT`")
    private Boolean isRestartEffect;
    
    /**
     * 容器内ip和域名映射列表json串
     */
    @Column(name = "`HOST_ALIASES`")
    private String hostAliases;
    
    /**
     * 初始化的容器cmomand
     * {"tcp":[{"host":"172.16.26.50","port":3306}],"http":[{"urlPath":"http://172.16.3.30/"}]}
     */
    @Column(name = "`INIT_CONTAINER`")
    private String initContainer;

    /**
     * 服务事件日志
     */
    @Lob
    @Column(columnDefinition = "TEXT", name = "`EVENT_LOGS`")
    private String eventLogs;

    /**
     * 镜像仓库中完整的镜像名称(租户名称/镜像名称:版本)
     */
    @Transient
    private String registryImageName;

    /**
     * 镜像名称
     */
    @Transient
    private String imageName;

	@Override
	public String toString() {
		return "Service [id=" + id + ", serviceName=" + serviceName + ", status=" + status + ", cpu=" + cpu
				+ ", memory=" + memory + ", gpu=" + gpu + ", instance=" + instance + ", tenantName=" + tenantName
				+ ", imageVersionId=" + imageVersionId + ", cmd=" + cmd + ", portAndProtocol=" + portAndProtocol
				+ ", env=" + env + ", isPodMutex=" + isPodMutex + ", hpa=" + hpa + ", description=" + description
				+ ", createTime=" + createTime + ", updateTime=" + updateTime + ", projectId=" + projectId
				+ ", createdBy=" + createdBy + ", isUsedApm=" + isUsedApm + ", isRestartEffect=" + isRestartEffect
				+ ", hostAliases=" + hostAliases + ", initContainer=" + initContainer + ", eventLogs=" + eventLogs
				+ ", registryImageName=" + registryImageName + ", imageName=" + imageName + "]";
	}

}
