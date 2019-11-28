package com.xxx.xcloud.rest.v1.springcloud;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudApplication;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.model.ServiceInfo;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudAppService;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudConfigFileService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.springcloud.model.SpringCloudApplicationDTO;
import com.xxx.xcloud.rest.v1.springcloud.model.SpringCloudApplicationStartOrStopDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * @ClassName: SpringCloudAppController
 * @Description: springCloudAppController
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Controller
@RequestMapping("/v1/springcloud")
public class SpringCloudAppController {

    private static final Logger LOG = LoggerFactory.getLogger(SpringCloudAppController.class);

    @Autowired
    private ISpringCloudAppService springCloudAppService;

    @Autowired
    private ISpringCloudConfigFileService springCloudConfigFileService;
    
    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;
    

    /**
     * 创建应用
     *
     * @throws ApiException
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建应用", notes = "")
    public ApiResult createSpringCloudApp(@RequestBody SpringCloudApplicationDTO json) {

        ApiResult result = null;
        SpringCloudApplication app = null;
        // 解析参数
        String tenantName = json.getTenantName();
        String appName = json.getAppName();
        String projectId = json.getProjectId();
        String orderId = json.getOrderId();
        String version = json.getVersion();
        if (StringUtils.isEmpty(version)) {
            version = "Edgware.SR4";
        }

        result = checkApp(tenantName, appName, json);
        if (null != result) {
            return result;
        }

        // 创建应用
        try {
            app = create(appName, tenantName, projectId, orderId, json, version);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, app, "创建应用成功");
    }

    public SpringCloudApplication create(String appName, String tenantName, String projectId, String orderId,
            SpringCloudApplicationDTO resource, String version) throws ErrorMessageException {

        // 解析校验资源
        // Map<String, Map<String, String>> resourceMap =
        // analyzeResource(resource);
        Map<String, Map<String, String>> resourceMap = analyzeResource(resource);

        // 保存应用
        SpringCloudApplication app = springCloudAppService.saveApp(appName, tenantName, projectId, orderId, resourceMap,
                version);

        // 保存服务
        JSONObject json = new JSONObject();
        try {
            json = springCloudAppService.saveServiceList(resourceMap, app, version);
        } catch (Exception e) {
            LOG.error("spring clould存service信息失败", e.getMessage());
            springCloudAppService.updateAppState(app, SpringCloudCommonConst.STATE_APP_FAILED);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "spring clould存service信息失败");
        }
        // 线程处理
        springCloudAppService.createService(resourceMap, json, app.getId());

        return app;
    }

    /*
     * 校验资源
     */
    private Map<String, Map<String, String>> analyzeResource(SpringCloudApplicationDTO json) {

        LOG.info("------------resource-----------------" + json);
        try {
            Map<String, Map<String, String>> serviceResouceMap = new HashMap<String, Map<String, String>>();

            LOG.info("------------json-----------------" + json);
            DecimalFormat df = new DecimalFormat("#.00");

            Map<String, String> eurekaResource = new HashMap<String, String>(3);
            Map<String, String> configbusResource = new HashMap<String, String>(3);
            Map<String, String> zuulResource = new HashMap<String, String>(2);

            eurekaResource.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, String.valueOf(json.getEurekaNodeNum()));

            Map<String, String> appResource = checkResource(json);

            // eureka
            eurekaResource.put(SpringCloudCommonConst.RESOURCE_CPU, df.format(json.getEurekaCpu()));
            eurekaResource.put(SpringCloudCommonConst.RESOURCE_MEMORY, df.format(json.getEurekaMemory()));

            // configbus
            configbusResource.put(SpringCloudCommonConst.RESOURCE_CPU, df.format(json.getConfigbusCpu()));
            configbusResource.put(SpringCloudCommonConst.RESOURCE_MEMORY, df.format(json.getConfigbusMemory()));
            // configbusResource.put(SpringCloudCommonConst.RESOURCE_STORAGE,
            // df.format(json.getConfigbusStorage()));
            configbusResource.put(SpringCloudCommonConst.CEPHFILEIDCONFIGBUS, json.getCephfileIdConfigbus());

            // zuul
            zuulResource.put(SpringCloudCommonConst.RESOURCE_CPU, df.format(json.getZuulCpu()));
            zuulResource.put(SpringCloudCommonConst.RESOURCE_MEMORY, df.format(json.getZuulMemory()));

            // 存放计算应用资源

            serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_APP, appResource);

