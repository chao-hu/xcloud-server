package com.xxx.xcloud.module.component.service.statecheck;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.repository.StatefulNodeRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


/**
 * @ClassName: BaseStateCheckRunner
 * @Description: 状态检查
 * @author lnn
 * @date 2019年11月21日
 *
 */
@Component
@Order(value = 2)
public abstract class BaseStateCheckRunner implements CommandLineRunner, Ordered {

    private static Logger LOG = LoggerFactory.getLogger(BaseStateCheckRunner.class);
    @Autowired
    protected StatefulNodeRepository statefulNodeRepository;
    @Autowired
    protected StatefulServiceRepository statefulServiceRepository;

    @Override
    public int getOrder() {
        return 0;
    }

    private static ThreadFactory stateCheckThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("bdos-state-check-worker-%d").build();

    private static final ExecutorService STATE_CHECK_POOL = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), stateCheckThreadFactory);

    /** 
     * @Title: run
     * @Description: 开启定时检查状态线程
     * @param args
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(String... args) {
        LOG.info("============开始状态检查==============");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LOG.info("------stateCheck------");
                while (true) {
                    try {
                        stateCheck();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("stateCheck异常！", e.getMessage());
                    }

                    try {
                        Thread.sleep(CommonConst.MINUTE * 5);
                    } catch (InterruptedException e) {
                        LOG.error("线程休眠中断！", e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error("线程休眠异常！", e);
                    }
                }
            }
        };
        STATE_CHECK_POOL.execute(runnable);
    }

    /**
     * 检查状态
     * @return
     */
    protected abstract void stateCheck();
}
