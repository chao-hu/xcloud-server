package com.xxx.xcloud.module.devops.actions.service;

import com.xxx.xcloud.module.devops.actions.pojo.Actions;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface ActionsService {

    Actions getActions(Job jobModel);

}
