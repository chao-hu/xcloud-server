package com.xxx.xcloud.module.ingress.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;

import lombok.Data;

/**
 * Description: 服务代理相关参数
 * @author  LYJ </br>
 * create time：2018年12月4日 上午9:49:10 </br>
 * @version 1.0
 * @since
 */
@Entity
@Table(name="`BDOS_INGRESS_PROXY`")
@Data
public class IngressProxy implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GenericGenerator(name="uuidGenerator", strategy="uuid")
	@GeneratedValue(generator="uuidGenerator")
	private String id;
	
	/**
	 * 生成的ingress资源名称
	 */
	@Column(name = "`INGRESS_NAME`")
	private String ingressName;
	
	/**
	 * 是否使用高级参数 0:不使用,1:使用
	 */
	@Column(name = "`CONFIG_STATUS`", columnDefinition="tinyint default 0")
	private Byte configStatus = 0;
	
	/**
	 * TLS相关配置
	 */
	@Column(name = "`TLS_CONFIG`")
	private String tlsConfig;
	
	/**
	 * 认证相关配置
	 */
	@Column(name = "`APPROVE_CONFIG`", columnDefinition = "TEXT")
	private String approveConfig;
	
	/**
	 * CA认证的Secret名称
	 */
	@Column(name = "`AUTH_TLS_SECRET_NAME`")
	private String authTlsSecretName;
	
	/**
	 * URL相关配置
	 */
	@Column(name = "`URL_CONFIG`")
	private String urlConfig;
	
	/**
	 * 跨资源共享相关配置
	 */
	@Column(name = "`RESORUCE_CONFIG`")
	private String resoruceConfig;
	
	/**
	 * nginx相关配置
	 */
	@Column(name = "`NGINX_CONFIG`", columnDefinition = "TEXT")
	private String nginxConfig;

	/**
	 * 其它配置
	 */
	@Column(name = "`OTHER_CONFIG`")
	private String otherConfig;
	
	/**
	 * 是否使用灰度发布 0:不使用,1:使用
	 */
	@Column(name = "`CANANRY_STATUS`", columnDefinition="tinyint default 0")
	private Byte canaryStatus = 0;
	
	/**
	 * 灰度发布配置
	 */
	@Column(name = "`CANARY_CONFIG`")
	private String canaryConfig;

	public CanaryConfig getCanaryConfig() {
		CanaryConfig parseObject = new CanaryConfig();
		try {
			parseObject = JSON.parseObject(canaryConfig, CanaryConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[TlsConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	public TlsConfig getTlsConfig() {
		TlsConfig parseObject = new TlsConfig();
		try {
			parseObject = JSON.parseObject(tlsConfig, TlsConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[TlsConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	public ApproveConfig getApproveConfig() {
		ApproveConfig parseObject = new ApproveConfig();
		try {
			parseObject = JSON.parseObject(approveConfig, ApproveConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[ApproveConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	public UrlConfig getUrlConfig() {
		UrlConfig parseObject = new UrlConfig();
		try {
			parseObject = JSON.parseObject(urlConfig, UrlConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[UrlConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	public ResourceConfig getResoruceConfig() {
		ResourceConfig parseObject = new ResourceConfig();
		try {
			parseObject = JSON.parseObject(resoruceConfig, ResourceConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[ResourceConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	public NginxConfig getNginxConfig() {
		NginxConfig parseObject = new NginxConfig();
		try {
			parseObject = JSON.parseObject(nginxConfig, NginxConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[NginxConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	public OtherConfig getOtherConfig() {
		OtherConfig parseObject = new OtherConfig();
		try {
			parseObject = JSON.parseObject(otherConfig, OtherConfig.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "参数[OtherConfig:"+parseObject+"]不符合规范");
		}
		return parseObject;
	}

	@Override
	public String toString() {
		return "IngressProxy [id=" + id + ", ingressName=" + ingressName + ", configStatus=" + configStatus
				+ ", tlsConfig=" + tlsConfig + ", approveConfig=" + approveConfig + ", authTlsSecretName="
				+ authTlsSecretName + ", urlConfig=" + urlConfig + ", resoruceConfig=" + resoruceConfig
				+ ", nginxConfig=" + nginxConfig + ", otherConfig=" + otherConfig + "]";
	}


	public static class TlsConfig{
		
		private Boolean sslPassThrough;
		
		private Boolean forceSslRedirect;
		
		private Boolean sslRedirect;
		
		private Boolean secureBackends;
		
		public Boolean getSslPassThrough() {
			return sslPassThrough;
		}
		
		public void setSslPassthrough(Boolean sslPassThrough) {
			this.sslPassThrough = sslPassThrough;
		}
		
		public Boolean getForceSslRedirect() {
			return forceSslRedirect;
		}
		
		public void setForceSslRedirect(Boolean forceSslRedirect) {
			this.forceSslRedirect = forceSslRedirect;
		}
		
		public Boolean getSslRedirect() {
			return sslRedirect;
		}
		
		public void setSslRedirect(Boolean sslRedirect) {
			this.sslRedirect = sslRedirect;
		}
		
		public Boolean getSecureBackends() {
			return secureBackends;
		}
		
		public void setSecureBackends(Boolean secureBackends) {
			this.secureBackends = secureBackends;
		}
		
		@Override
		public String toString() {
			return "TlsConfig [sslPassThrough=" + sslPassThrough + ", forceSslRedirect=" + forceSslRedirect
					+ ", sslRedirect=" + sslRedirect + ", secureBackends=" + secureBackends + "]";
		}
		
	}
	
	public static class ApproveConfig{
		
		/**
		 * CA认证
		 */
		private String authType;
		
		private String authTlsSecret;
		
		private String authRealm;
		
		private Integer authTlsVerifyDepth;
		
		private String authTlsVerifyClient;
		
		private String authTlsErrorPage;
		
		private String whitelistSourceRange;
		
		
		private Boolean authTlsPassCertificateToUpstream;
		
		public String getAuthType() {
			return authType;
		}

		public void setAuthType(String authType) {
			this.authType = authType;
		}

		public String getAuthRealm() {
			return authRealm;
		}
		
		public void setAuthRealm(String authRealm) {
			this.authRealm = authRealm;
		}
		
		public Integer getAuthTlsVerifyDepth() {
			return authTlsVerifyDepth;
		}
		
		public void setAuthTlsVerifyDepth(Integer authTlsVerifyDepth) {
			this.authTlsVerifyDepth = authTlsVerifyDepth;
		}
		
		public String getAuthTlsVerifyClient() {
			return authTlsVerifyClient;
		}
		
		public void setAuthTlsVerifyClient(String authTlsVerifyClient) {
			this.authTlsVerifyClient = authTlsVerifyClient;
		}
		
		public String getAuthTlsErrorPage() {
			return authTlsErrorPage;
		}
		
		public void setAuthTlsErrorPage(String authTlsErrorPage) {
			this.authTlsErrorPage = authTlsErrorPage;
		}
		
		public String getWhitelistSourceRange() {
			return whitelistSourceRange;
		}
		
		public void setWhitelistSourceRange(String whitelistSourceRange) {
			this.whitelistSourceRange = whitelistSourceRange;
		}
		
		public String getAuthTlsSecret() {
			return authTlsSecret;
		}
		
		public void setAuthTlsSecret(String authTlsSecret) {
			this.authTlsSecret = authTlsSecret;
		}
		
		public Boolean getAuthTlsPassCertificateToUpstream() {
			return authTlsPassCertificateToUpstream;
		}
		
		public void setAuthTlsPassCertificateToUpstream(Boolean authTlsPassCertificateToUpstream) {
			this.authTlsPassCertificateToUpstream = authTlsPassCertificateToUpstream;
		}

		@Override
		public String toString() {
			return "ApproveConfig [authType=" + authType + ", authTlsSecret=" + authTlsSecret+ ", authRealm=" + authRealm + ", authTlsVerifyDepth=" + authTlsVerifyDepth
					+ ", authTlsVerifyClient=" + authTlsVerifyClient + ", authTlsErrorPage=" + authTlsErrorPage
					+ ", whitelistSourceRange=" + whitelistSourceRange + ", authTlsPassCertificateToUpstream="
					+ authTlsPassCertificateToUpstream + "]";
		}

	}
	
	public static class UrlConfig{
		private String appRoot;
		
		private String rewriteTarget;
		
		public String getAppRoot() {
			return appRoot;
		}
		
		public void setAppRoot(String appRoot) {
			this.appRoot = appRoot;
		}
		
		public String getRewriteTarget() {
			return rewriteTarget;
		}
		
		public void setRewriteTarget(String rewriteTarget) {
			this.rewriteTarget = rewriteTarget;
		}
		
		@Override
		public String toString() {
			return "UrlConfig [appRoot=" + appRoot + ", rewriteTarget=" + rewriteTarget + "]";
		}
		
	}
	
	public static class ResourceConfig{
		private String corsAllowOrigin;
		
		private String corsAllowHeaders;
		
		private String corsAllowMethods;
		
		private Integer corsMaxAge;
		
		private Boolean enableCors;
		
		private Boolean corsAllowCredentials;
		
		public String getCorsAllowOrigin() {
			return corsAllowOrigin;
		}
		
		public void setCorsAllowOrigin(String corsAllowOrigin) {
			this.corsAllowOrigin = corsAllowOrigin;
		}
		
		public String getCorsAllowHeaders() {
			return corsAllowHeaders;
		}
		
		public void setCorsAllowHeaders(String corsAllowHeaders) {
			this.corsAllowHeaders = corsAllowHeaders;
		}
		
		public String getCorsAllowMethods() {
			return corsAllowMethods;
		}
		
		public void setCorsAllowMethods(String corsAllowMethods) {
			this.corsAllowMethods = corsAllowMethods;
		}
		
		public Integer getCorsMaxAge() {
			return corsMaxAge;
		}
		
		public void setCorsMaaxAge(Integer corsMaxAge) {
			this.corsMaxAge = corsMaxAge;
		}
		
		public Boolean getEnableCors() {
			return enableCors;
		}
		
		public void setEnableCors(Boolean enableCors) {
			this.enableCors = enableCors;
		}
		
		public Boolean getCorsAllowCredentials() {
			return corsAllowCredentials;
		}
		
		public void setCorsAllowCredentials(Boolean corsAllowCredentials) {
			this.corsAllowCredentials = corsAllowCredentials;
		}
		
		@Override
		public String toString() {
			return "ResourceConfig [corsAllowOrigin=" + corsAllowOrigin + ", corsAllowHeaders=" + corsAllowHeaders
					+ ", corsAllowMethods=" + corsAllowMethods + ", corsMaxAge=" + corsMaxAge + ", enableCors="
					+ enableCors + ", corsAllowCredentials=" + corsAllowCredentials + "]";
		}
		
	}
	
	public static class NginxConfig{
		
		private String configurationSnippet;
		
		private String serverSnippet;
		
		private String proxyNextUpstream;
		
		private String proxyRequestBuffering;
		
		private String proxyRedirectFrom;
		
		private String proxyRedirectTo;
		
		private Integer proxyConnectTimeout;
		
		private Integer proxySendTimeout;
		
		private Integer proxyReadTimeout;
		
		private String proxyBodySize;
		
		private String proxyBufferSize;
		
		private String clientBodyBufferSize;
		
		public String getConfigurationSnippet() {
			return configurationSnippet;
		}
		
		public void setConfigurationSnippet(String configurationSnippet) {
			this.configurationSnippet = configurationSnippet;
		}
		
		public String getServerSnippet() {
			return serverSnippet;
		}
		
		public void setServerSnippet(String serverSnippet) {
			this.serverSnippet = serverSnippet;
		}
		
		public String getProxyNextUpstream() {
			return proxyNextUpstream;
		}
		
		public void setProxyNextUpstream(String proxyNextUpstream) {
			this.proxyNextUpstream = proxyNextUpstream;
		}
		
		public String getProxyRequestBuffering() {
			return proxyRequestBuffering;
		}
		
		public void setProxyRequestBuffering(String proxyRequestBuffering) {
			this.proxyRequestBuffering = proxyRequestBuffering;
		}
		
		public String getProxyRedirectFrom() {
			return proxyRedirectFrom;
		}
		
		public void setProxyRedirectFrom(String proxyRedirectFrom) {
			this.proxyRedirectFrom = proxyRedirectFrom;
		}
		
		public String getProxyRedirectTo() {
			return proxyRedirectTo;
		}
		
		public void setProxyRedirectTo(String proxyRedirectTo) {
			this.proxyRedirectTo = proxyRedirectTo;
		}
		
		public Integer getProxyConnectTimeout() {
			return proxyConnectTimeout;
		}
		
		public void setProxyConnectTimeout(Integer proxyConnectTimeout) {
			this.proxyConnectTimeout = proxyConnectTimeout;
		}
		
		public Integer getProxySendTimeout() {
			return proxySendTimeout;
		}
		
		public void setProxySendTimeout(Integer proxySendTimeout) {
			this.proxySendTimeout = proxySendTimeout;
		}
		
		public Integer getProxyReadTimeout() {
			return proxyReadTimeout;
		}
		
		public void setProxyReadTimeout(Integer proxyReadTimeout) {
			this.proxyReadTimeout = proxyReadTimeout;
		}
		
		public String getProxyBodySize() {
			return proxyBodySize;
		}
		
		public void setProxyBodySize(String proxyBodySize) {
			this.proxyBodySize = proxyBodySize;
		}
		
		public String getProxyBufferSize() {
			return proxyBufferSize;
		}
		
		public void setProxyBufferSize(String proxyBufferSize) {
			this.proxyBufferSize = proxyBufferSize;
		}
		
		public String getClientBodyBufferSize() {
			return clientBodyBufferSize;
		}
		
		public void setClientBodyBufferSize(String clientBodyBufferSize) {
			this.clientBodyBufferSize = clientBodyBufferSize;
		}
		
		@Override
		public String toString() {
			return "NginxConfig [configurationSnippet=" + configurationSnippet + ", serverSnippet=" + serverSnippet
					+ ", proxyNextUpstream=" + proxyNextUpstream + ", proxyRequestBuffering=" + proxyRequestBuffering
					+ ", proxyRedirectFrom=" + proxyRedirectFrom + ", proxyRedirectTo=" + proxyRedirectTo
					+ ", proxyConnectTimeout=" + proxyConnectTimeout + ", proxySendTimeout=" + proxySendTimeout
					+ ", proxyReadTimeout=" + proxyReadTimeout + ", proxyBodySize=" + proxyBodySize
					+ ", proxyBufferSize=" + proxyBufferSize + ", clientBodyBufferSize=" + clientBodyBufferSize + "]";
		}
		
	}
	
	public static class OtherConfig{
		
		private String affinity;
		
		private String sessionCookieName;
		
		private String sessionCookieHash;
		
		private String upstreamHashBy;
		
		private String defaultBackend;
		
		private String authUrl;
		
		private Integer limitConnections;
		
		private Integer limitRps;
		
		private Integer limitRpm;
		
		private Integer upstreamMaxFails;
		
		private Integer upstreamFailTimeout;
		
		/**
		 * GET,POST
		 */
		private String authMethod;
		
		private String fromToWwwRedirect;
		
		private String serviceUpstream;
		
		public String getAffinity() {
			return affinity;
		}
		
		public void setAffinity(String affinity) {
			this.affinity = affinity;
		}
		
		public String getSessionCookieName() {
			return sessionCookieName;
		}
		
		public void setSessionCookieName(String sessionCookieName) {
			this.sessionCookieName = sessionCookieName;
		}
		
		public String getSessionCookieHash() {
			return sessionCookieHash;
		}
		
		public void setSessionCookieHash(String sessionCookieHash) {
			this.sessionCookieHash = sessionCookieHash;
		}
		
		public String getUpstreamHashBy() {
			return upstreamHashBy;
		}
		
		public void setUpstreamHashBy(String upstreamHashBy) {
			this.upstreamHashBy = upstreamHashBy;
		}
		
		public String getDefaultBackend() {
			return defaultBackend;
		}
		
		public void setDefaultBackend(String defaultBackend) {
			this.defaultBackend = defaultBackend;
		}
		
		public String getAuthUrl() {
			return authUrl;
		}
		
		public void setAuthUrl(String authUrl) {
			this.authUrl = authUrl;
		}
		
		public Integer getLimitConnections() {
			return limitConnections;
		}
		
		public void setLimitConnections(Integer limitConnections) {
			this.limitConnections = limitConnections;
		}
		
		public Integer getLimitRps() {
			return limitRps;
		}
		
		public void setLimitRps(Integer limitRps) {
			this.limitRps = limitRps;
		}
		
		public Integer getLimitRpm() {
			return limitRpm;
		}
		
		public void setLimitRpm(Integer limitRpm) {
			this.limitRpm = limitRpm;
		}
		
		public Integer getUpstreamMaxFails() {
			return upstreamMaxFails;
		}
		
		public void setUpstreamMaxFails(Integer upstreamMaxFails) {
			this.upstreamMaxFails = upstreamMaxFails;
		}
		
		public Integer getUpstreamFailTimeout() {
			return upstreamFailTimeout;
		}
		
		public void setUpstreamFailTimeout(Integer upstreamFailTimeout) {
			this.upstreamFailTimeout = upstreamFailTimeout;
		}
		
		public String getAuthMethod() {
			return authMethod;
		}
		
		public void setAuthMethod(String authMethod) {
			this.authMethod = authMethod;
		}
		
		public String getFromToWwwRedirect() {
			return fromToWwwRedirect;
		}
		
		public void setFromToWwwRedirect(String fromToWwwRedirect) {
			this.fromToWwwRedirect = fromToWwwRedirect;
		}
		
		public String getServiceUpstream() {
			return serviceUpstream;
		}
		
		public void setServiceUpstream(String serviceUpstream) {
			this.serviceUpstream = serviceUpstream;
		}
		
		@Override
		public String toString() {
			return "OtherConfig [affinity=" + affinity + ", sessionCookieName=" + sessionCookieName
					+ ", sessionCookieHash=" + sessionCookieHash + ", upstreamHashBy=" + upstreamHashBy
					+ ", defaultBackend=" + defaultBackend + ", authUrl=" + authUrl + ", limitConnections="
					+ limitConnections + ", limitRps=" + limitRps + ", limitRpm=" + limitRpm + ", upstreamMaxFails="
					+ upstreamMaxFails + ", upstreamFailTimeout=" + upstreamFailTimeout + ", authMethod=" + authMethod
					+ ", fromToWwwRedirect=" + fromToWwwRedirect + ", serviceUpstream=" + serviceUpstream + "]";
		}
	}
	
	public static class CanaryConfig {
		
		private String headerKey;
		
		private String headerValue;
		
		private String cookie;
		
		private String canaryWeight;

		public String getHeaderKey() {
			return headerKey;
		}

		public void setHeaderKey(String headerKey) {
			this.headerKey = headerKey;
		}

		public String getHeaderValue() {
			return headerValue;
		}

		public void setHeaderValue(String headerValue) {
			this.headerValue = headerValue;
		}

		public String getCookie() {
			return cookie;
		}

		public void setCookie(String cookie) {
			this.cookie = cookie;
		}

		public String getCanaryWeight() {
			return canaryWeight;
		}

		public void setCanaryWeight(String canaryWeight) {
			this.canaryWeight = canaryWeight;
		}

		@Override
		public String toString() {
			return "canaryConfig [headerKey=" + headerKey + ", headerValue=" + headerValue + ", cookie=" + cookie
					+ ", canaryWeight=" + canaryWeight + "]";
		}
		
		
	}
}