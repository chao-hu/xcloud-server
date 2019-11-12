package com.xxx.xcloud.module.devops.build.mvn.service;

import com.xxx.xcloud.module.devops.build.mvn.pojo.Maven;
import com.xxx.xcloud.module.devops.model.Job;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface MvnService {

    /**
     * 生成Maven对象
     *
     * @param
     * @return Maven
     * @date: 2019年3月18日 下午3:00:34
     */
    Maven getMvn(Job job);

}
