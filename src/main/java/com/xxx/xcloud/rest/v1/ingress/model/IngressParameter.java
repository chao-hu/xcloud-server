package com.xxx.xcloud.rest.v1.ingress.model;

import com.xxx.xcloud.module.ingress.entity.IngressProxy;
import com.xxx.xcloud.module.ingress.entity.ServiceIngress;

import lombok.Data;

/**
 * Description: 创建或者修改服务代理所需参数
 * @author  LYJ </br>
 * create time：2018年12月5日 下午2:17:32 </br>
 * @version 1.0
 * @since
 */
@Data
public class IngressParameter {
	
	/**
	 * 租户名称
	 */
	private String tenantName;

	private ServiceIngress serviceIngress;
	
	private IngressProxy ingressProxy;
	
	private String projectCode;
	
	private String domain;
	
    @Override
    public String toString() {
        return "IngressParameter [tenantName=" + tenantName + ", serviceIngress=" + serviceIngress + ", ingressProxy="
                + ingressProxy + ", projectCode=" + projectCode + ", domain=" + domain + "]";
    }

}
