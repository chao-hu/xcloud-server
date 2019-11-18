package com.xxx.xcloud.module.application.service;

import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.rest.v1.service.model.ServiceDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceRequest;

/**
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月4日 下午2:14:21
 */
public interface IAppCreateService {
    /**
     *
     * <p>
     * Description: 创建服务
     * </p>
     *
     * @param apiService
     *            {@link com.xxx.xcloud.module.application.model.ServiceRequest}
     * @return 成功返回Service，失败抛出异常
     */
    Service createService(ServiceRequest apiService);

    /**
     * <p>
     * Description: 验证服务名称是否可用
     * </p>
     *
     * @param tenantName
     *            租户名称
     * @param serviceName
     *            服务名称
     * @return 可用时返回true
     */
    Boolean validateServiceName(String tenantName, String serviceName);

    /**
     *
     * <p>
     * Description: 获取可用的节点列表
     * </p>
     *
     * @return 以半英逗号分隔的节点列表
     */
    String getK8SNodes();

    /**
     * Description:获取服务创建参数
     *
     * @param serviceId
     *            服务id
     * @return ServiceRequest
     */
    ServiceDTO getServiceRequestById(String serviceId);

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
     * Description: 根据服务名称和租户名称获取服务详情
     *
     * @author LYJ
     * @param serviceName
     *            服务名称
     * @param tenantname
     *            租户名称
     * @return Service
     */
    Service getServiceByNameAndTenantName(String serviceName, String tenantname);

}
