package com.xxx.xcloud.module.env.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.env.entity.EnvTemplate;
import com.xxx.xcloud.module.env.repository.EnvTemplateRepository;
import com.xxx.xcloud.module.env.service.EnvService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.utils.StringUtils;

/**
 * 
 * <p>
 * Description: 环境变量模板功能实现类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Service
public class EnvServiceImpl implements EnvService {

    private static final Logger LOG = LoggerFactory.getLogger(EnvServiceImpl.class);

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Autowired
    private EnvTemplateRepository envTemplateRepository;

    @Override
    public EnvTemplate add(EnvTemplate envTemplate) {
        Tenant tenant = tenantService.findTenantByTenantName(envTemplate.getTenantName());
        if (null == tenant) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "指定的租户不存在");
        }

        if (envTemplateRepository.findByTenantNameAndTemplateName(envTemplate.getTenantName(),
                envTemplate.getTemplateName()) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "指定的模板已经存在");
        }

        if (StringUtils.isEmpty(envTemplate.getEnvData())) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "模板数据为空");
        }

        envTemplate.setCreateTime(new Date());
        envTemplate = envTemplateRepository.save(envTemplate);

        return envTemplate;
    }

    @Override
    public void delete(String id) {
        envTemplateRepository.deleteById(id);
    }

    @Override
    public void update(String id, Map<String, Object> envData) {
        EnvTemplate envTemplate = envTemplateRepository.findById(id).get();
        if (null == envTemplate) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "指定的模板不存在");
        }

        envTemplate.setEnvData(JSON.toJSONString(envData));
        envTemplateRepository.save(envTemplate);
    }

    @Override
    public EnvTemplate get(String id) {
        return envTemplateRepository.findById(id).get();
    }

    @Override
    public List<EnvTemplate> list(String tenantName, String templateName, String projectId) {
        if (StringUtils.isEmpty(templateName)) {
            templateName = "";
        }
        templateName = "%" + templateName + "%";
        List<EnvTemplate> envTemplates = new ArrayList<EnvTemplate>();
        if (StringUtils.isEmpty(projectId)) {
            envTemplates = envTemplateRepository.findByTenantNameAndTemplateNameLike(tenantName, templateName);
        } else {
            envTemplates = envTemplateRepository.findByTenantNameAndTemplateNameLikeAndProjectId(tenantName,
                    templateName, projectId);
        }
        if (envTemplates.size() > 0) {
            for (EnvTemplate envTemplate : envTemplates) {
                Map<String, String> envData = JSON.parseObject(envTemplate.getEnvData(),
                        new TypeReference<Map<String, String>>() {});
                envTemplate.setVariableNumber(envData.size());
            }
        }
        return envTemplates;
    }

    @Override
    public EnvTemplate getEnvTemplateByNameAndTenantName(String envTemplateName, String tenantname) {
        EnvTemplate envTemplate = null;
        try {
            envTemplate = envTemplateRepository.findByTenantNameAndTemplateName(tenantname, envTemplateName);
        } catch (Exception e) {
            LOG.error("查询环境变量模板失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询环境变量模板失败");
        }
        return envTemplate;
    }

    @Override
    public Page<EnvTemplate> getEnvTemplateList(String tenantName, String templateName, String projectId,
            PageRequest pageable) {
        Page<EnvTemplate> envTemplatePage = null;

        try {
            if (StringUtils.isNotEmpty(templateName)) {
                if (StringUtils.isEmpty(projectId)) {
                    envTemplatePage = envTemplateRepository.findByTemplateNameLikeAndTenantNameOrderByCreateTimeDesc(
                            "%" + templateName + "%", tenantName, pageable);
                } else {
                    envTemplatePage = envTemplateRepository
                            .findByTemplateNameLikeAndTenantNameAndProjectIdOrderByCreateTimeDesc(
                                    "%" + templateName + "%", tenantName, projectId, pageable);
                }
            } else {
                if (StringUtils.isEmpty(projectId)) {
                    envTemplatePage = envTemplateRepository.findByTenantNameOrderByCreateTimeDesc(tenantName, pageable);
                } else {
                    envTemplatePage = envTemplateRepository
                            .findByTenantNameAndProjectIdOrderByCreateTimeDesc(tenantName, projectId, pageable);
                }
            }

            if (null != envTemplatePage && envTemplatePage.getContent().size() > 0) {
                for (int i = 0; i < envTemplatePage.getContent().size(); i++) {
                    Map<String, String> envData = JSON.parseObject(envTemplatePage.getContent().get(i).getEnvData(),
                            new TypeReference<Map<String, String>>() {});
                    envTemplatePage.getContent().get(i).setVariableNumber(envData.size());
                }
            }
        } catch (Exception e) {
            LOG.error("查询环境变量模板列表失败 ", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询环境变量模板列表失败");
        }

        return envTemplatePage;
    }

}
