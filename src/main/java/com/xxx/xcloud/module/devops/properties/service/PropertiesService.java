package com.xxx.xcloud.module.devops.properties.service;

import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.ScmModel;
import com.xxx.xcloud.module.devops.properties.pojo.Properties;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface PropertiesService {

    Properties getProperties(ScmModel scmModel) throws DevopsException;

}
