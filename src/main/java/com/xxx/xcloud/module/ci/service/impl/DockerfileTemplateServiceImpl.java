package com.xxx.xcloud.module.ci.service.impl;

import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.entity.DockerfileTemplate;
import com.xxx.xcloud.module.ci.entity.DockerfileType;
import com.xxx.xcloud.module.ci.repository.DockerfileTemplateRepository;
import com.xxx.xcloud.module.ci.repository.DockerfileTypeRepository;
import com.xxx.xcloud.module.ci.service.DockerfileTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author mengaijun
 * @Description: dockerfile模版操作接口
 * @date: 2018年12月7日 下午5:31:18
 */
@Service
public class DockerfileTemplateServiceImpl implements DockerfileTemplateService {

    @Autowired
    private DockerfileTemplateRepository templateRepository;

    @Autowired
    private DockerfileTypeRepository typeRepository;

    private static final Logger LOG = LoggerFactory.getLogger(DockerfileTemplateServiceImpl.class);

    @Override
    public DockerfileTemplate addTemplate(String tenantName, String dockerfileContent, String dockerfileName,
            String createdBy, String projectId) {
        // 判断名称是否存在
        List<DockerfileTemplate> templates = getTemplatesByName(tenantName, dockerfileName, projectId);
        if (templates.size() > 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "名称已经存在!");
        }

        // 添加信息
        DockerfileTemplate template = new DockerfileTemplate(tenantName, dockerfileContent, dockerfileName, new Date(),
                createdBy, projectId);
        return saveTemplate(template);
    }

    /**
     * 保存Dockerfile模版
     *
     * @param template
     * @return DockerfileTemplate
     * @date: 2019年2月28日 下午3:05:55
     */
    private DockerfileTemplate saveTemplate(DockerfileTemplate template) {
        try {
            return templateRepository.save(template);
        } catch (Exception e) {
            LOG.error("保存Dockerfile模版失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存Dockerfile模版失败!");
        }
    }

    /**
     * 根据名称和项目ID查询模版, (项目ID为空, 只根据名称查询; 项目ID不为空, 根据两个条件查询)
     *
     * @param tenantName
     * @param dockerfileName
     * @param projectId
     * @return List<DockerfileTemplate>
     * @date: 2019年2月28日 下午3:03:52
     */
    private List<DockerfileTemplate> getTemplatesByName(String tenantName, String dockerfileName, String projectId) {
        try {
            if (StringUtils.isEmpty(projectId)) {
                return templateRepository.getAllByName(tenantName, dockerfileName);
            } else {
                return templateRepository.getAllByNameAndProjectId(tenantName, dockerfileName, projectId);
            }
        } catch (Exception e) {
            LOG.error("根据dockerfile名称查询模版失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "根据名称查询dockerfile模版失败!");
        }
    }

    @Override
    public boolean modifyTemplate(String id, String dockerfileContent) {
        Optional<DockerfileTemplate> templateOptional = null;
        try {
            templateOptional = templateRepository.findById(id);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询数据库当前记录失败!");
        }

        if (!templateOptional.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "更新记录不存在!");
        }

        DockerfileTemplate template = templateOptional.get();
        template.setDockerfileContent(dockerfileContent);
        saveTemplate(template);
        return true;
    }

    @Override
    public boolean deleteTemplate(String id) {
        try {
            templateRepository.deleteById(id);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除数据库记录失败或数据库不存在此条记录!");
        }
        return true;
    }

    @Override
    public Page<DockerfileTemplate> getTemplates(String tenantName, String projectId, Pageable pageable) {
        try {
            if (StringUtils.isEmpty(projectId)) {
                return templateRepository.getAll(tenantName, pageable);
            }
            return templateRepository.getAll(tenantName, projectId, pageable);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询多条记录失败!");
        }
    }

    @Override
    public Page<DockerfileTemplate> getTemplatesPage(Pageable pageable) {
        try {
            return templateRepository.findAll(pageable);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询多条记录失败!");
        }
    }

    @Override
    public DockerfileTemplate getTemplateById(String id) {
        Optional<DockerfileTemplate> templateOptional = null;
        try {
            templateOptional = templateRepository.findById(id);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询数据库当前记录失败!");
        }
        if (!templateOptional.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询记录不存在");
        }
        return templateOptional.get();
    }

    @Override
    public List<DockerfileType> getTypes() {
        List<DockerfileType> typeList = null;
        try {
            typeList = typeRepository.findAll();
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询模版类型失败!");
        }
        return typeList;
    }

    @Override
    public Page<DockerfileTemplate> getTemplatesPage(String typeId, Pageable pageable) {
        try {
            return templateRepository.findByTypeId(typeId, pageable);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询多条记录失败!");
        }
    }

}
