package com.xxx.xcloud.module.ingress.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ingress.entity.IngressDomain;

/**
 * @author LYJ </br>
 *         create time：2018年12月5日 下午3:38:10 </br>
 * @version 1.0
 * @since
 */
@Repository
public interface IngressDomainRepository extends JpaRepository<IngressDomain, String> {

    /**
     * Description:根据租户名查询
     * 
     * @param tenantName
     *            租户名称
     * @return IngressDomain
     */
    public IngressDomain findByTenantName(String tenantName);

    /**
     * Description: 分页查询
     * 
     * @param tenantName
     *            租户民称
     * @param type
     *            TLD为一级域名,SLD二级域名
     * @param pageable
     *            分页参数
     * @return Page<IngressDomain>
     */
    public Page<IngressDomain> findByTenantNameAndType(String tenantName, String type, Pageable pageable);

    /**
     * Description:通过域名查询
     * 
     * @param domain
     *            域名
     * @return IngressDomain
     */
    public List<IngressDomain> findByDomain(String domain);

    /**
     * Description:通过域名和类型查询
     * 
     * @param domain
     *            域名
     * @param type
     *            TLD为一级域名,SLD二级域名
     * @return List<IngressDomain>
     */
    public List<IngressDomain> findByDomainAndType(String domain, String type);

    /**
     * Description:通过项目编码查询
     * 
     * @param projectCode
     *            项目编码
     * @return IngressDomain
     */
    public IngressDomain findByProjectCode(String projectCode);

    /**
     * Description:通过项目编码查询
     * 
     * @param projectCode
     *            项目编码
     * @param domain
     * @return IngressDomain
     */
    public IngressDomain findByProjectCodeAndDomain(String projectCode, String domain);

}
