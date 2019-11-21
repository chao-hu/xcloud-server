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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;
import com.xxx.xcloud.rest.v1.component.model.CodisCreateDTO;
import com.xxx.xcloud.rest.v1.component.model.CodisOperatorDTO;
import com.xxx.xcloud.rest.v1.component.model.CodisResourceDTO;
import com.xxx.xcloud.rest.v1.component.model.ConfigDTO;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.module.component.service.IComponentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * @ClassName: CodisController
 * @Description: codis 功能
 * @author lnn
 * @date 2019年11月12日
 *
 */
@Controller
@Api(value = "Codis接口")
@RequestMapping("/v1/component")
public class CodisController {

    @Autowired
    @Qualifier("componentServiceCodisImpl")
    IComponentService componentServiceCodisImpl;

    /**
     * 创建集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建集群", notes = "")
    public ApiResult createCodisCluster(@RequestBody CodisCreateDTO json) {

        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, String> map = null;

        // 解析参数
        try {
            map = componentServiceCodisImpl.checkClusterCreate(json.getTenantName(), jsonObject);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        // 创建集群
        StatefulService statefulService = null;
        try {
            statefulService = componentServiceCodisImpl.clusterCreate(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, statefulService, "创建codis服务成功");
    }

    /**
     * 启动,停止,新增实例
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis/{serviceId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "服务启动,服务停止,新增实例", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "codis服务ID", required = true, dataType = "String")
    public ApiResult operatorCodisCluster(@PathVariable("serviceId") String serviceId,
            @RequestBody CodisOperatorDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();
        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);

        try {
            switch (operator) {
            case "stop":
                componentServiceCodisImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceCodisImpl.clusterStop(tenantName, serviceId);
                break;
            case "start":
                componentServiceCodisImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceCodisImpl.clusterStart(tenantName, serviceId);
                break;
            case "expand":
                Map<String, String> map = componentServiceCodisImpl.checkClusterExpand(tenantName, serviceId,
                        jsonObject);
                componentServiceCodisImpl.clusterExpand(map);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作codis服务成功");
    }

    /**
     * 删除集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis/{serviceId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除集群", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "codis服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteCodisCluster(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        try {
            componentServiceCodisImpl.checkClusterUpdate(tenantName, serviceId);
            componentServiceCodisImpl.clusterDelete(tenantName, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除codis服务成功");
    }

    /**
     * 修改服务资源
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis/{serviceId}/resource" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务资源", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "codis服务ID", required = true, dataType = "String")
    public ApiResult changeCodisResource(@PathVariable("serviceId") String serviceId,
            @RequestBody CodisResourceDTO json) {

        String tenantName = json.getTenantName();
        String jsonResource = JSON.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonResource);
        try {
            Map<String, String> map = componentServiceCodisImpl.checkChangeResource(tenantName, serviceId, jsonObject);
            componentServiceCodisImpl.changeResource(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "codis修改资源成功");
    }

    /**
     * 修改服务配置
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis/{serviceId}/config" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务配置", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "codis服务ID", required = true, dataType = "String")
    public ApiResult changeCodisConfig(@PathVariable("serviceId") String serviceId, @RequestBody ConfigDTO json) {

        String tenantName = json.getTenantName();
        try {
            Map<String, String> map = componentServiceCodisImpl.checkChangeConfig(tenantName, serviceId, json.getInfo());
            componentServiceCodisImpl.changeConfig(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务配置成功");
    }

    /**
     * 查询服务列表(page)
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务列表(page)", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceState", value = "服务状态", required = false, dataType = "String") })
    public ApiResult getCodisPage(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "serviceState", required = false) String serviceState) {

        Pageable pageable = PageUtil.getPageable(page, size);
        Page<StatefulService> serviceList = null;
        try {
            serviceList = componentServiceCodisImpl.getServiceList(tenantName, projectId, serviceName, serviceState,
                    CommonConst.APPTYPE_CODIS, pageable);
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
    @RequestMapping(value = { "/codis/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult getCodisInfo(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        StatefulService service = null;

        try {
            service = componentServiceCodisImpl.getServiceInfo(tenantName, serviceId);
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
    @RequestMapping(value = { "/codis/{serviceId}/node" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询节点列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "instanceName", value = "节点名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "nodeState", value = "节点状态", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "role", value = "节点角色", required = false, dataType = "String") })
    public ApiResult getCodisNodeList(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "instanceName", required = false) String instanceName,
            @RequestParam(value = "nodeState", required = false) String nodeState,
            @RequestParam(value = "role", required = false) String role) {

        List<StatefulNode> nodeList = null;
        try {
            nodeList = componentServiceCodisImpl.getNodeList(tenantName, serviceId, instanceName, role, nodeState);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, nodeList, "查询节点列表成功");
    }

    /**
     * 查询集群的配置参数
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis/config" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询集群的配置参数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID(创建查询为空)", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "version", value = "版本(创建后查询为空)", required = false, dataType = "String") })
    public ApiResult getCodisConfig(@RequestParam(value = "serviceId", required = false) String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "version", required = false) String version) {

        Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> configMap = null;
        try {
            configMap = componentServiceCodisImpl.getServiceConfig(tenantName, serviceId, CommonConst.APPTYPE_CODIS,
                    version);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, configMap, "查询集群的配置参数");
    }

    /**
     * 查询版本列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/codis/version" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询版本列表", notes = "")
    @ApiImplicitParam(paramType = "query", name = "extendedField", value = "扩展字段", required = false, dataType = "String")
    public ApiResult getCodisVersion(@RequestParam(value = "extendedField", required = false) String extendedField) {

        List<String> versionList = null;
        try {
            versionList = componentServiceCodisImpl.getUnitVersion(CommonConst.APPTYPE_CODIS, extendedField);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, versionList, "查询版本列表成功");
    }

}
