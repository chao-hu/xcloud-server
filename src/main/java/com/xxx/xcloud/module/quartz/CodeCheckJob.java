package com.xxx.xcloud.module.quartz;

import com.xxx.xcloud.module.sonar.service.SonarService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * 代码检查定时处理类
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月3日 下午2:33:58
 */
public class CodeCheckJob implements Job, Serializable {

    private static final long serialVersionUID = 6259305980870555585L;

    @Autowired
    private SonarService sonarService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String id = (String) context.getJobDetail().getJobDataMap().get("id");
        sonarService.startupCheckCode(id);
    }

}
