package com.xxx.xcloud.module.ceph.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ceph.rados.exceptions.RadosException;
import com.xxx.xcloud.client.ceph.CephClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.entity.CephRbd;
import com.xxx.xcloud.module.ceph.entity.SnapStrategy;
import com.xxx.xcloud.module.ceph.repository.CephRbdRepository;
import com.xxx.xcloud.module.ceph.repository.CephSnapRepository;
import com.xxx.xcloud.module.ceph.repository.ServiceCephRbdRepository;
import com.xxx.xcloud.module.ceph.repository.SnapStrategyRepository;

/**
 * 
 * <p>
 * Description: 块存储抽象类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
public abstract class AbstractCephRbdService implements CephRbdService {

    private static final String SPLIT = ",";
    private static final String RBD_POOL_CREATE_FAILED = "块存储创建池:%s失败";
    private static final String RBD_POOL_DELETE_FAILED = "块存储池:%s删除失败";
    private static final String CEPH_RBD_CLIENT = "CephRbd客户端异常";
    private static final int NAME_MIN_LENGTH = 0;
    private static final int NAME_MAX_LENGTH = 8;
    private static final String AUTO_SNAP_NAME_PREFIX = "auto_";
    private static final String AUTO_SNAP_DESP_PREFIX = "autoCreated cephSnap of Rbd-";
    private static final String AUTO_SNAP_CREATE_FAILED = "autoCreating cephSnap of Rbd- %s failed";

    protected static final Logger log = LoggerFactory.getLogger(AbstractCephRbdService.class);

    // protected Rados cluster;

    @Autowired
    protected CephRbdRepository cephRbdRepository;

    @Autowired
    protected CephSnapRepository cephSnapRepository;

    @Autowired
    protected SnapStrategyRepository snapStrategyRepository;

    @Autowired
    protected ServiceCephRbdRepository serviceCephRbdRepository;

    @Override
    public boolean createCephRbdPool(String tenantName) {
        checkCephRbdClient();

        // create pool
        String[] pools;
        try {
            pools = CephClientFactory.getCephRbdClient().poolList();
            boolean poolExist = false;
            for (String pool : pools) {
                if (tenantName.equals(pool)) {
                    poolExist = true;
                    break;
                }
            }

            if (!poolExist) {
                CephClientFactory.getCephRbdClient().poolCreate(tenantName);
            }
        } catch (RadosException e) {
            String msg = String.format(RBD_POOL_CREATE_FAILED, tenantName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INIT, msg);
        }

        return true;
    }

    @Override
    public boolean destroyCephRbdPool(String tenantName) {
        checkCephRbdClient();

        // stop snapStrategy
        List<CephRbd> cephRbds = cephRbdRepository.findByTenantName(tenantName);
        for (CephRbd cephRbd : cephRbds) {
            stopSnapStrategy(snapStrategyRepository.findByCephRbdId(cephRbd.getId()));
        }

        // delete pool
        try {
            String[] pools = CephClientFactory.getCephRbdClient().poolList();
            for (String pool : pools) {
                if (tenantName.equals(pool)) {
                    CephClientFactory.getCephRbdClient().poolDelete(tenantName);
                    break;
                }
            }
        } catch (RadosException e) {
            String msg = String.format(RBD_POOL_DELETE_FAILED, tenantName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DESTROY, msg);
        }

        // delete datebase(snap/rbd/snapStrategy/serviceCephRbd)
        for (CephRbd cephRbd : cephRbds) {
            snapStrategyRepository.deleteByCephRbdId(cephRbd.getId());
            serviceCephRbdRepository.deleteByCephRbdId(cephRbd.getId());
            cephSnapRepository.deleteByCephRbdId(cephRbd.getId());
            cephRbdRepository.delete(cephRbd);
        }

        return true;
    }

    
    /**
     * 开始快照策略
     * @Title: startSnapStrategy
     * @Description: 开始快照策略
     * @param snapStrategy 快照策略 
     * @throws
     */
    protected abstract void startSnapStrategy(SnapStrategy snapStrategy);

    /**
     * 停止快照策略
     * @Title: stopSnapStrategy
     * @Description: 停止快照策略
     * @param snapStrategy 快照策略 
     * @throws
     */
    protected abstract void stopSnapStrategy(SnapStrategy snapStrategy);

    /**
     * @Description 检查Rados是否正常连接
     * @throws ErrorMessageException
     */
    protected void checkCephRbdClient() {
        try {
            CephClientFactory.getCephRbdClient().clusterFsid();
        } catch (Exception | Error e) {
            log.error(CEPH_RBD_CLIENT, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, CEPH_RBD_CLIENT);
        }
    }

    public class SnapStrategyTask implements Runnable {
        private Set<Integer> week = new TreeSet<Integer>();
        private Set<Integer> time = new TreeSet<Integer>();
        private SnapStrategy snapStrategy;

        public SnapStrategyTask(SnapStrategy snapStrategy) {
            if (snapStrategy != null) {
                String[] weeks = snapStrategy.getWeek().split(SPLIT);
                for (String w : weeks) {
                    week.add(Integer.parseInt(w));
                }

                String[] times = snapStrategy.getTime().split(SPLIT);
                for (String t : times) {
                    time.add(Integer.parseInt(t));
                }
                this.snapStrategy = snapStrategy;
            }
        }

        public void setSnapStrategy(SnapStrategy snapStrategy) {
            if (snapStrategy != null) {
                week.clear();
                String[] weeks = snapStrategy.getWeek().split(SPLIT);
                for (String w : weeks) {
                    week.add(Integer.parseInt(w));
                }

                time.clear();
                String[] times = snapStrategy.getTime().split(SPLIT);
                for (String t : times) {
                    time.add(Integer.parseInt(t));
                }
                this.snapStrategy = snapStrategy;
            }
        }

        @Override
        public void run() {
            if (!Thread.currentThread().isInterrupted()) {
                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();
                if (snapStrategy.getStatus() == SnapStrategy.STATUS_RUNNING
                        && snapStrategy.getEndTime().compareTo(now) > 0
                        && week.contains(calendar.get(Calendar.DAY_OF_WEEK) - 1)
                        && time.contains(calendar.get(Calendar.MINUTE) >= 59 ? calendar.get(Calendar.HOUR_OF_DAY) + 1
                                : calendar.get(Calendar.HOUR_OF_DAY))) {
                    try {
                        createSnap(snapStrategy.getCephRbdId(),
                                AUTO_SNAP_NAME_PREFIX
                                        + UUID.randomUUID().toString().substring(NAME_MIN_LENGTH, NAME_MAX_LENGTH),
                                AUTO_SNAP_DESP_PREFIX + snapStrategy.getCephRbdId());
                    } catch (ErrorMessageException e) {
                        log.error(String.format(AUTO_SNAP_CREATE_FAILED, snapStrategy.getCephRbdId()), e);
                    }
                }
            }
        }

    }
}
