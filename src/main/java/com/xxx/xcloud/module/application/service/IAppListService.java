package com.xxx.xcloud.module.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.module.application.entity.Service;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午3:30:41
 */
public interface IAppListService {
    /**
     * Description:获取服务列表
     *
     * @author LYJ
     * @param tenantName
     *            租户名
     * @param projectId
     *            项目Id
     * @param serviceName
     *            模糊搜索的服务名
     * @param pageable
     * @return Page<Service>
     */
    Page<Service> getServiceList(String tenantName, String projectId, String serviceName, Pageable pageable);

    /**
     *
     * <p>
     * Description: 删除服务
     * </p>
     *
     * @param serviceId
     *            服务id
     * @return 成功返回true，失败抛出异常
     */
    boolean deleteService(String serviceId);

    /**
     *
     * <p>
     * Description: 启动服务
     * </p>
     *
     * @param serviceId
     *            服务id
     * @return 成功返回true，失败抛出异常
     */
    boolean startService(String serviceId);

    /**
     *
     * <p>
     * Description: 停止服务
     * </p>
     *
     * @param serviceId
     *            服务id
     * @return 成功返回true，失败抛出异常
     */
    boolean stopService(String serviceId);

    /**
     * Description:取消自动伸缩
     *
     * @author LYJ
     * @param serviceId
     *            服务id
     * @return 成功返回true，失败抛出异常
     */
    boolean cancleServiceAutomaticScale(String serviceId);
}
