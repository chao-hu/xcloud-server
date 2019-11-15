package com.xxx.xcloud.rest.v1.service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.service.IAppListService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.service.model.ServiceUpdateModelDTO;

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
public class ServiceListController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceListController.class);

    @Autowired
    private IAppListService appListService;

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    /**
     * 服务列表(page)
     */
    @ResponseBody
    @RequestMapping(value = { "/page" }, method = RequestMethod.GET)
    @ApiOperation(value = "服务列表（page）", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页条数", required = false, defaultValue = "2000", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "当前页码", required = false, defaultValue = "0", dataType = "int") })
    public ApiResult servicePage(
            @NotBlank(message = "租户名称不能为空") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page) {

        ApiResult result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }
        Page<Service> servicePages = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        try {
            servicePages = appListService.getServiceList(tenantName, projectId, serviceName, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, servicePages, "获取服务列表成功");
    }

    /**
     * 删除服务
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult deleteService(@PathVariable("serviceId") String serviceId) {

        try {
            appListService.deleteService(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务成功");
    }

    /**
     * 启动、停止服务
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "启动、停止服务", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult stopOrStartService(@PathVariable("serviceId") String serviceId,
            @RequestBody ServiceUpdateModelDTO json) {

        String operator = json.getOperation();
        try {
            switch (operator) {
            case Global.SERVICE_START:
                appListService.startService(serviceId);
                break;

            case Global.SERVICE_STOP:
                appListService.stopService(serviceId);
                break;
            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作成功");
    }

    /**
     * check
     * 
     * @param tenantName
     * @return ApiResult
     * @date: 2019年11月4日 上午10:48:55
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
