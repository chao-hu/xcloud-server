package com.xxx.xcloud.module.ceph.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * 
 * <p>
 * Description: 对象存储操作接口
 *     创建租户时，调用createCephObjUser创建对象存储的S3用户 
 *     删除租户时，清空租户的所有对象存储数据（用户、桶及桶内对象）
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
public interface CephObjectService {

    /**
     * 创建对象存储网关用户，初始化租户的登录认证
     * @Title: createCephObjUser
     * @Description: 创建对象存储网关用户，初始化租户的登录认证
     * @param tenantName 租户名
     * @return boolean  
     * @throws
     */
    boolean createCephObjUser(String tenantName);

    /**
     * 删除指定租户的对象存储网关用户，清空对象存储数据
     * @Title: destroyCephObjUser
     * @Description: 删除指定租户的对象存储网关用户，清空对象存储数据
     * @param tenantName 租户名
     * @return boolean 
     * @throws
     */
    boolean destroyCephObjUser(String tenantName);

    /**
     * 创建桶
     * @Title: createBucket
     * @Description: 创建桶
     * @param tenantName 租户名称
     * @param bucketName 桶名称
     * @param accessControlList  访问权限（private/publicread/publicreadwrite 分别代表：私有/公共读/公共读写）
     * @return boolean 
     * @throws
     */
    boolean createBucket(String tenantName, String bucketName, String accessControlList);

    /**
     * 删除桶
     * @Title: deleteBucket
     * @Description: 删除桶
     * @param tenantName 租户名
     * @param bucketName 桶名称
     * @return boolean 
     * @throws
     */
    boolean deleteBucket(String tenantName, String bucketName);

    /**
     * 获取桶详情
     * @Title: getBucket
     * @Description: 获取桶详情
     * @param tenantName 租户名
     * @param bucketName 桶名称
     * @return Bucket 
     * @throws
     */
    Bucket getBucket(String tenantName, String bucketName);

    /**
     * 桶列表
     * @Title: listBuckets
     * @Description: 桶列表
     * @param tenantName 租户名
     * @return List<Bucket> 
     * @throws
     */
    List<Bucket> listBuckets(String tenantName);

    /**
     * 对象列表
     * @Title: listObjects
     * @Description: 对象列表
     * @param tenantName 租户名
     * @param bucketName 桶名称
     * @return List<S3ObjectSummary> 
     * @throws
     */
    List<S3ObjectSummary> listObjects(String tenantName, String bucketName);

    /**
     * 删除桶内对象
     * @Title: deleteObject
     * @Description: 删除桶内对象
     * @param tenantName 租户名称
     * @param bucketName 桶名称
     * @param objName 对象名称
     * @return boolean 
     * @throws
     */
    boolean deleteObject(String tenantName, String bucketName, String objName);

    /**
     * 桶内上传对象
     * @Title: upLoad
     * @Description: 桶内上传对象
     * @param file 文件
     * @param tenantName 租户名
     * @param acl 存储类型（STANDARD/GLACIER/STANDARD_IA 分别代表：标准/归档/低频访问）
     * @param storageClass 桶名称
     * @param bucketName 桶名称
     * @return boolean 
     * @throws
     */
    boolean upLoad(MultipartFile file, String tenantName, String acl, String storageClass, String bucketName);

    /**
     * 桶内文件下载
     * @Title: downLoad
     * @Description: 桶内文件下载
     * @param tenantName 租户名
     * @param bucketName 桶名称
     * @param objName 对象名称
     * @param response void 
     * @throws
     */
    void downLoad(String tenantName, String bucketName, String objName, HttpServletResponse response);

}
