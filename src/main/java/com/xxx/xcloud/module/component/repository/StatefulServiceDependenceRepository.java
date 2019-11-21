package com.xxx.xcloud.module.component.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.StatefulServiceDependence;

/**
 * 组件依赖表jpa
 * 
 * @author LiuYue
 * @date 2018年12月12日
 */
@Repository
public interface StatefulServiceDependenceRepository extends JpaRepository<StatefulServiceDependence, String> {

    /**
     * 获取依赖于serviceId的记录
     * 
     * @param serviceId
     * @return
     */
    public List<StatefulServiceDependence> findByDependenceServiceId(String serviceId);

    /**
     * 获取serviceId依赖的所有记录
     * 
     * @param serviceId
     * @return
     */
    public List<StatefulServiceDependence> findByServiceId(String serviceId);

    /**
     * 通过serviceId删除记录
     * 
     * @param serviceId
     * @return
     */
    void deleteByServiceId(String serviceId);
}
