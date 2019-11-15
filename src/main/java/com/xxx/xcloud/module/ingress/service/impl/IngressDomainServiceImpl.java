package com.xxx.xcloud.module.ingress.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ingress.apiutils.IngressApi;
import com.xxx.xcloud.module.ingress.consts.IngressAnnotations;
import com.xxx.xcloud.module.ingress.entity.IngressDomain;
import com.xxx.xcloud.module.ingress.entity.ServiceIngress;
import com.xxx.xcloud.module.ingress.repository.IngressDomainRepository;
import com.xxx.xcloud.module.ingress.repository.ServiceIngressRepository;
import com.xxx.xcloud.module.ingress.service.IngressDomainService;
import com.xxx.xcloud.module.ingress.service.IngressProxyService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.repository.TenantRepository;
import com.xxx.xcloud.utils.Base64Util;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;

/**
 * Description:域名接口实现类
 *
 * @author LYJ </br>
 *         create time：2018年12月5日 下午2:54:44 </br>
 * @version 1.0
 * @since
 */
@Service
public class IngressDomainServiceImpl implements IngressDomainService {

	private static final Logger LOG = LoggerFactory.getLogger(IngressDomainServiceImpl.class);

	@Autowired
	private IngressDomainRepository ingressDomainRepository;

	@Autowired
	private ServiceIngressRepository serviceIngressRepository;

	@Autowired
	private IngressProxyService ingressProxyService;

	@Autowired
	private TenantRepository tenantRepository;

    public static final int SAVE_FLAG_SAVE = 1;
    public static final int SAVE_FLAG_QUERY = 2;

	@Override
	public Page<IngressDomain> ingressDomain(String tenantName, String type, Pageable pageable) {
		Page<IngressDomain> ingressDomains = null;
		try {
			if (null == tenantName) {
				tenantName = "admin";
			}
			ingressDomains = ingressDomainRepository.findByTenantNameAndType(tenantName, type, pageable);
		} catch (Exception e) {
			LOG.error("查询失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询失败");
		}
		return ingressDomains;
	}

