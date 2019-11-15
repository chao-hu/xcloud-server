package com.xxx.xcloud.module.ceph.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.xxx.xcloud.module.ceph.entity.CephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.model.FileInfo;

/**
 * 
 * <p>
 * Description: 文件存储操作接口
 *     创建租户时，需要调用createCephFsRoot方法为租户创建文件存储根目录，目录名称与租户名称相同
 *     删除租户时，需要调用destroyCephFsRoot释放租户文件存储根目录
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日 @
 */
public interface CephFileService {

    /**
     * 根据指定的租户名称，创建租户文件存储根目录
     * @Title: createCephFsRoot
     * @Description:根据指定的租户名称，创建租户文件存储根目录
     * @param tenantName 租户名称
     * @return boolean 
     * @throws
     */
    boolean createCephFsRoot(String tenantName);

    /**
     * 删除租户的文件存储空间
     * @Title: destroyCephFsRoot
     * @Description: 删除租户的文件存储空间
     * @param tenantName 租户名称
     * @return boolean 
     * @throws
     */
    boolean destroyCephFsRoot(String tenantName);

    
    /**
     * 新增卷
     * @Title: add
     * @Description: 新增卷
     * @param cephFile
     * @return CephFile 
     * @throws
     */
    CephFile add(CephFile cephFile);

    /**
     * 卷内增加文件夹
     * @Title: addFolder
     * @Description: 卷内增加文件夹
     * @param cephFileId 存储卷ID
     * @param folderName 文件夹名
     * @param path 文件夹在卷内的相对路径
     * @return boolean 
     * @throws
     */
    boolean addFolder(String cephFileId, String folderName, String path);

    /**
     * 上传文件至卷内
     * @Title: upLoadFile
     * @Description: 上传文件至卷内
     * @param cephFileId 存储卷ID
     * @param path 上传至卷内的相对路径
     * @param file 文件
     * @return boolean 
     * @throws
     */
    boolean upLoadFile(String cephFileId, String path, MultipartFile file);

    /**
     * 卷内文件下载
     * @Title: downLoadFile
     * @Description: 卷内文件下载
     * @param cephFileId 存储卷ID
     * @param path 下载文件在卷内的相对路径
     * @param request
     * @param response
     * @return boolean 
     * @throws
     */
    boolean downLoadFile(String cephFileId, String path, HttpServletRequest request, HttpServletResponse response);

    /**
     * 卷内删除文件/文件夹
     * @Title: removeFile
     * @Description: 卷内删除文件/文件夹
     * @param cephFileId 存储卷ID
     * @param filePath 待删除文件/文件夹在卷内的相对路径（尾部加上文件/文件夹）
     * @return boolean 
     * @throws
     */
    boolean removeFile(String cephFileId, String filePath);

    /**
     * 清空卷
     * @Title: clear
     * @Description: 清空卷
     * @param cephFileId 存储卷ID
     * @return boolean 
     * @throws
     */
    boolean clear(String cephFileId);

    /**
     * 删除卷
     * @Title: delete
     * @Description: 删除卷（如果该文件存储卷正在被服务挂载，则禁止删除）
     * @param cephFileId 存储卷ID
     * @return boolean 
     * @throws
     */
    boolean delete(String cephFileId);

    /**
     * 获取文件存储详情
     * @Title: get
     * @Description: 获取文件存储详情
     * @param cephFileId 存储卷ID
     * @return CephFile 
     * @throws
     */
    CephFile get(String cephFileId);

    /**
     * 获取文件存储列表（模糊查询）
     * @Title: list
     * @Description: 获取文件存储列表（模糊查询）
     * @param tenantName  租户名
     * @param name 存储卷名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<CephFile> 
     * @throws
     */
    Page<CephFile> list(String tenantName, String name, String projectId, Pageable pageable);

    /**
     * 展示文件存储卷下指定path的文件列表
     * @Title: listFiles
     * @Description: 展示文件存储卷下指定path的文件列表
     * @param cephFileId 卷Id
     * @param path 相对路径
     * @return List<FileInfo> 
     * @throws
     */
    List<FileInfo> listFiles(String cephFileId, String path);

    /**
     * 获取指定服务id挂载的文件存储列表
     * @Title: listMountInService
     * @Description: 获取指定服务id挂载的文件存储列表
     * @param serviceId 服务id
     * @return List<ServiceAndCephFile> 
     * @throws
     */
    List<ServiceAndCephFile> listMountInService(String serviceId);

    /**
     * 新增/修改服务挂载文件存储
     * @Title: mountSave
     * @Description: 新增/修改服务挂载文件存储
     * @param serviceCephFile void 
     * @throws
     */
    void mountSave(ServiceAndCephFile serviceCephFile);

    /**
     * 取消文件存储在指定服务上的 挂载
     * @Title: mountCancel
     * @Description: 取消文件存储在指定服务上的 挂载
     * @param serviceId 服务id
     * @param cephFileId void 
     * @throws
     */
    void mountCancel(String serviceId, String cephFileId);

    /**
     * 清空指定服务的文件存储挂载
     * @Title: mountClear
     * @Description: 清空指定服务的文件存储挂载
     * @param serviceId void 
     * @throws
     */
    void mountClear(String serviceId);
}
