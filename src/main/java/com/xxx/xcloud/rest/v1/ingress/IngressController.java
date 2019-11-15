package com.xxx.xcloud.rest.v1.ingress;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
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
import com.xxx.xcloud.module.ingress.entity.IngressDomain;
import com.xxx.xcloud.module.ingress.entity.IngressProxy;
import com.xxx.xcloud.module.ingress.entity.ServiceIngress;
import com.xxx.xcloud.module.ingress.repository.IngressDomainRepository;
import com.xxx.xcloud.module.ingress.service.IngressDomainService;
import com.xxx.xcloud.module.ingress.service.IngressProxyService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.ingress.model.IngressParameter;
import com.xxx.xcloud.rest.v1.ingress.model.ProxyCreateModelDTO;
import com.xxx.xcloud.rest.v1.ingress.model.ProxyUpdateModelDTO;
import com.xxx.xcloud.rest.v1.ingress.model.ServiceDomainModelDTO;
import com.xxx.xcloud.rest.v1.service.model.DomainModelDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author ruzz
 *
 */
@Controller
@RequestMapping("/v1/ingress")
public class IngressController {

    private static final Logger LOG = LoggerFactory.getLogger(IngressController.class);

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Autowired
    private IngressDomainService ingressDomainService;

    @Autowired
    private IngressProxyService ingressProxyService;

    @Autowired
    private IngressDomainRepository ingressDomainRepository;

    @Autowired
    private IAppDetailService appDetailService;

    /**
     * 域名创建
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/domain" }, method = RequestMethod.POST)
    @ApiOperation(value = "域名创建", notes = "")
    public ApiResult createDomain(@RequestBody DomainModelDTO json) {

        ApiResult result = null;
        // 校验参数
        String domain = json.getDomain();
        result = checkDomain(domain);
        if (null != result) {
            return result;
        }

        // 包装Domain信息
        IngressDomain ingressDomain = generateDomainTLD(json);

        try {
            ingressDomain = ingressDomainService.createIngressDomain(ingressDomain);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, ingressDomain, "创建域名成功");
    }

    /**
     * 构建域名
     *
     * @param json
     * @return
     */

    private IngressDomain generateDomainTLD(DomainModelDTO json) {

        IngressDomain ingressDomain = new IngressDomain();

        if (!Global.DOMAIN_TLD.equals(json.getType()) && !Global.DOMAIN_SLD.equals(json.getType())) {
            LOG.info("type参数值只能为" + Global.DOMAIN_TLD + "或" + Global.DOMAIN_SLD);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "type参数值只能为" + Global.DOMAIN_TLD + "或" + Global.DOMAIN_SLD);
        }

        ingressDomain.setType(json.getType());
        ingressDomain.setTenantName("admin");
        ingressDomain.setDomain(json.getDomain());

