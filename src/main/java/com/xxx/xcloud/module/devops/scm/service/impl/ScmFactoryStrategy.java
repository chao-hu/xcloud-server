package com.xxx.xcloud.module.devops.scm.service.impl;

import com.xxx.xcloud.module.devops.common.ScmType;
import com.xxx.xcloud.module.devops.scm.service.ScmFactory;
import com.xxx.xcloud.utils.SpringContextHolder;

public class ScmFactoryStrategy {

    public static ScmFactory getScmFactory(String scmType) {
        switch (scmType) {
        case ScmType.SCM_GIT_LAB:
            return (ScmFactory) SpringContextHolder.getBean("gitLabFactory");
        case ScmType.SCM_GIT_HUB:
            return (ScmFactory) SpringContextHolder.getBean("gitHubFactory");
        case ScmType.SCM_SVN:
            return (ScmFactory) SpringContextHolder.getBean("svnFactory");
        default:
            return null;
        }

    }

}
