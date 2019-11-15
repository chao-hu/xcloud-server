package com.xxx.xcloud.rest.v1.service;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
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
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.service.IAppDetailService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.service.model.ServiceHostAliasesModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceImageModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceInitContainerModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceQuotaModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceScaleModelDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceUpdatePartialInfoModelDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月4日 上午10:46:02
 */
@Controller
@RequestMapping("/v1/service")
@Validated
public class ServiceDetailController {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceDetailController.class);

    @Autowired
    private IAppDetailService appDetailService;

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    /**
     * 服务详情
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据服务ID获取服务详情", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult serviceInfo(@PathVariable("serviceId") String serviceId) {

        Service service = null;

        try {
            service = appDetailService.getServiceById(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, service, "获取服务详情成功");
    }

    /**
     * 服务事件日志
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/event" }, method = RequestMethod.GET)
    @ApiOperation(value = "服务启动日志", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult serviceStartInfo(@PathVariable("serviceId") String serviceId,
            @NotBlank(message = "租户名称不能为空") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName) {
        checkTenantName(tenantName);
        Map<String, Object> event = null;
        try {
            event = appDetailService.getServiceStartLogs(serviceId, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, event, "获取服务事件日志成功");
    }

    /**
     * 修改服务配额
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/quota" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务配额", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateService(@PathVariable("serviceId") String serviceId,
            @RequestBody ServiceQuotaModelDTO json) {

        double cpu = json.getCpu();
        double memory = json.getMemory();
        int gpu = json.getGpu();
        if (Double.isNaN(cpu) || cpu <= 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "cpu需为正整数");
        }
        if (Double.isNaN(memory) || memory <= 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "memory需为正整数");
        }
        if (gpu < 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "gpu需为正整数");
        }

        try {
            appDetailService.updateServiceQuota(serviceId, cpu, memory, gpu);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务配额成功");
    }

    /**
     * 弹性伸缩
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/scale" }, method = RequestMethod.PUT)
    @ApiOperation(value = "弹性伸缩", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult scaleService(@PathVariable("serviceId") String serviceId, @RequestBody ServiceScaleModelDTO json) {

        int instance = json.getInstance();

        if (instance < 1) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "实例个数不能小于1");
        }

        try {
            appDetailService.serviceElasticScale(serviceId, instance);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "弹性伸缩成功");
    }

    /**
     * 修改服务部分属性
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/updateinfo" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务部分属性", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateServicePartialInfo(@PathVariable("serviceId") String serviceId,
            @RequestBody ServiceUpdatePartialInfoModelDTO json) {

        Boolean updateFlag = false;
        String cmd = json.getCmd();
        String description = json.getDescription();
        Boolean isUsedApm = json.getIsUsedApm();
        List<ServiceHostAliasesModelDTO> hostAliases = json.getHostAliases();
        ServiceInitContainerModelDTO initContainer = json.getInitContainer();
        String hostAliasesStr = null;
        if (hostAliases != null) {
            hostAliasesStr = JSON.toJSONString(hostAliases);
        }
        String initContainerStr = null;
        if (initContainer != null) {
            initContainerStr = JSON.toJSONString(initContainer);
        }

        try {
            updateFlag = appDetailService.updateServicePartialInfo(serviceId, cmd, description, hostAliasesStr,
                    initContainerStr, isUsedApm);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, updateFlag, "修改服务信息成功");
    }

    /**
     * 服务版本升级
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/upgrade" }, method = RequestMethod.PUT)
    @ApiOperation(value = "服务版本升级", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult imageService(@PathVariable("serviceId") String serviceId, @RequestBody ServiceImageModelDTO json) {

        String imageVersionId = json.getImageId();
        IntOrString maxUnavailable = null;
        if (StringUtils.isEmpty(imageVersionId)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "镜像版本ID为空");
        }
        if (json.getStepLength() > 0) {
            maxUnavailable = new IntOrString(json.getStepLength());
        } else {
            maxUnavailable = new IntOrString(json.getPercentage() + "%");
        }

        try {
            appDetailService.upgradeImageVersion(serviceId, imageVersionId, maxUnavailable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "服务版本升级成功");
    }

    /**
     * check
     * 
     * @param tenantName
     * @return ApiResult
     * @date: 2019年11月4日 上午10:53:11
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
}
