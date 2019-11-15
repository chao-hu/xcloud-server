package com.xxx.xcloud.module.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.application.entity.ServiceAffinity;

/**
 * 
 * <p>
 * Description: 服务亲和属性持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月6日
 */
@Repository
public interface ServiceAffinityRepository extends JpaRepository<ServiceAffinity, String> {

	/**
	 * Description:通过服务ID查询
	 * 
	 * @param serviceId
	 *            服务ID
	 * @return ServiceAffinity
	 */
	ServiceAffinity findByServiceId(String serviceId);

	/**
     * 
     * <p>Description: 根据服务id删除相关记录</p>
     *
     * @param serviceId
     */
    @Query("delete from ServiceAffinity s where s.serviceId = ?1")
	@Modifying
	void deleteByServiceId(String serviceId);

    /**
     * find
     * 
     * @param id
     * @param serviceId
     * @return ServiceAffinity
     */
    ServiceAffinity findByIdAndServiceId(String id, String serviceId);
}
