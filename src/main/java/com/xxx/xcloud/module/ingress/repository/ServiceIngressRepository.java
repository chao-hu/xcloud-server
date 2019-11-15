package com.xxx.xcloud.module.ingress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ingress.entity.ServiceIngress;

/**
 * @author  LYJ </br>
 * create time：2018年12月5日 下午3:28:37 </br>
 * @version 1.0
 * @since
 */
@Repository
public interface ServiceIngressRepository extends JpaRepository<ServiceIngress, String> {

	/**
     * Description:通过ingressDomainId查询
     * 
     * @param ingressDomainId
     *            域名ID
     * @return List<ServiceIngress>
     */
	List<ServiceIngress> findByIngressDomainId(String ingressDomainId);

	/**
	 * Description:通过服务ID查询
	 * @param serviceId
	 * 			服务ID
	 * @return
	 * ServiceIngress
	 */
	ServiceIngress findByServiceId(String serviceId);

    /**
     * find
     * 
     * @param serviceId
     * @param ingressDomainId
     * @return ServiceIngress
     */
	ServiceIngress findByServiceIdAndIngressDomainId(String serviceId, String ingressDomainId);

}
