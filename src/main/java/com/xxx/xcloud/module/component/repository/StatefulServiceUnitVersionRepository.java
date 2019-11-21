package com.xxx.xcloud.module.component.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.StatefulServiceUnitVersion;

/**
 * 组件统一安装包版本管理表jpa
 * 
 * @author LiuYue
 * @date 2018年12月12日
 */
@Repository
public interface StatefulServiceUnitVersionRepository extends JpaRepository<StatefulServiceUnitVersion, String> {

    /**
     * appType，组件server，版本
     * 
     * @param appType
     * @param version
     * @return
     */
    public StatefulServiceUnitVersion findByAppTypeAndExtendedFieldIsNullAndVersion(String appType, String version);

    /**
     * appType,extendedField,版本
     * 
     * @param appType
     * @param extendedField
     * @param version
     * @return
     */
    public StatefulServiceUnitVersion findByAppTypeAndExtendedFieldAndVersion(String appType, String extendedField,
            String version);

    /**
     * appType,组件server，只返回版本
     * 
     * @param appType
     * @return
     * @throws RuntimeException
     */
    @Modifying
    @Transactional
    @Query("SELECT DISTINCT a.version FROM StatefulServiceUnitVersion a WHERE a.appType = ?1 and a.extendedField is null")
    public List<String> findByAppType(String appType) throws RuntimeException;

    /**
     * appType,extendedFiled,只返回版本
     * 
     * @param appType
     * @param extendedField
     * @return
     * @throws RuntimeException
     */
    @Modifying
    @Transactional
    @Query("SELECT DISTINCT a.version FROM StatefulServiceUnitVersion a WHERE a.appType = ?1 and a.extendedField = ?2")
    public List<String> findByAppTypeAndExtendedField(String appType, String extendedField) throws RuntimeException;

}
