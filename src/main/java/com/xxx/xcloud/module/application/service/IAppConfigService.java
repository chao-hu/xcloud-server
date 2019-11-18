package com.xxx.xcloud.module.application.service;

import java.util.List;
import java.util.Map;

import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceContainerLifecycle;
import com.xxx.xcloud.module.application.entity.ServiceHealth;
import com.xxx.xcloud.module.application.entity.ServiceHostpath;
import com.xxx.xcloud.module.application.exception.ConfigMapException;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;

/**
 * @ClassName: IApplicationConfigService
 * @Description: 服务相关接口定义
 * @author zyh
 * @date 2019年10月25日
 *
 */
public interface IAppConfigService {

    /**
          * 更新SVC的端口
     * @Title: updateServicePorts
     * @Description: 更新SVC的端口
     * @param service
     * @return Boolean
     * @throws
     */
    Boolean updateServicePorts(Service service);

    /**
          * 获取服务的卷存储
     * @Title: getServiceCephFile
     * @Description: 获取服务的卷存储
     * @param serviceId
     * @return List<ServiceAndCephFile>
     * @throws
     */
    List<ServiceAndCephFile> getServiceCephFile(String serviceId);

    /**
          * 新增/修改服务的卷存储
     * @Title: updateServiceCephFile
     * @Description: 新增/修改服务的卷存储(服务必须在未启动/已停止/启动失败状态)
     * @param serviceCephFile
     * @return Boolean
     * @throws
     */
    Boolean updateServiceCephFile(ServiceAndCephFile serviceCephFile);

    /**
     * 删除服务的卷存储
     * @Title: deleteServiceCephFile
     * @Description: 删除服务的卷存储(服务必须在未启动/已停止/启动失败状态)
     * @param serviceCephFileId
     * @param serviceId
     * @return Boolean
     * @throws
     */
    Boolean deleteServiceCephFile(String serviceCephFileId, String serviceId);

    /**
          * 获取服务的本地存储
     * @Title: getServiceHostpath
     * @Description: 获取服务的本地存储
     * @param serviceId
     * @return List<ServiceHostpath>
     * @throws
     */
    List<ServiceHostpath> getServiceHostpath(String serviceId);

    /**
     * 新增/修改服务的本地存储
     * @Title: updateServiceHostpath
     * @Description: 新增/修改服务的本地存储(服务必须在未启动/已停止/启动失败状态)
     * @param serviceHostpath
     * @return Boolean 成功返回true，失败抛出异常
     * @throws
     */
    Boolean updateServiceHostpath(ServiceHostpath serviceHostpath);

    /**
          * 删除服务的本地存储
     * @Title: deleteServiceHostpath
     * @Description: 删除服务的本地存储(服务必须在未启动/已停止/启动失败状态)
     * @param serviceHostpathId
     * @param serviceId
     * @return Boolean 成功返回true，失败抛出异常
     * @throws
     */
    Boolean deleteServiceHostpath(String serviceHostpathId, String serviceId);

    /**
          * 获取服务的块存储
     * @Title: getServiceCephRbd
     * @Description: 获取服务的块存储
     * @param serviceId
     * @return List<ServiceCephRbd>
     * @throws
     */
    List<ServiceCephRbd> getServiceCephRbd(String serviceId);

    /**
          * 新增/修改服务的块存储
     * @Title: updateServiceCephRbd
     * @Description: 新增/修改服务的块存储(服务必须在未启动/已停止/启动失败状态)
     * @param serviceCephRbd
     * @return
     * @throws AbstractCephException Boolean
     * @throws
     */
    Boolean updateServiceCephRbd(ServiceCephRbd serviceCephRbd);

    /**
          * 删除服务的块存储
     * @Title: deleteServiceCephRbd
     * @Description: 删除服务的块存储(服务必须在未启动/已停止/启动失败状态)
     * @param serviceCephRbdId
     * @param serviceId
     * @return Boolean
     * @throws
     */
    Boolean deleteServiceCephRbd(String serviceCephRbdId, String serviceId);

    /**
          * 获取服务的配置文件
     * @Title: getServiceConfig
     * @Description: 获取服务的配置文件
     * @param serviceId
     * @return List<ServiceConfig>
     * @throws
     */
    List<ServiceConfig> getServiceConfig(String serviceId);

