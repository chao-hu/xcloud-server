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
 *
 * <p>
 * Description: 健康检查
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月5日
 */
@Entity
@Table(name = "`SERVICE_HEALTH`")
@Data
public class ServiceHealth implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8672433120542794072L;

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
     * 初始化等待时间(s)
     */
    @Column(name = "`INITIAL_DELAY`")
    private Integer initialDelay;

    /**
     * 间隔时间(s)
     */
    @Column(name = "`PERIOD_DETCTION`")
    private Integer periodDetction;

    /**
     * 超时时间(s)
     */
    @Column(name = "`TIMEOUT_DETCTION`")
    private Integer timeoutDetction;

    /**
     * 连续成功次数
     */
    @Column(name = "`SUCCESS_THRESHOLD`")
    private Byte successThreshold;

    /**
     * 探针类型，请参见 {@link com.xxx.xcloud.consts.Global}
     */
    @Column(name = "`PROBE_TYPE`")
    private Byte probeType;

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
     * 是否启动当前探针
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

    @Override
    public String toString() {
        return "ServiceHealth [id=" + id + ", serviceId=" + serviceId + ", initialDelay=" + initialDelay
                + ", periodDetction=" + periodDetction + ", timeoutDetction=" + timeoutDetction + ", successThreshold="
                + successThreshold + ", probeType=" + probeType + ", exec=" + exec + ", tcp=" + tcp + ", httpData="
                + httpData + ", isTurnOn=" + isTurnOn + "]";
    }

}
