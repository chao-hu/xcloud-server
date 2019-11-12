/*
 * 文件名：SchedulerConfig.java
 * 版权：Copyright by www.huawei.com
 * 描述：
 * 修改人：Hello World
 * 修改时间：2017年9月23日
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */

package com.xxx.xcloud.module.quartz;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 定时任务SchedulerFactoryBean对象配置
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月3日 上午11:32:54
 */
@Configuration
public class SchedulerConfig {
      @Autowired
      private JobFactory jobFactory;

      @Bean
      public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(jobFactory);
        return schedulerFactoryBean;
      }

      @Bean
      public Scheduler scheduler() {
        return schedulerFactoryBean().getScheduler();
      }
}