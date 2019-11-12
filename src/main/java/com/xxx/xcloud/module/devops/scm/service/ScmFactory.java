package com.xxx.xcloud.module.devops.scm.service;

import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.ScmModel;
import com.xxx.xcloud.module.devops.scm.pojo.Scm;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface ScmFactory {

    Scm getScm(ScmModel scmModel) throws DevopsException;

}
