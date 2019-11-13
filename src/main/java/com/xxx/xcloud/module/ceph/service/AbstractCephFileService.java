package com.xxx.xcloud.module.ceph.service;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.xxx.xcloud.client.ceph.CephClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.entity.CephFile;
import com.xxx.xcloud.module.ceph.repository.CephFileRepository;
import com.xxx.xcloud.module.ceph.repository.ServiceAndCephFileRepository;
import com.xxx.xcloud.utils.FileUtils;

/**
 * 
 * <p>Description: 文件存储抽象类</p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
public abstract class AbstractCephFileService implements CephFileService {

    private static final String CEPH_FILE_CLIENT = "Ceph文件存储客户端连接异常";
    private static final String CEPH_FILE_PATH_NOT_EXIST = "cephFile指定的文件路径不存在";
    private static final String CEPH_FILE_TENANT_ROOT_CREATE_FAILED = "文件存储租户根路径:%s创建失败";
    public static final String FILE_SPLIT = "/";
    public static final String NAME_SPLIT = "%";

    protected static final Logger log = LoggerFactory.getLogger(AbstractCephFileService.class);

    // protected CephMount cephMount;

    protected static final int MODE = 511;

    @Autowired
    protected CephFileRepository cephFileRepository;

    @Autowired
    protected ServiceAndCephFileRepository serviceAndCephFileRepository;

    @Override
    public boolean createCephFsRoot(String tenantName) {
        checkCephMount();
        try {
            String[] listdir = CephClientFactory.getCephFileClient().listdir(FILE_SPLIT);
            for (String strDir : listdir) {
                if (strDir.equals(tenantName)) {
                    return true;
                }
            }
            CephClientFactory.getCephFileClient().mkdir(FILE_SPLIT + tenantName, MODE);
        } catch (Exception e) {
            String msg = String.format(CEPH_FILE_TENANT_ROOT_CREATE_FAILED, tenantName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, msg);
        }

        return true;
    }

    @Override
    public boolean destroyCephFsRoot(String tenantName){
        checkCephMount();
        try {
            /**
             * delete subFileFolders
             *
             * 修复“如果租户文件夹不为空，则销毁出错”的问题
             *
             */
            FileUtils.delAllFile(XcloudProperties.getConfigMap().get(Global.CEPH_SSH_MOUNTPOINT) + tenantName);
            CephClientFactory.getCephFileClient().rmdir(tenantName);
        } catch (FileNotFoundException e) {
            log.error(CEPH_FILE_PATH_NOT_EXIST, e);
        }

        // delete database(cephFile/serviceAndCephFile)
        Page<CephFile> cephFiles = cephFileRepository.findByTenantName(tenantName, null);
        for (CephFile cephFile : cephFiles) {
            serviceAndCephFileRepository.deleteAllByCephFileId(cephFile.getId());
            cephFileRepository.deleteById(cephFile.getId());
        }

        return true;
    }

    /**
     * @Description 检查cephMount连接的正确性
     * @throws ErrorMessageException
     */
    protected void checkCephMount() {
        try {
            // cephMount.mount("/");//会报重复挂载异常
            CephClientFactory.getCephFileClient().chmod(FILE_SPLIT, MODE);
        } catch (Exception e) {
            log.error(CEPH_FILE_CLIENT, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, CEPH_FILE_CLIENT);
        }
    }
}
