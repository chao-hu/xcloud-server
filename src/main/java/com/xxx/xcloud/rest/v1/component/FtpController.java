package com.xxx.xcloud.rest.v1.component;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;
import com.xxx.xcloud.module.component.service.IComponentService;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.rest.v1.component.model.FtpCreateDTO;
import com.xxx.xcloud.rest.v1.component.model.FtpOperatorDTO;
import com.xxx.xcloud.rest.v1.component.model.FtpUserCreateDTO;
import com.xxx.xcloud.rest.v1.component.model.FtpUserUpdateDTO;
import com.xxx.xcloud.rest.v1.component.model.NodeOperatorDTO;
import com.xxx.xcloud.rest.v1.component.model.UpdateResourceDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: FtpController
 * @Description: ftp操作
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Controller
@Api(value = "ftp接口")
@RequestMapping("/v1/component")
public class FtpController {

    @Autowired
    @Qualifier("componentServiceFtpImpl")
    IComponentService componentServiceFtpImpl;

    /**
     * 创建集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建集群", notes = "")
    public ApiResult createFtpCluster(@RequestBody FtpCreateDTO json) {

        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, String> map = null;

        // 解析参数
        try {
            map = componentServiceFtpImpl.checkClusterCreate(json.getTenantName(), jsonObject);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        // 创建集群
        StatefulService statefulService = null;
        try {
            statefulService = componentServiceFtpImpl.clusterCreate(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, statefulService, "创建ftp服务成功");
    }

    /**
     * 启动,停止
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "服务启动,服务停止", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "ftp服务ID", required = true, dataType = "String")
    public ApiResult operatorFtpCluster(@PathVariable("serviceId") String serviceId,
            @RequestBody FtpOperatorDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();
        try {
            switch (operator) {
            case "stop":
                componentServiceFtpImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceFtpImpl.clusterStop(tenantName, serviceId);
                break;
            case "start":
                componentServiceFtpImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceFtpImpl.clusterStart(tenantName, serviceId);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作ftp服务成功");
    }

    /**
     * 删除集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除集群", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "ftp服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteFtpCluster(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        try {
            componentServiceFtpImpl.checkClusterUpdate(tenantName, serviceId);
            componentServiceFtpImpl.clusterDelete(tenantName, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除ftp服务成功");
    }

    /**
     * 节点启动，停止
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/node/{nodeId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "实例启动，停止", notes = "")
    @ApiImplicitParam(paramType = "path", name = "nodeId", value = "ftp节点ID", required = true, dataType = "String")
    public ApiResult operatorFtpNode(@PathVariable("nodeId") String nodeId, @RequestBody NodeOperatorDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();
        try {

            componentServiceFtpImpl.checkNodeUpdate(tenantName, nodeId);
            switch (operator) {
            case "stop":
                componentServiceFtpImpl.nodeStop(tenantName, nodeId);
                break;
            case "start":
                componentServiceFtpImpl.nodeStart(tenantName, nodeId);
                break;
            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "实例操作成功");
    }

    /**
     * 删除节点
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/node/{nodeId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除节点", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "nodeId", value = "ftp节点ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteFtpNode(@PathVariable("nodeId") String nodeId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        try {
            componentServiceFtpImpl.checkNodeUpdate(tenantName, nodeId);
            componentServiceFtpImpl.nodeDelete(tenantName, nodeId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除ftp节点成功");
    }

    /**
     * 查询服务列表(page)
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务列表(page)", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceState", value = "服务状态", required = false, dataType = "String") })
    public ApiResult getFtpPage(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "serviceState", required = false) String serviceState) {

        Pageable pageable = PageUtil.getPageable(page, size);
        Page<StatefulService> serviceList = null;
        try {
            serviceList = componentServiceFtpImpl.getServiceList(tenantName, projectId, serviceName, serviceState,
                    CommonConst.APPTYPE_FTP, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceList, "查询服务列表(page)成功");
    }

    /**
     * 查询服务详情
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult getFtpInfo(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        StatefulService service = null;

        try {
            service = componentServiceFtpImpl.getServiceInfo(tenantName, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, service, "查询服务详情成功");
    }

    /**
     * 查询节点列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/node" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询节点列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "instanceName", value = "节点名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "nodeState", value = "节点状态", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "role", value = "节点角色", required = false, dataType = "String") })
    public ApiResult getFtpNode(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "instanceName", required = false) String instanceName,
            @RequestParam(value = "nodeState", required = false) String nodeState,
            @RequestParam(value = "role", required = false) String role) {

        List<StatefulNode> nodeList = null;
        try {
            nodeList = componentServiceFtpImpl.getNodeList(tenantName, serviceId, instanceName, role, nodeState);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, nodeList, "查询节点列表成功");
    }

    /**
     * 查询版本列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/version" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询版本列表", notes = "")
    @ApiImplicitParam(paramType = "query", name = "extendedField", value = "扩展字段", required = false, dataType = "String")
    public ApiResult getFtpVersion(@RequestParam(value = "extendedField", required = false) String extendedField) {

        List<String> versionList = null;
        try {
            versionList = componentServiceFtpImpl.getUnitVersion(CommonConst.APPTYPE_FTP, extendedField);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, versionList, "查询版本列表成功");
    }

    /**
     * 创建用户
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/user" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建用户", notes = "")
    public ApiResult createUser(@RequestBody FtpUserCreateDTO json) {

        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, String> map = null;

        // 创建集群
        FtpUser ftpUser = null;
        try {
            map = componentServiceFtpImpl.checkUserCreate(json.getTenantName(), json.getServiceId(), jsonObject);
            ftpUser = componentServiceFtpImpl.userCreate(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, ftpUser, "创建ftp用户成功");
    }

    /**
     * 修改用户
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/user/{userName}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改用户", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "userName", value = "用户名称", required = true, dataType = "String") })
    public ApiResult updateUser(@PathVariable("userName") String userName, @PathVariable("serviceId") String serviceId,
            @RequestBody FtpUserUpdateDTO json) {

        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, String> map = null;

        // 创建集群
        FtpUser ftpUser = null;
        try {
            map = componentServiceFtpImpl.checkUserUpdate(json.getTenantName(), serviceId, userName, jsonObject);
            ftpUser = componentServiceFtpImpl.userUpdate(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, ftpUser, "修改ftp用户成功");
    }

    /**
     * 删除用户
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/user" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "userName", value = "用户名称", required = true, dataType = "String") })
    public ApiResult deleteUser(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "userName", required = true) String userName) {

        try {
            componentServiceFtpImpl.checkUserDelete(tenantName, serviceId, userName);
            componentServiceFtpImpl.userDelete(tenantName, serviceId, userName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除ftp用户成功");
    }

    /**
     * 查询用户详情
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/user/{userName}" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询用户详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "userName", value = "用户名称", required = true, dataType = "String") })
    public ApiResult getFtpUserInfo(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @PathVariable("userName") String userName) {

        FtpUser ftpUser = null;

        try {
            ftpUser = componentServiceFtpImpl.getUserInfo(tenantName, serviceId, userName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, ftpUser, "查询用户详情成功");
    }

    /**
     * 查询用户列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/user" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询用户列表(page)", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "userName", value = "用户名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getFtpUserList(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        Pageable pageable = PageUtil.getPageable(page, size);
        Page<FtpUser> userList = null;

        try {
            userList = componentServiceFtpImpl.getUserList(tenantName, serviceId, userName, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, userList, "查询用户列表成功");
    }

    /**
     * 校验用户名是否存在
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/user/{userName}/check" }, method = RequestMethod.GET)
    @ApiOperation(value = "校验用户名是否存在", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "userName", value = "用户名称", required = true, dataType = "String") })
    public ApiResult checkFtpUser(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @PathVariable("userName") String userName) {

        boolean flag = false;

        try {
            flag = componentServiceFtpImpl.checkUserNameIsNotExist(tenantName, serviceId, userName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        if (!flag) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "用户名称已存在");
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, "用户名称不存在");
    }

    /**
     * 修改服务资源
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/ftp/{serviceId}/resource" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务资源", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "ftp服务ID", required = true, dataType = "String")
    public ApiResult changeResource(@PathVariable("serviceId") String serviceId,
            @RequestBody UpdateResourceDTO json) {

        String tenantName = json.getTenantName();
        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        try {
            Map<String, String> map = componentServiceFtpImpl.checkChangeResource(tenantName, serviceId, jsonObject);
            componentServiceFtpImpl.changeResource(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务资源成功");
    }

}
