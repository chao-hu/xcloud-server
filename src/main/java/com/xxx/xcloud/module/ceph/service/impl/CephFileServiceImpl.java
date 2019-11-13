package com.xxx.xcloud.module.ceph.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xxx.xcloud.client.ceph.CephClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.entity.CephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.pojo.FileInfo;
import com.xxx.xcloud.module.ceph.service.AbstractCephFileService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.utils.FileUtils;
import com.xxx.xcloud.utils.SftpUtil;
import com.xxx.xcloud.utils.StringUtils;

/**
 * 
 * <p>
 * Description: 文件存储功能实现类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Service
public class CephFileServiceImpl extends AbstractCephFileService {

    private static final String TENANT_NOT_EXIST = "指定的租户:%s不存在";
    private static final String CEPH_FILE_ALREADY_EXIST = "文件存储卷:%s已经存在";
    private static final String CEPH_FILE_NOT_EXIST = "文件存储卷不存在";
    private static final String CEPH_FILE_NAME_ILLEGAL = "文件存储卷名称不合法，应由5到15位字母数字和下划线组成，以字母开头";
    private static final String CEPH_FILE_SIZE_ILLEGAL = "文件存储卷大小:%s不合法";
    private static final String CEPH_FILE_CREATE_FAILED = "租户:%s创建卷:%s异常";
    private static final String CEPH_FILE_DELETE_FAILED = "文件存储卷:%s删除失败";
    private static final String CEPH_FILE_MOUNTED = "文件存储卷:%s已被挂载，不能删除";
    private static final String CEPH_FILE_UPLOAD_FAILED = "文件上传失败";
    private static final String CEPH_FILE_DOWNLOAD_FAILED = "下载文件异常";
    private static final String CEPH_FILE_DOWNLOAD_NOT_EXIST = "在存储卷内不存在";
    private static final String CEPH_FILE_PATH_EMPTY = "文件路径为空";
    private static final String CEPH_FILE_CLEAR_FAILED = "文件存储卷:%s清空失败";
    private static final String CEPH_FILE_GET_RESOURCE_FAILED = "获取卷:%s已使用容量失败";
    private static final String CEPH_FILE_FOLDER_ILLEGAL = "文件夹前缀不存在或不是文件夹";
    private static final String CEPH_FILE_FOLDER_ALREADY_EXIST = "文件夹已经存在";
    private static final String CEPH_FILE_EMPTY = "上传的文件为空";
    private static final double HEX = 1024d;

    public static final int NAME_MIN_LENGTH = 4;
    public static final int NAME_MAX_LENGTH = 15;
    public static final double CEPH_FILE_MIN_SIZE = 0d;

    private static final Logger LOG = LoggerFactory.getLogger(CephFileServiceImpl.class);

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CephFile add(String tenantName, String createdBy, String projectId, String name, double size,
            String description) {
        checkCephMount();

        // check if tenant exists
        Tenant tenant = tenantService.findTenantByTenantName(tenantName);
        if (null == tenant) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                    String.format(TENANT_NOT_EXIST, tenantName));
        }

        // check if the cephfile exists
        if (cephFileRepository.findByNameAndTenantName(name, tenantName) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_EXIST, String.format(CEPH_FILE_ALREADY_EXIST, name));
        }

        // check name
        if (StringUtils.isEmpty(name) || !StringUtils.isAccountName(name, NAME_MIN_LENGTH, NAME_MAX_LENGTH)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_FILE_NAME_ILLEGAL);
        }

        // check size
        if (size <= CEPH_FILE_MIN_SIZE) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM,
                    String.format(CEPH_FILE_SIZE_ILLEGAL, size));
        }

        // create folder
        try {
            CephClientFactory.getCephFileClient().chdir(FILE_SPLIT + tenantName);
            CephClientFactory.getCephFileClient().mkdir(name, MODE);
            CephClientFactory.getCephFileClient().chmod(FILE_SPLIT + tenantName, MODE);
        } catch (Exception e) {
            String msg = String.format(CEPH_FILE_CREATE_FAILED, tenantName, name);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CREATE, msg);
        }

        CephFile cephFile = CephFile.builder().withCreateTime(new Date()).withDescription(description).withName(name)
                .withTenantName(tenantName).withSize(size).withUpdateTime(new Date()).withCreateBy(createdBy)
                .withProjectId(projectId).build();

        cephFileRepository.save(cephFile);

        return cephFile;
    }

    @Override
    public boolean addFolder(String cephFileId, String folderName, String path) {
        checkCephMount();

        if (StringUtils.isEmpty(folderName)
                || !StringUtils.isAccountName(folderName, NAME_MIN_LENGTH, NAME_MAX_LENGTH)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_FILE_NAME_ILLEGAL);
        }

        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (cephFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        File file = new File(XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path);
        if (!file.exists() || !file.isDirectory()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, path + CEPH_FILE_FOLDER_ILLEGAL);
        }

        file = new File(XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path + FILE_SPLIT + folderName);

        // check if the folder exists
        if (file.exists()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_FILE_FOLDER_ALREADY_EXIST);
        }

        return file.mkdir();
    }

    @Override
    public boolean upLoadFile(String cephFileId, String path, MultipartFile multipartFile) {
        checkCephMount();

        // check cephFile
        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (cephFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        // check file
        if (multipartFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_FILE_EMPTY);
        }

        // check path
        LOG.info("------path-------" + XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT)
                + cephFile.getTenantName() + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path);
        File file = new File(XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path);
        if (!file.exists() || !file.isDirectory()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, path + CEPH_FILE_FOLDER_ILLEGAL);
        }

        File[] existFiles = file.listFiles();
        Set<String> set = new HashSet<String>();
        for (File oneFile : existFiles) {
            if (!oneFile.isDirectory()) {
                set.add(oneFile.getName());
            }
        }

        String filePath = null;
        if (StringUtils.isEmpty(path)) {
            filePath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                    + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + multipartFile.getOriginalFilename();
        } else {
            filePath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                    + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path + FILE_SPLIT
                    + multipartFile.getOriginalFilename();
        }
        LOG.info("----------filePath-------------" + filePath);
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filePath)))) {
            out.write(multipartFile.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_UPLOAD, CEPH_FILE_UPLOAD_FAILED);
        }

        return true;
    }

    @Override
    public boolean downLoadFile(String cephFileId, String path, HttpServletRequest request,
            HttpServletResponse response) {

        // check cephFile
        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (cephFile == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        // check path
        if (StringUtils.isEmpty(path)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_FILE_PATH_EMPTY);
        }

        String wholePath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path;

        File file = new File(wholePath);
        if (!file.exists()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, path + CEPH_FILE_DOWNLOAD_NOT_EXIST);
        }

        response.setContentType(request.getServletContext().getMimeType(wholePath) + ";charset=utf-8");
        try {
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(path.substring(path.lastIndexOf(FILE_SPLIT) + 1), "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        response.setCharacterEncoding("UTF-8");

        try (InputStream myStream = new FileInputStream(wholePath)) {
            IOUtils.copy(myStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error(CEPH_FILE_DOWNLOAD_FAILED, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DOWNLOAD, CEPH_FILE_DOWNLOAD_FAILED);
        }
        return true;
    }

    @Override
    public boolean removeFile(String cephFileId, String filePath) {

        checkCephMount();

        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (null == cephFile) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        if (StringUtils.isEmpty(filePath)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_FILE_PATH_EMPTY);
        }

        String fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + filePath;
        File file = new File(fullPath);
        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.delAllFile(fullPath);
            }
            file.delete();
        } else {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        return true;
    }

    @Override
    public boolean clear(String cephFileId) {
        checkCephMount();

        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (null == cephFile) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        String fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName();

        if (!FileUtils.delAllFile(fullPath)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DELETE,
                    String.format(CEPH_FILE_CLEAR_FAILED, cephFile.getName()));
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String cephFileId) {

        checkCephMount();

        // check if the cephFile exists
        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (null == cephFile) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        // check if the cephFile is occupied
        List<ServiceAndCephFile> serviceAndCephFiles = serviceAndCephFileRepository.findByCephFileId(cephFileId);
        if (serviceAndCephFiles.size() > 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_OCCUPIED,
                    String.format(CEPH_FILE_MOUNTED, cephFile.getName()));
        }

        try {
            // delete folders
            FileUtils.delAllFile(XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT)
                    + cephFile.getTenantName() + FILE_SPLIT + cephFile.getName());
            CephClientFactory.getCephFileClient().chdir(FILE_SPLIT + cephFile.getTenantName());
            CephClientFactory.getCephFileClient().rmdir(cephFile.getName());

            // delete cephfile
            cephFileRepository.deleteById(cephFileId);
        } catch (Exception e) {
            String msg = String.format(CEPH_FILE_DELETE_FAILED, cephFile.getName());
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DELETE, msg);
        }

        return true;
    }

    @Override
    public CephFile get(String cephFileId) {
        CephFile cephFile = null;
        Optional<CephFile> cephFileOptional = cephFileRepository.findById(cephFileId);
        if (cephFileOptional.isPresent()) {
            cephFile = cephFileOptional.get();
        }
        if (null == cephFile) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        String fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                + FILE_SPLIT + cephFile.getName();

        cephFile.setUsed(fileSize(fullPath));


        return cephFile;
    }

    @Override
    public Page<CephFile> list(String tenantName, String name, String projectId, Pageable pageable) {
        Page<CephFile> list = null;
        if (StringUtils.isNotEmpty(name)) {
            name = NAME_SPLIT + name + NAME_SPLIT;
            if (StringUtils.isNotEmpty(projectId)) {
                list = cephFileRepository.findByTenantNameAndNameAndProjectId(tenantName, name, projectId, pageable);
            } else {
                list = cephFileRepository.findByTenantNameAndName(tenantName, name, pageable);
            }
        } else {
            if (StringUtils.isNotEmpty(projectId)) {
                list = cephFileRepository.findByTenantNameAndProjectId(tenantName, projectId, pageable);
            } else {
                list = cephFileRepository.findByTenantName(tenantName, pageable);
            }
        }

        // cal usedSize of every cephfile
        List<CephFile> cephFiles = list.getContent();
        for (CephFile cephFile : cephFiles) {
            String fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                    + FILE_SPLIT + cephFile.getName();
            cephFile.setUsed(fileSize(fullPath));
        }

        return list;
    }

    @Override
    public List<ServiceAndCephFile> listMountInService(String serviceId) {
        List<ServiceAndCephFile> serviceAndCephFiles = serviceAndCephFileRepository.findByServiceId(serviceId);
        for (ServiceAndCephFile serviceAndCephFile : serviceAndCephFiles) {
            CephFile cephFile = cephFileRepository.findById(serviceAndCephFile.getCephFileId()).get();
            try {
                String fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT)
                        + cephFile.getTenantName() + FILE_SPLIT + cephFile.getName();
                cephFile.setUsed(fileSize(fullPath));
            } catch (ErrorMessageException e) {
                String msg = String.format(CEPH_FILE_GET_RESOURCE_FAILED, cephFile.getName());
                LOG.error(msg, e);
                cephFile.setUsed(CEPH_FILE_MIN_SIZE);
            }
            serviceAndCephFile.setCephFile(cephFile);
        }

        return serviceAndCephFiles;
    }

    @Override
    public void mountSave(ServiceAndCephFile serviceCephFile) {
        serviceAndCephFileRepository.save(serviceCephFile);
        return;
    }

    @Override
    public void mountCancel(String serviceId, String cephFileId) {
        serviceAndCephFileRepository.deleteByCephFileIdAndServiceId(cephFileId, serviceId);
        return;
    }

    @Override
    public void mountClear(String serviceId) {
        serviceAndCephFileRepository.deleteAllByServiceId(serviceId);
        return;
    }

    @Override
    public List<FileInfo> listFiles(String cephFileId, String path) {

        // check if the cephFile exists
        CephFile cephFile = cephFileRepository.findById(cephFileId).get();
        if (null == cephFile) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }

        List<FileInfo> list = null;
        String fullPath = null;
        if (StringUtils.isNotEmpty(path)) {
            fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                    + FILE_SPLIT + cephFile.getName() + FILE_SPLIT + path;
        } else {
            fullPath = XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + cephFile.getTenantName()
                    + FILE_SPLIT + cephFile.getName();
        }
        File file = new File(fullPath);
        if (file.exists() && file.isDirectory()) {
            list = SftpUtil.listFileInfo(fullPath);
        }

        return list;
    }

    private double fileSize(String filePath) {
        long hasUse = 0;
        if (new File(filePath).exists()) {
            hasUse = SftpUtil.getHasUsed(filePath);
        } else {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_FILE_NOT_EXIST);
        }
        double hasUseDouble = hasUse / HEX / HEX / HEX;
        return hasUseDouble;
    }

}
