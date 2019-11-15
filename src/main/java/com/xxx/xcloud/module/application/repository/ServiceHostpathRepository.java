package com.xxx.xcloud.module.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.application.entity.ServiceHostpath;

/**
 * 
 * <p>
 * Description: 本地存储持久层接口
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月6日
 */
@Repository
public interface ServiceHostpathRepository extends JpaRepository<ServiceHostpath, String> {

    /**
     * Description:通过服务ID查询
     * @param serviceId
     *          服务ID
     * @return
     * List<ServiceHostpath>
     */
    List<ServiceHostpath> findByServiceId(String serviceId);
    
    /**
     * 
     * <p>Description: 根据服务id删除相关记录</p>
     *
     * @param serviceId
     */
    @Modifying
    @Query("delete from ServiceHostpath s where s.serviceId = ?1")
	void deleteByServiceId(String serviceId);

}