    /**
          * 新增/修改服务的配置文件
     * @Title: updateServiceConfig
     * @Description: 新增/修改服务的配置文件(服务必须在未启动/已停止/启动失败状态)
     * @param serviceConfig
     * @return
     * @throws ConfigMapException Boolean
     * @throws
     */
    Boolean updateServiceConfig(ServiceConfig serviceConfig) throws ConfigMapException;

    /**
          * 删除服务的配置文件
     * @Title: deleteServiceConfig
     * @Description: 删除服务的配置文件(服务必须在未启动/已停止/启动失败状态)
     * @param serviceConfigId
     * @param serviceId
     * @return Boolean
     * @throws
     */
    Boolean deleteServiceConfig(String serviceConfigId, String serviceId);

    /**
          * 获取服务的环境变量
     * @Title: getServiceEnv
     * @Description: 获取服务的环境变量
     * @param serviceId
     * @return Map<String,Object>
     * @throws
     */
    Map<String, Object> getServiceEnv(String serviceId);

    /**
          * 新增/修改/删除服务的环境变量
     * @Title: updateServiceEnv
     * @Description: 新增/修改/删除服务的环境变量(环境变量为空是即是删除,服务必须在未启动/已停止/启动失败状态)
     * @param serviceId
     * @param env
     * @return Boolean 成功返回true，失败抛出异常
     * @throws
     */
    Boolean updateServiceEnv(String serviceId, Map<String, Object> env);

    /**
          * 获取服务的健康检查
     * @Title: getServiceHealth
     * @Description: 获取服务的健康检查
     * @param serviceId
     * @return List<ServiceHealth>
     * @throws
     */
    List<ServiceHealth> getServiceHealth(String serviceId);

    /**
          * 新增/修改服务的健康检查
     * @Title: updateServiceHealth
     * @Description: 新增/修改服务的健康检查(服务必须在未启动/已停止/启动失败状态)
     * @param serviceHealth
     * @return Boolean 成功返回true，失败抛出异常
     * @throws
     */
    Boolean updateServiceHealth(ServiceHealth serviceHealth);

    /**
          * 删除服务的健康检查
     * @Title: deleteServiceHealth
     * @Description: 删除服务的健康检查(服务必须在未启动/已停止/启动失败状态)
     * @param serviceHealthId
     * @param serviceId
     * @return Boolean 成功返回true，失败抛出异常
     * @throws
     */
    Boolean deleteServiceHealth(String serviceHealthId, String serviceId);

    /**
          * 获取容器生命周期
     * @Title: getServiceContainerLifecycle
     * @Description: 获取容器生命周期
     * @param serviceId
     * @return List<ServiceContainerLifecycle>
     * @throws
     */
    List<ServiceContainerLifecycle> getServiceContainerLifecycle(String serviceId);

    /**
          * 新增/修改容器生命周期
     * @Title: updataServiceContainerLifecycle
     * @Description: 新增/修改容器生命周期(服务必须在未启动/已停止/启动失败状态)
     * @param serviceContainerLifecycle
     * @return Boolean
     * @throws
     */
    Boolean updataServiceContainerLifecycle(ServiceContainerLifecycle serviceContainerLifecycle);

    /**
          * 删除容器生命周期
     * @Title: deleteServiceContainerLifecycle
     * @Description: 删除容器生命周期(服务必须在未启动/已停止/启动失败状态)
     * @param serviceContainerLifecycleId
     * @param serviceId
     * @return Boolean
     * @throws
     */
    Boolean deleteServiceContainerLifecycle(String serviceContainerLifecycleId, String serviceId);

    /**
          * 自动伸缩
     * @Title: serviceAutomaticScale
     * @Description: 自动伸缩(服务必须在运行状态)
     * @param serviceId 服务id
     * @param minReplicas   最小副本数
     * @param maxReplicas   最大副本数
     * @param cpuThreshold  cpu阈值
     * @param isTurnOn   是否开启自动伸缩
     * @return boolean 成功返回true，失败抛出异常
     * @throws
     */
    boolean serviceAutomaticScale(String serviceId, Integer minReplicas, Integer maxReplicas, Integer cpuThreshold,
            Boolean isTurnOn);
}
