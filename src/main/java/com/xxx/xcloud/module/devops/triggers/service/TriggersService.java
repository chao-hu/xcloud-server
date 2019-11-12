package com.xxx.xcloud.module.devops.triggers.service;

import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.triggers.pojo.Triggers;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface TriggersService {

    Triggers getTriggers(Job job);

}
