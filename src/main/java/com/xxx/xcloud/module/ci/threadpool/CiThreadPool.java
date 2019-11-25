package com.xxx.xcloud.module.ci.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 构建线程池
 * 
 * @author mengaijun
 * @date: 2019年5月16日 下午5:22:49
 */
public class CiThreadPool {

    private static final Logger LOG = LoggerFactory.getLogger(CiThreadPool.class);

    public static ThreadFactory stateCheckThreadFactory = null;
    public static volatile ThreadPoolExecutor executor = null;

    /**
     * 线程池实例
     * 
     * @author mengaijun
     * @date: 2019年5月16日 下午5:23:32
     */
    public static ThreadPoolExecutor getExecotur() {

        if (null != executor) {
            return executor;
        }

        intiThreadPool();
        return executor;
    }

    private static synchronized void intiThreadPool() {

        if (null == executor) {

            try {
                stateCheckThreadFactory = new ThreadFactoryBuilder().setNameFormat("bdos-ci-codecheck-worker-%d")
                        .build();
                executor = new ThreadPoolExecutor(
                        Integer.valueOf(XcloudProperties.getConfigMap().get(Global.CI_THREAD_POOL_CORE_SIZE)),
                        Integer.valueOf(XcloudProperties.getConfigMap().get(Global.CI_THREAD_POOL_MAX_SIZE)), 120L,
                        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), stateCheckThreadFactory);
                executor.allowCoreThreadTimeOut(true);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                LOG.info("----------初始化构建线程池失败------------");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "初始化构建线程池失败");
            }
        }
    }

}
