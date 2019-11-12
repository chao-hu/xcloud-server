package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.Ci;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mengaijun
 * @date: 2018年12月7日 下午2:14:32
 */
@Repository
public interface CiRepository extends JpaRepository<Ci, String> {
    /**
     * 获取租户下所有对象
     *
     * @param tenantName
     * @return List<Ci>
     * @date: 2019年1月3日 下午6:10:40
     */
    @Query("select c from Ci c where c.tenantName=?1 order by c.createTime desc")
    List<Ci> getAll(String tenantName);

    /**
     * 获取租户下所有对象(分页)
     *
     * @param tenantName
     * @param projectId
     * @param pageable
     * @return Page<Ci>
     * @date: 2019年1月3日 下午6:10:57
     */
    @Query("select c from Ci c where c.tenantName=?1 and c.projectId=?2 order by c.createTime desc")
    Page<Ci> getAll(String tenantName, String projectId, Pageable pageable);

    /**
     * 查询
     *
     * @param id
     * @return Ci
     * @date: 2019年1月3日 下午6:11:19
     */
    @Query("select c from Ci c where c.id=?1")
    Ci getById(String id);

    /**
     * 查询
     *
     * @param id
     * @return Ci
     * @date: 2019年1月3日 下午6:11:19
     */
    @Query("select c from Ci c where c.id=?1")
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Ci getByIdForUpdate(String id);

    /**
     * 根据名称和类型获取记录
     *
     * @param tenantName
     * @param ciName
     * @return Ci
     * @date: 2019年1月3日 下午6:11:27
     */
    @Query("select c from Ci c where c.tenantName=?1 and c.ciName=?2")
    Ci getByCiName(String tenantName, String ciName);

    /**
     * 根据名称和类型获取记录
     *
     * @param tenantName
     * @param ciName
     * @param ciType
     * @return Ci
     * @date: 2019年1月3日 下午6:11:27
     */
    @Query("select c from Ci c where c.tenantName=?1 and c.ciName=?2 and c.ciType=?3")
    Ci getByCiNameAndType(String tenantName, String ciName, byte ciType);

    /**
     * 获取表达式非空记录
     *
     * @return List<Ci>
     * @date: 2019年1月3日 下午6:12:07
     */
    @Query("select c from Ci c where c.cron is not null")
    List<Ci> getAllCronIsNotNull();

    /**
     * 根据镜像名称和版本获取构建记录
     *
     * @param tenantName
     *            租户
     * @param imageName
     *            镜像名
     * @param imageVersion
     *            镜像版本
     * @return Ci 构建对象
     * @date: 2019年1月7日 下午2:40:11
     */
    @Query("select c from Ci c where c.tenantName=?1 and c.imageName=?2 and c.imageVersion=?3")
    Ci getByCiImageNameAndVersion(String tenantName, String imageName, String imageVersion);

