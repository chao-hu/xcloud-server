package com.xxx.xcloud.module.image.repository;

import com.xxx.xcloud.module.image.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Map;

/**
 * @author mengaijun
 * @date: 2018年12月7日 下午2:40:51
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
    /**
     * 获取租户下所有镜像
     *
     * @param tenantName
     * @return List<Image>
     * @date: 2019年1月3日 下午6:42:29
     */
    @Query("select i from Image i where i.tenantName=?1  order by i.createTime desc")
    List<Image> getAll(String tenantName);

    /**
     * 获取租户下所有镜像
     *
     * @param tenantName
     * @return List<Image>
     * @date: 2019年1月3日 下午6:42:29
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageType = 2  order by i.createTime desc")
    List<Image> getAllPrivate(String tenantName);

    /**
     * 获取租户下所有镜像
     *
     * @param tenantName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:42:29
     */
    @Query("select i from Image i where i.tenantName=?1 and i.projectId=?2 and i.imageType = 2  order by i.createTime desc")
    List<Image> getAllPrivate(String tenantName, String projectId);

    /**
     * 获取租户下所有镜像
     *
     * @param tenantName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:42:29
     */
    @Query("select i from Image i where i.tenantName=?1 and i.projectId=?2  order by i.createTime desc")
    List<Image> getAll(String tenantName, String projectId);

    /**
     * 分页获取租户下所有镜像
     *
     * @param tenantName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年1月3日 下午6:42:48
     */
    @Query("select i from Image i where i.tenantName=?1  order by i.createTime desc")
    Page<Image> getAll(String tenantName, Pageable pageable);

    /**
     * 根据ID获取
     *
     * @param id
     * @return Image
     * @date: 2019年1月3日 下午6:43:04
     */
    @Query("select i from Image i where i.id=?1 ")
    Image getById(String id);

    /**
     * 根据镜像名称获取
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:05
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName=?2 and i.projectId=?3  order by i.createTime desc")
    List<Image> getByName(String tenantName, String imageName, String projectId);

    /**
     * 根据镜像名称获取
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @param pageable
     * @return Page<Image>
     * @date: 2019年1月3日 下午6:44:05
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName=?2 and i.projectId=?3  order by i.createTime desc")
    Page<Image> getByName(String tenantName, String imageName, String projectId, Pageable pageable);

    /**
     * 根据镜像名称获取
     *
     * @param tenantName
     * @param imageName
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:05
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName=?2  order by i.createTime desc")
    List<Image> getByName(String tenantName, String imageName);

    /**
     * 根据镜像名称获取(分页)
     *
     * @param tenantName
     * @param imageName
     * @param pageable
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:19
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName=?2  order by i.createTime desc")
    Page<Image> getByName(String tenantName, String imageName, Pageable pageable);

    /**
     * 根据镜像名称获取(like)
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:44
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName like ?2 and i.projectId=?3 and i.imageType = 2  order by i.createTime desc")
    List<Image> getByNameLikePrivate(String tenantName, String imageName, String projectId);

    /**
     * 根据镜像名称获取(like)
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:44
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName like ?2 and i.projectId=?3  order by i.createTime desc")
    List<Image> getByNameLike(String tenantName, String imageName, String projectId);

    /**
     * 根据镜像名称获取(like)
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:44
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName like ?2  order by i.createTime desc")
    List<Image> getByNameLike(String tenantName, String imageName);

    /**
     * 根据镜像名称获取(like)
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月3日 下午6:44:44
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName like ?2 and i.imageType = 2  order by i.createTime desc")
    List<Image> getByNameLikePrivate(String tenantName, String imageName);

    /**
     * 根据镜像名称获取(like, 分页)
     *
     * @param tenantName
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年1月3日 下午6:45:07
     */
    @Query("select i from Image i where i.tenantName=?1 and i.imageName like ?2  order by i.createTime desc")
    Page<Image> getByNameLike(String tenantName, String imageName, Pageable pageable);

    /**
     * 根据名称查询公有镜像
     *
     * @param imageName
     * @return List<Image>
     * @date: 2019年1月3日 下午6:45:45
     */
    @Query("select i from Image i where i.imageName like ?1 and i.imageType=1  order by i.createTime desc")
    List<Image> getPublicImagesByNameLike(String imageName);

    /**
     * 根据名称查询公有镜像(分页)
     *
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年1月3日 下午6:46:03
     */
    @Query("select i from Image i where i.imageName like ?1 and i.imageType=1  order by i.createTime desc")
    Page<Image> getPublicImagesByNameLike(String imageName, Pageable pageable);

    /**
     * 更新image描述信息
     *
     * @param description
     * @param id
     * @date: 2019年7月3日 下午5:42:43
     */
    @Modifying
    @Query("update Image i set i.description = ?1 where i.id = ?2")
    void updateImageDescreption(String description, String id);

    /**
     * 修改镜像类型
     *
     * @param imageType
     * @param id
     * @date: 2019年7月3日 下午5:50:37
     */
    @Modifying
    @Query("update Image i set i.imageType = ?1 where i.id = ?2")
    void updateImageType(byte imageType, String id);

    /**
     * 根据名称查询公有镜像
     *
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年1月3日 下午6:46:03
     */
    @Query("select i from Image i where i.imageName = ?1 and i.imageType=1  order by i.createTime desc")
    Page<Image> getPublicImagesByName(String imageName, Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param imageName
     * @return List<Image>
     * @date: 2019年9月3日 下午4:03:24
     */
    List<Image> findByTenantNameAndImageName(String tenantName, String imageName);

    /**
     * find
     *
     * @param tenantName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年9月3日 下午4:03:30
     */
    Page<Image> findByTenantName(String tenantName, Pageable pageable);

    /**
     * find
     *
     * @param imageType
     * @param pageable
     * @return Page<Image>
     * @date: 2019年9月3日 下午4:03:34
     */
    Page<Image> findByImageType(byte imageType, Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param imageType
     * @param pageable
     * @return Page<Image>
     * @date: 2019年9月3日 下午4:03:39
     */
    Page<Image> findByTenantNameOrImageType(String tenantName, byte imageType, Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param imageType
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年9月3日 下午4:03:43
     */
    @Query("select i from Image i where (i.tenantName = ?1 or i.imageType=?2) and i.imageName like ?3")
    Page<Image> getByTenantNameOrImageTypeAndImageNameLike(String tenantName, byte imageType, String imageName,
            Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年9月3日 下午4:03:48
     */
    Page<Image> findByTenantNameAndImageNameLike(String tenantName, String imageName, Pageable pageable);

    /**
     * find
     *
     * @param imageType
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年9月3日 下午4:03:53
     */
    Page<Image> findByImageTypeAndImageNameLike(byte imageType, String imageName, Pageable pageable);

    /**
     * find
     *
     * @param imageName
     * @param imageType
     * @return List<Image>
     * @date: 2019年9月3日 下午4:04:00
     */
    List<Image> findByImageNameAndImageType(String imageName, byte imageType);

    /**
     * find for update
     *
     * @param id
     * @return Image
     * @date: 2019年9月3日 下午4:04:05
     */
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Image i where i.id=?1 ")
    Image getByIdForUpdate(String id);

    // Page<Image> findByTenantNameAndProjectIdAndImageNameLike(String
    // tenantName, String imageName, Pageable pageable);
    //
    // Page<Image> findByTenantNameAndImageNameLike(String tenantName, String
    // imageName, Pageable pageable);

    /**
     * delete
     *
     * @param tenantName void
     * @date: 2019年9月3日 下午4:04:16
     */
    public void deleteByTenantName(String tenantName);

    /**
     * find
     *
     * @return List<Map < String, Object>>
     * @date: 2019年9月3日 下午4:04:24
     */
    @Query(value = "select i.id imageId, i.image_name imageName, "
            + "s.id serviceId, s.service_name serviceName, s.tenant_name tenantName, "
            + "v.code_base_Name codeBaseName " + "from bdos_service s "
            + "inner join bdos_image_version v on s.image_version_id = v.id "
            + "inner join bdos_image i on v.image_id = i.id", nativeQuery = true)
    List<Map<String, Object>> getImagesUsed();

}
