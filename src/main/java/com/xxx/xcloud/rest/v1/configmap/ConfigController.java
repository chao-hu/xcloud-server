package com.xxx.xcloud.rest.v1.configmap;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.configmap.entity.ConfigTemplate;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;
import com.xxx.xcloud.module.configmap.service.ConfigService;
import com.xxx.xcloud.rest.v1.configmap.dto.ConfigDTO;
import com.xxx.xcloud.rest.v1.configmap.dto.ConfigMountDTO;
import com.xxx.xcloud.rest.v1.configmap.dto.ConfigUpdateDTO;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * <p>
 * Description: 配置文件模板控制器
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Controller
@RequestMapping("/v1/config")
@Validated
public class ConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigService configService;

    /**
     * 创建服务配置模版
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建服务配置模版", notes = "")
    public ApiResult createConfig(@Valid @RequestBody ConfigDTO dto) {

        ConfigTemplate config = null;
        try {
            config = configService.add(dto.buildConfigTemplate());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, config, "创建服务配置模版成功");
    }

    /**
     * 删除服务配置模版
     */
    @ResponseBody
    @RequestMapping(value = { "/{configId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务配置模版", notes = "")
    @ApiImplicitParam(paramType = "path", name = "configId", value = "服务配置模版ID", required = true, dataType = "String")
    public ApiResult deleteConfig(@PathVariable("configId") String configId) {

        try {
            configService.delete(configId);
        } catch (ErrorMessageException e) {
            LOG.error("删除服务配置模版失败", e);
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务配置模版成功");
    }

    /**
     * 修改服务配置模版
     */
    @ResponseBody
    @RequestMapping(value = { "/{configId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务配置模版", notes = "")
    @ApiImplicitParam(paramType = "path", name = "configId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateConfig(@PathVariable("configId") String configId, @Valid @RequestBody ConfigUpdateDTO json) {

        try {
            configService.update(configId, json.getConfigData());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务配置模版成功");
    }

    /**
     * 通过配置模板名称，查询服务配置模版
     */
    @ResponseBody
    @RequestMapping(value = { "/templateName" }, method = RequestMethod.GET)
    @ApiOperation(value = "通过配置模板名称，查询服务配置模版", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "templateName", value = "模版名称", required = true, dataType = "String") })
    public ApiResult getConfigByName(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "templateName", required = true) String templateName) {

        ConfigTemplate config = null;
        try {
            config = configService.getConfigTemplateByNameAndTenantName(templateName, tenantName);

        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOG.error("获取服务配置模版详情失败", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "获取服务配置模版详情失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, config, "获取服务配置模版详情成功");
    }

    /**
     * 获取服务配置模版详情
     */
    @ResponseBody
    @RequestMapping(value = { "/{configId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务配置模版详情", notes = "")
    @ApiImplicitParam(paramType = "path", name = "configId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getConfig(@PathVariable("configId") String configId) {

        ConfigTemplate config = null;
        try {
            config = configService.get(configId);
        } catch (Exception e) {
            LOG.error("获取服务配置模版详情失败", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "获取服务配置模版详情失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, config, "获取服务配置模版详情成功");
    }

    /**
     * 获取服务配置模版列表
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务配置模版列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "templateName", value = "模版名称", required = false, dataType = "String") })
    public ApiResult getConfigList(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "templateName", required = false) String templateName) {

        List<ConfigTemplate> configTemplates = null;
        try {
            configTemplates = configService.list(tenantName, templateName, projectId);
        } catch (Exception e) {
            LOG.error("获取服务配置模版列表失败", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "获取服务配置模版列表失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, configTemplates, "获取服务配置模版列表成功");
    }

    /**
     * 获取服务配置挂载列表
     */
    @ResponseBody
    @RequestMapping(value = { "/mount" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务配置挂载列表", notes = "")
    @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getConfigMountList(@RequestParam(value = "serviceId", required = true) String serviceId) {

        List<ServiceConfig> configTemplates = null;
        try {
            configTemplates = configService.listMount(serviceId);
        } catch (Exception e) {
            LOG.error("获取服务配置挂载列表失败", e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "获取服务配置挂载列表失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, configTemplates, "获取服务配置挂载列表成功");
    }

    /**
     * 保存服务配置挂载模版
     */
    @ResponseBody
    @RequestMapping(value = { "/mount" }, method = RequestMethod.POST)
    @ApiOperation(value = "保存服务配置挂载模版", notes = "")
    public ApiResult saveConfigMount(@Valid @RequestBody ConfigMountDTO json) {

        String serviceId = json.getServiceId();

        Map<String, String> config = json.getConfigIdAndPath();
        if (config == null || config.isEmpty()) {
            LOG.info("服务配置挂载模版参数[configIdsAndPath]不能为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "服务配置挂载模版参数[configIdsAndPath]不能为空");
        }

        try {
            configService.mountSave(serviceId, config);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOG.error("保存服务配置挂载失败", e);
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存服务配置挂载失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "保存服务配置挂载成功");
    }

    /**
     * 取消服务配置挂载模版
     */
    @ResponseBody
    @RequestMapping(value = { "/mount" }, method = RequestMethod.PUT)
    @ApiOperation(value = "取消服务配置挂载模版", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "configTemplateId", value = "挂载模版ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult cancelConfigMount(
            @RequestParam(value = "configTemplateId", required = true) String configTemplateId,
            @RequestParam(value = "serviceId", required = true) String serviceId) {

        try {
            configService.mountCancel(serviceId, configTemplateId);
        } catch (Exception e) {
            LOG.error("取消服务配置挂载模版失败", e);
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "取消服务配置挂载模版失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "取消服务配置挂载模版成功");
    }

    /**
     * 清空服务配置挂载模版
     */
    @ResponseBody
    @RequestMapping(value = { "/mount" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "清空服务配置挂载模版", notes = "")
    @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult cancelConfigMount(@RequestParam(value = "serviceId", required = true) String serviceId) {

        try {
            configService.mountClear(serviceId);
        } catch (Exception e) {
            LOG.error("取消服务配置挂载模版失败", e);
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "清空配置挂载失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "清空配置挂载成功");
    }

    /**
     * 配置文件page查询
     */
    @ResponseBody
    @RequestMapping(value = { "/page" }, method = RequestMethod.GET)
    @ApiOperation(value = "配置文件列表查询", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "templateName", value = "配置文件名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页条数", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "当前页码", required = false, dataType = "int"), })
    public ApiResult findConfigTemplatePage(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "templateName", required = false) String templateName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page) {

        Page<ConfigTemplate> configTemplatePage = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        try {
            configTemplatePage = configService.getConfigTemplateList(tenantName, templateName, projectId, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, configTemplatePage, "配置文件列表查询成功");
    }

}
