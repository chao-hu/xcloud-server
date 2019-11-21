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
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;
import com.xxx.xcloud.module.component.service.IComponentService;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.rest.v1.component.model.ConfigDTO;
import com.xxx.xcloud.rest.v1.component.model.KafkaCreateDTO;
import com.xxx.xcloud.rest.v1.component.model.KafkaOperatorDTO;
import com.xxx.xcloud.rest.v1.component.model.NodeOperatorDTO;
import com.xxx.xcloud.rest.v1.component.model.UpdateResourceDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: KafkaController
 * @Description: kafka操作
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Controller
@Api(value = "Kafka接口")
@RequestMapping("/v1/component")
public class KafkaController {

    @Autowired
    @Qualifier("componentServiceKafkaImpl")
    IComponentService componentServiceKafkaImpl;

    /**
     * 创建集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/kafka" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建集群", notes = "")
    public ApiResult createKafkaCluster(@RequestBody KafkaCreateDTO json) {

        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, String> map = null;

        // 解析参数
        try {
            map = componentServiceKafkaImpl.checkClusterCreate(json.getTenantName(), jsonObject);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        // 创建集群
        StatefulService statefulService = null;
        try {
            statefulService = componentServiceKafkaImpl.clusterCreate(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, statefulService, "创建kafka服务成功");
    }

    /**
     * 启动,停止,新增实例
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/kafka/{serviceId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "服务启动,服务停止,新增实例", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "kafka服务ID", required = true, dataType = "String")
    public ApiResult operatorKafkaCluster(@PathVariable("serviceId") String serviceId,
            @RequestBody KafkaOperatorDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();
        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        try {
            switch (operator) {
            case "stop":
                componentServiceKafkaImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceKafkaImpl.clusterStop(tenantName, serviceId);
                break;
            case "start":
                componentServiceKafkaImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceKafkaImpl.clusterStart(tenantName, serviceId);
                break;
            case "expand":
                Map<String, String> map = componentServiceKafkaImpl.checkClusterExpand(tenantName, serviceId,
                        jsonObject);
                componentServiceKafkaImpl.clusterExpand(map);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作kafka服务成功");
    }

    /**
     * 删除集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/kafka/{serviceId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除集群", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "kafka服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteKafkaCluster(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        try {
            componentServiceKafkaImpl.checkClusterUpdate(tenantName, serviceId);
            componentServiceKafkaImpl.clusterDelete(tenantName, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除kafka服务成功");
    }

    /**
     * 节点启动，停止
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/kafka/node/{nodeId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "实例启动，停止", notes = "")
    @ApiImplicitParam(paramType = "path", name = "nodeId", value = "kafka节点ID", required = true, dataType = "String")
    public ApiResult operatorKafkaNode(@PathVariable("nodeId") String nodeId, @RequestBody NodeOperatorDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();
        try {

            componentServiceKafkaImpl.checkNodeUpdate(tenantName, nodeId);
            switch (operator) {
            case "stop":
                componentServiceKafkaImpl.nodeStop(tenantName, nodeId);
                break;
            case "start":
                componentServiceKafkaImpl.nodeStart(tenantName, nodeId);
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
    @RequestMapping(value = { "/kafka/node/{nodeId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除节点", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "nodeId", value = "kafka节点ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteKafkaNode(@PathVariable("nodeId") String nodeId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        try {
            componentServiceKafkaImpl.checkNodeUpdate(tenantName, nodeId);
            componentServiceKafkaImpl.nodeDelete(tenantName, nodeId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除kafka节点成功");
    }

    /**
     * 修改服务资源
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/kafka/{serviceId}/resource" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务资源", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "kafka服务ID", required = true, dataType = "String")
    public ApiResult changeKafkaResource(@PathVariable("serviceId") String serviceId,
            @RequestBody UpdateResourceDTO json) {

        String tenantName = json.getTenantName();
        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        try {
            Map<String, String> map = componentServiceKafkaImpl.checkChangeResource(tenantName, serviceId, jsonObject);
            componentServiceKafkaImpl.changeResource(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务资源成功");
    }

    /**
     * 修改服务配置
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/kafka/{serviceId}/config" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务配置", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "kafka服务ID", required = true, dataType = "String")
    public ApiResult changeKafkaConfig(@PathVariable("serviceId") String serviceId, @RequestBody ConfigDTO json) {

        String tenantName = json.getTenantName();
        try {
            Map<String, String> map = componentServiceKafkaImpl.checkChangeConfig(tenantName, serviceId, json.getInfo());
            componentServiceKafkaImpl.changeConfig(map);
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
    @RequestMapping(value = { "/kafka" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务列表(page)", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceState", value = "服务状态", required = false, dataType = "String") })
    public ApiResult getKafkaPage(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "serviceState", required = false) String serviceState) {

        Pageable pageable = PageUtil.getPageable(page, size);
        Page<StatefulService> serviceList = null;
        try {
            serviceList = componentServiceKafkaImpl.getServiceList(tenantName, projectId, serviceName, serviceState,
                    CommonConst.APPTYPE_KAFKA, pageable);
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
    @RequestMapping(value = { "/kafka/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult getKafkaInfo(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        StatefulService service = null;

        try {
            service = componentServiceKafkaImpl.getServiceInfo(tenantName, serviceId);
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
    @RequestMapping(value = { "/kafka/{serviceId}/node" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询节点列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "instanceName", value = "节点名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "nodeState", value = "节点状态", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "role", value = "节点角色", required = false, dataType = "String") })
    public ApiResult getKafkaNodeList(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "instanceName", required = false) String instanceName,
            @RequestParam(value = "nodeState", required = false) String nodeState,
            @RequestParam(value = "role", required = false) String role) {

        List<StatefulNode> nodeList = null;
        try {
            nodeList = componentServiceKafkaImpl.getNodeList(tenantName, serviceId, instanceName, role, nodeState);
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
    @RequestMapping(value = { "/kafka/config" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询集群的配置参数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID(创建查询为空)", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "version", value = "版本(创建后查询为空)", required = false, dataType = "String") })
    public ApiResult getKafkaConfig(@RequestParam(value = "serviceId", required = false) String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "version", required = false) String version) {

        Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> configMap = null;
        try {
            configMap = componentServiceKafkaImpl.getServiceConfig(tenantName, serviceId, CommonConst.APPTYPE_KAFKA,
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
    @RequestMapping(value = { "/kafka/version" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询版本列表", notes = "")
    @ApiImplicitParam(paramType = "query", name = "extendedField", value = "扩展字段", required = false, dataType = "String")
    public ApiResult getKafkaVersion(@RequestParam(value = "extendedField", required = false) String extendedField) {

        List<String> versionList = null;
        try {
            versionList = componentServiceKafkaImpl.getUnitVersion(CommonConst.APPTYPE_KAFKA, extendedField);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, versionList, "查询版本列表成功");
    }

}
