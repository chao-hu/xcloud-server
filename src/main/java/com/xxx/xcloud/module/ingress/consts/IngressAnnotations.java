package com.xxx.xcloud.module.ingress.consts;

/**
 * Description: Ingress注解参数<br/>
 * date: 2019年7月17日 上午11:00:46 <br/>
 * 
 * @author LYJ
 * @version
 * @since JDK 1.8
 */
public class IngressAnnotations {

	/**
	 * Location参数
	 */

	/**
	 * TLS相关
	 **/
	public static final String INGRESS_LOCATION_SSL_PASSTHROUGH = "nginx.ingress.kubernetes.io/ssl-passthrough";
	public static final String INGRESS_LOCATIONI_SSL_REDIRECT = "nginx.ingress.kubernetes.io/ssl-redirect";
	public static final String INGRESS_LOCATION_FORCE_SSL_REDIRECT = "nginx.ingress.kubernetes.io/force-ssl-redirect";
	public static final String INGRESS_LOCATION_SECURE_BACKENDS = "nginx.ingress.kubernetes.io/secure-backends";
	/**
	 * 认证相关
	 **/
	public static final String INGRESS_LOCATION_AUTH_TYPE = "nginx.ingress.kubernetes.io/auth-type";
	public static final String INGRESS_LOCATION_AUTH_TLS_SECRET = "nginx.ingress.kubernetes.io/auth-tls-secret";
	public static final String INGRESS_LOCATION_AUTH_REALM = "nginx.ingress.kubernetes.io/auth-realm";
	public static final String INGRESS_LOCATION_AUTH_TLS_VERIFY_DEPTH = "nginx.ingress.kubernetes.io/auth-tls-verify-depth";
	public static final String INGRESS_LOCATION_AUTH_TLS_VERIFY_CLIENT = "nginx.ingress.kubernetes.io/auth-tls-verify-client";
	public static final String INGRESS_LOCATION_AUTH_TLS_ERROR_PAGE = "nginx.ingress.kubernetes.io/auth-tls-error-page";
	public static final String INGRESS_LOCATION_AUTH_TLS_PASS_CERTIFICATE_TO_UPSTREAM = "nginx.ingress.kubernetes.io/auth-tls-pass-certificate-to-upstream";
	public static final String INGRESS_LOCATION_WHITELIST_SOURCE_RANGE = "nginx.ingress.kubernetes.io/whitelist-source-range";
	/**
	 * URL相关
	 **/
	public static final String INGRESS_LOCATION_APP_ROOT = "nginx.ingress.kubernetes.io/app-root";
	public static final String INGRESS_LOCATION_REWRITE_TARGET = "nginx.ingress.kubernetes.io/rewrite-target";
	/**
	 * 跨资源共享相关
	 **/
	public static final String INGRESS_LOCATION_ENABLE_CORS = "nginx.ingress.kubernetes.io/enable-cors";
	public static final String INGRESS_LOCATION_CORS_ALLOW_ORIGIN = "nginx.ingress.kubernetes.io/cors-allow-origin";
	public static final String INGRESS_LOCATION_CORS_ALLOW_HEADERS = "nginx.ingress.kubernetes.io/cors-allow-headers";
	public static final String INGRESS_LOCATION_CORS_ALLOW_METHODS = "nginx.ingress.kubernetes.io/cors-allow-methods";
	public static final String INGRESS_LOCATION_CORS_ALLOW_CREDENTIALS = "nginx.ingress.kubernetes.io/cors-allow-credentials";
	public static final String INGRESS_LOCATION_CORS_MAX_AGE = "nginx.ingress.kubernetes.io/cors-max-age";
	/**
	 * nginx.conf中的一些配置
	 **/
	public static final String INGRESS_LOCATION_CONFIGURATION_SNIPPET = "nginx.ingress.kubernetes.io/configuration-snippet";
	public static final String INGRESS_LOCATION_SERVER_SNIPPET = "nginx.ingress.kubernetes.io/server-snippet";
	public static final String INGRESS_LOCATION_PROXY_CONNECT_TIMEOUT = "nginx.ingress.kubernetes.io/proxy-connect-timeout";
	public static final String INGRESS_LOCATION_PROXY_SEND_TIMEOUT = "nginx.ingress.kubernetes.io/proxy-send-timeout";
	public static final String INGRESS_LOCATION_PROXY_READ_TIMEOUT = "nginx.ingress.kubernetes.io/proxy-read-timeout";
	public static final String INGRESS_LOCATION_PROXY_NEXT_UPSTREAM = "nginx.ingress.kubernetes.io/proxy-next-upstream";
	public static final String INGRESS_LOCATION_PROXY_REQUEST_BUFFERING = "nginx.ingress.kubernetes.io/proxy-request-buffering";
	public static final String INGRESS_LOCATION_PROXY_REDIRECT_FROM = "nginx.ingress.kubernetes.io/proxy-redirect-from";
	public static final String INGRESS_LOCATION_PROXY_REDIRECT_TO = "nginx.ingress.kubernetes.io/proxy-redirect-to";
	public static final String INGRESS_LOCATION_PROXY_BODY_SIZE = "nginx.ingress.kubernetes.io/proxy-body-size";
	public static final String INGRESS_LOCATION_PROXY_BUFFER_SIZE = "nginx.ingress.kubernetes.io/proxy-buffer-size";
	public static final String INGRESS_LOCATION_CLIENT_BODY_BUFFER_SIZE = "nginx.ingress.kubernetes.io/client-body-buffer-size";
	/**
	 * 其它配置
	 **/
	public static final String INGRESS_LOCATION_LIMIT_CONNECTIONS = "nginx.ingress.kubernetes.io/limit-connections";
	public static final String INGRESS_LOCATION_LIMIT_RPS = "nginx.ingress.kubernetes.io/limit-rps";
	public static final String INGRESS_LOCATION_LIMIT_RPM = "nginx.ingress.kubernetes.io/limit-rpm";
	public static final String INGRESS_LOCATION_AFFINITY = "nginx.ingress.kubernetes.io/affinity";
	public static final String INGRESS_LOCATION_SESSION_COOKIE_NAME = "nginx.ingress.kubernetes.io/session-cookie-name";
	public static final String INGRESS_LOCATION_SESSION_COOKIE_HASH = "nginx.ingress.kubernetes.io/session-cookie-hash";
	public static final String INGRESS_LOCATION_UPSTREAM_MAX_FAILS = "nginx.ingress.kubernetes.io/upstream-max-fails";
	public static final String INGRESS_LOCATION_UPSTREAM_FAIL_TIMEOUT = "nginx.ingress.kubernetes.io/upstream-fail-timeout";
	public static final String INGRESS_LOCATION_UPSTREAM_HASH_BY = "nginx.ingress.kubernetes.io/upstream-hash-by";
	public static final String INGRESS_LOCATION_DEFAULT_BACKEND = "nginx.ingress.kubernetes.io/default-backend";
	public static final String INGRESS_LOCATION_AUTH_URL = "nginx.ingress.kubernetes.io/auth-url";
	public static final String INGRESS_LOCATION_AUTH_METHOD = "nginx.ingress.kubernetes.io/auth-method";
	public static final String INGRESS_LOCATION_FORM_TO_WWW_REDIRECT = "nginx.ingress.kubernetes.io/from-to-www-redirect";
	public static final String INGRESS_LOCATION_SERVICE_UPSTREAM = "nginx.ingress.kubernetes.io/service-upstream";