    /**
     * find
     * 
     * @param tenantName
     * @param projectId
     * @param ciName
     * @param pageable
     * @return Page<Ci>
     * @date: 2019年1月21日 上午10:39:07
     */
    Page<Ci> findByTenantNameAndProjectIdAndCiNameLikeOrderByCreateTimeDesc(String tenantName, String projectId,
            String ciName, Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param ciName
     * @param pageable
     * @return Page<Ci>
     * @date: 2019年1月21日 上午10:39:18
     */
    Page<Ci> findByTenantNameAndCiNameLikeOrderByCreateTimeDesc(String tenantName, String ciName, Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param projectId
     * @param ciType
     * @param ciName
     * @param pageable
     * @return Page<Ci>
     * @date: 2019年1月21日 上午10:39:26
     */
    Page<Ci> findByTenantNameAndProjectIdAndCiTypeAndCiNameLikeOrderByCreateTimeDesc(String tenantName,
            String projectId, byte ciType, String ciName, Pageable pageable);

    /**
     * find
     *
     * @param tenantName
     * @param ciType
     * @param ciName
     * @param pageable
     * @return Page<Ci>
     * @date: 2019年1月21日 上午10:39:43
     */
    Page<Ci> findByTenantNameAndCiTypeAndCiNameLikeOrderByCreateTimeDesc(String tenantName, byte ciType, String ciName,
            Pageable pageable);

    /**
     * 根据租户名删除
     *
     * @param tenantName
     *            void
     * @date: 2019年5月20日 下午5:18:28
     */
    void deleteByTenantName(String tenantName);

    /**
     * 根据租户名查询
     *
     * @param tenantName
     * @return List<Ci>
     * @date: 2019年5月20日 下午5:18:45
     */
    List<Ci> findByTenantName(String tenantName);

    /**
     * 根据状态查询构建任务
     *
     * @param status
     * @return List<Ci>
     * @date: 2019年6月19日 上午9:47:56
     */
    List<Ci> findByConstructionStatus(byte status);

    /**
     * find
     * 
     * @param tenantName
     * @param projectId
     * @return List<Ci>
     * @date: 2019年9月3日 下午4:01:58
     */
    List<Ci> findByTenantNameAndProjectId(String tenantName, String projectId);

    /**
     * find
     * 
     * @param tenantNameSet
     * @param imageNameSet
     * @param date
     * @return List<Map<String,Object>>
     * @date: 2019年9月3日 下午4:02:13
     */
    @Query(value = "SELECT c.tenant_name tenantName, c.image_name imageName,  count(*) constructionTotal, "
            + "sum(case r.construction_result when 4 then 1 else 0 end) constructionFailTotal, "
            + "sum(case r.construction_result when 3 then 1 else 0 end) constructionOkTotal, "
            + "convert(sum(case r.construction_result when 3 then 1 else 0 end)/count(*),decimal(10,4)) constructionOkRate "
            + "FROM "
            + "bdos_ci c inner join " + "`bdos_ci_record` r on c.id=r.ci_id "
            + "where c.tenant_name in ?1 and c.image_name in ?2 and r.construction_time > ?3  "
            + "group by c.tenant_name, c.image_name "
            + ";", nativeQuery = true)
    List<Map<String, Object>> getCiStatistics(Set<String> tenantNameSet, Set<String> imageNameSet, Date date);

    /**
     * find
     * 
     * @param tenantNameSet
     * @param imageNameSet
     * @return List<Map<String,Object>>
     * @date: 2019年9月3日 下午4:02:20
     */
    @Query(value = "SELECT c.tenant_name tenantName, c.image_name imageName,  count(*) constructionTotal, "
            + "sum(case r.construction_result when 4 then 1 else 0 end) constructionFailTotal, "
            + "sum(case r.construction_result when 3 then 1 else 0 end) constructionOkTotal, "
            + "convert(sum(case r.construction_result when 3 then 1 else 0 end)/count(*),decimal(10,4)) constructionOkRate "
            + "FROM " + "bdos_ci c inner join " + "`bdos_ci_record` r on c.id=r.ci_id "
            + "where c.tenant_name in ?1 and c.image_name in ?2 " + "group by c.tenant_name, c.image_name "
            + ";", nativeQuery = true)
    List<Map<String, Object>> getCiStatistics(Set<String> tenantNameSet, Set<String> imageNameSet);

    /**
     * find
     * 
     * @param serviceId
     * @return Map<String,Object>
     * @date: 2019年9月3日 下午4:02:27
     */
    @Query(value = "select i.id imageId, i.image_name imageName, "
            + "s.id serviceId, s.service_name serviceName, s.tenant_name tenantName, "
            + "v.code_base_Name codeBaseName " + "from bdos_service s "
            + "inner join bdos_image_version v on s.image_version_id = v.id "
            + "inner join bdos_image i on v.image_id = i.id " + "where s.id=?1 ", nativeQuery = true)
    Map<String, Object> getImagesUsed(String serviceId);

    /**
     * 根据租户名和镜像名查找
     * 
     * @param tenantName
     * @param imageName
     * @return List<Ci>
     * @date: 2019年9月2日 下午5:23:12
     */
    @Query(value = "select id from bdos_ci c where c.tenant_name=?1 and c.image_name=?2", nativeQuery = true)
    List<String> getCiIdsByTenantNameAndImageName(String tenantName, String imageName);

    /**
     * find
     * 
     * @param ciIds
     * @return Map<String,Object>
     * @date: 2019年9月3日 下午4:02:34
     */
    @Query(value = "SELECT count(*) constructionTotal, "
            + "sum(case r.construction_result when 4 then 1 else 0 end) constructionFailTotal, "
            + "sum(case r.construction_result when 3 then 1 else 0 end) constructionOkTotal, "
            + "convert(sum(case r.construction_result when 3 then 1 else 0 end)/count(*),decimal(10,4)) constructionOkRate "
            + "FROM " + "`bdos_ci_record` r " + "where r.ci_id in ?1  "
            + ";", nativeQuery = true)
    Map<String, Object> getCiStatistics(List<String> ciIds);

    /**
     * find
     * 
     * @param ciIds
     * @return Map<String,Object>
     * @date: 2019年9月3日 下午4:02:42
     */
    @Query(value = "SELECT construction_result constructionResult, construction_time constructionTime "
            + "FROM " + "`bdos_ci_record` r " + "where r.ci_id in ?1 order by construction_result desc "
            + " limit 1;", nativeQuery = true)
    Map<String, Object> getNewstCiStatus(List<String> ciIds);

    /**
     * find
     * 
     * @param ciIds
     * @param date
     * @return Map<String,Object>
     * @date: 2019年9月3日 下午4:02:50
     */
    @Query(value = "SELECT count(*) constructionTotal "
            // + "sum(case r.construction_result when 4 then 1 else 0 end)
            // constructionFailTotal, "
            // + "sum(case r.construction_result when 3 then 1 else 0 end)
            // constructionOkTotal, "
            // + "convert(sum(case r.construction_result when 3 then 1 else 0
            // end)/count(*),decimal(10,4)) constructionOkRate "
            + "FROM " + "`bdos_ci_record` r " + "where r.ci_id in ?1 and r.construction_time > ?2  "
            + ";", nativeQuery = true)
    Map<String, Object> getCiStatistics(List<String> ciIds, Date date);
}
