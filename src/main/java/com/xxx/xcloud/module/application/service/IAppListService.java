package com.xxx.xcloud.module.application.service;

import com.xxx.xcloud.module.application.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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

    /**
     * 根据镜像版本ID查询服务信息
     *
     * @param imageVersionId
     *            镜像版本ID
     * @return List<Service> 服务信息
     * @date: 2019年3月5日 上午11:09:26
     */
    List<Service> getServicesByImageVersionId(String imageVersionId);
}
