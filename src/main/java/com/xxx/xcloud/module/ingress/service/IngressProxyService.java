package com.xxx.xcloud.module.ingress.service;

import com.xxx.xcloud.rest.v1.ingress.model.IngressParameter;

/**
 * Description:服务http代理接口
 * @author  LYJ </br>
 * create time：2018年12月5日 上午11:06:47 </br>
 * @version 1.0
 * @since
 */
public interface IngressProxyService {
	
	/**
	 * Description:查询服务的http代理信息
	 * @param serviceId
	 * 			服务ID
	 * @param tenantName
	 * 			租户名称
	 * @return
	 * IngressProxyParameter
	 */
	public IngressParameter getIngressProxy(String serviceId, String tenantName);

	/**
	 * Description:创建服务的http代理
	 * @param ingressProxyParameter
	 * 			http代理参数
	 * @return
	 * Boolean
	 */
    public Boolean createIngressProxy(IngressParameter ingressProxyParameter);
	
	/**
	 * Description:修改服务的http代理
	 * @param ingressProxyParameter
	 * 				http代理参数
	 * @return
	 * Boolean
	 */
	public Boolean updateIngressProxy(IngressParameter ingressProxyParameter);
	
	/**
	 * Description:删除服务的http代理
	 * @param serviceId
	 * 			服务ID
	 * @param tenantName
	 * 			租户名称
	 * @return
	 * Boolean
	 */
	public Boolean deleteIngressProxy(String serviceId, String tenantName);
}
