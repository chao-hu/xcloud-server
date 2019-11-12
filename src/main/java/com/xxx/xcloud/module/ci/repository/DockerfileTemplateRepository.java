package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.DockerfileTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author mengaijun
 *
 * @date: 2018年12月7日 下午2:24:49
 */
@Repository
public interface DockerfileTemplateRepository extends JpaRepository<DockerfileTemplate, String> {
    /**
     * 获取租户下所有
     *
     * @param tenantName
     * @return List<DockerfileTemplate>
     * @date: 2019年1月3日 下午6:12:43
     */
    @Query("select d from DockerfileTemplate d where d.tenantName=?1 order by d.createTime desc")
    List<DockerfileTemplate> getAll(String tenantName);

    /**
     * 租户下 根据名称查询
     *
     * @param tenantName
     * @param name
     * @return List<DockerfileTemplate>
     * @date: 2019年1月3日 下午6:12:50
     */
    @Query("select d from DockerfileTemplate d where d.tenantName=?1 and d.dockerfileName=?2 order by d.createTime desc")
    List<DockerfileTemplate> getAllByName(String tenantName, String name);

    /**
     * 租户下 根据名称查询
     *
     * @param tenantName
     * @param name
     * @param projectId
     * @return List<DockerfileTemplate>
     * @date: 2019年1月3日 下午6:12:50
     */
    @Query("select d from DockerfileTemplate d where d.tenantName=?1 and d.dockerfileName=?2 and d.projectId=?3 order by d.createTime desc")
    List<DockerfileTemplate> getAllByNameAndProjectId(String tenantName, String name, String projectId);

    /**
     * 租户下获取所有(分页)
     *
     * @param tenantName
     * @param projectId
     * @param request
     * @return Page<DockerfileTemplate>
     * @date: 2019年1月3日 下午6:12:57
     */
    @Query("select d from DockerfileTemplate d where d.tenantName=?1 and d.projectId=?2 order by d.createTime desc")
    Page<DockerfileTemplate> getAll(String tenantName, String projectId, Pageable request);

    /**
     * 租户下获取所有(分页)
     *
     * @param tenantName
     * @param request
     * @return Page<DockerfileTemplate>
     * @date: 2019年1月3日 下午6:12:57
     */
    @Query("select d from DockerfileTemplate d where d.tenantName=?1 order by d.createTime desc")
    Page<DockerfileTemplate> getAll(String tenantName, Pageable request);

    /**
     * 分页查询所有
     *
     * @param request
     * @return Page<DockerfileTemplate>
     * @date: 2019年1月3日 下午6:12:57
     */
    @Override
    Page<DockerfileTemplate> findAll(Pageable request);

    /**
     * find
     * 
     * @param typeId
     * @param request
     * @return Page<DockerfileTemplate>
     * @date: 2019年9月3日 下午4:02:59
     */
    Page<DockerfileTemplate> findByTypeId(String typeId, Pageable request);
}
