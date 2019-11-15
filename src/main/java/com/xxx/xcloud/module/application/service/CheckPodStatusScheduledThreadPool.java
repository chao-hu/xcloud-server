package com.xxx.xcloud.module.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory.XcloudKubernetesClient;
import com.xxx.xcloud.common.Global;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;

/**
 * <p>
 * Description: 定时检测服务实例状态线程池
 * </p>
 *
 * @author wangkebiao
 * @date 2019年4月9日
 * @see com.xxx.xcloud.module.ceph.service.ScheduledThreadPoolTool
 */
public class CheckPodStatusScheduledThreadPool {

	private static final Logger LOG = LoggerFactory.getLogger(CheckPodStatusScheduledThreadPool.class);

	private ScheduledExecutorService scheduledExecutorService;
	private Map<String, ScheduledFuture<?>> map;
    private final XcloudKubernetesClient client = KubernetesClientFactory.getClient();
    // private final IInstanceManageService instanceManageService =
    // SpringUtils.getBean(IInstanceManageService.class);

	private CheckPodStatusScheduledThreadPool() {
		scheduledExecutorService = new ScheduledThreadPoolExecutor(10, new ServiceThreadFactory());
		map = new ConcurrentHashMap<String, ScheduledFuture<?>>(64);
	}

	public static CheckPodStatusScheduledThreadPool getInstance() {
		return ScheduledThreadPoolToolInner.INSTANCE;
	}

    /**
     * 多线程下保证单例可用
     * 
     * @Description: TODO
     * @date: 2019年11月11日 下午3:29:10
     */
	private static class ScheduledThreadPoolToolInner {
		private final static CheckPodStatusScheduledThreadPool INSTANCE = new CheckPodStatusScheduledThreadPool();
	}

	/**
	 * <p>
	 * Description: 添加任务到线程池
	 * </p>
	 *
	 * @param podIdentifier
	 *            规则为 tenantName + Global.CONCATENATE + serviceName
	 */
	public void add(String podIdentifier) {
		if (map.get(podIdentifier) == null) {
			// 每隔五分钟延时检测
			ScheduledFuture<?> scheduledFuture = scheduledExecutorService
					.scheduleWithFixedDelay(new CheckPodState(podIdentifier), 10L, 5L, TimeUnit.MINUTES);
			map.put(podIdentifier, scheduledFuture);
		}
	}

	/**
	 * <p>
	 * Description: 从线程池移除任务
	 * </p>
	 *
	 * @param podIdentifier
	 *            规则为 tenantName + Global.CONCATENATE + serviceName
	 */
	public void remove(String podIdentifier) {
		ScheduledFuture<?> scheduledFuture = map.get(podIdentifier);
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			map.remove(podIdentifier);
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
			namePrefix = "bdos-checkPodState-";
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

		public CheckPodState(String podIdentifier) {
			this.tenantName = podIdentifier.split(Global.CONCATENATE)[0];
			this.serviceName = podIdentifier.split(Global.CONCATENATE)[1];
		}

		@Override
		public void run() {
			Map<String, String> labelSelector = new HashMap<String, String>(16);
			labelSelector.put("app", serviceName);
			PodList podList = null;
			boolean restartFlag = false;
			try {
				podList = client.pods().inNamespace(tenantName).withLabels(labelSelector).list();
			} catch (Exception e) {
				podList = null;
			}

			if (podList == null || podList.getItems().size() == 0) {
				return;
			} else {
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
							restartFlag = true;
                            // *************************************************************
//                            instanceManageService.deletePod(tenantName, pod.getMetadata().getName());
						}
					}
				}
			}
			LOG.debug("线程" + Thread.currentThread().getName() + "对租户" + tenantName + "下的服务" + serviceName + "状态检查结束，"
					+ (restartFlag == true ? "服务状态异常并对异常的实例进行重启操作" : "服务状态正常"));
		}
	}
}