        return ingressDomain;
    }

    /**
     * 构建域名
     *
     * @param json
     * @return
     */
    private IngressDomain generateDomain(String domainId, DomainModelDTO json) {

        IngressDomain ingressDomain = null;
        if (null == domainId) {
            ingressDomain = new IngressDomain();
            ingressDomain.setDomain(json.getDomain());
        } else {
            ingressDomain = ingressDomainRepository.getOne(domainId);
            if (ingressDomain.getDomain().equals(json.getDomain())) {
                LOG.info("域名不能修改");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "域名不能修改");
            }
        }
        if (StringUtils.isEmpty(json.getType())) {
            LOG.info("type不能为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "type不能为空");
        } else if (!Global.DOMAIN_TLD.equals(json.getType()) && !Global.DOMAIN_SLD.equals(json.getType())) {
            LOG.info("type参数值只能为" + Global.DOMAIN_TLD + "或" + Global.DOMAIN_SLD);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "type参数值只能为" + Global.DOMAIN_TLD + "或" + Global.DOMAIN_SLD);
        }
        ingressDomain.setType(json.getType());
        if (StringUtils.isNotEmpty(json.getTenantName())) {
            ingressDomain.setTenantName(json.getTenantName());
        } else {
            ingressDomain.setTenantName("admin");
        }
        if (Global.DOMAIN_USE_CONFIG != json.getConfigStatus()
                && Global.DOMAIN_NOT_USE_CONFIG != json.getConfigStatus()) {
            LOG.info("configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
        }
        ingressDomain.setConfigStatus(json.getConfigStatus());
        ingressDomain.setAddBaseUrl(json.getAddBaseUrl());
        ingressDomain.setBaseUrlScheme(json.getBaseUrlScheme());
        ingressDomain.setxForwardedPrefix(json.getXForwardedPrefix());
        ingressDomain.setProxyPassParams(json.getXForwardedPrefix());
        ingressDomain.setServerAlias(json.getServerAlias());
        ingressDomain.setLimitRate(json.getLimitRate());
        ingressDomain.setLimitRateAfter(json.getLimitRateAfter());
        ingressDomain.setHttpsSecretName(json.getHttpsSecretName());
        if (Global.DOMAIN_USE_HTTPS != json.getHttpsStatus() && Global.DOMAIN_NOT_USE_HTTPS != json.getHttpsStatus()) {
            LOG.info("httpsStatus参数值只能为" + Global.DOMAIN_USE_HTTPS + "或" + Global.DOMAIN_NOT_USE_HTTPS);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "httpsStatus参数值只能为" + Global.DOMAIN_USE_HTTPS + "或" + Global.DOMAIN_NOT_USE_HTTPS);
        }
        ingressDomain.setHttpsStatus(json.getHttpsStatus());
        ingressDomain.setHttpsTlsCrt(json.getHttpsTlsCrt());
        ingressDomain.setHttpsTlsKey(json.getHttpsTlsKet());
        ingressDomain.setProjectCode(json.getProjectCode());

        return ingressDomain;
    }

    /**
     * 校验域名
     *
     *
     * @param domain
     * @return
     */
    private ApiResult checkDomain(String domain) {

        LOG.info(domain);
        String[] domainEnding = StringUtils.split(domain, ".");
        LOG.info(String.valueOf(domainEnding.length));
        LOG.info(String.valueOf(domainEnding[domainEnding.length - 1]));

        if (!Global.getIngressDomainType().contains(domainEnding[domainEnding.length - 1])
                || !domain.matches(Global.CHECK_DOMAIN_NAME)) {
            LOG.info("------------域名不符合互联网域名规范-------------" + domain);
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "域名不符合互联网域名规范");
        }
        LOG.info("------------域名符合互联网域名规范-------------" + domain);
        List<IngressDomain> ingressDomain = new ArrayList<IngressDomain>();

        ingressDomain = ingressDomainRepository.findByDomain(domain);

        if (!ingressDomain.isEmpty()) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "域名已存在");
        }

        return null;
    }

    /**
     * 校验域名
     *
     *
     * @param domain
     * @return
     */
    private ApiResult checkDomainIsExist(String domain) {

        if (StringUtils.isEmpty(domain)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "域名为空");
        }
        LOG.info(domain);
        String[] domainEnding = StringUtils.split(domain, ".");
        LOG.info(String.valueOf(domainEnding.length));
        LOG.info(domainEnding[domainEnding.length - 1]);
        if (!Global.getIngressDomainType().contains(domainEnding[domainEnding.length - 1])
                || !domain.matches(Global.CHECK_DOMAIN_NAME)) {
            LOG.info("------------域名不符合互联网域名规范-------------" + domain);
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "域名不符合互联网域名规范");
        }
        LOG.info("------------域名符合互联网域名规范-------------" + domain);
        List<IngressDomain> ingressDomain = new ArrayList<IngressDomain>();

        ingressDomain = ingressDomainRepository.findByDomain(domain);

        if (ingressDomain.isEmpty()) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "域名不存在");
        }

        return null;
    }

    /**
     * 修改域名
     */
    @ResponseBody
    @RequestMapping(value = { "/domain/{domainId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改域名", notes = "")
    @ApiImplicitParam(paramType = "path", name = "domainId", value = "域名ID", required = true, dataType = "String")
    public ApiResult updateDomain(@PathVariable("domainId") String domainId, @RequestBody DomainModelDTO json) {
        ApiResult result = checkDomainIsExist(json.getDomain());
        if (null != result) {
            return result;
        }
        IngressDomain ingressDomain = generateDomain(domainId, json);
        try {
            ingressDomain = ingressDomainService.updateIngressDomain(ingressDomain);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, ingressDomain, "修改域名成功");
    }

    /**
     * 删除域名
     */
    @ResponseBody
    @RequestMapping(value = { "/domain/{domainId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除域名", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "domainId", value = "域名ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = false, dataType = "String") })
    public ApiResult deleteDomain(@PathVariable("domainId") String domainId,
            @NotBlank(message = "租户名称不能为空") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = false) String tenantName) {
        if (null != tenantName) {
            ApiResult apiResult = checkTenantName(tenantName);
            if (apiResult != null) {
                return apiResult;
            }
        }
        try {
            ingressDomainService.deleteIngressDomain(domainId, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除域名成功");
    }

    /**
     * 域名查询
     */
    @ResponseBody
    @RequestMapping(value = { "/domain" }, method = RequestMethod.GET)
    @ApiOperation(value = "域名查询", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "2000", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, defaultValue = "0", dataType = "int") })
    public ApiResult findDomainPage(@RequestParam(value = "size", required = true, defaultValue = "2000") int size,
            @RequestParam(value = "page", required = true, defaultValue = "0") int page) {

        PageRequest pageable = PageRequest.of(page, size);

        String type = Global.DOMAIN_TLD;
        Page<IngressDomain> pageIngress = null;

        try {
            pageIngress = ingressDomainService.ingressDomain(null, type, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, pageIngress, "查询域名成功");
    }

    /**
     * Http代理创建
     */
    @ResponseBody
    @RequestMapping(value = { "/proxy" }, method = RequestMethod.POST)
    @ApiOperation(value = "Http代理创建", notes = "")
    public ApiResult createProxy(@RequestBody ProxyCreateModelDTO json) {

        ApiResult result = new ApiResult();
        String tenantName = json.getTenantName();
        String serviceId = json.getServiceIngress().getServiceId();
        result = checkDomainIsExist(json.getServiceDomain().getDomain());
        if (null != result) {
            return result;
        }
        // 创建二级域名
        IngressDomain ingressDomain = generateServiceDomain(json.getServiceDomain(), tenantName);
        LOG.info("----1---ingressDomain-------" + JSON.toJSONString(ingressDomain));
        try {
            ingressDomain = ingressDomainService.createIngressDomain(ingressDomain);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        LOG.info("----2---ingressDomain-------" + JSON.toJSONString(ingressDomain));
        // 校验参数
        result = checkProxyParam(tenantName, serviceId, ingressDomain.getId());
        if (null != result) {
            return result;
        }
        json.getServiceIngress().setIngressDomainId(ingressDomain.getId());
        // 包装
        IngressParameter ingressParameter = generateCreateProxy(json);
        // 创建
        try {
            ingressProxyService.createIngressProxy(ingressParameter);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "Http代理创建成功");
    }

    private IngressDomain generateServiceDomain(ServiceDomainModelDTO json, String tenantName) {
        IngressDomain ingressDomain = new IngressDomain();
        ingressDomain.setDomain(json.getDomain());
        ingressDomain.setType(Global.DOMAIN_SLD);
        ingressDomain.setTenantName(tenantName);
        ingressDomain.setConfigStatus(json.getConfigStatus());
        ingressDomain.setAddBaseUrl(json.getAddBaseUrl());
        ingressDomain.setBaseUrlScheme(json.getBaseUrlScheme());
        ingressDomain.setxForwardedPrefix(json.getXForwardedPrefix());
        ingressDomain.setProxyPassParams(json.getXForwardedPrefix());
        ingressDomain.setServerAlias(json.getServerAlias());
        ingressDomain.setLimitRate(json.getLimitRate());
        ingressDomain.setLimitRateAfter(json.getLimitRateAfter());
        ingressDomain.setHttpsSecretName(json.getHttpsSecretName());
        ingressDomain.setHttpsStatus(json.getHttpsStatus());
        ingressDomain.setHttpsTlsCrt(json.getHttpsTlsCrt());
        ingressDomain.setHttpsTlsKey(json.getHttpsTlsKey());
        ingressDomain.setProjectCode(json.getProjectCode());
        ingressDomain.setIngressDomainName(ingressDomain.getProjectCode() + "." + ingressDomain.getDomain());

        return ingressDomain;
    }

    private IngressParameter generateCreateProxy(ProxyCreateModelDTO json) {

        IngressParameter ingressParameter = new IngressParameter();
        ServiceIngress serviceIngress = new ServiceIngress();
        IngressProxy ingressProxy = new IngressProxy();
        serviceIngress.setIngressDomainId(json.getServiceIngress().getIngressDomainId());
        serviceIngress.setServiceId(json.getServiceIngress().getServiceId());
        serviceIngress.setPathAndPort(json.getServiceIngress().getPathAndPort());
        ingressProxy.setApproveConfig(json.getIngressProxy().getApproveConfig());
        if (Global.DOMAIN_USE_CONFIG != json.getIngressProxy().getConfigStatus()
                && Global.DOMAIN_NOT_USE_CONFIG != json.getIngressProxy().getConfigStatus()) {
            LOG.info("configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
        }
        ingressProxy.setConfigStatus(json.getIngressProxy().getConfigStatus());
        ingressProxy.setNginxConfig(json.getIngressProxy().getNginxConfig());
        ingressProxy.setOtherConfig(json.getIngressProxy().getOtherConfig());
        ingressProxy.setResoruceConfig(json.getIngressProxy().getResourceConfig());
        ingressProxy.setTlsConfig(json.getIngressProxy().getTlsConfig());
        ingressProxy.setUrlConfig(json.getIngressProxy().getUrlConfig());
        ingressProxy.setCanaryStatus(json.getIngressProxy().getCanaryStatus());
        if(null != json.getIngressProxy().getCanaryConfig()) {
        	ingressProxy.setCanaryConfig(JSON.toJSONString(json.getIngressProxy().getCanaryConfig()));
        }
        
        ingressParameter.setIngressProxy(ingressProxy);
        ingressParameter.setServiceIngress(serviceIngress);
        ingressParameter.setTenantName(json.getTenantName());
        return ingressParameter;
    }

    private IngressParameter generateUpdateProxy(ProxyUpdateModelDTO json) {

        IngressParameter ingressParameter = new IngressParameter();
        ServiceIngress serviceIngress = new ServiceIngress();
        IngressProxy ingressProxy = new IngressProxy();

        serviceIngress.setId(json.getServiceIngress().getId());
        serviceIngress.setIngressProxyId(json.getServiceIngress().getIngressProxyId());
        serviceIngress.setIngressDomainId(json.getServiceIngress().getIngressDomainId());
        serviceIngress.setServiceId(json.getServiceIngress().getServiceId());
        serviceIngress.setPathAndPort(json.getServiceIngress().getPathAndPort());

        ingressProxy.setApproveConfig(json.getIngressProxy().getApproveConfig());
        if (Global.DOMAIN_USE_CONFIG != json.getIngressProxy().getConfigStatus()
                && Global.DOMAIN_NOT_USE_CONFIG != json.getIngressProxy().getConfigStatus()) {
            LOG.info("configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
        }
        ingressProxy.setConfigStatus(json.getIngressProxy().getConfigStatus());
        ingressProxy.setNginxConfig(json.getIngressProxy().getNginxConfig());
        ingressProxy.setOtherConfig(json.getIngressProxy().getOtherConfig());
        ingressProxy.setResoruceConfig(json.getIngressProxy().getResourceConfig());
        ingressProxy.setTlsConfig(json.getIngressProxy().getTlsConfig());
        ingressProxy.setUrlConfig(json.getIngressProxy().getUrlConfig());
        ingressProxy.setId(json.getIngressProxy().getId());
        ingressProxy.setCanaryStatus(json.getIngressProxy().getCanaryStatus());
        if(null != json.getIngressProxy().getCanaryConfig()) {
        	ingressProxy.setCanaryConfig(JSON.toJSONString(json.getIngressProxy().getCanaryConfig()));
        }

        ingressParameter.setIngressProxy(ingressProxy);
        ingressParameter.setServiceIngress(serviceIngress);
        ingressParameter.setTenantName(json.getTenantName());
        return ingressParameter;
    }

    private ApiResult checkProxyParam(String tenantName, String serviceId, String ingressDomainId) {

        ApiResult result = new ApiResult();

        // 校验租户名
        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }
        result = checkServiceId(serviceId);
        if (null != result) {
            return result;
        }
        return null;
    }

    private ApiResult checkServiceId(String serviceId) {

        if (StringUtils.isEmpty(serviceId)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "服务ID不存在");
        }

        try {
            Service service = appDetailService.getServiceById(serviceId);
            if (null == service) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "服务不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return null;
    }

    /**
     * Http代理修改
     */
    @ResponseBody
    @RequestMapping(value = { "/proxy" }, method = RequestMethod.PUT)
    @ApiOperation(value = " Http代理修改", notes = "")
    public ApiResult updateProxy(@RequestBody ProxyUpdateModelDTO json) {

        if (StringUtils.isEmpty(json.getServiceIngress().getId())) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "id不能为空");
        }
        if (StringUtils.isEmpty(json.getIngressProxy().getId())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "ingressProxyId不能为空");
        }
        if (StringUtils.isEmpty(json.getServiceIngress().getIngressDomainId())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "ingressDomainId不能为空");
        }
        // 包装
        IngressParameter ingressParameter = generateUpdateProxy(json);
        try {
            ingressProxyService.updateIngressProxy(ingressParameter);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "Http代理修改成功");
    }

    /**
     * Http代理删除
     */
    @ResponseBody
    @RequestMapping(value = { "/proxy" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "Http代理删除", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteProxy(@RequestParam(value = "serviceId", required = true) String serviceId,
            @NotBlank(message = "租户名称不能为空") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName) {
        ApiResult apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }
        try {
            ingressProxyService.deleteIngressProxy(serviceId, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "Http代理删除成功");
    }

    /**
     * Http代理查询
     */
    @ResponseBody
    @RequestMapping(value = { "/proxy" }, method = RequestMethod.GET)
    @ApiOperation(value = "Http代理查询", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult findProxy(@RequestParam(value = "serviceId", required = true) String serviceId,
            @NotBlank(message = "租户名称不能为空") @Pattern(regexp = Global.CHECK_TENANT_NAME, message = "租户名称规则不符合规范") @RequestParam(value = "tenantName", required = true) String tenantName) {
        ApiResult apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }
        IngressParameter ingressParameter = null;
        try {
            ingressParameter = ingressProxyService.getIngressProxy(serviceId, tenantName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, ingressParameter, "Http代理查询成功");
    }

    private ApiResult checkTenantName(String tenantName) {

        ApiResult result = null;

        Tenant tenant = null;
        try {
            tenant = tenantService.findTenantByTenantName(tenantName);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询tenantName: " + tenantName + " 失败");
        }
        if (null == tenant) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户tenantName: " + tenantName + " 不存在");
        }
        return result;
    }

}
