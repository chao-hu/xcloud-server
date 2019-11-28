package com.xxx.xcloud.module.springcloud.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudApplication;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.model.ServiceInfo;


/**
 * @ClassName: ISpringCloudAppService
 * @Description: spring cloud 应用接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
public interface ISpringCloudAppService {

    JSONObject saveServiceList(Map<String, Map<String, String>> resourceMap, SpringCloudApplication app,
            String version);

    public Page<SpringCloudApplication> findAppPage(String tenantName, String projectId, int page, int size);

    public Page<SpringCloudService> findServicePage(String tenantName, String appId, int page, int size);

    public List<ServiceInfo> findSeviceList(String tenantName, String appId);

    public ApiResult updateAppStateWaiting(SpringCloudApplication app);

    public void updateAppState(SpringCloudApplication app, String appState);

    public SpringCloudApplication findByTenantNameAndId(String tenantName, String id);

    public List<SpringCloudApplication> findByTenantNameAndAppNameAndStateNot(String tenantName, String appName,
            String state);

    public void createService(Map<String, Map<String, String>> resourceMap, JSONObject json, String id);

    public void startService(List<SpringCloudService> serviceList, String appId);

    public void stopService(List<SpringCloudService> serviceList, String appId);

    public SpringCloudApplication findByTenantNameAndIdAndStateNot(String tenantName, String appId,
            String stateAppDeleted);

    public void deleteService(List<SpringCloudService> serviceList, String appId);

    public SpringCloudApplication saveApp(String appName, String tenantName, String projectId, String orderId,
            Map<String, Map<String, String>> resourceMap, String version);

    public boolean checkServiceNameIsExist(String tenantName, String serviceName);

}