            // 存放各组件资源
            configbusResource.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, Integer.toString(1));
            zuulResource.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, Integer.toString(1));
            serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_EUREKA, eurekaResource);
            serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_CONFIG_BUS, configbusResource);
            serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_ZUUL, zuulResource);

            LOG.info("------------serviceResouceMap-----------------" + serviceResouceMap);
            return serviceResouceMap;
        } catch (Exception e) {
            LOG.error("spring clould资源解析失败", e.getMessage());
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "spring clould资源解析失败");
        }
    }

    /*
     * cpu、memory 资源校验
     */
    private Map<String, String> checkResource(SpringCloudApplicationDTO json) {

        DecimalFormat df = new DecimalFormat("#.00");
        Double totalCpu = 0.0;
        Double totalMemory = 0.0;

        if (json.getEurekaNodeNum() < 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "Eurka节点应该大于0");
        }
        int eurekaNum = json.getEurekaNodeNum();

        Double eurekaCpu = json.getEurekaCpu();
        Double eurekaMemory = json.getEurekaMemory();

        Double configbusCpu = json.getConfigbusCpu();
        Double configbusMemory = json.getConfigbusMemory();

        Double zuulCpu = json.getZuulCpu();
        Double zuulMemory = json.getZuulMemory();

        if (eurekaCpu <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "eurekaCpu应该大于0");
        }
        if (eurekaMemory <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "eurekaMemory应该大于0");
        }
        if (configbusCpu <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "configbusCpu应该大于0");
        }
        if (configbusMemory <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "configbusMemory应该大于0");
        }
        /*
         * if (configbusStorage <= 0) { throw new
         * ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
         * "configbusStorage应该大于0"); }
         */
        if (zuulCpu <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "zuulCpu应该大于0");
        }
        if (zuulMemory <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "zuulMemory应该大于0");
        }

        totalCpu = eurekaCpu * eurekaNum + configbusCpu + zuulCpu;
        totalMemory = eurekaMemory * eurekaNum + configbusMemory + zuulMemory;
   
        Map<String, String> appResource = new HashMap<String, String>(3);
        appResource.put(SpringCloudCommonConst.RESOURCE_CPU, df.format(totalCpu));
        appResource.put(SpringCloudCommonConst.RESOURCE_MEMORY, df.format(totalMemory));
       
        return appResource;
    }

    @SuppressWarnings("unused")
    private Map<String, Map<String, String>> generateResource(int resourceTypeBaseCpu, int resourceTypeBaseMemory,
            int resourceTypeBaseStorage) {
        Map<String, Map<String, String>> serviceResouceMap = new HashMap<String, Map<String, String>>();
        Map<String, String> eurekaResouce = new HashMap<String, String>();
        eurekaResouce.put(SpringCloudCommonConst.RESOURCE_CPU, Integer.toString(resourceTypeBaseCpu / 3));
        eurekaResouce.put(SpringCloudCommonConst.RESOURCE_MEMORY, Integer.toString(resourceTypeBaseMemory / 3));
        eurekaResouce.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, Integer.toString(1));
        serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_EUREKA, eurekaResouce);

        Map<String, String> zuulResouce = new HashMap<String, String>();
        zuulResouce.put(SpringCloudCommonConst.RESOURCE_CPU, Integer.toString(resourceTypeBaseCpu / 3));
        zuulResouce.put(SpringCloudCommonConst.RESOURCE_MEMORY, Integer.toString(resourceTypeBaseMemory / 3));
        zuulResouce.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, Integer.toString(1));
        serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_ZUUL, zuulResouce);

        Map<String, String> busResouce = new HashMap<String, String>();
        busResouce.put(SpringCloudCommonConst.RESOURCE_CPU, Integer.toString(resourceTypeBaseCpu / 3));
        busResouce.put(SpringCloudCommonConst.RESOURCE_MEMORY, Integer.toString(resourceTypeBaseMemory / 3));
        busResouce.put(SpringCloudCommonConst.RESOURCE_STORAGE, Integer.toString(resourceTypeBaseStorage));
        busResouce.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, Integer.toString(1));
        serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_CONFIG_BUS, busResouce);

        Map<String, String> resouce = new HashMap<String, String>();
        resouce.put(SpringCloudCommonConst.RESOURCE_CPU, Integer.toString(resourceTypeBaseCpu));
        resouce.put(SpringCloudCommonConst.RESOURCE_MEMORY, Integer.toString(resourceTypeBaseMemory));
        resouce.put(SpringCloudCommonConst.RESOURCE_STORAGE, Integer.toString(resourceTypeBaseStorage));
        serviceResouceMap.put(SpringCloudCommonConst.APPTYPE_APP, resouce);

        return serviceResouceMap;
    }

    /*
     * 根据组件分类校验资源，并返回服务资源信息
     */
    @SuppressWarnings("unused")
    private Map<String, String> checkResource(String appType, JSONObject serviceResource) {

        Map<String, String> resouceMap = new HashMap<String, String>();
        switch (appType) {
        case SpringCloudCommonConst.APPTYPE_EUREKA:
            resouceMap = checkEurekaResource(serviceResource);

            break;
        case SpringCloudCommonConst.APPTYPE_CONFIG_BUS:
            resouceMap = checkConfigBusResource(serviceResource);
            break;
        case SpringCloudCommonConst.APPTYPE_ZUUL:
            resouceMap = checkEurekaResource(serviceResource);
            break;
        default:
            break;
        }

        return resouceMap;
    }

    /*
     * eureka 资源校验
     */
    private Map<String, String> checkEurekaResource(JSONObject serviceResource) {

        Map<String, String> resouceMap = new HashMap<String, String>();
        DecimalFormat df = new DecimalFormat("#.00");
        Double cpu = 0.0;
        Double memory = 0.0;
        int nodeNum = 0;
        try {
            cpu = serviceResource.getDoubleValue(SpringCloudCommonConst.RESOURCE_CPU);
            memory = serviceResource.getDoubleValue(SpringCloudCommonConst.RESOURCE_MEMORY);
            nodeNum = serviceResource.getIntValue(SpringCloudCommonConst.RESOURCE_NODE_NUM);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "eureka 资源解析失败");
        }

        if (Double.doubleToLongBits(memory) <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "内存应该大于0");
        }

        if (Double.doubleToLongBits(cpu) <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "CPU应该大于0");
        }

        if (nodeNum <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "Eurka节点应该大于0");
        }
        resouceMap.put(SpringCloudCommonConst.RESOURCE_CPU, df.format(cpu));
        resouceMap.put(SpringCloudCommonConst.RESOURCE_MEMORY, df.format(memory));
        resouceMap.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, String.valueOf(nodeNum));
        return resouceMap;
    }

    /*
     * configBus 资源校验
     */
    private Map<String, String> checkConfigBusResource(JSONObject serviceResource) {
        Map<String, String> resouceMap = new HashMap<String, String>();
        DecimalFormat df = new DecimalFormat("#.00");

        Double cpu = 0.0;
        Double memory = 0.0;
        Double storage = 0.0;
        int nodeNum = 0;
        try {
            cpu = serviceResource.getDoubleValue(SpringCloudCommonConst.RESOURCE_CPU);
            memory = serviceResource.getDoubleValue(SpringCloudCommonConst.RESOURCE_MEMORY);
            storage = serviceResource.getDoubleValue(SpringCloudCommonConst.RESOURCE_STORAGE);
            nodeNum = serviceResource.getIntValue(SpringCloudCommonConst.RESOURCE_NODE_NUM);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "eureka 资源解析失败");
        }

        if (Double.doubleToLongBits(memory) <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "内存应该大于0");
        }

        if (Double.doubleToLongBits(cpu) <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "CPU应该大于0");
        }

        if (Double.doubleToLongBits(storage) <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "存储应该大于0");
        }

        if (nodeNum <= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "Eurka节点应该大于0");
        }
        resouceMap.put(SpringCloudCommonConst.RESOURCE_CPU, df.format(cpu));
        resouceMap.put(SpringCloudCommonConst.RESOURCE_MEMORY, df.format(memory));
        resouceMap.put(SpringCloudCommonConst.RESOURCE_STORAGE, df.format(storage));
        resouceMap.put(SpringCloudCommonConst.RESOURCE_NODE_NUM, String.valueOf(nodeNum));

        return resouceMap;

    }

    /*
     * 校验服务参数
     */
    private ApiResult checkApp(String tenantName, String appName, SpringCloudApplicationDTO resource) {

        ApiResult result = null;

        // 校验服务名称
        result = checkAppName(tenantName, appName);
        if (null != result) {
            return result;
        }
        // 校验resource
        // result = checkResource(resource);
        // if (null != result) {
        // return result;
        // }

        return null;
    }

    /*
     * 校验资源字段是否为空
     */
    @SuppressWarnings("unused")
    private ApiResult checkResource(String resource) {

        if (StringUtils.isEmpty(resource)) {
            LOG.error("参数校验失败，resource为空");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，resource为空");
        }

        return null;
    }

    /*
     * 校验应用名称及租户
     */
    private ApiResult checkAppName(String tenantName, String appName) {

        ApiResult result = null;
        // 校验租户
        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        List<SpringCloudApplication> appList = springCloudAppService.findByTenantNameAndAppNameAndStateNot(tenantName,
                appName, SpringCloudCommonConst.STATE_APP_DELETED);

        if (null != appList && !appList.isEmpty()) {
            LOG.error("参数校验失败，应用名称" + appName + "已存在");
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "操作参数校验失败，应用名称" + appName + "已存在");
        }
        return null;

    }

    /**
     * 启动，停止应用
     *
     * @throws ApiException
     */
    @ResponseBody
    @RequestMapping(value = { "/{appId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "服务启动,服务停止", notes = "")
    @ApiImplicitParam(paramType = "path", name = "appId", value = "应用ID", required = true, dataType = "String")
    public ApiResult startOrStopSpringCloudApp(@PathVariable("appId") String appId,
            @RequestBody SpringCloudApplicationStartOrStopDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();

        ApiResult result = null;
        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        // 获取应用信息
        SpringCloudApplication app = null;
        try {
            app = springCloudAppService.findByTenantNameAndId(tenantName, appId);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "校验应用ID" + appId + "查询失败");
        }
        if (null == app) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "校验应用ID" + appId + "不存在");
        }

        // 根据操作处理
        try {
            switch (operator) {
            case "stop":
                stop(app);
                break;
            case "start":
                start(app);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "返回服务信息格式解析失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作应用成功");
    }

    /*
     * 启动操作
     */
    private void start(SpringCloudApplication app) {
        String appId = app.getId();
        // 状态校验
        if (!SpringCloudCommonConst.STATE_APP_STOPPED.equals(app.getState())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "校验应用ID" + appId + "不是Stopped状态");
        }

        // 修改应用状态和服务状态
        ApiResult returnDate = springCloudAppService.updateAppStateWaiting(app);

        String serviceString = JSONObject.toJSONString(returnDate.getData());
        List<SpringCloudService> serviceList = JSONObject.parseArray(serviceString,
                com.xxx.xcloud.module.springcloud.entity.SpringCloudService.class);
        // 线程处理
        springCloudAppService.startService(serviceList, appId);
    }

    /*
     * 停止操作
     */
    private void stop(SpringCloudApplication app) {
        String appId = app.getId();
        // 状态校验
        if (!SpringCloudCommonConst.STATE_APP_RUNNING.equals(app.getState())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "校验应用ID" + appId + "不是Running状态");
        }

        // 修改应用状态和服务状态
        ApiResult returnDate = springCloudAppService.updateAppStateWaiting(app);

        String serviceString = JSONObject.toJSONString(returnDate.getData());
        List<SpringCloudService> serviceList = JSONObject.parseArray(serviceString,
                com.xxx.xcloud.module.springcloud.entity.SpringCloudService.class);
        // 线程处理
        springCloudAppService.stopService(serviceList, appId);
    }

    /**
     * 删除应用
     */
    @ResponseBody
    @RequestMapping(value = { "/{appId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除应用", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "appId", value = "应用ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteTenant(@PathVariable("appId") String appId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        ApiResult result = null;
        // 校验租户
        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }
        // 校验应用
        SpringCloudApplication app = null;
        try {
            app = springCloudAppService.findByTenantNameAndIdAndStateNot(tenantName, appId,
                    SpringCloudCommonConst.STATE_APP_DELETED);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        if (null == app) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "校验应用ID" + appId + "不存在");
        }

        LOG.info("---------校验应用完成----------");

        try {
            // 删除应用
            delete(app);

            // 删除应用configbus配置文件
            Page<SpringCloudService> servicePage = springCloudAppService.findServicePage(tenantName, appId, 0, 2000);
            List<SpringCloudService> serviceList = servicePage.getContent();
            for (SpringCloudService sevice : serviceList) {
                if (SpringCloudCommonConst.APPTYPE_CONFIG_BUS.equals(sevice.getAppType())) {
                    springCloudConfigFileService.deleteByServiceId(sevice.getId());
                }
            }
        } catch (Exception e) {
            LOG.info("---------删除应用失败----------");
            return new ApiResult(ReturnCode.CODE_K8S_DELETE_SERVICE_FAILED, "删除应用失败：" + e.getMessage());
        }
        LOG.info("---------删除应用完成----------");
        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除应用成功");
    }

    /*
     * 删除操作
     */
    private void delete(SpringCloudApplication app) {
        String appId = app.getId();

        // 修改应用状态和服务状态
        ApiResult returnDate = springCloudAppService.updateAppStateWaiting(app);

        String serviceString = JSONObject.toJSONString(returnDate.getData());
        List<SpringCloudService> serviceList = JSONObject.parseArray(serviceString,
                com.xxx.xcloud.module.springcloud.entity.SpringCloudService.class);
        // 线程处理
        springCloudAppService.deleteService(serviceList, appId);

    }

    /**
     * 获取应用列表（page）
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取应用分页列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult findAppPage(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult result = null;
        Page<SpringCloudApplication> appPage = null;

        // 校验租户
        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        // 获取应用列表
        try {
            appPage = springCloudAppService.findAppPage(tenantName, projectId, page, size);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, appPage, "获取应用列表成功");
    }

    /**
     * 获取应用下所有服务列表（page）
     */
    @ResponseBody
    @RequestMapping(value = { "/{appId}/page" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取应用分页列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "appId", value = "应用ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult findServicePage(@RequestParam(value = "tenantName", required = true) String tenantName,
            @PathVariable("appId") String appId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult result = null;
        Page<SpringCloudService> servicePage = null;

        // 校验紫狐和应用ID
        result = checkAppIdAndTenantName(tenantName, appId);
        if (null != result) {
            return result;
        }
        LOG.info("------------------校验结束--------------------");
        // 获取应用列表
        try {
            servicePage = springCloudAppService.findServicePage(tenantName, appId, page, size);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, servicePage, "获取应用列表成功");
    }

    /**
     * 获取应用下所有服务列表（list）
     */
    @ResponseBody
    @RequestMapping(value = { "/{appId}/node" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "appId", value = "应用ID", required = true, dataType = "String") })
    public ApiResult findServiceList(@RequestParam(value = "tenantName", required = true) String tenantName,
            @PathVariable("appId") String appId) {

        ApiResult result = null;
        List<ServiceInfo> seviceList = null;
        // 校验租户和应用ID
        result = checkAppIdAndTenantName(tenantName, appId);
        if (null != result) {
            return result;
        }
        LOG.info("------------------校验结束--------------------");
        // 获取应用列表
        try {
            // servicePage = springCloudAppService.findServicePage(tenantName,
            // appId, 0, 2000);
            seviceList = springCloudAppService.findSeviceList(tenantName, appId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        // return new ApiResult(ReturnCode.CODE_SUCCESS,
        // servicePage.getContent(), "获取应用列表成功");
        return new ApiResult(ReturnCode.CODE_SUCCESS, seviceList, "获取应用列表成功");
    }

    /*
     * 校验租户和应用列表
     */
    private ApiResult checkAppIdAndTenantName(String tenantName, String appId) {
        SpringCloudApplication application = null;
        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }
        LOG.info("-----------租户校验成功-----------");
        if (null == appId) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "应用ID为空");
        }

        try {
            application = springCloudAppService.findByTenantNameAndId(tenantName, appId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        LOG.info("-----------application-----------" + JSON.toJSONString(application));
        if (null == application) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "应用" + appId + "不存在");
        }

        return null;
    }

    /*
     * 校验租户
     */
    private ApiResult checkTenantName(String tenantName) {

        Tenant tenant = null;
        try {
            tenant = tenantService.findTenantByTenantName(tenantName);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询租户: " + tenantName + " 失败");
        }
        if (null == tenant) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "租户: " + tenantName + " 不存在");
        }
        return null;
    }

    /**
     * 校验集群名称是否已重复
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/check" }, method = RequestMethod.GET)
    @ApiOperation(value = "校验集群名称是否已重复，true：不存在，false：已存在", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = true, dataType = "String") })
    public ApiResult checkMysqlName(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "serviceName", required = true) String serviceName) {

        boolean flag = false;
        try {
            flag = springCloudAppService.checkServiceNameIsExist(tenantName, serviceName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, flag, "校验集群名称成功");
    }

}
