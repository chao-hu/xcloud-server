package com.xxx.xcloud.module.springcloud.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.module.springcloud.entity.SpringCloudConfigFile;


/**
 * @ClassName: ISpringCloudConfigFileService
 * @Description: spring cloud 配置接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
public interface ISpringCloudConfigFileService {

    public SpringCloudConfigFile findTopByServiceIdAndConfigNameOrderByCreateTimeDesc(String serviceId,
            String configName) throws Exception;

    public SpringCloudConfigFile findTopByServiceIdAndConfigNameAndEnableOrderByCreateTimeDesc(String serviceId,
            String configName, int enable) throws Exception;

    public SpringCloudConfigFile saveFile(SpringCloudConfigFile configFile) throws Exception;

    public void delete(String configId) throws Exception;

    public List<SpringCloudConfigFile> findByServiceIdAndConfigName(String serviceId, String configName)
            throws Exception;

    public SpringCloudConfigFile findById(String configId) throws Exception;

    public void deleteByServiceIdAndConfigName(String serviceId, String configName) throws Exception;
    
    public void deleteByServiceId(String serviceId) throws Exception;

    public Page<SpringCloudConfigFile> findByServiceIdAndConfigName(String serviceId, String configName,
            Pageable pageable);

    public Map<String, Integer> findConfigNameList(String serviceId);
    
    public Map<String, SpringCloudConfigFile> findConfigContentList(String serviceId);

    public SpringCloudConfigFile saveConfig(SpringCloudConfigFile configFile, SpringCloudConfigFile configFileOld,
            String cephFileId) throws FileNotFoundException, IOException;

}
