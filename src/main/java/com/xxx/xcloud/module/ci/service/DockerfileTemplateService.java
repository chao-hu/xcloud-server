package com.xxx.xcloud.module.ci.service;

import com.xxx.xcloud.module.ci.entity.DockerfileTemplate;
import com.xxx.xcloud.module.ci.entity.DockerfileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author mengaijun
 */
public interface DockerfileTemplateService {
    /**
     * 添加模版
     *
     * @param tenantName        租户名称
     * @param dockerfileContent 模版内容
     * @param dockerfileName    模版名称
     * @param createdBy
     * @param projectId
     * @return DockerfileTemplate 保存后的对象
     * @date: 2018年12月3日 下午3:39:11
     */
    DockerfileTemplate addTemplate(String tenantName, String dockerfileContent, String dockerfileName, String createdBy,
            String projectId);

    /**
     * 修改模版
     *
     * @param id                修改记录ID
     * @param dockerfileContent 修改后内容
     * @return boolean 是否修改成功
     * @date: 2018年12月3日 下午3:41:51
     */
    boolean modifyTemplate(String id, String dockerfileContent);

    /**
     * 删除模版
     *
     * @param id 删除记录ID
     * @return boolean 删除是否成功
     * @date: 2018年12月3日 下午3:44:03
     */
    boolean deleteTemplate(String id);

    /**
     * 获取模版列表
     *
     * @param tenantName
     * @param projectId
     * @param pageable
     * @return Page<DockerfileTemplate>
     * @date: 2018年12月3日 下午3:45:28
     */
    Page<DockerfileTemplate> getTemplates(String tenantName, String projectId, Pageable pageable);

    /**
     * 根据ID获取模版
     *
     * @param id
     * @return DockerfileTemplate
     * @date: 2018年12月3日 下午3:45:28
     */
    DockerfileTemplate getTemplateById(String id);

    /**
     * 分页获取模版
     *
     * @param pageable
     * @return Page<DockerfileTemplate>
     * @date: 2019年5月20日 下午5:20:59
     */
    Page<DockerfileTemplate> getTemplatesPage(Pageable pageable);

    /**
     * 获取模版类型列表
     *
     * @return
     */
    List<DockerfileType> getTypes();

    /**
     * 分页获取模版
     *
     * @param typeId
     * @param pageable
     * @return Page<DockerfileTemplate>
     * @date: 2019年5月20日 下午5:20:59
     */
    Page<DockerfileTemplate> getTemplatesPage(String typeId, Pageable pageable);

}
