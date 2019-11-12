package com.xxx.xcloud.rest.v1.env;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.env.entity.EnvTemplate;
import com.xxx.xcloud.module.env.service.EnvService;
import com.xxx.xcloud.rest.v1.env.dto.EnvDTO;
import com.xxx.xcloud.rest.v1.env.dto.EnvUpdateDTO;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * <p>
 * Description: 环境变量模板控制器
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Controller
@RequestMapping("/v1/env")
@Validated
public class EnvController {

    @Autowired
    private EnvService envService;

    /**
     * 创建环境变量模版
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建环境变量模版", notes = "")
    public ApiResult createEnv(@Valid @RequestBody EnvDTO json, BindingResult result) {

        EnvTemplate envTemplate = new EnvTemplate();
        envTemplate.setCreatedBy(json.getCreatedBy());
        envTemplate.setEnvData(JSON.toJSONString(json.getEnvData()));
        envTemplate.setProjectId(json.getProjectId());
        envTemplate.setTemplateName(json.getTemplateName());
        envTemplate.setTenantName(json.getTenantName());

        try {
            envTemplate = envService.add(envTemplate);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, envTemplate, "创建环境变量模版成功");
    }

    /**
     * 删除环境变量模版
     */
    @ResponseBody
    @RequestMapping(value = { "/{envId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除环境变量模版", notes = "")
    @ApiImplicitParam(paramType = "path", name = "envId", value = "环境变量模版ID", required = true, dataType = "String")
    public ApiResult updateEnv(@PathVariable("envId") String envId) {

        try {
            envService.delete(envId);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "删除环境变量模版失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除环境变量模版成功");
    }

    /**
     * 修改环境变量模版
     */
    @ResponseBody
    @RequestMapping(value = { "/{envId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改环境变量模版", notes = "")
    @ApiImplicitParam(paramType = "path", name = "envId", value = "环境变量模版ID", required = true, dataType = "String")
    public ApiResult updateEnv(@PathVariable("envId") String envId, @Valid @RequestBody EnvUpdateDTO json, BindingResult result) {

        try {
            envService.update(envId, json.getEnvData());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改环境变量模版成功");
    }

    /**
     * 获取环境变量模版详情
     */
    @ResponseBody
    @RequestMapping(value = { "/{envId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取环境变量模版详情", notes = "")
    @ApiImplicitParam(paramType = "path", name = "envId", value = "环境变量模版ID", required = true, dataType = "String")
    public ApiResult getEnv(@PathVariable("envId") String envId) {

        EnvTemplate env = null;
        try {
            env = envService.get(envId);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "获取环境变量模版详情失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, env, "获取环境变量模版详情成功");
    }

    /**
     * 获取环境变量模版列表
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取环境变量模版列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "templateName", value = "模版名称", required = false, dataType = "String") })
    public ApiResult getEnvList(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "templateName", required = false) String templateName) {

        List<EnvTemplate> env = null;
        try {
            env = envService.list(tenantName, templateName, projectId);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "获取环境变量模版列表失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, env, "获取环境变量模版列表成功");
    }

    /**
     * 通过环境变量模板名称，查询环境变量模版
     */
    @ResponseBody
    @RequestMapping(value = { "/templateName" }, method = RequestMethod.GET)
    @ApiOperation(value = "通过环境变量模板名称，查询环境变量模版", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "templateName", value = "模版名称", required = true, dataType = "String") })
    public ApiResult getEnvByName(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "templateName", required = true) String templateName) {

        EnvTemplate env = null;

        try {
            env = envService.getEnvTemplateByNameAndTenantName(templateName, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, env, "获取环境变量模版列表成功");
    }

    /**
     * 环境变量模版page查询
     */
    @ResponseBody
    @RequestMapping(value = { "/page" }, method = RequestMethod.GET)
    @ApiOperation(value = "环境变量模版列表查询", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "templateName", value = "环境变量模版名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页条数", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "当前页码", required = false, dataType = "int"), })
    public ApiResult findEnvTemplatePage(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "templateName", required = false) String templateName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page) {

        Page<EnvTemplate> envTemplatePage = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        try {
            envTemplatePage = envService.getEnvTemplateList(tenantName, templateName, projectId, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, envTemplatePage, "环境变量模版列表查询成功");
    }
}
