package com.xxx.xcloud.rest.v1.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.HttpData;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceHealth;
import com.xxx.xcloud.module.application.service.IAppCreateService;
import com.xxx.xcloud.module.image.model.ImageDetail;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.service.model.ServiceCephFileAddDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceCephRbdAddDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceConfigAddDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceHealthUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceLocalAddDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceRequest;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * service创建
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月4日 上午10:10:05
 */
@Controller
@RequestMapping("/v1/service")
@Validated
public class ServiceCreateController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCreateController.class);

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Autowired
    private ImageService imageService;

    @Autowired
    IAppCreateService appCreateService;

    /**
     * 创建服务
     *
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建服务", notes = "")
    public ApiResult createService(@Valid @RequestBody ServiceDTO json) {
        LOG.info("========================serviceDTO:" + JSON.toJSONString(json));
        // 校验必传参数
        String tenantName = json.getTenantName();
        Double memory = json.getMemory() * 1024;
        Double cpu = json.getCpu();
        Integer gpu = json.getGpu();
        String imageVersionId = json.getImageVersionId();
        int instance = json.getInstance();
        String serviceName = json.getServiceName();
        JSONObject portAndProtocol = json.getPortAndProtocol();
        ApiResult checkParamRequired = checkCreateParam(tenantName, serviceName, imageVersionId, instance, cpu, memory,
                portAndProtocol, gpu);
        if (null != checkParamRequired) {
            return checkParamRequired;
        }
        /*
         * Boolean isUsedApm = json.getIsUsedApm(); if(isUsedApm == null){
         * return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
         * "APM监控为空"); }
         */
        // 校验部分参数取值范围
        Byte nodeAffinityType = json.getNodeAffinityType();
        Byte serviceAffinityType = json.getServiceAffinityType();
        if (nodeAffinityType != null && Global.NOT_USE_AFFINITY != nodeAffinityType
                && Global.AFFINITY != nodeAffinityType && Global.ANTI_AFFINITY != nodeAffinityType) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "节点亲和状态为不符合规范");
        }
        if (serviceAffinityType != null && 0 != serviceAffinityType && 1 != serviceAffinityType
                && 2 != serviceAffinityType) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "服务亲和状态为不符合规范");
        }

        // 构建保存服务信息
        ServiceRequest serviceRequest = generateResource(json);
        Service service = null;
        // 创建服务信息
        try {
            service = appCreateService.createService(serviceRequest);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, service, "创建服务成功");
    }

    /**
     * 获取服务创建参数
     */
    @ResponseBody
    @RequestMapping(value = { "allinfo/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据服务ID获取服务创建参数", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult serviceCreateInfo(@PathVariable("serviceId") String serviceId) {

        ServiceDTO swServiceModel = null;

        try {
            swServiceModel = appCreateService.getServiceRequestById(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, swServiceModel, "获取服务创建参数成功");
    }

    /**
     * 服务详情
     */
    @ResponseBody
    @RequestMapping(value = { "/info" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据租户名称和服务名称获取服务详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = true, dataType = "String") })
    public ApiResult serviceList(
            @NotEmpty(message = "租户名称不能为空！") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名不符合规范！") @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "serviceName", required = true) String serviceName) {

        ApiResult result = checkTenantName(tenantName);
        if (result != null) {
            return result;
        }
        Service service = null;
        try {
            service = appCreateService.getServiceByNameAndTenantName(serviceName, tenantName);
            if (service == null) {
                return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "未查询到当前服务");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, service, "获取服务详情成功");
    }

    /**
     * 获取工作节点列表
     */
    @ResponseBody
    @RequestMapping(value = { "/nodes" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取工作节点列表", notes = "")
    public ApiResult getNodes() {

        String nodes = "";

        try {
            nodes = appCreateService.getK8SNodes();
            if ("".equals(nodes)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "获取工作节点列表失败");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, nodes, "获取工作节点列表成功");
    }

    /**
     * 验证服务名称是否可用
     */
    @ResponseBody
    @RequestMapping(value = { "/validatename" }, method = RequestMethod.GET)
    @ApiOperation(value = "验证服务名称是否可用", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = true, dataType = "String") })
    public ApiResult validateServiceName(
            @NotBlank(message = "租户名称不能为空") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "serviceName", required = true) String serviceName) {

        ApiResult result = checkTenantName(tenantName);
        if (result != null) {
            return result;
        }
        if (StringUtils.isEmpty(serviceName)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "服务名称为空");
        }

        Boolean flag = false;
        try {
            flag = appCreateService.validateServiceName(tenantName, serviceName);
            if (flag == false) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "服务名称已被使用");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, flag, "当前服务名称可用");
    }

    /**
     * check
     *
     * @param tenantName
     * @param serviceName
     * @param imageVersionId
     * @param instance
     * @param cpu
     * @param memory
     * @param portAndProtocol
     * @param gpu
     * @return ApiResult
     * @date: 2019年11月4日 上午10:39:37
     */
    private ApiResult checkCreateParam(String tenantName, String serviceName, String imageVersionId, int instance,
            Double cpu, Double memory, JSONObject portAndProtocol, Integer gpu) {

        ApiResult result = null;

        // 校验租户名
        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        // 校验服务名
        result = checkServiceName(serviceName, tenantName);
        if (null != result) {
            return result;
        }
        // 校验镜像ID
        result = checkImageVersionId(imageVersionId);
        if (null != result) {
            return result;
        }
        // 校验cpu
        if (Double.doubleToLongBits(cpu) <= 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "CPU应该大于0");
        }
        // 校验内存
        if (Double.doubleToLongBits(memory) <= 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "内存应该大于0");
        }
        // 校验instance
        if (instance <= 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "实例个数应该大于0");
        }
        // 校验portAndProtocol
        if (null == portAndProtocol || portAndProtocol.isEmpty()) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数portAndProtocol不能为空");
        }

        // 校验gpu
        if (null != gpu && gpu < 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "GPU应该大于等于0");
        }
        return null;
    }

    /**
     * check
     *
     * @param tenantName
     * @return ApiResult
     * @date: 2019年11月4日 上午10:39:44
     */
    private ApiResult checkTenantName(String tenantName) {

        Tenant tenant = null;
        try {
            tenant = tenantService.findTenantByTenantName(tenantName);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询tenantName: " + tenantName + " 失败");
        }
        if (null == tenant) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户tenantName: " + tenantName + " 不存在");
        }
        return null;
    }

    /**
     * check
     *
     * @param serviceName
     * @param tenantName
     * @return ApiResult
     * @date: 2019年11月4日 上午10:39:49
     */
    private ApiResult checkServiceName(String serviceName, String tenantName) {

        Service service = null;
        try {
            service = appCreateService.getServiceByNameAndTenantName(serviceName, tenantName);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "serviceName: " + serviceName + " ,查询服务信息失败");
        }

        if (null != service) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "serviceName: " + serviceName + " ,查询服务已存在");
        }

        return null;
    }

    /**
     * check
     *
     * @param imageVersionid
     * @return ApiResult
     * @date: 2019年11月4日 上午10:39:55
     */
    private ApiResult checkImageVersionId(String imageVersionid) {

        ImageDetail image = null;
        try {
            image = imageService.getDetailByImageVersionId(imageVersionid);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "imageVersionid: " + imageVersionid + " ,查询镜像信息失败");
        }

        if (null == image) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "imageVersionid: " + imageVersionid + " ,查询镜像不存在");
        }

        return null;
    }

    /**
     * 将对象转为json格式字符串
     *
     * @param obj
     * @return String
     * @date: 2019年11月14日 下午3:41:34
     */
    private String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }

        return JSON.toJSONString(obj);
    }

    /**
     * check
     *
     * @param json
     * @return ServiceRequest
     * @date: 2019年11月4日 上午10:40:00
     */
    private ServiceRequest generateResource(ServiceDTO json) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setCmd(json.getCmd());
        serviceRequest.setCpu(json.getCpu());
        serviceRequest.setCreateTime(new Date());
        serviceRequest.setDescription(json.getDescription());
        serviceRequest.setEnv(toJSONString(json.getEnvData()));
        serviceRequest.setImageVersionId(json.getImageVersionId());
        serviceRequest.setInstance(json.getInstance());
        serviceRequest.setMemory(json.getMemory());
        serviceRequest.setNodeAffinity(json.getNodeAffinity());
        serviceRequest.setNodeAffinityType(json.getNodeAffinityType());
        serviceRequest.setServiceAffinity(json.getServiceAffinity());
        serviceRequest.setServiceAffinityType(json.getServiceAffinityType());
        serviceRequest.setServiceName(json.getServiceName());
        serviceRequest.setTenantName(json.getTenantName());
        serviceRequest.setIsPodMutex(json.getIspodmutex());
        serviceRequest.setCreatedBy(json.getCreatedBy());
        serviceRequest.setProjectId(json.getProjectId());
        serviceRequest.setPortAndProtocol(toJSONString(json.getPortAndProtocol()));
        generateHealthCheck(json.getHealthCheck(), serviceRequest);
        serviceRequest.setIsUsedApm(json.getIsUsedApm());
        List<ServiceConfigAddDTO> config = json.getConfig();
        if (null != config && !config.isEmpty()) {
            JSONObject obj = new JSONObject();
            for (ServiceConfigAddDTO serviceConfigAddDTO : config) {
                obj.put(serviceConfigAddDTO.getConfigTemplateId(), serviceConfigAddDTO.getPath());
            }
            serviceRequest.setConfig(toJSONString(obj));
        }
        List<ServiceCephFileAddDTO> storageFile = json.getStorageFile();
        if (null != storageFile && !storageFile.isEmpty()) {
            JSONObject obj = new JSONObject();
            for (ServiceCephFileAddDTO file : storageFile) {
                obj.put(file.getCephFileId(), file.getMountPath());
            }
            serviceRequest.setStorageFile(toJSONString(obj));
        }
        List<ServiceLocalAddDTO> storageLocal = json.getStorageLocal();
        if (null != storageLocal && !storageLocal.isEmpty()) {
            JSONObject obj = new JSONObject();
            for (ServiceLocalAddDTO local : storageLocal) {
                obj.put(local.getHostPath(), local.getMountPath());
            }
            serviceRequest.setStorageLocal(toJSONString(obj));
        }
        List<ServiceCephRbdAddDTO> storageRbd = json.getStorageRbd();
        if (null != storageRbd && !storageRbd.isEmpty()) {
            JSONObject obj = new JSONObject();
            for (ServiceCephRbdAddDTO rbd : storageRbd) {
                obj.put(rbd.getCephRbdId(), rbd.getMountPath());
            }
            serviceRequest.setStorageRbd(toJSONString(obj));
        }
        if (null != json.getGpu()) {
            serviceRequest.setGpu(json.getGpu());
        }
        if (null != json.getHostAliases()) {
            serviceRequest.setHostAliases(toJSONString(json.getHostAliases()));
        }
        if (null != json.getInitContainer()) {
            serviceRequest.setInitContainer(toJSONString(json.getInitContainer()));
        }
        return serviceRequest;
    }

    private void generateHealthCheck(List<ServiceHealthUpdateDTO> healthCheckDTO, ServiceRequest serviceRequest) {

        if (null != healthCheckDTO && !healthCheckDTO.isEmpty()) {
            List<ServiceHealth> serviceHealths = new ArrayList<ServiceHealth>();
            for (ServiceHealthUpdateDTO serviceHealthUpdateDTO : healthCheckDTO) {
                String id = serviceHealthUpdateDTO.getId();
                int exec = serviceHealthUpdateDTO.getExec() == null ? 0 : 1;
                int http = serviceHealthUpdateDTO.getHttp() == null ? 0 : 1;
                int tcp = serviceHealthUpdateDTO.getTcp() == null ? 0 : 1;
                if (exec + http + tcp != 1) {
                    throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                            "exec、http、tcp只能选择其一");
                }
                ServiceHealth serviceHealth = new ServiceHealth();
                if (StringUtils.isNotEmpty(id)) {
                    serviceHealth.setId(id);
                }
                serviceHealth.setExec(serviceHealthUpdateDTO.getExec());
                serviceHealth.setInitialDelay(serviceHealthUpdateDTO.getInitialDelay());
                serviceHealth.setPeriodDetction(serviceHealthUpdateDTO.getPeriodDetction());
                serviceHealth.setProbeType(serviceHealthUpdateDTO.getProbe().byteValue());
                serviceHealth.setSuccessThreshold(serviceHealthUpdateDTO.getSuccessThreshold().byteValue());
                if (serviceHealthUpdateDTO.getTcp() != null) {
                    serviceHealth.setTcp(JSONObject.toJSONString(serviceHealthUpdateDTO.getTcp()));
                }
                if (serviceHealthUpdateDTO.getHttp() != null) {
                    HttpData httpData = new HttpData();
                    httpData.setHttpHeade(serviceHealthUpdateDTO.getHttp().getHttpHeade());
                    httpData.setPath(serviceHealthUpdateDTO.getHttp().getPath());
                    httpData.setPort(serviceHealthUpdateDTO.getHttp().getPort());
                    serviceHealth.setHttpData(JSONObject.toJSONString(httpData));
                }
                serviceHealth.setTimeoutDetction(serviceHealthUpdateDTO.getTimeoutDetction());
                serviceHealth.setIsTurnOn(serviceHealthUpdateDTO.getIsTurnOn());

                serviceHealths.add(serviceHealth);
            }

            serviceRequest.setHealthCheck(serviceHealths);
        }
    }
}
