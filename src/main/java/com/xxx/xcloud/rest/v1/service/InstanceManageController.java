package com.xxx.xcloud.rest.v1.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.service.IAppInstanceManageService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.impl.TenantServiceImplV1;
import com.xxx.xcloud.rest.v1.service.model.Event;
import com.xxx.xcloud.rest.v1.service.model.PodInfo;
import com.xxx.xcloud.rest.v1.service.model.ServiceExecContainerDTO;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: ServiceConfigController
 * @Description: ServiceConfigController
 * @author zyh
 * @date 2019年10月25日
 *
 */
@Controller
@RequestMapping("/v1/service")
@Validated
public class InstanceManageController {

    @Autowired
    private TenantServiceImplV1 tenantService;
    @Autowired
    private IAppInstanceManageService instanceManageService;

    /**
     * 获取服务实例详情列表
     * @Title: getPodInfoList
     * @Description: 获取服务实例详情列表
     * @param serviceId
     * @param tenantName
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/podinfo" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务实例详情列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult getPodInfoList(@PathVariable("serviceId") String serviceId,
            @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName) {
        checkTenantName(tenantName);
        List<PodInfo> podInfo = new ArrayList<PodInfo>();
        try {
            podInfo = instanceManageService.getPodInfo(serviceId, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, podInfo, "获取服务实例详情列表成功");
    }

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
     * 获取服务实例的不同状态个数
     * @Title: podNumberDifferentStates
     * @Description: 获取服务实例的不同状态个数
     * @param serviceId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/podNum" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务实例的不同状态个数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult podNumberDifferentStates(@PathVariable("serviceId") String serviceId) {
        Map<String, Integer> map = null;
        try {
            map = instanceManageService.podNumberDifferentStates(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, map, "获取服务实例的不同状态个数成功");
    }

    /**
     * 删除单个实例
     * @Title: deletePod
     * @Description: 删除单个实例
     * @param tenantName
     * @param podName
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/pod" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除单个实例", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "实例名称", required = true, dataType = "String") })
    public ApiResult deletePod(
            @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName) {

        checkTenantName(tenantName);

        try {
            instanceManageService.deletePod(tenantName, podName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除单个实例成功");
    }

    /**
     * 根据租户名称和服务名称查询pod信息
     * @Title: getPods
     * @Description: 根据租户名称和服务名称查询pod信息
     * @param tenantName
     * @param serviceName
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/pods" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据租户名称和服务名称查询pod信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = true, dataType = "String") })
    public ApiResult getPods(
            @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "serviceName", required = true) String serviceName) {

        ApiResult result = checkTenantName(tenantName);
        if (result != null) {
            return result;
        }

        Map<String, Object> terminatingPods = null;
        try {
            terminatingPods = instanceManageService.getPods(tenantName, serviceName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, terminatingPods, "获取pod信息成功");
    }

    /**
     * 获取实例事件日志
     * @Title: podStartLogs
     * @Description: 获取实例事件日志
     * @param podName
     * @param tenantName
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/podevent" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取实例事件日志", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "实例名称", required = true, dataType = "String") })
    public ApiResult podStartLogs(@RequestParam(value = "podName", required = true) String podName,
            @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName) {
        checkTenantName(tenantName);
        List<Event> eventList = null;
        try {
            eventList = instanceManageService.getContainerStartLogs(podName, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, eventList, "获取实例事件日志成功");
    }

    /**
     * 获取当前Pod的日志
     * @Title: getPodLogs
     * @Description: 获取当前Pod的日志
     * @param tenantName
     * @param podName
     * @param apptype
     * @param logtail
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/podlogs" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取当前Pod的日志", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "实例名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "logtail", value = "日志条数", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "apptype", value = "服务类型", required = false, dataType = "String") })
    public ApiResult getPodLogs(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName,
            @RequestParam(value = "apptype", required = false) String apptype,
            @RequestParam(value = "logtail", required = false, defaultValue = "1000") Integer logtail) {

        Map<String, String> map = new HashMap<>(1);

        try {
            String logs = instanceManageService.getPodLogs(tenantName, podName, apptype, logtail);
            map.put("logs", logs);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, map, "获取实例日志成功");
    }

    /**
     * 获取当前Pod的实时日志
     * @Title: getCurrentPodLogs
     * @Description: 获取当前Pod的实时日志
     * @param tenantName
     * @param podName
     * @param apptype
     * @param logtail
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/currentpodlogs" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取当前Pod的实时日志", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "实例名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "logtail", value = "日志条数", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "apptype", value = "服务类型", required = false, dataType = "String") })
    public ApiResult getCurrentPodLogs(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName,
            @RequestParam(value = "apptype", required = false) String apptype,
            @RequestParam(value = "logtail", required = false, defaultValue = "1000") Integer logtail) {

        Map<String, String> map = new HashMap<>(1);

        try {
            String logs = instanceManageService.getCurrentPodLogs(tenantName, podName, apptype, logtail);
            map.put("logs", logs);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, map, "获取实例日志成功");
    }

    /**
     * 根据起止时间获取日志
     * @Title: getPodLogByPeriod
     * @Description: 根据起止时间获取日志
     * @param tenantName
     * @param podName
     * @param apptype
     * @param since
     * @param until
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/periodpodlogs" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据起止时间获取日志", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "实例名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "since", value = "开始时间", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "until", value = "结束时间", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "apptype", value = "服务类型", required = false, dataType = "String") })
    public ApiResult getPodLogByPeriod(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName,
            @RequestParam(value = "apptype", required = false) String apptype,
            @RequestParam(value = "since", required = true) String since,
            @RequestParam(value = "until", required = true) String until) {

        Map<String, String> map = new HashMap<>(1);

        try {
            String logs = instanceManageService.getPodLogByPeriod(tenantName, podName, apptype, since, until);
            map.put("logs", logs);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, map, "获取实例日志成功");
    }

    /**
     * 容器日志下载
     * @Title: downloadPodCurrentLog
     * @Description: 容器日志下载
     * @param tenantName
     * @param podName
     * @param apptype
     * @param request
     * @param response void
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/downloadpodcurrentlog" }, method = RequestMethod.GET)
    @ApiOperation(value = "容器日志下载", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "pod名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "apptype", value = "服务类型", required = false, dataType = "String") })
    public void downloadPodCurrentLog(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName,
            @RequestParam(value = "apptype", required = false) String apptype, HttpServletRequest request,
            HttpServletResponse response) {

        instanceManageService.downloadPodCurrentLog(tenantName, podName, apptype, request, response);

    }

    /**
     * 容器文件上传
     * @Title: uploadPodFile
     * @Description: 容器文件上传
     * @param tenantName
     * @param podName
     * @param destPath
     * @param apptype
     * @param ftpPath
     * @param file
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/uploadpodfile" }, method = RequestMethod.POST)
    @ApiOperation(value = "容器文件上传", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "pod名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "destPath", value = "文件目标地址", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "ftpPath", value = "FTP地址", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "form", name = "file", value = "上传文件", required = false, dataType = "file"),
            @ApiImplicitParam(paramType = "query", name = "apptype", value = "服务类型", required = false, dataType = "String") })
    public ApiResult uploadPodFile(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName,
            @RequestParam(value = "destPath", required = true) String destPath,
            @RequestParam(value = "apptype", required = false) String apptype, String ftpPath, MultipartFile file) {

        if (ftpPath == null && file == null) {
            return new ApiResult(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED, "请至少选择一种文件上传方式");
        }

        try {
            if (ftpPath != null) {
                instanceManageService.uploadPodFileViaFtp(tenantName, podName, ftpPath, destPath, apptype);
            }
            if (file != null) {
                instanceManageService.uploadPodFileViaBrowser(tenantName, podName, file, destPath, apptype);
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "容器文件上传成功");
    }

    /**
     * 容器文件下载
     * @Title: downloadPodFile
     * @Description: 容器文件下载
     * @param tenantName
     * @param podName
     * @param fullPath
     * @param apptype
     * @param request
     * @param response void
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/downloadpodfile" }, method = RequestMethod.GET)
    @ApiOperation(value = "容器文件下载", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "podName", value = "pod名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "fullPath", value = "文件全路径", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "apptype", value = "服务类型", required = false, dataType = "String") })
    public void downloadPodFile(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "podName", required = true) String podName,
            @RequestParam(value = "fullPath", required = true) String fullPath,
            @RequestParam(value = "apptype", required = false) String apptype, HttpServletRequest request,
            HttpServletResponse response) {

        instanceManageService.downloadPodFile(tenantName, podName, fullPath, apptype, request, response);

    }

    /**
     * 容器内执行命令
     * @Title: execCmdInContainer
     * @Description: 容器内执行命令
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/execcommand" }, method = RequestMethod.POST)
    @ApiOperation(value = "容器内执行命令", notes = "")
    public ApiResult execCmdInContainer(@RequestBody ServiceExecContainerDTO json) {

        String tenantName = json.getTenantName();
        String podName = json.getPodName();
        String command = json.getCommand();
        String apptype = json.getAppType();

        Map<String, String> result = new HashMap<String, String>(1);
        try {
            String resultStr = instanceManageService.execCmdInContainer(tenantName, podName, command, apptype);
            result.put("result", resultStr);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, result, "命令执行成功");
    }

}
