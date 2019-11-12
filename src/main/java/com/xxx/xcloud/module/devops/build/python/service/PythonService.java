package com.xxx.xcloud.module.devops.build.python.service;

import com.xxx.xcloud.module.devops.build.python.pojo.PythonBuilder;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年4月3日 上午10:19:38
 */
public interface PythonService {
    /**
     * 根据Job信息获取PythonBuilder信息
     * 
     * @param job
     * @return PythonBuilder
     * @date: 2019年4月3日 上午10:20:59
     */
    PythonBuilder getPythonBuilder(Job job);
}
