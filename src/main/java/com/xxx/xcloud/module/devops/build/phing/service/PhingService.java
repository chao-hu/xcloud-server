package com.xxx.xcloud.module.devops.build.phing.service;

import com.xxx.xcloud.module.devops.build.phing.pojo.PhingBuilder;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * 根据Job获取对象
 * 
 * @author mengaijun
 * @date: 2019年4月2日 上午10:32:35
 */
public interface PhingService {
    /**
     * 根据Job获取Phing信息
     * 
     * @param job
     * @return PhingBuilder
     * @date: 2019年4月2日 上午10:34:35
     */
    PhingBuilder getPhing(Job job);
}
