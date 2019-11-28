package com.xxx.xcloud.module.springcloud.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.springcloud.entity.SpringCloudConfigFile;


/**
 * @ClassName: SpringCloudConfigFileRepository
 * @Description: spring cloud 应用表jpa
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Repository
public interface SpringCloudConfigFileRepository extends JpaRepository<SpringCloudConfigFile, String> {

    public SpringCloudConfigFile findTopByServiceIdAndConfigNameAndEnableOrderByCreateTimeDesc(String serviceId,
            String configName, int enable);

    public SpringCloudConfigFile findTopByServiceIdAndConfigNameOrderByCreateTimeDesc(String serviceId,
            String configName);

    public void deleteByServiceIdAndConfigName(String serviceId, String configName);

    public List<SpringCloudConfigFile> findByServiceIdAndConfigName(String serviceId, String configName);

    public List<SpringCloudConfigFile> findByServiceIdAndConfigNameOrderByCreateTimeDesc(String serviceId,
            String configName);

    public Page<SpringCloudConfigFile> findByServiceIdAndConfigName(String serviceId, String configName,
            Pageable pageable);

    @Query("select DISTINCT s.configName from SpringCloudConfigFile s where s.serviceId = ?1")
    public List<String> findConfigNameList(String serviceId);

    @Query("select count(*) from SpringCloudConfigFile s where s.serviceId = ?1 and s.configName = ?2")
    public int findNumByServiceIdAndConfigName(String serviceId, String configName);
}
