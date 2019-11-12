package com.xxx.xcloud.module.devops.triggers.service.impl;

import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.triggers.pojo.Triggers;
import com.xxx.xcloud.module.devops.triggers.service.TriggersService;
import org.springframework.stereotype.Service;

/**
 *
 * 如果job中配置了hook为true，则返回Triggers
 *
 * @author daien
 * @date 2019年3月15日
 */
@Service
public class TriggersServiceImpl implements TriggersService {

    @Override
    public Triggers getTriggers(Job job) {
        Triggers triggers = null;
        if (job.getHook()) {
//            triggers = new Triggers();
//            GitLabPushTrigger gitLabPushTrigger = new GitLabPushTrigger();
//            gitLabPushTrigger.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_HOOK_GITLAB));
        }

        return triggers;
    }

}
