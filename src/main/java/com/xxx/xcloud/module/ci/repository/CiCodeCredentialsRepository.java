package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.CiCodeCredentials;
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
public interface CiCodeCredentialsRepository extends JpaRepository<CiCodeCredentials, String> {
    /**
     * 根据ID获取
     * 
     * @param id
     * @return CiCodeCredentials
     * @date: 2019年1月3日 下午6:07:37
     */
    @Query("select c from CiCodeCredentials c where c.id=?1")
    CiCodeCredentials getById(String id);

    /**
     * 根据参数获取对象
     * 
     * @param tenantName
     * @param userName
     * @param registoryAdress
     * @param codeControlType
     * @return CiCodeCredentials
     * @date: 2019年1月3日 下午6:08:32
     */
    @Query("select c from CiCodeCredentials c where c.tenantName=?1 and c.userName=?2 and c.registoryAddress=?3 and c.codeControlType=?4")
    CiCodeCredentials getByUserNameAndRegistoryAdress(String tenantName, String userName, String registoryAdress,
            byte codeControlType);

    /**
     * 根据参数获取对象
     * 
     * @param tenantName
     * @param userName
     * @param registoryAdress
     * @param codeControlType
     * @param isPublic
     * @return CiCodeCredentials
     * @date: 2019年1月3日 下午6:08:32
     */
    @Query("select c from CiCodeCredentials c where c.tenantName=?1 and c.userName=?2 and c.registoryAddress=?3 and c.codeControlType=?4 and c.isPublic=?5")
    CiCodeCredentials getByUserNameAndRegistoryAdress(String tenantName, String userName, String registoryAdress,
            byte codeControlType, byte isPublic);

    /**
     * 根据参数获取对象
     * 
     * @param tenantName
     * @param registoryAdress
     * @param codeControlType
     * @param isPublic
     * @return CiCodeCredentials
     * @date: 2019年1月3日 下午6:08:32
     */
    @Query("select c from CiCodeCredentials c where c.tenantName=?1 and c.registoryAddress=?2 and c.codeControlType=?3 and c.isPublic=?4")
    CiCodeCredentials getByUserNameAndRegistoryAdress(String tenantName, String registoryAdress, byte codeControlType,
            byte isPublic);

    /**
     * 获取租户下的所有认证
     * 
     * @param tenantName
     * @param projectId
     * @param codeControlType
     * @return List<CiCodeCredentials>
     * @date: 2019年1月3日 下午6:09:24
     */
    @Query("select c from CiCodeCredentials c where c.tenantName=?1 and c.projectId=?2 and c.codeControlType=?3 order by c.createTime desc")
    List<CiCodeCredentials> getCodeCredentials(String tenantName, String projectId, byte codeControlType);

    /**
     * 获取租户下的所有认证
     * 
     * @param tenantName
     * @param projectId
     * @param codeControlType
     * @return List<CiCodeCredentials>
     * @date: 2019年1月3日 下午6:09:24
     */
    @Query("select c from CiCodeCredentials c where c.tenantName=?1 and c.codeControlType=?2 order by c.createTime desc")
    List<CiCodeCredentials> getCodeCredentials(String tenantName, byte codeControlType);

    /**
     * 根据用户名获取对应类型认证
     * 
     * @param tenantName
     * @param userName
     * @param codeControlType
     * @return List<CiCodeCredentials>
     * @date: 2019年1月3日 下午6:09:24
     */
    @Query("select c from CiCodeCredentials c where c.tenantName=?1 and c.userName=?2 and c.codeControlType=?3")
    CiCodeCredentials getCodeCredentialsByUserNameAndType(String tenantName, String userName, byte codeControlType);

    /**
     * 根据租户名获取
     * 
     * @param tenantName
     * @return List<CiCodeCredentials>
     * @date: 2019年5月20日 下午5:17:47
     */
    List<CiCodeCredentials> findByTenantName(String tenantName);
}
