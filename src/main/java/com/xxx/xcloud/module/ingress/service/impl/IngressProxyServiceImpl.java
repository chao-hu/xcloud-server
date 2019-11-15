package com.xxx.xcloud.module.ingress.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.ingress.apiutils.IngressApi;
import com.xxx.xcloud.module.ingress.consts.IngressAnnotations;
import com.xxx.xcloud.module.ingress.entity.IngressDomain;
import com.xxx.xcloud.module.ingress.entity.IngressProxy;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.ApproveConfig;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.CanaryConfig;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.NginxConfig;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.OtherConfig;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.ResourceConfig;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.TlsConfig;
import com.xxx.xcloud.module.ingress.entity.IngressProxy.UrlConfig;
import com.xxx.xcloud.module.ingress.entity.ServiceIngress;
import com.xxx.xcloud.module.ingress.repository.IngressDomainRepository;
import com.xxx.xcloud.module.ingress.repository.IngressProxyRepository;
import com.xxx.xcloud.module.ingress.repository.ServiceIngressRepository;
import com.xxx.xcloud.module.ingress.service.IngressProxyService;
import com.xxx.xcloud.rest.v1.ingress.model.IngressParameter;
import com.xxx.xcloud.utils.Base64Util;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;

/**
 * Description:服务代理实现类
 *
 * @author LYJ </br>
 *         create time：2018年12月5日 上午11:07:43 </br>
 * @version 1.0
 * @since
 */
@Service
public class IngressProxyServiceImpl implements IngressProxyService {

	private static final Logger LOG = LoggerFactory.getLogger(IngressProxyServiceImpl.class);

	@Autowired
	private ServiceIngressRepository serviceIngressRepository;

	@Autowired
	private IngressProxyRepository ingressProxyRepository;

	@Autowired
	private IngressDomainRepository ingressDomainRepository;

	@Autowired
	private ServiceRepository serviceRepository;

	@Override
	public IngressParameter getIngressProxy(String serviceId, String tenantName) {
		IngressParameter ingressParameter = new IngressParameter();
		try {
			ServiceIngress serviceIngress = serviceIngressRepository.findByServiceId(serviceId);
			if (null != serviceIngress) {
				ingressParameter.setTenantName(tenantName);
				ingressParameter.setServiceIngress(serviceIngress);
				Optional<IngressProxy> ingressProxyOptional = ingressProxyRepository
						.findById(serviceIngress.getIngressProxyId());
				ingressParameter.setIngressProxy(ingressProxyOptional.get());
				Optional<IngressDomain> ingressDomainOptional = ingressDomainRepository
						.findById(serviceIngress.getIngressDomainId());
				if (ingressDomainOptional.isPresent()) {
					ingressParameter.setProjectCode(ingressDomainOptional.get().getProjectCode());
					ingressParameter.setDomain(ingressDomainOptional.get().getDomain());
				}
			}
		} catch (Exception e) {
			LOG.error("查询失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询失败!");
		}
		return ingressParameter;
	}

