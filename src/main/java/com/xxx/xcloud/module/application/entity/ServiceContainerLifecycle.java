package com.xxx.xcloud.module.application.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.utils.StringUtils;

import lombok.Data;


/**
 * Description: 容器生命周期
 * 
 * @author  LYJ </br>
 * create time：2019年4月19日 下午2:41:24 </br>
 * @version 1.0
 * @since
 */
@Entity
@Table(name = "`SERVICE_CONTAINER_LIFECYCLE`")
@Data
public class ServiceContainerLifecycle implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * 所属服务id
     */
    @Column(name = "`SERVICE_ID`")
    private String serviceId;


    /**
     * shell脚本(多条命令用逗号分割的json串)
     */
    @Lob
    @Column(columnDefinition = "TEXT", name = "`EXEC`")
    private String exec;

    /**
     * tcp类型(json串)
     */
    @Lob
    @Column(name = "`TCP`")
    private String tcp;

    /**
     * http类型(json串)
     */
    @Lob
    @Column(name = "`HTTP`")
    private String httpData;
    
    /**
     * 钩子类型，请参见 {@link com.xxx.xcloud.consts.Global}
     */
    @Column(name = "`PROBE_TYPE`")
    private Byte lifecycleType;
    
    /**
     * 要连接的主机名，默认为pod IP
     */
    @Column
    private String host;
    
    /**
     * 是否使用当前钩子
     */
    @Column(name = "`IS_TURN_ON`")
    private Boolean isTurnOn;

    public HttpData getHttpData() {
        if (StringUtils.isEmpty(httpData)) {
            return null;
        }
        HttpData data = JSON.parseObject(httpData, HttpData.class);
        return data;
    }

    /**
     * 
     */
    public ServiceContainerLifecycle() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param id
     * @param serviceId
     * @param exec
     * @param tcp
     * @param httpData
     * @param lifecycleType
     * @param host
     * @param isTurnOn
     */
    public ServiceContainerLifecycle(String id, String serviceId, String exec, String tcp, String httpData,
            Byte lifecycleType, String host, Boolean isTurnOn) {
        super();
        this.id = id;
        this.serviceId = serviceId;
        this.exec = exec;
        this.tcp = tcp;
        this.httpData = httpData;
        this.lifecycleType = lifecycleType;
        this.host = host;
        this.isTurnOn = isTurnOn;
    }

    @Override
    public String toString() {
        return "ServiceContainerLifecycle [id=" + id + ", serviceId=" + serviceId + ", exec=" + exec + ", tcp=" + tcp
                + ", httpData=" + httpData + ", lifecycleType=" + lifecycleType + ", host=" + host + ", isTurnOn="
                + isTurnOn + "]";
    }

}