	/**
	 * 灰度发布
	 **/
	public static final String INGRESS_LOCATION_CANARY = "nginx.ingress.kubernetes.io/canary";
	public static final String INGRESS_LOCATION_CANARY_BY_HEADER = "nginx.ingress.kubernetes.io/canary-by-header";
	public static final String INGRESS_LOCATION_CANARY_BY_HEADER_VALUE = "nginx.ingress.kubernetes.io/canary-by-header-value";
	public static final String INGRESS_LOCATION_CANARY_BY_COOKIE = "nginx.ingress.kubernetes.io/canary-by-cookie";
	public static final String INGRESS_LOCATION_CANARY_WEIGHT = "nginx.ingress.kubernetes.io/canary-weight";
	
	/**
	 * server参数
	 **/
	
	/**
	 * URL相关
	 **/
	public static final String INGRESS_SERVER_ADD_BASE_URL = "nginx.ingress.kubernetes.io/add-base-url";
	public static final String INGRESS_SERVER_BASE_URL_SCHEME = "nginx.ingress.kubernetes.io/base-url-scheme";
	public static final String INGRESS_SERVER_X_FORWARDED_PREFIX = "nginx.ingress.kubernetes.io/x-forwarded-prefix";
	/**
	 * 其它配置
	 **/
	public static final String INGRESS_SERVER_PROXY_PASS_PARAMS = "nginx.ingress.kubernetes.io/proxy-pass-params";
	public static final String INGRESS_SERVER_SERVER_ALIAS = "nginx.ingress.kubernetes.io/server-alias";
	public static final String INGRESS_SERVER_LIMIT_RATE = "nginx.ingress.kubernetes.io/limit-rate";
	public static final String INGRESS_SERVER_LIMIT_RATE_AFTER = "nginx.ingress.kubernetes.io/limit-rate-after";
	
}