	@Override
	@Transactional(rollbackFor = ErrorMessageException.class)
	public Boolean createIngressProxy(IngressParameter ingressProxyParameter) {
		ServiceIngress serviceIngress = ingressProxyParameter.getServiceIngress();
		String serviceId = ingressProxyParameter.getServiceIngress().getServiceId();
		IngressProxy ingressProxy = ingressProxyParameter.getIngressProxy();
		String tenantName = ingressProxyParameter.getTenantName();
		Optional<com.xxx.xcloud.module.application.entity.Service> serviceOptional = serviceRepository
				.findById(serviceId);
		com.xxx.xcloud.module.application.entity.Service service = null;
		if (serviceOptional.isPresent()) {
			service = serviceOptional.get();
		} else {
			LOG.info("服务不存在");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
		}
		Optional<IngressDomain> ingressDomainOptional = ingressDomainRepository
				.findById(serviceIngress.getIngressDomainId());
		IngressDomain ingressDomain = null;
		if (ingressDomainOptional.isPresent()) {
			ingressDomain = ingressDomainOptional.get();
		} else {
			LOG.info("域名不存在");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "域名不存在");
		}
		Map<String, String> map = new HashMap<String, String>(16);
		// 获取页面填写的访问路径和端口
		Map<String, String> pathAndPort = JSON.parseObject(serviceIngress.getPathAndPort(),
				new TypeReference<Map<String, String>>() {
				});
		if (null != pathAndPort) {
			for (Map.Entry item : pathAndPort.entrySet()) {
				map.put(item.getKey().toString(), (item.getValue().toString()));
			}
		}
		serviceIngress.setPathAndPort(JSON.toJSONString(map));
		ingressProxy.setIngressName(ingressDomain.getIngressDomainName() + "." + service.getServiceName());
		Ingress ingress = null;
		try {
			ingress = generateIngressLocation(tenantName, map, ingressProxy, ingressDomain.getIngressDomainName(),
					service.getServiceName());
		} catch (Exception e) {
			LOG.error("封装Ingress资源对象失败!" + " error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED, "封装Ingress资源对象失败!");
		}
		IngressApi.createOrReplaceIngress(ingress, tenantName);
		// CA认证
		ApproveConfig approveConfig = ingressProxy.getApproveConfig();
		if (null != approveConfig) {
			if (null != approveConfig.getAuthTlsSecret() && null != approveConfig.getAuthType()) {
				ingressProxy.setAuthTlsSecretName(ingressProxy.getIngressName() + ".ca.secret");
				createCaSecret(tenantName, approveConfig, ingressProxy.getAuthTlsSecretName());
			}
		}
		ServiceIngress serviceIngressFind = null;
		try {
			serviceIngressFind = serviceIngressRepository.findByServiceIdAndIngressDomainId(serviceId,
					ingressDomain.getId());
		} catch (Exception e) {
			LOG.error("服务代理创建失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "服务代理创建失败");
		}
		if (null != serviceIngressFind) {
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "此域名下服务代理已创建,只能修改/删除或者选择不同的域名进行创建");
		}
		// 保存到数据库
		try {
			IngressProxy saveIngressProxy = ingressProxyRepository.save(ingressProxy);
			serviceIngress.setIngressProxyId(saveIngressProxy.getId());
			serviceIngressRepository.save(serviceIngress);
		} catch (Exception e) {
			LOG.error("数据库保存数据失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "服务代理创建失败");
		}
		LOG.info("服务代理:" + ingressProxy.getIngressName() + "创建成功");
		return true;
	}

	@Override
	@Transactional(rollbackFor = ErrorMessageException.class)
	public Boolean updateIngressProxy(IngressParameter ingressProxyParameter) {
		String serviceId = ingressProxyParameter.getServiceIngress().getServiceId();
		ServiceIngress serviceIngress = ingressProxyParameter.getServiceIngress();
		IngressProxy ingressProxy = ingressProxyParameter.getIngressProxy();
		Optional<IngressProxy> optional = ingressProxyRepository.findById(serviceIngress.getIngressProxyId());
		if (!optional.isPresent()) {
			throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "ingressProxyId不正确");
		}
		Optional<ServiceIngress> optionalServiceIngress = serviceIngressRepository.findById(serviceIngress.getId());
		if (!optionalServiceIngress.isPresent()) {
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "serviceIngressId不正确");
		}
		String tenantName = ingressProxyParameter.getTenantName();
		Optional<com.xxx.xcloud.module.application.entity.Service> serviceOptional = serviceRepository
				.findById(serviceId);
		com.xxx.xcloud.module.application.entity.Service service = null;
		if (serviceOptional.isPresent()) {
			service = serviceOptional.get();
		} else {
			LOG.info("服务不存在");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
		}
		Optional<IngressDomain> ingressDomainOptional = ingressDomainRepository
				.findById(serviceIngress.getIngressDomainId());
		IngressDomain ingressDomain = null;
		if (!ingressDomainOptional.isPresent()) {
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "域名不存在,ingressDomainId不正确");
		} else {
			ingressDomain = ingressDomainOptional.get();
		}
		ingressProxy.setIngressName(ingressDomain.getIngressDomainName() + "." + service.getServiceName());
		// 获取访问路径和端口
		Map<String, String> pathAndPort = JSON.parseObject(serviceIngress.getPathAndPort(),
				new TypeReference<Map<String, String>>() {
				});
		Ingress ingress = null;
		try {
			ingress = generateIngressLocation(tenantName, pathAndPort, ingressProxy,
					ingressDomain.getIngressDomainName(), service.getServiceName());
		} catch (Exception e) {
			LOG.error("封装Ingress资源对象失败!" + " error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
					"服务代理:" + ingressProxy.getIngressName() + "修改失败");
		}
		IngressApi.createOrReplaceIngress(ingress, tenantName);
		// CA认证
		ApproveConfig approveConfig = ingressProxy.getApproveConfig();
		if (null != approveConfig) {
			if (null != approveConfig.getAuthTlsSecret() && null != approveConfig.getAuthType()) {
				ingressProxy.setAuthTlsSecretName(ingressProxy.getIngressName() + ".ca.secret");
				createCaSecret(tenantName, approveConfig, ingressProxy.getAuthTlsSecretName());
			}
		}
		// 修改数据库表数据
		try {
			ingressProxyRepository.save(ingressProxy);
			serviceIngressRepository.save(serviceIngress);
		} catch (Exception e) {
			LOG.error("服务代理数据库表数据修改失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "服务代理数据库表数据修改失败");
		}
		LOG.info("服务代理:" + ingressProxy.getIngressName() + "修改成功");
		return true;
	}

	@Override
	@Transactional(rollbackFor = ErrorMessageException.class)
	public Boolean deleteIngressProxy(String serviceId, String tenantName) {
		ServiceIngress serviceIngress = serviceIngressRepository.findByServiceId(serviceId);
		if (null == serviceIngress) {
			LOG.info("服务的http代理配置不存在");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务的http代理配置不存在");
		}
		IngressProxy ingressProxy = null;
		Optional<IngressProxy> ingressProxyOptional = ingressProxyRepository
				.findById(serviceIngress.getIngressProxyId());
		if (ingressProxyOptional.isPresent()) {
			ingressProxy = ingressProxyOptional.get();
		} else {
			LOG.info("http代理配置不存在");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "http代理配置不存在");
		}
		// 删除代理的Ingress资源
		IngressApi.deleteIngress(ingressProxy.getIngressName(), tenantName);
		// 删除CA认证的secret
		if (null != ingressProxy.getAuthTlsSecretName()) {
			Boolean deleteSecret = true;
			try {
				deleteSecret = KubernetesClientFactory.getClient().secrets().inNamespace(tenantName)
						.withName(ingressProxy.getAuthTlsSecretName()).cascading(true).delete();
				if (deleteSecret) {
					LOG.info("Secret:" + ingressProxy.getAuthTlsSecretName() + "成功失败");
				}
			} catch (Exception e) {
				LOG.info("Secret:" + ingressProxy.getAuthTlsSecretName() + "删除失败");
			}
			if (!deleteSecret) {
				LOG.info("Secret:" + ingressProxy.getAuthTlsSecretName() + "删除失败");
			}
		}
		// 删除数据库表中的数据
		try {
			ingressProxyRepository.delete(ingressProxy);
			serviceIngressRepository.delete(serviceIngress);
		} catch (Exception e) {
			LOG.error("服务代理数据库表信息删除失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "服务代理数据库表信息删除失败");
		}
		LOG.info("服务代理删除成功");
		return true;
	}

	/**
	 * Description:封装ingress资源的location参数
	 * 
	 * @param tenantName
	 *            租户名称
	 * @param serviceIngress
	 *            服务和代理关联的信息
	 * @param pathAndPort
	 *            访问路径和服务的端口:Map<visitPath, servicePort>
	 * @param domain
	 *            域名
	 * @param serviceName
	 *            服务名称
	 * @return Ingress
	 */
	private Ingress generateIngressLocation(String tenantName, Map<String, String> pathAndPort,
			IngressProxy ingressProxy, String domain, String serviceName) {
		Ingress ingress = new Ingress();
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName(ingressProxy.getIngressName());
		objectMeta.setNamespace(tenantName);
		// 使用高级参数
		if (Global.DOMAIN_USE_CONFIG == ingressProxy.getConfigStatus()) {
			Map<String, String> annotations = new HashMap<String, String>(16);
			/*
			 * TLS相关
			 */
            setTlsConfigInfo(ingressProxy, annotations);

			/**
			 * 认证相关
			 */
            setApproveConfigInfo(ingressProxy, annotations);
			
			/*
			 * URL相关
			 */
            setUrlConfigInfo(ingressProxy, annotations);

			/*
			 * 跨资源共享相关
			 */
            setResourceConfigInfo(ingressProxy, annotations);
			
			/*
			 * nginx.conf中的一些配置
			 */
            setNginxInfo(ingressProxy, annotations);

			/*
			 * 其它配置
			 */
            setOtherInfo(ingressProxy, annotations);
			
			objectMeta.setAnnotations(annotations);
		}
		/*
		 * 灰度发布
		 */
        setGrayscaleRelease(ingressProxy, objectMeta);

        setIngressInfo(domain, pathAndPort, serviceName, objectMeta, ingress);
		return ingress;
	}

    /**
     * set
     * 
     * @param domain
     * @param pathAndPort
     * @param serviceName
     * @param objectMeta
     * @param ingress
     *            void
     * @date: 2019年11月15日 上午10:50:25
     */
    private void setIngressInfo(String domain, Map<String, String> pathAndPort, String serviceName,
            ObjectMeta objectMeta, Ingress ingress) {
        IngressSpec ingressSpec = new IngressSpec();
        List<IngressRule> rules = new ArrayList<IngressRule>();
        IngressRule ingressRule = new IngressRule();
        ingressRule.setHost(domain);
        List<HTTPIngressPath> paths = new ArrayList<HTTPIngressPath>();
        HTTPIngressRuleValue httpIngressRuleValue = new HTTPIngressRuleValue();
        if (null != pathAndPort) {
            for (Map.Entry item : pathAndPort.entrySet()) {
                HTTPIngressPath httpIngressPath = new HTTPIngressPath();
                IngressBackend ingressBackend = new IngressBackend();
                ingressBackend.setServiceName(serviceName);

                IntOrString servicePort = new IntOrString(Integer.valueOf(item.getValue().toString()));
                ingressBackend.setServicePort(servicePort);
                httpIngressPath.setPath(item.getKey().toString());
                httpIngressPath.setBackend(ingressBackend);
                paths.add(httpIngressPath);

                httpIngressRuleValue.setPaths(paths);
            }
            rules.add(ingressRule);
            ingressRule.setHttp(httpIngressRuleValue);
        }
        ingressSpec.setRules(rules);
        ingress.setSpec(ingressSpec);
        ingress.setMetadata(objectMeta);
    }

    /**
     * 设置灰度发布信息
     * 
     * @param ingressProxy
     * @param objectMeta
     *            void
     * @date: 2019年11月15日 上午10:47:05
     */
    private void setGrayscaleRelease(IngressProxy ingressProxy, ObjectMeta objectMeta) {
	    if (null != ingressProxy.getCanaryStatus() && Global.DOMAIN_USE_CANARY == ingressProxy.getCanaryStatus()) {
            Map<String, String> annotations = objectMeta.getAnnotations();
            if(annotations == null){
                annotations = new HashMap<String, String>(16);  
            }
            annotations.put(IngressAnnotations.INGRESS_LOCATION_CANARY, "true");
            CanaryConfig canaryConfig = ingressProxy.getCanaryConfig();
            if(null != canaryConfig) {
                String canaryWeight = canaryConfig.getCanaryWeight();
                if(StringUtils.isNotEmpty(canaryWeight)) {
                    annotations.put(IngressAnnotations.INGRESS_LOCATION_CANARY_WEIGHT, canaryWeight);
                }
                String headerKey = canaryConfig.getHeaderKey();
                if (StringUtils.isNotEmpty(headerKey)) {
                    annotations.put(IngressAnnotations.INGRESS_LOCATION_CANARY_BY_HEADER, headerKey);
                }
                String headerValue = canaryConfig.getHeaderValue();
                if (StringUtils.isNotEmpty(headerValue)) {
                    annotations.put(IngressAnnotations.INGRESS_LOCATION_CANARY_BY_HEADER_VALUE, headerValue);
                }
                String cookie = canaryConfig.getCookie();
                if (StringUtils.isNotEmpty(cookie)) {
                    annotations.put(IngressAnnotations.INGRESS_LOCATION_CANARY_BY_COOKIE, cookie);
                }
            }
            objectMeta.setAnnotations(annotations);
        }
	}

    /**
     * set
     * 
     * @param ingressProxy
     * @param annotations
     *            void
     * @date: 2019年11月15日 上午10:34:45
     */
    private void setTlsConfigInfo(IngressProxy ingressProxy, Map<String, String> annotations) {
        TlsConfig tlsConfig = ingressProxy.getTlsConfig();
        if (null != tlsConfig) {
            if (null != tlsConfig.getSslPassThrough()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_SSL_PASSTHROUGH,
                        tlsConfig.getSslPassThrough().toString());
            }
            if (null != tlsConfig.getSslRedirect()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATIONI_SSL_REDIRECT,
                        tlsConfig.getSslRedirect().toString());
            }
            if (null != tlsConfig.getForceSslRedirect()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_FORCE_SSL_REDIRECT,
                        tlsConfig.getForceSslRedirect().toString());
            }
            if (null != tlsConfig.getSecureBackends()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_SECURE_BACKENDS,
                        tlsConfig.getSecureBackends().toString());
            }
        }
    }

    /**
     * set
     * 
     * @param ingressProxy
     * @param annotations
     *            void
     * @date: 2019年11月15日 上午10:35:48
     */
    private void setApproveConfigInfo(IngressProxy ingressProxy, Map<String, String> annotations) {
        ApproveConfig approveConfig = ingressProxy.getApproveConfig();
        if (null != approveConfig) {
            // CA认证
            if (StringUtils.isNotEmpty(approveConfig.getAuthType())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_TYPE, approveConfig.getAuthType());
            }
            if (null != approveConfig.getAuthTlsSecret() && !"".equals(approveConfig.getAuthTlsSecret())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_TLS_SECRET,
                        ingressProxy.getAuthTlsSecretName());
            }
            // 用户名密码认证
            /*
             * if (null != approveConfig.getAuthSecret() &&
             * !"".equals(approveConfig.getAuthSecret())) {
             * annotations.put("nginx.ingress.kubernetes.io/auth-secret",
             * approveConfig.getAuthSecret()); }
             */
            if (StringUtils.isNotEmpty(approveConfig.getAuthRealm())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_REALM, approveConfig.getAuthRealm());
            }
            if (null != approveConfig.getAuthTlsVerifyDepth()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_TLS_VERIFY_DEPTH,
                        approveConfig.getAuthTlsVerifyDepth().toString());
            }
            if (StringUtils.isNotEmpty(approveConfig.getAuthTlsVerifyClient())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_TLS_VERIFY_CLIENT,
                        approveConfig.getAuthTlsVerifyClient());
            }
            if (StringUtils.isNotEmpty(approveConfig.getAuthTlsErrorPage())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_TLS_ERROR_PAGE,
                        approveConfig.getAuthTlsErrorPage());
            }
            if (null != approveConfig.getAuthTlsPassCertificateToUpstream()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_TLS_PASS_CERTIFICATE_TO_UPSTREAM,
                        approveConfig.getAuthTlsPassCertificateToUpstream().toString());
            }
            if (StringUtils.isNotEmpty(approveConfig.getWhitelistSourceRange())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_WHITELIST_SOURCE_RANGE,
                        approveConfig.getWhitelistSourceRange());
            }
        }
    }

    /**
     * set
     * 
     * @param ingressProxy
     * @param annotations
     *            void
     * @date: 2019年11月15日 上午10:37:22
     */
    private void setUrlConfigInfo(IngressProxy ingressProxy, Map<String, String> annotations) {
        UrlConfig urlConfig = ingressProxy.getUrlConfig();
        if (null != urlConfig) {
            if (StringUtils.isNotEmpty(urlConfig.getAppRoot())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_APP_ROOT, urlConfig.getAppRoot());
            }
            if (StringUtils.isNotEmpty(urlConfig.getRewriteTarget())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_REWRITE_TARGET, urlConfig.getRewriteTarget());
            }
        }
    }

    /**
     * set
     * 
     * @param ingressProxy
     * @param annotations
     * @date: 2019年11月15日 上午10:38:20
     */
    private void setResourceConfigInfo(IngressProxy ingressProxy, Map<String, String> annotations) {
        ResourceConfig resoruceConfig = ingressProxy.getResoruceConfig();
        if (null != resoruceConfig) {
            if (null != resoruceConfig.getEnableCors()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_ENABLE_CORS,
                        resoruceConfig.getEnableCors().toString());
            }
            if (StringUtils.isNotEmpty(resoruceConfig.getCorsAllowOrigin())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CORS_ALLOW_ORIGIN,
                        resoruceConfig.getCorsAllowOrigin());
            }
            if (StringUtils.isNotEmpty(resoruceConfig.getCorsAllowHeaders())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CORS_ALLOW_HEADERS,
                        resoruceConfig.getCorsAllowHeaders());
            }
            if (StringUtils.isNotEmpty(resoruceConfig.getCorsAllowMethods())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CORS_ALLOW_METHODS,
                        resoruceConfig.getCorsAllowMethods());
            }
            if (null != resoruceConfig.getCorsAllowCredentials()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CORS_ALLOW_CREDENTIALS,
                        resoruceConfig.getCorsAllowCredentials().toString());
            }
            if (null != resoruceConfig.getCorsMaxAge()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CORS_MAX_AGE,
                        resoruceConfig.getCorsMaxAge().toString());
            }
        }
    }

    /**
     * 设置nginx信息
     * 
     * @param ingressProxy
     * @param annotations
     *            void
     * @date: 2019年11月15日 上午10:39:06
     */
    private void setNginxInfo(IngressProxy ingressProxy, Map<String, String> annotations) {
        NginxConfig nginxConfig = ingressProxy.getNginxConfig();
        if (null != nginxConfig) {
            if (StringUtils.isNotEmpty(nginxConfig.getConfigurationSnippet())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CONFIGURATION_SNIPPET,
                        nginxConfig.getConfigurationSnippet());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getServerSnippet())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_SERVER_SNIPPET, nginxConfig.getServerSnippet());
            }
            if (null != nginxConfig.getProxyConnectTimeout()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_CONNECT_TIMEOUT,
                        nginxConfig.getProxyConnectTimeout().toString());
            }
            if (null != nginxConfig.getProxySendTimeout()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_SEND_TIMEOUT,
                        nginxConfig.getProxySendTimeout().toString());
            }
            if (null != nginxConfig.getProxyReadTimeout()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_READ_TIMEOUT,
                        nginxConfig.getProxyReadTimeout().toString());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getProxyNextUpstream())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_NEXT_UPSTREAM,
                        nginxConfig.getProxyNextUpstream());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getProxyRequestBuffering())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_REQUEST_BUFFERING,
                        nginxConfig.getProxyRequestBuffering());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getProxyRedirectFrom())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_REDIRECT_FROM,
                        nginxConfig.getProxyRedirectFrom());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getProxyRedirectTo())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_REDIRECT_TO,
                        nginxConfig.getProxyRedirectTo());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getProxyBodySize())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_BODY_SIZE, nginxConfig.getProxyBodySize());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getProxyBufferSize())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_PROXY_BUFFER_SIZE,
                        nginxConfig.getProxyBufferSize());
            }
            if (StringUtils.isNotEmpty(nginxConfig.getClientBodyBufferSize())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_CLIENT_BODY_BUFFER_SIZE,
                        nginxConfig.getClientBodyBufferSize());
            }
        }
    }

    /**
     * 设置其他信息
     * 
     * @param ingressProxy
     * @param annotations
     * @date: 2019年11月15日 上午10:40:04
     */
    private void setOtherInfo(IngressProxy ingressProxy, Map<String, String> annotations) {
        OtherConfig otherConfig = ingressProxy.getOtherConfig();
        if (null != otherConfig) {
            if (null != otherConfig.getLimitConnections()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_LIMIT_CONNECTIONS,
                        otherConfig.getLimitConnections().toString());
            }
            if (null != otherConfig.getLimitConnections()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_LIMIT_RPS, otherConfig.getLimitRps().toString());
            }
            if (null != otherConfig.getLimitConnections()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_LIMIT_RPM, otherConfig.getLimitRpm().toString());
            }
            if (StringUtils.isNotEmpty(otherConfig.getAffinity())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AFFINITY, otherConfig.getAffinity());
            }
            if (StringUtils.isNotEmpty(otherConfig.getSessionCookieName())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_SESSION_COOKIE_NAME,
                        otherConfig.getSessionCookieName());
            }
            if (StringUtils.isNotEmpty(otherConfig.getSessionCookieHash())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_SESSION_COOKIE_HASH,
                        otherConfig.getSessionCookieHash());
            }
            if (null != otherConfig.getUpstreamMaxFails()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_UPSTREAM_MAX_FAILS,
                        otherConfig.getUpstreamMaxFails().toString());
            }
            if (null != otherConfig.getUpstreamFailTimeout()) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_UPSTREAM_FAIL_TIMEOUT,
                        otherConfig.getUpstreamFailTimeout().toString());
            }
            if (StringUtils.isNotEmpty(otherConfig.getUpstreamHashBy())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_UPSTREAM_HASH_BY, otherConfig.getUpstreamHashBy());
            }
            if (StringUtils.isNotEmpty(otherConfig.getDefaultBackend())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_DEFAULT_BACKEND, otherConfig.getDefaultBackend());
            }
            if (StringUtils.isNotEmpty(otherConfig.getAuthUrl())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_URL, otherConfig.getAuthUrl());
            }
            if (StringUtils.isNotEmpty(otherConfig.getAuthMethod())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_AUTH_METHOD, otherConfig.getAuthMethod());
            }
            if (StringUtils.isNotEmpty(otherConfig.getFromToWwwRedirect())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_FORM_TO_WWW_REDIRECT,
                        otherConfig.getFromToWwwRedirect());
            }
            if (StringUtils.isNotEmpty(otherConfig.getServiceUpstream())) {
                annotations.put(IngressAnnotations.INGRESS_LOCATION_SERVICE_UPSTREAM, otherConfig.getServiceUpstream());
            }
        }
    }

	/**
	 * Description:创建CA证书的Secret
	 * 
	 * @param tenantName
	 *            租户名称
	 * @param approveConfig
	 *            创建CA证书数据
	 * @param authTlsSecretName
	 *            CA证书名称 void
	 */
	private void createCaSecret(String tenantName, ApproveConfig approveConfig, String authTlsSecretName) {
		try {
			// 创建secret
			Secret authTlsSecret = new Secret();
			ObjectMeta metadata = new ObjectMeta();
			metadata.setName(authTlsSecretName);
			metadata.setNamespace(tenantName);
			authTlsSecret.setMetadata(metadata);
			Map<String, String> data = new HashMap<>(16);
			// base64加密
			String authTlsSecretBASE64 = Base64Util.encrypt(approveConfig.getAuthTlsSecret());
			data.put("ca.crt", authTlsSecretBASE64);
			authTlsSecret.setData(data);
			authTlsSecret.setType("opaque");
			Secret secret = KubernetesClientFactory.getClient().secrets().inNamespace(tenantName)
					.withName(authTlsSecretName).createOrReplace(authTlsSecret);
			if (null == secret) {
				LOG.info("Secret:" + authTlsSecretName + "创建失败");
			}
			LOG.info("Secret:" + authTlsSecretName + "创建成功");
		} catch (Exception e) {
			LOG.error("Secret:" + authTlsSecretName + "创建失败! error msg:{}", e);
		}
	}
}
