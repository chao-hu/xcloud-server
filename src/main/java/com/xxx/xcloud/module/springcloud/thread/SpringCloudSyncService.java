package com.xxx.xcloud.module.springcloud.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.tomcat.jni.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudApplication;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudApplicationRepository;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudServiceRepository;
import com.xxx.xcloud.module.springcloud.service.CheckPodStatusScheduledThreadPool;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudService;



/**
 * @ClassName: SpringCloudSyncService
 * @Description: 使用线程的方法(方法上加上@Async注解即可)
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Component
public class SpringCloudSyncService {

    private static final Logger LOG = LoggerFactory.getLogger(SpringCloudSyncService.class);

    @Autowired
    @Qualifier("eurekaServiceImpl")
    private ISpringCloudService eurekaServiceImpl;

    @Autowired
    @Qualifier("configBusServiceImpl")
    private ISpringCloudService configBusServiceImpl;

    @Autowired
    @Qualifier("zuulServiceImpl")
    private ISpringCloudService zuulServiceImpl;

    @Autowired
    private SpringCloudApplicationRepository springCloudApplicationRepository;

    @Autowired
    private SpringCloudServiceRepository springCloudServiceRepository;

    /**
     * 创建Eureka服务
     * 
     * @param service
     * @param imageId
     */
    @Async
    public Future<ApiResult> createEureka(String serviceId) {

        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = eurekaServiceImpl.create(serviceId);
        } catch (Exception e) {
            LOG.error("eureka服务" + serviceId + "创建失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);

    }

    /**
     * 创建ConfigBus服务
     * 
     * @param service
     * @param imageId
     */
    @Async
    public Future<ApiResult> createConfigBus(String serviceId) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = configBusServiceImpl.create(serviceId);
        } catch (Exception e) {
            LOG.error("configBus服务" + serviceId + "创建失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);

    }

    /**
     * 操作后更新应用状态
     * 
     * @param service
     * @param imageId
     */
    @Async
    public void updateState(String appId, String action, List<Future<ApiResult>> result) {

        Optional<SpringCloudApplication> springCloudApplication = null;
        SpringCloudApplication application = null;

        // 查询应用信息
        springCloudApplication = springCloudApplicationRepository.findById(appId);
        if (null == springCloudApplication || null == springCloudApplication.get()) {
            LOG.info("操作后更新应用状态，查询应用" + appId + "失败");
            return;
        }
        application = springCloudApplication.get();
        boolean flag = true;
        // 校验服务，确定应用状态
        for (Future<ApiResult> serviceResult : result) {

            // 服务状态
            try {
                if (200 != serviceResult.get().getCode()) {
                    flag = false;
                }
            } catch (InterruptedException e) {
                flag = false;
                e.printStackTrace();
            } catch (ExecutionException e) {
                flag = false;
                e.printStackTrace();
            } catch (Exception e) {
                flag = false;
                e.printStackTrace();
            }
        }

        switch (action) {
        case SpringCloudCommonConst.OPERATOR_CREATE:
            // 修改应用状态
            changState(application, flag, SpringCloudCommonConst.STATE_APP_RUNNING);
            break;

        case SpringCloudCommonConst.OPERATOR_STOP:
            // 修改应用状态
            changState(application, flag, SpringCloudCommonConst.STATE_APP_STOPPED);
            break;

        case SpringCloudCommonConst.OPERATOR_START:
            // 修改应用状态
            changState(application, flag, SpringCloudCommonConst.STATE_APP_RUNNING);
            break;

        case SpringCloudCommonConst.OPERATOR_DELETE:
            // 修改应用状态
            changState(application, flag, SpringCloudCommonConst.STATE_APP_DELETED);
            break;

        default:
            break;
        }

    }

    private void changState(SpringCloudApplication application, boolean flag, String state) {
        // List<SpringCloudService> serviceList = new
        // ArrayList<SpringCloudService>();
        if (flag) {
            application.setState(state);
            saveApp(application);
        } else {
            application.setState(SpringCloudCommonConst.STATE_APP_FAILED);
            saveApp(application);

            // 修改服务状态failed
            // 查找服务list
            /*
             * try { serviceList =
             * springCloudServiceRepository.findByAppId(application.getId()); }
             * catch (Exception e) { throw new
             * ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
             * "查找应用" + application.getId() + "下的所有服务失败"); } if
             * (serviceList.isEmpty()) { throw new
             * ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "应用"
             * + application.getId() + "下没有服务"); }
             * 
             * // 修改服务状态failed try { for (SpringCloudService service :
             * serviceList) {
             * service.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
             * springCloudServiceRepository.save(service); } } catch (Exception
             * e) { throw new
             * ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
             * "保存应用" + application.getId() + "下服务Waiting状态失败"); }
             */
        }
    }

    /*
     * 保存应用
     */
    private void saveApp(SpringCloudApplication application) {
        try {
            springCloudApplicationRepository.save(application);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("操作后更新应用状态，保存应用" + application.getId() + "状态失败");
        }
    }

    /*
     * 删除服务
     */
    @Async
    public Future<ApiResult> deleteEureka(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = eurekaServiceImpl.delete(id);
        } catch (Exception e) {
            LOG.error("eureka服务" + id + "删除失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

    /*
     * 删除服务
     */
    @Async
    public Future<ApiResult> deleteConfigBus(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = configBusServiceImpl.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("configBus服务" + id + "删除失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

    /*
     * 停止服务
     */
    @Async
    public Future<ApiResult> stop(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = eurekaServiceImpl.stop(id);
        } catch (Exception e) {
            LOG.info("服务" + id + "停止失败");
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

    /*
     * 启动eureka
     */
    @Async
    public Future<ApiResult> startEureka(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = eurekaServiceImpl.start(id);
        } catch (Exception e) {
            LOG.error("Eureka服务" + id + "启动失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

    /*
     * 启动configBus
     */
    @Async
    public Future<ApiResult> startConfigBus(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = configBusServiceImpl.start(id);
        } catch (Exception e) {
            LOG.error("configBus服务" + id + "启动失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

    /**
     * Description: 应用创建或启动后检测状态,若成功加入到定时检测服务实例状态线程池
     * 
     * @param appId
     *            void
     */
    @Async
    public void checkPodState(String appId) {
        try {
            Time.sleep(900000);
            SpringCloudApplication springCloudApplication = springCloudApplicationRepository.findById(appId).get();
            if (SpringCloudCommonConst.STATE_APP_RUNNING.equals(springCloudApplication.getState())) {
                List<SpringCloudService> springCloudServiceList = new ArrayList<SpringCloudService>();
                springCloudServiceList = springCloudServiceRepository.findByAppId(springCloudApplication.getId());
                if (springCloudServiceList.size() > 0) {
                    for (SpringCloudService springCloudService : springCloudServiceList) {
                        if (SpringCloudCommonConst.STATE_APP_RUNNING.equals(springCloudService.getServiceState())) {
                            CheckPodStatusScheduledThreadPool.getInstance()
                                    .add(springCloudService.getTenantName() + Global.CONCATENATE
                                            + springCloudService.getServiceName() + Global.CONCATENATE + appId
                                            + Global.CONCATENATE + springCloudService.getServiceType());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建Zuul服务
     * 
     * @param service
     * @param imageId
     */
    @Async
    public Future<ApiResult> createZuul(String serviceId) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = zuulServiceImpl.create(serviceId);
        } catch (Exception e) {
            LOG.error("zuul服务" + serviceId + "创建失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);

    }

    /*
     * 删除服务
     */
    @Async
    public Future<ApiResult> deleteZuul(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = zuulServiceImpl.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("zuul服务" + id + "删除失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

    /*
     * 启动zuul
     */
    @Async
    public Future<ApiResult> startZuul(String id) {
        ApiResult apiResult = null;
        // 调用服务接口
        try {
            apiResult = zuulServiceImpl.start(id);
        } catch (Exception e) {
            LOG.error("zuul服务" + id + "启动失败", e);
        }
        return new AsyncResult<ApiResult>(apiResult);
    }

}
