package com.xxx.xcloud.module.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author mengaijun
 * @Description: 定时任务工具类
 * @date: 2018年12月25日 下午7:46:33
 */
@Component
public class QuartzUtils {
	private static final Logger LOG = LoggerFactory.getLogger(QuartzUtils.class);
	
	@Autowired
	private Scheduler scheduler;
	
	/**
	 * 构造方法
	 */
	public QuartzUtils() {
		LOG.info("初始化定时任务调度器!");
	}
	
	/**
	 * 添加定时任务
	 * @param name 任务名称
	 * @param group 用户组
	 * @param clazz 定时任务处理类
	 * @param cronExpression 表达式
	 * @param map 参数
	 * @throws SchedulerException
	 * @date: 2018年12月26日 上午9:38:23
	 */
	public void addJob(String name, String group, Class<? extends Job> clazz, 
            String cronExpression, Map<String, Object> map) throws SchedulerException {
		// 构造任务
		JobDetail job = JobBuilder.newJob(clazz)
				.withIdentity(name, group)
				.build();
		
		// 动态传入参数
		job.getJobDataMap().putAll(map);
		
		//构造任务触发器
		Trigger trg = TriggerBuilder.newTrigger()
				.withIdentity(name, group)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
				.build();
		
		// 将作业添加到调度器
		scheduler.scheduleJob(job, trg);
		
		LOG.info("创建作业=> [作业名称：" + name + " 作业组：" + group + "] ");
	}
	
	/**
	 * 删除任务
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 * @date: 2018年12月26日 上午10:04:26
	 */
	public void removeJob(String name, String group) throws SchedulerException {
		TriggerKey tk = TriggerKey.triggerKey(name, group);
		// 停止触发器
		scheduler.pauseTrigger(tk);
		// 移除触发器
		scheduler.unscheduleJob(tk);
		//删除作业
		JobKey jobKey = JobKey.jobKey(name, group);
		scheduler.deleteJob(jobKey);
		LOG.info("删除作业=> [作业名称：" + name + " 作业组：" + group + "] ");
	}
	
	/**
	 * 修改定时任务执行时间
	 * @param name
	 * @param group
	 * @param cronExpression
	 * @throws SchedulerException
	 * @date: 2018年12月26日 上午9:39:05
	 */
	public void modifyTime(String name, String group, String cronExpression) throws SchedulerException {
		TriggerKey tk = TriggerKey.triggerKey(name, group);
		//构造任务触发器
		Trigger trg = TriggerBuilder.newTrigger()
				.withIdentity(name, group)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
				.build();
		scheduler.rescheduleJob(tk, trg);
		LOG.info("修改作业触发时间=> [作业名称：" + name + " 作业组：" + group + "] ");
	}
	
	/**
	 * 启动定时任务(不需要调用, 定时任务就能启动)
	 * @throws SchedulerException
	 * @date: 2018年12月26日 上午9:56:20
	 */
    public void start() throws SchedulerException {
        scheduler.start();
        LOG.info("启动调度器 ");
    }
    
    /**
     * 停止定时任务
     * @throws SchedulerException
     * @date: 2018年12月26日 上午9:56:46
     */
    public void shutdown() throws SchedulerException {
        scheduler.shutdown();
        LOG.info("停止调度器 ");
    }
    
    /**
     * 验证cron表达式是否符合规范
     * @param cronStr
     * @return boolean 
     * @date: 2019年1月28日 上午11:04:48
     */
    public boolean isValidExpression(String cronStr) {
    	return CronExpression.isValidExpression(cronStr);
    }
}
