package com.xxx.xcloud.module.application.service;

import java.util.Map;

import com.xxx.xcloud.module.application.entity.Service;

import io.fabric8.kubernetes.api.model.IntOrString;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月5日 上午10:04:10
 */
public interface IAppDetailService {
    /**
     * Description:根据服务id获取服务详情
     *
     * @author LYJ
     * @param serviceId
     *            服务id
     * @return Service
     */
    Service getServiceById(String serviceId);

    /**
     * Description:获取服务事件日志
     *
     * @author LYJ
     * @param serviceId
     *            服务ID
     * @param tenantName
     *            租户名称
     * @return Event
     */
    Map<String, Object> getServiceStartLogs(String serviceId, String tenantName);

    /**
     * Description: 更新服务配额(服务必须在停止状态)
     *
     * @author LYJ
     * @param serviceId
     *            服务id
     * @param cpu
     *            新的cpu大小
     * @param memory
     *            新的内存大小
     * @param gpu
     * @return 成功返回true，失败抛出异常
     */
    boolean updateServiceQuota(String serviceId, Double cpu, Double memory, Integer gpu);

    /**
     * Description:弹性伸缩(服务必须在运行状态并且如果已经使用自动伸缩后不能使用弹性伸缩)
     *
     * @author LYJ
     * @param serviceId
     *            服务id
     * @param instanceNum
     *            要增加的副本数
     * @return 成功返回true，失败抛出异常
     */
    boolean serviceElasticScale(String serviceId, Integer instanceNum);

    /**
     *
     * <p>
     * Description: 修改服务部分属性
     * </p>
     *
     * @param serviceId
     *            服务id
     * @param cmd
     *            自定义启动命令
     * @param description
     *            服务描述信息
     * @param hostAliases
     * @param initContainer
     * @param isUsedApm
     *            是否使用APM监控
     * @return 成功返回true，失败抛出异常
     */
    Boolean updateServicePartialInfo(String serviceId, String cmd, String description, String hostAliases,
            String initContainer, Boolean isUsedApm);

    /**
     * Description:服务镜像版本滚动升级(服务必须在运行状态)
     *
     * @author LYJ
     * @param serviceId
     *            服务id
     * @param imageVersionId
     *            要升级的镜像版本id
     * @param maxUnavailable
     *            升级时不可用pod的上限(若pod期望值为10,maxUnavailable值设置为3,则升级时至少有10-3=7个pod实例可用,maxSurge=0时maxUnavailable必须大于0)
     * @return 成功返回true，失败抛出异常
     */
    boolean upgradeImageVersion(String serviceId, String imageVersionId, IntOrString maxUnavailable);
}
