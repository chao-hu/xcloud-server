/**
 * @Title: TenantController.java
 * @Package com.xxx.xcloud.rest.v1.tenant
 * @Description: TODO(用一句话描述该文件做什么)
 * @author huchao
 * @date 2019年10月24日
 * @version V1.0
 */
package com.xxx.xcloud.rest.v1.tenant;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.tenant.model.TenantDTO;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: TenantController
 * @Description: 租户的相关接口
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Controller
@RequestMapping("/v1/tenant")
@Validated
public class TenantController {

    @Autowired
    ITenantService tenantServiceImplV1;

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建租户", notes = "")
    public ApiResult createTenant(@Valid @RequestBody TenantDTO obj, BindingResult result) {

        String tenantName = obj.getTenantName();

        Tenant tenant = tenantServiceImplV1.findTenantByTenantName(tenantName);
        if (null == tenant) {

            tenant = tenantServiceImplV1.createTenant(tenantName);
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, tenant, "创建租户成功");
    }

    @ResponseBody
    @RequestMapping(value = { "/{tenantName}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取租户信息", notes = "")
    @ApiImplicitParam(paramType = "path", name = "tenantName", value = "租户名称", required = true, dataType = "String")
    public ApiResult findTenant(
            @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @PathVariable("tenantName") String tenantName) {

        Tenant tenant = tenantServiceImplV1.findTenantByTenantName(tenantName);
        if (null == tenant) {

            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户不存在");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, tenant, "获取租户信息成功");
    }

    @ResponseBody
    @RequestMapping(value = { "/{tenantName}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除租户", notes = "")
    @ApiImplicitParam(paramType = "path", name = "tenantName", value = "租户名称", required = false, dataType = "String")
    public ApiResult deleteTenant(
            @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @PathVariable("tenantName") String tenantName) {

        Tenant tenant = tenantServiceImplV1.findTenantByTenantName(tenantName);
        if (null != tenant) {

            tenantServiceImplV1.deleteTenant(tenantName);
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, tenant, "删除租户成功");
    }

}
