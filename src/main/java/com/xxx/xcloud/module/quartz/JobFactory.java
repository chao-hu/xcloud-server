package com.xxx.xcloud.module.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义JobFactory     注入了一个 自定义的JobFactory ，然后 把其设置为SchedulerFactoryBean 的
 * JobFactory。其目的是因为我在具体的Job 中 需要Spring 注入一些Service。
 * 所以我们要自定义一个jobfactory， 让其在具体job 类实例化时 使用Spring 的API 来进行依赖注入
 * @author mengaijun
 * @date: 2019年1月3日 上午11:38:12
 */
@Component
public class JobFactory extends AdaptableJobFactory {
	
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        // 调用父类的方法
        Object jobInstance = super.createJobInstance(bundle);
        // 进行注入
        capableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}