	@Override
	public IngressDomain createIngressDomain(IngressDomain ingressDomain) {
		ingressDomain.setCreateTime(new Date());
		ingressDomain.setUpdateTime(new Date());
		if (Global.DOMAIN_TLD.equals(ingressDomain.getType())) {
			ingressDomain.setTenantName("admin");
			List<IngressDomain> domain = ingressDomainRepository.findByDomain(ingressDomain.getDomain());
			if (domain.size() > 0) {
				LOG.info("一级域名:" + ingressDomain.getDomain() + "已存在");
				throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST,
						"一级域名:" + ingressDomain.getDomain() + "已存在");
			}
			saveIngressDomain(ingressDomain);
		} else if (Global.DOMAIN_SLD.equals(ingressDomain.getType())) {
			ingressDomain = saveIngressDomain(ingressDomain);
            if (SAVE_FLAG_QUERY == ingressDomain.getSaveFlag()) {
				return ingressDomain;
			}
			// 创建真正使用的域名(泛域名.一级域名)
			if (null == ingressDomain.getTenantName()) {
				LOG.info("租户名不能为空");
				throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "租户名不能为空");
			}
			// 泛域名为项目编码
			if (null == ingressDomain.getProjectCode()) {
				LOG.info("项目编码不能为空");
				throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "项目编码不能为空");
			}
			List<IngressDomain> domain = new ArrayList<IngressDomain>();
			domain = ingressDomainRepository.findByDomainAndType(ingressDomain.getDomain(), Global.DOMAIN_TLD);
			if (domain.isEmpty()) {
				throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
						"域名" + ingressDomain.getDomain() + "不存在");
			}
			if (Global.DOMAIN_USE_HTTPS != ingressDomain.getHttpsStatus()
					&& Global.DOMAIN_NOT_USE_HTTPS != ingressDomain.getHttpsStatus()) {
				LOG.info("httpsStatus参数值只能为" + Global.DOMAIN_USE_HTTPS + "或" + Global.DOMAIN_NOT_USE_HTTPS);
				throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
						"httpsStatus参数值只能为" + Global.DOMAIN_USE_HTTPS + "或" + Global.DOMAIN_NOT_USE_HTTPS);
			}
			if (Global.DOMAIN_USE_HTTPS == ingressDomain.getHttpsStatus()) {
				ingressDomain.setHttpsSecretName(ingressDomain.getIngressDomainName() + "https");
				try {
					Secret httpsSecret = createOrUpdateHttps(ingressDomain);
					if (null == httpsSecret) {
						LOG.info("Secret:" + ingressDomain.getHttpsSecretName() + "创建失败");
						throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_SECRET_FAILED,
								"Secret:" + ingressDomain.getHttpsSecretName() + "创建失败");
					}
					LOG.info("Secret:" + ingressDomain.getHttpsSecretName() + "创建成功");
				} catch (Exception e) {
					LOG.error("Secret:" + ingressDomain.getHttpsSecretName() + "创建失败! error msg:{}", e);
					throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_SECRET_FAILED,
							"Secret:" + ingressDomain.getHttpsSecretName() + "创建失败");
				}
			}
			Ingress ingress = null;
			try {
				ingress = generateIngressServer(ingressDomain);
			} catch (Exception e) {
				LOG.error("二级域名:" + ingressDomain.getIngressDomainName() + "创建失败! error msg:{}", e);
				ingressDomainRepository.delete(ingressDomain);
				throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
						"二级域名:" + ingressDomain.getIngressDomainName() + "创建失败");
			}
			try {
				IngressApi.createOrReplaceIngress(ingress, ingressDomain.getTenantName());
				LOG.info("二级域名:" + ingressDomain.getIngressDomainName() + "创建成功");
			} catch (Exception e) {
				LOG.error("二级域名:" + ingressDomain.getIngressDomainName() + "创建失败! error msg:{}", e);
				throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
						"二级域名:" + ingressDomain.getIngressDomainName() + "创建失败");
			}
		}
		LOG.info("域名创建成功");
		return ingressDomain;
	}

	private synchronized IngressDomain saveIngressDomain(IngressDomain ingressDomain) {
		try {
			IngressDomain ingDomain = ingressDomainRepository.findByProjectCodeAndDomain(ingressDomain.getProjectCode(),
					ingressDomain.getDomain());
			if (ingDomain == null) {
				ingressDomain = ingressDomainRepository.save(ingressDomain);
			} else {
				ingDomain.setSaveFlag(2);
				return ingDomain;
			}
		} catch (Exception e) {
			LOG.info("域名保存失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
					"域名:" + ingressDomain.getIngressDomainName() + "保存失败!");
		}
		return ingressDomain;
	}

	@Override
	public Boolean deleteIngressDomain(String domainId, String tenantName) {
		Optional<IngressDomain> ingressDomainOptional = ingressDomainRepository.findById(domainId);
		if (!ingressDomainOptional.isPresent()) {
			LOG.info("域名不存在");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "域名不存在");
		} else {
			IngressDomain ingressDomain = ingressDomainOptional.get();
			// 管理员操作
			if (null == tenantName) {
				List<IngressDomain> ingressDomainList = ingressDomainRepository
						.findByDomainAndType(ingressDomain.getDomain(), Global.DOMAIN_SLD);
				// 一级域名下创建了二级域名
				if (ingressDomainList.size() > 0) {
					for (IngressDomain ingressDomainSLD : ingressDomainList) {
						List<ServiceIngress> serviceIngressList = serviceIngressRepository
								.findByIngressDomainId(ingressDomainSLD.getId());
						if (serviceIngressList.size() > 0) {
							for (ServiceIngress serviceIngress : serviceIngressList) {
								// 删除服务代理
								Boolean deleteIngressProxy = ingressProxyService.deleteIngressProxy(
										serviceIngress.getServiceId(), ingressDomainSLD.getTenantName());
								if (!deleteIngressProxy) {
									LOG.info("域名下的服务代理删除失败");
									throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "域名下的服务代理删除失败");
								}
							}
						}
						// 删除二级域名
						deleteIngressDomainSLD(ingressDomainSLD.getTenantName(), ingressDomainSLD);
					}
				}
				try {
					ingressDomainRepository.delete(ingressDomain);
					LOG.info("域名:" + ingressDomain.getDomain() + "删除成功");
				} catch (Exception e) {
					LOG.error("域名:" + ingressDomain.getDomain() + "删除失败! error msg:{}", e);
					throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
							"域名:" + ingressDomain.getDomain() + "删除失败");
				}
			} else { // 普通租户操作
				// 删除二级域名
				deleteIngressDomainSLD(tenantName, ingressDomain);
			}
		}
		return true;
	}

	@Override
	public IngressDomain updateIngressDomain(IngressDomain ingressDomain) {
		ingressDomain.setUpdateTime(new Date());
		if (Global.DOMAIN_TLD.equals(ingressDomain.getType())) {
			LOG.info("一级域名:" + ingressDomain.getDomain() + "不能执行修改操作");
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE,
					"一级域名:" + ingressDomain.getDomain() + "不能执行修改操作");
		} else if (Global.DOMAIN_SLD.equals(ingressDomain.getType())) {
			if (Global.DOMAIN_USE_HTTPS == ingressDomain.getHttpsStatus()) {
				ingressDomain.setHttpsSecretName(ingressDomain.getIngressDomainName() + "https");
				try {
					Secret httpsSecret = createOrUpdateHttps(ingressDomain);
					if (null == httpsSecret) {
						LOG.info("Secret:" + ingressDomain.getHttpsSecretName() + "创建失败");
						throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_SECRET_FAILED,
								"Secret:" + ingressDomain.getHttpsSecretName() + "创建失败");
					}
					LOG.info("Secret:" + ingressDomain.getHttpsSecretName() + "创建成功");
				} catch (Exception e) {
					LOG.error("Secret:" + ingressDomain.getHttpsSecretName() + "创建失败! error msg:{}", e);
					throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_SECRET_FAILED,
							"Secret:" + ingressDomain.getHttpsSecretName() + "创建失败");
				}
			}
			Ingress k8sIngress = null;
			try {
				Ingress ingress = generateIngressServer(ingressDomain);
				k8sIngress = KubernetesClientFactory.getClient().extensions().ingresses()
						.inNamespace(ingressDomain.getTenantName()).createOrReplace(ingress);
			} catch (Exception e) {
				LOG.error("二级域名:" + ingressDomain.getIngressDomainName() + "修改失败! error msg:{}", e);
				throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
						"二级域名:" + ingressDomain.getIngressDomainName() + "修改失败");
			}
			if (null == k8sIngress) {
				LOG.info("二级域名:" + ingressDomain.getIngressDomainName() + "修改失败");
				throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
						"二级域名:" + ingressDomain.getIngressDomainName() + "修改失败");
			}
			LOG.info("二级域名:" + ingressDomain.getIngressDomainName() + "修改成功");
		}
		try {
			ingressDomain = ingressDomainRepository.save(ingressDomain);
		} catch (Exception e) {
			LOG.error("域名修改失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "域名修改失败");
		}
		LOG.info("域名修改成功");
		return ingressDomain;
	}

	/**
	 * Description: 封装ingress资源的server参数
	 *
	 * @param ingressDomain
	 *            ingress资源数据
	 * @return Ingress
	 */
	private Ingress generateIngressServer(IngressDomain ingressDomain) {
		Ingress ingress = new Ingress();
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName(ingressDomain.getIngressDomainName());
		objectMeta.setNamespace(ingressDomain.getTenantName());
		if (Global.DOMAIN_USE_CONFIG != ingressDomain.getConfigStatus()
				&& Global.DOMAIN_NOT_USE_CONFIG != ingressDomain.getConfigStatus()) {
			LOG.info("configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
					"configStatus参数值只能为" + Global.DOMAIN_USE_CONFIG + "或" + Global.DOMAIN_NOT_USE_CONFIG);
		}
		if (Global.DOMAIN_USE_CONFIG == ingressDomain.getConfigStatus()) {
			// Server高级参数
			Map<String, String> annotations = new HashMap<String, String>(16);
			/*
			 * URL相关
			 */
			if (StringUtils.isNotEmpty(ingressDomain.getAddBaseUrl())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_ADD_BASE_URL, ingressDomain.getAddBaseUrl());
			}
			if (StringUtils.isNotEmpty(ingressDomain.getBaseUrlScheme())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_BASE_URL_SCHEME, ingressDomain.getBaseUrlScheme());
			}
			if (StringUtils.isNotEmpty(ingressDomain.getxForwardedPrefix())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_X_FORWARDED_PREFIX,
						ingressDomain.getxForwardedPrefix());
			}
			/*
			 * 其它配置
			 */
			if (StringUtils.isNotEmpty(ingressDomain.getProxyPassParams())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_PROXY_PASS_PARAMS,
						ingressDomain.getProxyPassParams());
			}
			if (StringUtils.isNotEmpty(ingressDomain.getServerAlias())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_SERVER_ALIAS, ingressDomain.getServerAlias());
			}
			if (StringUtils.isNotEmpty(ingressDomain.getLimitRate())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_LIMIT_RATE, ingressDomain.getLimitRate());
			}
			if (StringUtils.isNotEmpty(ingressDomain.getLimitRateAfter())) {
				annotations.put(IngressAnnotations.INGRESS_SERVER_LIMIT_RATE_AFTER, ingressDomain.getLimitRateAfter());
			}
			objectMeta.setAnnotations(annotations);
		}
		IngressSpec ingressSpec = new IngressSpec();
		List<IngressRule> rules = new ArrayList<IngressRule>();
		IngressRule ingressRule = new IngressRule();
		rules.add(ingressRule);
		ingressRule.setHost(ingressDomain.getIngressDomainName());
		ingressSpec.setRules(rules);
		if (Global.DOMAIN_USE_HTTPS == ingressDomain.getHttpsStatus()) {
			// 密钥tls
			List<IngressTLS> tls = new ArrayList<IngressTLS>();
			IngressTLS ingressTLS = new IngressTLS();

			ingressTLS.setSecretName(ingressDomain.getIngressDomainName() + ".https.secret");
			List<String> hosts = new ArrayList<String>();
			hosts.add(ingressDomain.getIngressDomainName());
			ingressTLS.setHosts(hosts);
			tls.add(ingressTLS);
			ingressSpec.setTls(tls);
		}
		ingress.setSpec(ingressSpec);
		ingress.setMetadata(objectMeta);
		return ingress;
	}

	/**
	 * Description: 创建Https
	 *
	 * @param ingressDomain
	 *            ingress资源数据 void
	 */
	private Secret createOrUpdateHttps(IngressDomain ingressDomain) {
		Secret secret = new Secret();
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName(ingressDomain.getHttpsSecretName());
		objectMeta.setNamespace(ingressDomain.getTenantName());
		secret.setMetadata(objectMeta);
		Map<String, String> data = new HashMap<>(2);
		String tlsKey = Base64Util.encrypt(ingressDomain.getHttpsTlsKey());
		String tlsCrt = Base64Util.encrypt(ingressDomain.getHttpsTlsCrt());
		data.put("tls.crt", tlsCrt);
		data.put("tls.key", tlsKey);
		secret.setData(data);
		secret.setType("kubernetes.io/tls");
		Secret k8sSecret = KubernetesClientFactory.getClient().secrets().inNamespace(ingressDomain.getTenantName())
				.createOrReplace(secret);
		return k8sSecret;
	}

	/**
	 * Description:删除二级域名
	 *
	 * @param tenantName
	 *            租户名称
	 * @param ingressDomain
	 *            域名信息 void
	 */
	private void deleteIngressDomainSLD(String tenantName, IngressDomain ingressDomain) {
		try {
			Tenant tenant = tenantRepository.findByTenantName(tenantName);
			if (null == tenant) {
				throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户" + tenantName + "不存在");
			}
		} catch (Exception e) {
			LOG.error("查询租户失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询租户失败");
		}
		try {
			IngressApi.deleteIngress(ingressDomain.getIngressDomainName(), tenantName);
		} catch (Exception e) {
			LOG.error("二级域名:" + ingressDomain.getIngressDomainName() + "删除失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_INGRESS_FAILED,
					"二级域名:" + ingressDomain.getIngressDomainName() + "删除失败");
		}
		try {
			KubernetesClientFactory.getClient().secrets().inNamespace(tenantName)
					.withName(ingressDomain.getHttpsSecretName()).cascading(true).delete();
			LOG.info("Secret:" + ingressDomain.getHttpsSecretName() + "删除成功");
		} catch (Exception e) {
			LOG.error("Secret:" + ingressDomain.getHttpsSecretName() + "删除失败! error msg:{}", e);
		}
		try {
			ingressDomainRepository.delete(ingressDomain);
			LOG.info("二级域名:" + ingressDomain.getIngressDomainName() + "删除成功");
		} catch (Exception e) {
			LOG.error("二级域名:" + ingressDomain.getIngressDomainName() + "删除失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
					"二级域名:" + ingressDomain.getIngressDomainName() + "删除失败");
		}
		LOG.info("二级域名:" + ingressDomain.getIngressDomainName() + "删除成功");
	}
}
