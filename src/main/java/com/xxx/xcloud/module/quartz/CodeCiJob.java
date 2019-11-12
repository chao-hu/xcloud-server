package com.xxx.xcloud.module.quartz;

import com.xxx.xcloud.module.ci.service.ICiService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 代码构建定时处理类
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月3日 下午2:33:58
 */
@Service
public class CodeCiJob implements Job, Serializable {

    private static final long serialVersionUID = -7367700362871198904L;
    @Autowired
    private ICiService ciService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String id = (String) context.getJobDetail().getJobDataMap().get("id");
        String createdBy = (String) context.getJobDetail().getJobDataMap().get("createdBy");
        ciService.startCi(id, createdBy);
    }

}
