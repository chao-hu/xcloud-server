package com.xxx.xcloud.module.ceph.service.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ceph.rados.IoCTX;
import com.ceph.rbd.Rbd;
import com.ceph.rbd.RbdImage;
import com.ceph.rbd.jna.RbdSnapInfo;
import com.xxx.xcloud.client.ceph.CephClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.entity.CephRbd;
import com.xxx.xcloud.module.ceph.entity.CephSnap;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.ceph.entity.SnapStrategy;
import com.xxx.xcloud.module.ceph.service.AbstractCephRbdService;
import com.xxx.xcloud.module.ceph.service.ScheduledThreadPoolTool;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.utils.StringUtils;

/**
 * 
 * <p>
 * Description: 块存储功能实现类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Service
public class CephRbdServiceImpl extends AbstractCephRbdService {

    private static final String CEPH_RBD_ALREADY_EXIST = "块:%s已经存在";
    private static final String CEPH_RBD_NOT_EXIST = "指定的块不存在";
    private static final String CEPH_RBD_SIZE_ILLEGAL = "块存储大小:%s不合法";
    private static final String CEPH_RBD_NAME_ILLEGAL = "块存储名称不合法，应由5到15位字母数字和下划线组成，以字母开头";
    private static final String CEPH_RBD_CREATE_FAILED = "块:%s创建失败";
    private static final String CEPH_RBD_MOUNTED = "块存储已经被挂载";
    private static final String CEPH_RBD_MOUNT_FAILED = "id不存在无法修改";
    private static final String CEPH_RBD_DELETE_FAILED = "租户:%s删除块或快照:%s时出现异常";
    private static final String TENANT_NOT_EXIST = "租户:%s不存在";
    private static final String SNAP_ALREADY_EXIST = "指定的快照:%s已经存在";
    private static final String SNAP_CREATE_FAILED = "快照:%s创建失败";
    private static final String CEPH_RBD_RESIZE_ILLEGAL = "扩容大小不合法，应大于之前的容量";
    private static final String CEPH_RBD_RESIZE_FAILED = "块存储扩容失败";
    private static final String SNAP_STRATEGY_RUNNING = "快照策略正在运行中";
    private static final String SNAP_DELETE_FAILED = "快照删除失败";
    private static final String SNAP_NOT_EXIST = "指定的快照不存在";
    private static final String SNAP_ROLLBACK_FAILED = "快照回滚失败";
    private static final String CEPH_RBD_HAS_SNAP = "块存储存在快照";
    private static final String CEPH_RBD_HAS_STRATEGY = "块存储已经存在快照策略";
    private static final String SNAP_STRATEGY_WEEK_ILLEGAL = "week参数不合法，应由0-6的数字组成，逗号隔开";
    private static final String SNAP_STRATEGY_TIME_ILLEGAL = "time参数不合法，应由0-23的数字组成，逗号隔开";
    private static final String SNAP_STRATEGE_ENDDATE_ILLEGAL = "endDate参数不合法，应非空且大于当前日期";
    private static final String SNAP_STRATEGE_STATUS_ILLEGAL = "status参数不合法，应为0或者1";
    private static final String SNAP_STRATEGE_NOT_EXIST = "指定的快照策略不存在";
    private static final long HEX = 1024L;
    private static final long FEATURES = 1L;

    public static final int NAME_MIN_LENGTH = 4;
    public static final int NAME_MAX_LENGTH = 15;
    public static final double CEPH_RBD_MIN_SIZE = 0d;

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(String tenantName, String createdBy, String projectId, String rbdName, double size,
            String description) {

        checkCephRbdClient();

        // check if the rbd exists
        CephRbd cephRbd = cephRbdRepository.findByTenantNameAndName(tenantName, rbdName);
        if (null != cephRbd) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_EXIST, String.format(CEPH_RBD_ALREADY_EXIST, rbdName));
        }

        // check if the tenant exists
        Tenant tenant = tenantService.findTenantByTenantName(tenantName);
        if (null == tenant) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                    String.format(TENANT_NOT_EXIST, tenantName));
        }

        // check size
        if (size <= CEPH_RBD_MIN_SIZE) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM,
                    String.format(CEPH_RBD_SIZE_ILLEGAL, size));
        }

        // check rbdName
        if (StringUtils.isEmpty(rbdName) || !StringUtils.isAccountName(rbdName, NAME_MIN_LENGTH, NAME_MAX_LENGTH)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_RBD_NAME_ILLEGAL);
        }

        // add rbd
        IoCTX ioctx = null;
        try {
            ioctx = CephClientFactory.getCephRbdClient().ioCtxCreate(tenantName);
            Rbd rbd = new Rbd(ioctx);
            long sizeL = (long) size;
            sizeL = sizeL * HEX * HEX * HEX;
            rbd.create(rbdName, sizeL, FEATURES);
        } catch (Exception e) {
            String msg = String.format(CEPH_RBD_CREATE_FAILED, rbdName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CREATE, msg);
        }
        finally {
            if (CephClientFactory.getCephRbdClient() != null) {
                if (ioctx != null) {
                    CephClientFactory.getCephRbdClient().ioCtxDestroy(ioctx);
                }
            }
        }

        cephRbd = CephRbd.builder().withCreateTime(new Date()).withUpdateTime(new Date()).withDescription(description)
                .withName(rbdName).withTenantName(tenantName).withSize(size).withCreatedBy(createdBy)
                .withProjectId(projectId).build();

        cephRbdRepository.save(cephRbd);
        return true;
    }

    @Override
    public CephRbd get(String cephRbdId) {
        // check if the rbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_RBD_NOT_EXIST);
        }

        CephRbd cephRbd = cephRbdOpt.get();
        if (cephRbd != null) {
            /*
             * ServiceCephRbd serviceCephRbd =
             * serviceCephRbdRepository.findByCephRbdId(cephRbdId); if (null !=
             * serviceCephRbd) {
             * cephRbd.setService(serviceRepository.findById(serviceCephRbd.
             * getServiceId()).get()); }
             */
            cephRbd.setSnapStrategy(snapStrategyRepository.findByCephRbdId(cephRbdId));
            cephRbd.setCephSnaps(cephSnapRepository.findByCephRbdId(cephRbdId));
        }

        return cephRbd;
    }

    @Override
    public Page<CephRbd> list(String tenantName, String name, String projectId, Pageable pageable) {
        Page<CephRbd> list = null;
        if (StringUtils.isNotEmpty(projectId)) {
            list = cephRbdRepository.findByNameAndTenantNameAndProjectId(StringUtils.isNotEmpty(name) ? name : "",
                    tenantName, projectId, pageable);
        } else {
            list = cephRbdRepository.findByNameAndTenantName(StringUtils.isNotEmpty(name) ? name : "", tenantName,
                    pageable);
        }

        /*
         * for (CephRbd cephRbd : list) { ServiceCephRbd serviceCephRbd =
         * serviceCephRbdRepository.findByCephRbdId(cephRbd.getId()); if (null
         * != serviceCephRbd) {
         * cephRbd.setService(serviceRepository.findById(serviceCephRbd.
         * getServiceId()).get()); } }
         */

        return list;
    }

    @Override
    public List<CephRbd> listAvailable(String tenantName) {
        return cephRbdRepository.findAvaliable(tenantName);
    }

    @Override
    public List<CephRbd> listAvailableInProject(String projectId) {
        return cephRbdRepository.findAvaliableInProject(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean delete(String cephRbdId) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }
        CephRbd cephRbd = cephRbdOpt.get();

        // check if the cephrbd mounted
        ServiceCephRbd serviceCephRbd = serviceCephRbdRepository.findByCephRbdId(cephRbdId);
        if (serviceCephRbd != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_OCCUPIED, CEPH_RBD_MOUNTED);
        }

        // stop snapStrategy
        stopSnapStrategy(snapStrategyRepository.findByCephRbdId(cephRbdId));

        // delete database
        cephRbdRepository.delete(cephRbd);
        cephSnapRepository.deleteByCephRbdId(cephRbdId);
        snapStrategyRepository.deleteByCephRbdId(cephRbdId);

        // remove rbd and all snaps
        IoCTX ioctx = null;
        try {
            ioctx = CephClientFactory.getCephRbdClient().ioCtxCreate(cephRbd.getTenantName());
            Rbd rbd = new Rbd(ioctx);
            RbdImage rbdImage = rbd.open(cephRbd.getName());
            List<RbdSnapInfo> snapInfos = rbdImage.snapList();
            for (RbdSnapInfo snapInfo : snapInfos) {
                rbdImage.snapRemove(snapInfo.name);
            }
            rbd.close(rbdImage);
            rbd.remove(cephRbd.getName());
        } catch (Exception e) {
            String msg = String.format(CEPH_RBD_DELETE_FAILED, cephRbd.getTenantName(), cephRbd.getName());
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DELETE, msg);
        }
        finally {
            if (CephClientFactory.getCephRbdClient() != null) {
                if (ioctx != null) {
                    CephClientFactory.getCephRbdClient().ioCtxDestroy(ioctx);
                }
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resize(String cephRbdId, double size) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }
        CephRbd cephRbd = cephRbdOpt.get();

        // check if the rbd is mounted
        ServiceCephRbd serviceCephRbd = serviceCephRbdRepository.findByCephRbdId(cephRbdId);
        if (serviceCephRbd != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_OCCUPIED, CEPH_RBD_MOUNTED);
        }

        // check if snapStrategy is running
        SnapStrategy snapStrategy = snapStrategyRepository.findByCephRbdId(cephRbdId);
        if (snapStrategy != null && snapStrategy.getStatus() == SnapStrategy.STATUS_RUNNING) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_OCCUPIED, SNAP_STRATEGY_RUNNING);
        }

        // check if some snaps exist
        List<CephSnap> cephSnaps = cephSnapRepository.findByCephRbdId(cephRbdId);
        if (cephSnaps != null && cephSnaps.size() > 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_OCCUPIED, CEPH_RBD_HAS_SNAP);
        }

        // check size(size must be greater than the elder)
        if (size <= cephRbd.getSize()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_RBD_RESIZE_ILLEGAL);
        }

        // check if tenant storage resource is enough
        Tenant tenant = tenantService.findTenantByTenantName(cephRbd.getTenantName());
        if (tenant == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, TENANT_NOT_EXIST);
        }

        cephRbd.setSize(size);
        cephRbdRepository.save(cephRbd);

        // resize the rbd
        IoCTX ioctx = null;
        try {
            ioctx = CephClientFactory.getCephRbdClient().ioCtxCreate(cephRbd.getTenantName());
            Rbd rbd = new Rbd(ioctx);
            RbdImage rbdImage = rbd.open(cephRbd.getName());
            long sizeL = (long) size;
            rbdImage.resize(sizeL * HEX * HEX * HEX);
            rbd.close(rbdImage);
        } catch (Exception e) {
            log.error(CEPH_RBD_RESIZE_FAILED, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_RESIZE, CEPH_RBD_RESIZE_FAILED);
        }
        finally {
            if (CephClientFactory.getCephRbdClient() != null) {
                if (CephClientFactory.getCephRbdClient() != null) {
                    CephClientFactory.getCephRbdClient().ioCtxDestroy(ioctx);
                }
            }
        }

        return true;
    }

    @Override
    public boolean createSnap(String cephRbdId, String snapName, String description) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }
        CephRbd cephRbd = cephRbdOpt.get();

        // check if the snap exists
        if (null != cephSnapRepository.findByCephRbdIdAndName(cephRbdId, snapName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_EXIST, String.format(SNAP_ALREADY_EXIST, snapName));
        }

        // check snapName
        if (StringUtils.isEmpty(snapName) || !StringUtils.isAccountName(snapName, NAME_MIN_LENGTH, NAME_MAX_LENGTH)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM,
                    String.format(CEPH_RBD_NAME_ILLEGAL, snapName));
        }

        // create snap
        IoCTX ioctx = null;
        try {
            ioctx = CephClientFactory.getCephRbdClient().ioCtxCreate(cephRbd.getTenantName());
            Rbd rbd = new Rbd(ioctx);
            RbdImage rbdImage = null;
            rbdImage = rbd.open(cephRbd.getName());
            rbdImage.snapCreate(snapName);
            rbd.close(rbdImage);
        } catch (Exception e) {
            String msg = String.format(SNAP_CREATE_FAILED, snapName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CREATE, msg);
        }
        finally {
            if (CephClientFactory.getCephRbdClient() != null) {
                if (ioctx != null) {
                    CephClientFactory.getCephRbdClient().ioCtxDestroy(ioctx);
                }
            }
        }

        CephSnap cephSnap = CephSnap.builder().withCephRbdId(cephRbdId).withCreateTime(new Date())
                .withDescription(description).withName(snapName).build();

        cephSnapRepository.save(cephSnap);

        return true;
    }

    @Override
    public boolean deleteSnap(String cephRbdId, String snapId) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }
        CephRbd cephRbd = cephRbdOpt.get();

        // check if the snap exists
        Optional<CephSnap> cephSnapOpt = cephSnapRepository.findById(snapId);
        if (!cephSnapOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, SNAP_NOT_EXIST);
        }
        CephSnap cephSnap = cephSnapOpt.get();

        // remove snap
        IoCTX ioctx = null;
        try {
            ioctx = CephClientFactory.getCephRbdClient().ioCtxCreate(cephRbd.getTenantName());
            Rbd rbd = new Rbd(ioctx);
            RbdImage rbdImage = rbd.open(cephRbd.getName());
            rbdImage.snapRemove(cephSnap.getName());
            rbd.close(rbdImage);
        } catch (Exception e) {
            log.error(SNAP_DELETE_FAILED, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DELETE, SNAP_DELETE_FAILED);
        }
        finally {
            if (CephClientFactory.getCephRbdClient() != null) {
                if (ioctx != null) {
                    CephClientFactory.getCephRbdClient().ioCtxDestroy(ioctx);
                }
            }
        }

        // update database
        cephSnapRepository.deleteById(snapId);

        return true;
    }

    @Override
    public boolean snapRollBack(String cephRbdId, String snapId) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }
        CephRbd cephRbd = cephRbdOpt.get();

        // check if the snap exists
        Optional<CephSnap> cephSnapOpt = cephSnapRepository.findById(snapId);
        if (!cephSnapOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, SNAP_NOT_EXIST);
        }
        CephSnap cephSnap = cephSnapOpt.get();

        // rollback snap
        IoCTX ioctx = null;
        try {
            ioctx = CephClientFactory.getCephRbdClient().ioCtxCreate(cephRbd.getTenantName());
            Rbd rbd = new Rbd(ioctx);
            RbdImage rbdImage = rbd.open(cephRbd.getName());
            rbdImage.snapRollBack(cephSnap.getName());
            rbd.close(rbdImage);
        } catch (Exception e) {
            log.error(SNAP_ROLLBACK_FAILED, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_ROLLBACK, SNAP_ROLLBACK_FAILED);
        }
        finally {
            if (CephClientFactory.getCephRbdClient() != null) {
                if (ioctx != null) {
                    CephClientFactory.getCephRbdClient().ioCtxDestroy(ioctx);
                }
            }
        }

        return true;
    }

    @Override
    public List<CephSnap> snapList(String cephRbdId) {
        return cephSnapRepository.findByCephRbdId(cephRbdId);
    }

    @Override
    public SnapStrategy getSnapStrategy(String cephRbdId) {
        return snapStrategyRepository.findByCephRbdId(cephRbdId);
    }

    @Override
    public boolean addSnapStrategy(String cephRbdId, String week, String time, Date endDate, int status) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }

        // check if the snapStrategy exists
        SnapStrategy snapStrategy = snapStrategyRepository.findByCephRbdId(cephRbdId);
        if (null != snapStrategy) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_EXIST, CEPH_RBD_HAS_STRATEGY);
        }

        // check week
        if (!StringUtils.isWeekStrings(week)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, SNAP_STRATEGY_WEEK_ILLEGAL);
        }

        // check time
        if (!StringUtils.isTimeStrings(time)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, SNAP_STRATEGY_TIME_ILLEGAL);
        }

        // check endDate
        if (endDate == null || endDate.compareTo(new Date()) < 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, SNAP_STRATEGE_ENDDATE_ILLEGAL);
        }

        // check status
        if (status != SnapStrategy.STATUS_RUNNING && status != SnapStrategy.STATUS_STOP) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, SNAP_STRATEGE_STATUS_ILLEGAL);
        }

        snapStrategy = SnapStrategy.builder().withCephRbdId(cephRbdId).withCreateTime(new Date())
                .withEndTime(new Date()).withStatus(status).withWeek(week).withTime(time).build();

        snapStrategyRepository.save(snapStrategy);

        // start snapStrategy
        if (status == SnapStrategy.STATUS_RUNNING) {
            startSnapStrategy(snapStrategy);
        }

        return true;
    }

    @Override
    public boolean updateSnapStrategy(String cephRbdId, String week, String time, Date endDate, int status) {
        checkCephRbdClient();

        // check if the cephrbd exists
        Optional<CephRbd> cephRbdOpt = cephRbdRepository.findById(cephRbdId);
        if (!cephRbdOpt.isPresent()) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }

        // check if the snapStrategy exists
        SnapStrategy snapStrategy = snapStrategyRepository.findByCephRbdId(cephRbdId);
        if (null == snapStrategy) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, SNAP_STRATEGE_NOT_EXIST);
        }

        SnapStrategy newSnapStrategy = SnapStrategy.builder().withId(snapStrategy.getId()).withCephRbdId(cephRbdId)
                .withCreateTime(snapStrategy.getCreateTime()).withEndTime(endDate).withStatus(status).withTime(time)
                .withWeek(week).build();

        snapStrategyRepository.save(newSnapStrategy);

        // update ScheduledThreadPoolTool
        if (status == SnapStrategy.STATUS_STOP) {
            stopSnapStrategy(snapStrategy);
        } else if (!week.equals(snapStrategy.getWeek()) || !time.equals(snapStrategy.getTime())
                || endDate.compareTo(snapStrategy.getEndTime()) != 0) {
            stopSnapStrategy(snapStrategy);
            startSnapStrategy(newSnapStrategy);
        }

        return true;
    }

    @Override
    public boolean deleteSnapStrategy(String cephRbdId) {
        SnapStrategy snapStrategy = snapStrategyRepository.findByCephRbdId(cephRbdId);

        // check if rbd exists
        if (snapStrategy == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }

        // delete ScheduledThreadPoolTool
        stopSnapStrategy(snapStrategy);
        snapStrategyRepository.delete(snapStrategy);
        return true;
    }

    @Override
    public List<ServiceCephRbd> mountInService(String serviceId) {
        List<ServiceCephRbd> serviceCephRbds = serviceCephRbdRepository.findByServiceId(serviceId);
        for (ServiceCephRbd serviceCephRbd : serviceCephRbds) {
            serviceCephRbd.setCephRbd(cephRbdRepository.findById(serviceCephRbd.getCephRbdId()).get());
        }

        return serviceCephRbds;
    }

    @Override
    public void mountSave(String id, String serviceId, String cephRbdId, String mountPath)
            throws ErrorMessageException {
        ServiceCephRbd serviceCephRbd = serviceCephRbdRepository.findByIdAndServiceIdAndCephRbdId(id, serviceId,
                cephRbdId);
        if (null != id && null == serviceCephRbd) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, CEPH_RBD_MOUNT_FAILED);
        }
        if (serviceCephRbd != null) {
            serviceCephRbd.setMountPath(mountPath);
            serviceCephRbdRepository.save(serviceCephRbd);
        } else {
            if (serviceCephRbdRepository.findByCephRbdId(cephRbdId) != null) {
                throw new ErrorMessageException(ReturnCode.CODE_CEPH_MOUNT, CEPH_RBD_MOUNTED);
            }

            serviceCephRbd = ServiceCephRbd.builder().withCephRbdId(cephRbdId).withServiceId(serviceId)
                    .withMountPath(mountPath).build();

            serviceCephRbdRepository.save(serviceCephRbd);
        }
    }

    @Override
    public void mountCancel(String serviceId, String cephRbdId) {
        ServiceCephRbd serviceCephRbd = serviceCephRbdRepository.findByServiceIdAndCephRbdId(serviceId, cephRbdId);
        if (serviceCephRbd != null) {
            serviceCephRbdRepository.delete(serviceCephRbd);
        } else {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, CEPH_RBD_NOT_EXIST);
        }
    }

    @Override
    public void mountClear(String serviceId) {
        serviceCephRbdRepository.deleteByServiceId(serviceId);
    }

    private long calculateInitialDelay(String[] times) {
        int[] timearray = new int[times.length];
        int i = 0;
        for (String t : times) {
            timearray[i++] = Integer.parseInt(t);
        }
        Arrays.sort(timearray);

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int nearby = -1;
        for (int t : timearray) {
            if (hour >= t) {
                continue;
            } else {
                nearby = t;
                break;
            }
        }

        if (nearby == -1) {
            calendar.add(Calendar.DATE, 1);
            nearby = timearray[0];
        }
        calendar.set(Calendar.HOUR_OF_DAY, nearby);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date delay = calendar.getTime();

        return delay.getTime() - now.getTime();
    }

    @Override
    protected void startSnapStrategy(SnapStrategy snapStrategy) {
        if (snapStrategy != null) {
            ScheduledThreadPoolTool.getInstance().add(snapStrategy.getCephRbdId(), new SnapStrategyTask(snapStrategy),
                    calculateInitialDelay(snapStrategy.getTime().split(",")), 1L * 3600L * 1000L,
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void stopSnapStrategy(SnapStrategy snapStrategy) {
        if (snapStrategy != null) {
            ScheduledThreadPoolTool.getInstance().remove(snapStrategy.getCephRbdId());
        }
    }

    @PostConstruct
    private void initSnapStrategies() {
        Iterator<SnapStrategy> iterator = snapStrategyRepository.findAll().iterator();
        while (iterator.hasNext()) {
            SnapStrategy snapStrategy = iterator.next();
            if (snapStrategy.getStatus() == SnapStrategy.STATUS_RUNNING) {
                startSnapStrategy(snapStrategy);
            }
        }
    }
}
