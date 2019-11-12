package com.xxx.xcloud.module.devops.build.service;

import com.xxx.xcloud.module.devops.build.pojo.Builders;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface BuildService {

	/**
     * 获取build对象
     * 
     * @param jobModel
     * @param jobType
     * @return Builders
     * @throws DevopsException
     * @date: 2019年3月19日 下午5:47:24
     */
    Builders getBuilders(Job jobModel, String jobType) throws DevopsException;
    
    /**
     * 获取jdk版本信息
     * 
     * @param jobModel
     * @return String
     * @date: 2019年3月19日 下午5:47:35
     */
    String getJdkVersion(Job jobModel);

}
