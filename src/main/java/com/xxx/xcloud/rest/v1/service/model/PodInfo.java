package com.xxx.xcloud.rest.v1.service.model;

import lombok.Data;

/**
 * @ClassName: PodInfo
 * @Description: pod信息描述
 * @author zyh
 * @date 2019年10月29日
 *
 */
@Data
public class PodInfo {

    /**
     * @Fields: 容器实际名称
     */
    private String podName;

    /**
     * @Fields: 外部IP
     */
    private String hostIp;

    /**
     * @Fields: 内部IP
     */
    private String podIp;

    /**
     * @Fields: 运行状态(1未启动 2创建中 3运行中 4已停止 5启动失败 6删除中 )
     */
    private int podStatus;

    /**
     * @Fields: 运行时长
     */
    private String runingTime;

    /**
     * @Fields: 重启策略
     */
    private String restartPolicy;

    /**
     * @Fields: 创建时间
     */
    private String createTime;

    /**
     * @Fields: 镜像名称
     */
    private String imageName;

}
