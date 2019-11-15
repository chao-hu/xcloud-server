package com.xxx.xcloud.module.ingress.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.module.ingress.entity.IngressDomain;

/**
 * Description:域名接口
 * @author  LYJ </br>
 * create time：2018年12月5日 下午2:54:23 </br>
 * @version 1.0
 * @since
 */
public interface IngressDomainService {

	/**
     * Description:一级域名列表
     * 
     * @param tenantName
     *            租户名称
     * @param type
     * @param pageable
     *            分页所需参数
     * @return Page<IngressDomain>
     */
	public Page<IngressDomain> ingressDomain(String tenantName, String type, Pageable pageable);
	
	/**
	 * Description:创建域名(tyep:TLD为一级域名,SLD二级域名)
	 * @param ingressDomain
	 * 			域名信息
	 * @return
	 * IngressDomain
	 */
	public IngressDomain createIngressDomain(IngressDomain ingressDomain);
	
	/**
	 * Description:删除域名(tyep:TLD为一级域名,SLD二级域名)
	 * @param ingressDomainId
	 * 			域名ID
	 * @param tenantName
	 * 			租户名称
	 * @return
	 * Boolean
	 */
	public Boolean deleteIngressDomain(String ingressDomainId, String tenantName);
	
	/**
	 * Description:修改域名(暂时只能修改二级域名)
	 * @param ingressDomain
	 * 			域名信息
	 * @return
	 * IngressDomain
	 */
	public IngressDomain updateIngressDomain(IngressDomain ingressDomain);
}
