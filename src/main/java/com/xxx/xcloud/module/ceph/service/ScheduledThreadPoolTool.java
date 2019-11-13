package com.xxx.xcloud.module.ceph.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * <p>
 * Description: 快照定时任务线程池
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
public class ScheduledThreadPoolTool {

    private ScheduledExecutorService scheduledExecutorService;
    private Map<String, ScheduledFuture<?>> map;

    private ScheduledThreadPoolTool() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(20, new CephThreadFactory());
        map = new HashMap<String, ScheduledFuture<?>>();
    }

    public static ScheduledThreadPoolTool getInstance() {
        return ScheduledThreadPoolToolInner.INSTANCE;
    }

    public boolean add(String cephRbdId, Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        if (map.get(cephRbdId) == null) {
            ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay,
                    period, unit);
            map.put(cephRbdId, scheduledFuture);
            return true;
        }
        return false;
    }

    public void remove(String cephRbdId) {
        ScheduledFuture<?> scheduledFuture = map.get(cephRbdId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            map.remove(cephRbdId);
        }
    }

    public void shutDown() {
        scheduledExecutorService.shutdownNow();
        map.clear();
    }

    private static class ScheduledThreadPoolToolInner {
        private final static ScheduledThreadPoolTool INSTANCE = new ScheduledThreadPoolTool();
    }

    private class CephThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private CephThreadFactory() {
            namePrefix = "bdos-snapstrategy-";
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
}
