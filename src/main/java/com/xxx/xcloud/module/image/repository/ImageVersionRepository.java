package com.xxx.xcloud.module.image.repository;

import com.xxx.xcloud.module.image.entity.ImageVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年7月5日 下午3:03:47
 */
@Repository
public interface ImageVersionRepository extends JpaRepository<ImageVersion, String> {
    /**
     * find
     *
     * @param imageId
     * @param imageVersion
     * @return List<ImageVersion>
     * @date: 2019年9月3日 下午4:04:36
     */
    List<ImageVersion> findByImageIdAndImageVersion(String imageId, String imageVersion);

    /**
     * find
     *
     * @param imageId
     * @return List<ImageVersion>
     * @date: 2019年9月3日 下午4:04:41
     */
    List<ImageVersion> findByImageId(String imageId);

    /**
     * find
     *
     * @param imageId
     * @param pageable
     * @return Page<ImageVersion>
     * @date: 2019年9月3日 下午4:04:45
     */
    Page<ImageVersion> findByImageId(String imageId, Pageable pageable);

    /**
     * find
     *
     * @param imageId
     * @param imageType
     * @param pageable
     * @return Page<ImageVersion>
     * @date: 2019年9月3日 下午4:04:49
     */
    Page<ImageVersion> findByImageIdAndImageType(String imageId, byte imageType, Pageable pageable);

    /**
     * find
     *
     * @param imageId
     * @return Long
     * @date: 2019年9月3日 下午4:04:53
     */
    Long countByImageId(String imageId);

    /**
     * find
     *
     * @param imageId
     * @param imageType
     * @return Long
     * @date: 2019年9月3日 下午4:05:02
     */
    Long countByImageIdAndImageType(String imageId, byte imageType);

    /**
     * find
     *
     * @param ciType
     * @return List<ImageVersion>
     * @date: 2019年9月3日 下午4:05:06
     */
    List<ImageVersion> findByCiTypeAndCodeBaseNameIsNull(byte ciType);

    /**
     * update
     *
     * @param imageByte
     * @param imageId   void
     * @date: 2019年9月3日 下午4:05:11
     */
    @Modifying
    @Query("update ImageVersion i set i.imageType = ?1 where i.id = ?2")
    void updateImageVersionType(byte imageByte, String imageId);

    /**
     * delete
     *
     * @param imageId void
     * @date: 2019年9月3日 下午4:05:20
     */
    public void deleteByImageId(String imageId);

}
