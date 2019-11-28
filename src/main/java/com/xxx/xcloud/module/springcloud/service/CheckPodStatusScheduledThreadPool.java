package com.xxx.xcloud.module.springcloud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory.XcloudKubernetesClient;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudApplication;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudApplicationRepository;
import com.xxx.xcloud.module.springcloud.repository.SpringCloudServiceRepository;
import com.xxx.xcloud.utils.SpringUtils;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;


/**
 * @ClassName: CheckPodStatusScheduledThreadPool
 * @Description: 定时检测服务实例状态线程池
 * @author lnn
 * @date 2019年11月26日
 *
 */
public class CheckPodStatusScheduledThreadPool {

    private static final Logger LOG = LoggerFactory.getLogger(CheckPodStatusScheduledThreadPool.class);

    private ScheduledExecutorService scheduledExecutorService;
    private Map<String, ScheduledFuture<?>> map;
    private final XcloudKubernetesClient client = KubernetesClientFactory.getClient();

    private CheckPodStatusScheduledThreadPool() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(10, new ServiceThreadFactory());
        map = new ConcurrentHashMap<String, ScheduledFuture<?>>(64);
    }

    public static CheckPodStatusScheduledThreadPool getInstance() {
        return ScheduledThreadPoolToolInner.INSTANCE;
    }

    // 多线程下保证单例可用
    private static class ScheduledThreadPoolToolInner {
        private final static CheckPodStatusScheduledThreadPool INSTANCE = new CheckPodStatusScheduledThreadPool();
    }

    /**
     * <p>
     * Description: 添加任务到线程池
     * </p>
     *
     * @param podIdentifier
     *            规则为 tenantName + Global.CONCATENATE + serviceName + appId
     */
    public void add(String podIdentifier) {
        if (map.get(podIdentifier) == null) {
            // 每隔五分钟延时检测
            ScheduledFuture<?> scheduledFuture = scheduledExecutorService
                    .scheduleWithFixedDelay(new CheckPodState(podIdentifier), 10L, 5L, TimeUnit.MINUTES);
            map.put(podIdentifier, scheduledFuture);
            LOG.info("bdos-springCloud-checkPodState 线程池中  添加任务 ---> " + podIdentifier);
        }
    }

    /**
     * <p>
     * Description: 从线程池移除任务
     * </p>
     *
     * @param podIdentifier
     *            规则为 tenantName + Global.CONCATENATE + serviceName + appId
     */
    public void remove(String podIdentifier) {
        ScheduledFuture<?> scheduledFuture = map.get(podIdentifier);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            map.remove(podIdentifier);
            LOG.info("bdos-springCloud-checkPodState 线程池中  移除任务 ---> " + podIdentifier);
        }
    }

    /**
     * <p>
     * Description: 关闭当前线程池
     * </p>
     */
    public void shutDown() {
        scheduledExecutorService.shutdownNow();
        map.clear();
    }

    private class ServiceThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private ServiceThreadFactory() {
            namePrefix = "bdos-springCloud-checkPodState-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

    }

    private class CheckPodState implements Runnable {

        private String tenantName;
        private String serviceName;
        private String appId;
        private String type;

        public CheckPodState(String podIdentifier) {
            this.tenantName = podIdentifier.split(Global.CONCATENATE)[0];
            this.serviceName = podIdentifier.split(Global.CONCATENATE)[1];
            this.appId = podIdentifier.split(Global.CONCATENATE)[2];
            this.type = podIdentifier.split(Global.CONCATENATE)[3];
        }

        @Override
        public void run() {
            LOG.info("线程" + Thread.currentThread().getName() + "对租户" + tenantName + "下的服务" + serviceName + "开始进行状态检查");
            Map<String, String> labelSelector = new HashMap<String, String>(16);
            labelSelector.put("serviceName", serviceName);
            PodList podList = null;
            try {
                podList = client.pods().inNamespace(tenantName).withLabels(labelSelector).list();
            } catch (Exception e) {
                podList = null;
            }

            if (podList != null && podList.getItems().size() > 0) {
                if (SpringCloudCommonConst.SERVICE_TYPE_SINGLE.equals(type)) {
                    for (Pod pod : podList.getItems()) {
                        if ("Running".equals(pod.getStatus().getPhase())) {
                            List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                            ContainerStateTerminated terminated = null;
                            ContainerStateWaiting waiting = null;
                            if (containerStatuses.size() > 0) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState containerState = containerStatus.getState();
                                terminated = containerState.getTerminated();
                                waiting = containerState.getWaiting();
                            }
                            if (null != terminated || null != waiting) {
                                // 修改应用状态
                                updateState(appId, serviceName, tenantName);
                            }
                        } else {
                            updateState(appId, serviceName, tenantName);
                        }
                    }
                } else {
                    int podNum = 0;
                    for (Pod pod : podList.getItems()) {
                        if ("Running".equals(pod.getStatus().getPhase())) {
                            List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                            ContainerStateTerminated terminated = null;
                            ContainerStateWaiting waiting = null;
                            if (containerStatuses.size() > 0) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState containerState = containerStatus.getState();
                                terminated = containerState.getTerminated();
                                waiting = containerState.getWaiting();
                            }
                            if (null != terminated || null != waiting) {
                                podNum++;
                            }
                        } else {
                            podNum++;
                        }
                    }
                    try {
                        SpringCloudServiceRepository springCloudServiceRepository = SpringUtils
                                .getBean(SpringCloudServiceRepository.class);
                        SpringCloudService springCloudService = springCloudServiceRepository
                                .findByTenantNameAndServiceNameAndServiceStateNot(tenantName, serviceName, SpringCloudCommonConst.STATE_APP_DELETED);
                        if (springCloudService.getNodeNum() == podNum) {
                            updateState(appId, serviceName, tenantName);
                        }
                    } catch (Exception e) {
                        LOG.error("数据库连接异常, error: ", e);
                    }
                }

            }
            LOG.info("线程" + Thread.currentThread().getName() + "对租户" + tenantName + "下的服务" + serviceName + "状态检查结束");
        }

    }

    @Transactional(rollbackOn = Exception.class)
    public void updateState(String appId, String serviceName, String tenantName) {
        try {
        	// 修改应用状态为Failed
            SpringCloudApplicationRepository springCloudApplicationRepository = SpringUtils.getBean(SpringCloudApplicationRepository.class);
            Optional<SpringCloudApplication> optional = springCloudApplicationRepository.findById(appId);
            SpringCloudApplication springCloudApplication = optional.get();
            springCloudApplication.setState(SpringCloudCommonConst.STATE_APP_FAILED);
            springCloudApplicationRepository.save(springCloudApplication);
            // 修改服务状态为Failed
            SpringCloudServiceRepository springCloudServiceRepository = SpringUtils.getBean(SpringCloudServiceRepository.class);
            SpringCloudService springCloudService = springCloudServiceRepository.findByTenantNameAndServiceNameAndServiceStateNot(tenantName, serviceName, SpringCloudCommonConst.STATE_APP_DELETED);
            springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
            springCloudServiceRepository.save(springCloudService);
            // 移除状态检测
            remove(springCloudService.getTenantName() + Global.CONCATENATE + springCloudService.getServiceName()
            + Global.CONCATENATE + springCloudApplication.getId());
            //List<SpringCloudService> springCloudServiceList = springCloudServiceRepository.findByAppId(appId);
           /* for (SpringCloudService springCloudService : springCloudServiceList) {
                springCloudService.setServiceState(SpringCloudCommonConst.STATE_APP_FAILED);
                springCloudServiceRepository.save(springCloudService);

                remove(springCloudService.getTenantName() + Global.CONCATENATE + springCloudService.getServiceName()
                        + Global.CONCATENATE + springCloudApplication.getId());
            }*/
            LOG.info("线程" + Thread.currentThread().getName() + "对租户" + springCloudApplication.getTenantName() + "下的服务"
                    + springCloudApplication.getAppName() + "状态检查结束,服务状态异常,修改服务状态成功");
        } catch (Exception e) {
            LOG.error("数据库连接异常，无法更新SpringCloud应用的状态, error: ", e);
            throw e;
        }
    }
}
