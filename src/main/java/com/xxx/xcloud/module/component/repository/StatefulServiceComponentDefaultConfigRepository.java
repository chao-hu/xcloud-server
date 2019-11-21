package com.xxx.xcloud.module.component.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;

/**
 * 组件默认参数表jpa
 * 
 * @author LiuYue
 * @date 2018年12月12日
 */
@Repository
public interface StatefulServiceComponentDefaultConfigRepository
        extends JpaRepository<StatefulServiceComponentDefaultConfig, String> {
    /**
     * appType,version
     * 
     * @param appType
     * @param version
     * @return
     */
    public List<StatefulServiceComponentDefaultConfig> findByAppTypeAndVersionOrderBySortAsc(String appType,
            String version);

    /**
     * appType，version，展示等级
     * 
     * @param appType
     * @param version
     * @param showLevel
     * @return
     */
    public List<StatefulServiceComponentDefaultConfig> findByAppTypeAndVersionAndShowLevelOrderBySortAsc(String appType,
            String version, int showLevel);

    /**
     * appType,version,group,showlevel
     * 
     * @param appType
     * @param version
     * @param groupName
     * @param showLevel
     * @return
     */
    public List<StatefulServiceComponentDefaultConfig> findByAppTypeAndVersionAndGroupAndShowLevelOrderBySortAsc(
            String appType, String version, String groupName, int showLevel);
    
    /**
     * appType,version,cnfFile,showlevel
     * 
     * @param appType
     * @param version
     * @param cnfFileName
     * @param showLevel
     * @return
     */
    public List<StatefulServiceComponentDefaultConfig> findByAppTypeAndVersionAndCnfFileAndShowLevelOrderBySortAsc(
            String appType, String version, String cnfFileName, int showLevel);

    /**
     * appType,version
     * 
     * @param appType
     * @param version
     * @return
     */
    public List<StatefulServiceComponentDefaultConfig> findByAppTypeAndVersion(String appType, String version);

    /**
     * appType,version,只返回group
     * 
     * @param appType
     * @param version
     * @return
     * @throws RuntimeException
     */
    @Modifying
    @Transactional
    @Query("SELECT DISTINCT a.group as group FROM StatefulServiceComponentDefaultConfig a WHERE a.showLevel != 0 AND a.appType = ?1 AND a.version = ?2")
    public List<String> getGroupList(String appType, String version) throws RuntimeException;

    /**
     * appType,version,只返回cnf_file
     * 
     * @param appType
     * @param version
     * @return
     * @throws RuntimeException
     */
    @Modifying
    @Transactional
    @Query("SELECT DISTINCT a.cnfFile as cnfFile FROM StatefulServiceComponentDefaultConfig a WHERE a.showLevel != 0 AND a.appType = ?1 AND a.version = ?2")
    public List<String> getCnfFileList(String appType, String version) throws RuntimeException;

}
