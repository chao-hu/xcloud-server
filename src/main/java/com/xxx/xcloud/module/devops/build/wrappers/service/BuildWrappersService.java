package com.xxx.xcloud.module.devops.build.wrappers.service;

import com.xxx.xcloud.module.devops.build.wrappers.pojo.BuildWrappers;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface BuildWrappersService {

    BuildWrappers getBuildWrappers(Job jobModel) throws DevopsException;

}
