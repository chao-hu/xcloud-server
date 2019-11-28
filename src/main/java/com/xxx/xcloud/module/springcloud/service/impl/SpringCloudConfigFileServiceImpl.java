package com.xxx.xcloud.module.springcloud.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.service.CephFileService;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudConfigFile;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudConfigFileRepository;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudServiceRepository;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudConfigFileService;
import com.xxx.xcloud.utils.FileUtils;
import com.xxx.xcloud.utils.StringUtils;


/**
 * @ClassName: SpringCloudConfigFileServiceImpl
 * @Description: spring cloud 配置接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Service
public class SpringCloudConfigFileServiceImpl implements ISpringCloudConfigFileService {

    private static final Logger LOG = LoggerFactory.getLogger(SpringCloudConfigFileServiceImpl.class);

    @Autowired
    private SpringCloudConfigFileRepository springCloudConfigFileRepository;

    @Autowired
    private SpringCloudServiceRepository springCloudServiceRepository;

    @Autowired
    private CephFileService cephFileService;

    @Override
    @Transactional(readOnly = true)
    public SpringCloudConfigFile findTopByServiceIdAndConfigNameAndEnableOrderByCreateTimeDesc(String serviceId,
            String configName, int enable) throws Exception {

        return springCloudConfigFileRepository.findTopByServiceIdAndConfigNameAndEnableOrderByCreateTimeDesc(serviceId,
                configName, enable);
    }

    @Override
    @Transactional(readOnly = true)
    public SpringCloudConfigFile findTopByServiceIdAndConfigNameOrderByCreateTimeDesc(String serviceId,
            String configName) throws Exception {
        return springCloudConfigFileRepository.findTopByServiceIdAndConfigNameOrderByCreateTimeDesc(serviceId,
                configName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpringCloudConfigFile saveFile(SpringCloudConfigFile configFile) throws Exception {
        return springCloudConfigFileRepository.save(configFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String configId) throws Exception {
        springCloudConfigFileRepository.deleteById(configId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpringCloudConfigFile> findByServiceIdAndConfigName(String serviceId, String configName)
            throws Exception {
        return springCloudConfigFileRepository.findByServiceIdAndConfigNameOrderByCreateTimeDesc(serviceId, configName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByServiceIdAndConfigName(String serviceId, String configName) throws Exception {
        // 删除数据库配置文件信息
        springCloudConfigFileRepository.deleteByServiceIdAndConfigName(serviceId, configName);

        // 删除ceph上配置文件
        SpringCloudService springCloudService;
        Optional<SpringCloudService> springCloudServiceOptional = springCloudServiceRepository.findById(serviceId);
        if (springCloudServiceOptional.isPresent()) {
            springCloudService = springCloudServiceOptional.get();
            deletecephFile(springCloudService.getCephfileId(), configName);
        } else {
            LOG.error("根据serviceId" + serviceId + "获取service失败,删除ceph文件失败");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByServiceId(String serviceId) throws Exception {
        Map<String, Integer> configNameList = findConfigNameList(serviceId);
        for (String configName : configNameList.keySet()) {
            deleteByServiceIdAndConfigName(serviceId, configName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SpringCloudConfigFile findById(String configId) throws Exception {
        return springCloudConfigFileRepository.findById(configId).get();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpringCloudConfigFile> findByServiceIdAndConfigName(String serviceId, String configName,
            Pageable pageable) {
        return springCloudConfigFileRepository.findByServiceIdAndConfigName(serviceId, configName, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> findConfigNameList(String serviceId) {

        Map<String, Integer> configInfo = new HashMap<String, Integer>();
        // 查询文件名称
        List<String> configNameList = springCloudConfigFileRepository.findConfigNameList(serviceId);
        // 查询文件个数
        if (null != configNameList && !configNameList.isEmpty()) {
            for (String configName : configNameList) {
                int s = springCloudConfigFileRepository.findNumByServiceIdAndConfigName(serviceId, configName);
                configInfo.put(configName, s);
            }
        }
        return configInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, SpringCloudConfigFile> findConfigContentList(String serviceId) {

        Map<String, SpringCloudConfigFile> configInfo = new HashMap<String, SpringCloudConfigFile>();
        // 查询文件名称
        List<String> configNameList = springCloudConfigFileRepository.findConfigNameList(serviceId);
        // 查询文件个数
        if (null != configNameList && !configNameList.isEmpty()) {
            for (String configName : configNameList) {
                List<SpringCloudConfigFile> configFileList = springCloudConfigFileRepository
                        .findByServiceIdAndConfigName(serviceId, configName);
                for (SpringCloudConfigFile configFile : configFileList) {
                    if (SpringCloudCommonConst.SERVICE_CONFIG_ENABLE == configFile.getEnable()) {
                        configInfo.put(configName, configFile);
                        break;
                    }
                }
            }
        }
        return configInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpringCloudConfigFile saveConfig(SpringCloudConfigFile configFile, SpringCloudConfigFile configFileOld,
            String cephFileId) throws FileNotFoundException, IOException {

        // 保存配置数据库
        saveConfigInfo(configFile, configFileOld);

        // 是否启用
        // 启用
        if (SpringCloudCommonConst.SERVICE_CONFIG_ENABLE == configFile.getEnable()) {
            // 上传配置文件
            updateFile(cephFileId, configFile.getConfigName(), configFile.getConfigContent());
        }

        return configFile;

    }

    private void saveConfigInfo(SpringCloudConfigFile configFile, SpringCloudConfigFile configFileOld) {
        if (null == configFileOld) {
            springCloudConfigFileRepository.save(configFile);
        } else {
            if (SpringCloudCommonConst.SERVICE_CONFIG_ENABLE == configFile.getEnable()
                    && SpringCloudCommonConst.SERVICE_CONFIG_ENABLE == configFileOld.getEnable()) {
                configFileOld.setEnable(SpringCloudCommonConst.SERVICE_CONFIG_DISABLE);
                springCloudConfigFileRepository.save(configFileOld);
                springCloudConfigFileRepository.save(configFile);
            } else {
                springCloudConfigFileRepository.save(configFile);
            }
        }
    }

    private void updateFile(String cephFileId, String configName, String configContent) throws IOException {
        // 包装文件
        String path = "/tmp/" + StringUtils.randomString(8) + "/";

        File f = new File(path);
        f.mkdirs();
        File file = new File(path + configName);
        file.createNewFile();
        FileUtils.writeContentToFile(configContent, path + configName);

        FileInputStream inputStream = new FileInputStream(file);
        LOG.info("--------file.getName()-------" + file.getName());
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), configName,
                ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        // 调用上传
        // CephFileServiceImpl cephFileServiceImpl = (CephFileServiceImpl)
        // SpringUtils
        // .getBean("com.bonc.bdos.service.ceph.service.impl.CephFileServiceImpl.class");
        // cephFileServiceImpl.upLoadFile(CephFileId,
        // SpringCloudCommonConst.CEPHFS_MOUNTPATH, multipartFile);
        try {
            cephFileService.upLoadFile(cephFileId, "", multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.delAllFile(path);
            FileUtils.delFolder(path);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_UPLOAD, "ceph 上传文件失败");
        }
        FileUtils.delAllFile(path);
        FileUtils.delFolder(path);

    }

    private void deletecephFile(String cephFileId, String configName) throws ErrorMessageException {
        // 包装文件
        LOG.info("--------删除配置文件-------" + configName);
        try {
            cephFileService.removeFile(cephFileId, configName);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_UPLOAD, "ceph 上传文件失败");
        }

    }

}
