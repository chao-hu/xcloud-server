package com.xxx.xcloud.module.sonar.repository;

import com.xxx.xcloud.module.sonar.entity.QualityProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 
 * @author mengaijun
 *
 * @date: 2018年12月7日 下午2:19:49
 */
@Repository
public interface QualityProfileRepository extends JpaRepository<QualityProfile, String> {
    /**
     * get
     * 
     * @param id
     * @return QualityProfileLocal
     * @date: 2019年1月3日 下午6:58:55
     */
    @Query("select s from QualityProfile s where s.id=?1")
    QualityProfile getById(String id);

    /**
     * get
     * 
     * @param key
     * @return QualityProfileLocal
     * @date: 2019年1月3日 下午6:59:04
     */
    @Query("select s from QualityProfile s where s.key=?1")
    QualityProfile getByKey(String key);

    /**
     * get
     * 
     * @param createdBy
     * @return List<QualityProfileLocal>
     * @date: 2019年1月3日 下午6:59:15
     */
    @Query("select s from QualityProfile s where s.tenantName in ?1")
    List<QualityProfile> getAllQualityProfiles(List<String> createdBy);

    /**
     * 根据名称和语言查询规则集
     * 
     * @param name
     *            名称
     * @param language
     *            语言
     * @return QualityProfileLocal
     * @date: 2019年1月3日 下午6:59:24
     */
    @Query("select s from QualityProfile s where s.name=?1 and s.language=?2")
    QualityProfile getQualityProfileByName(String name, String language);
}
