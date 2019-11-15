package com.xxx.xcloud.module.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.application.entity.ServiceContainerLifecycle;

/**
 * Description: 容器生命周期持久层
 * 
 * @author  LYJ </br>
 * create time：2019年4月22日 上午11:33:26 </br>
 * @version 1.0
 * @since
 */
@Repository
public interface ServiceContainerLifecycleRepository extends JpaRepository<ServiceContainerLifecycle, String> {

    /**
     * find
     * 
     * @param serviceId
     * @return List<ServiceContainerLifecycle>
     */
    List<ServiceContainerLifecycle> findByServiceId(String serviceId);

    
}
