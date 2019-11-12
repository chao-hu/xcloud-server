package com.xxx.xcloud.module.devops.build.ant.service;

import com.xxx.xcloud.module.devops.build.ant.pojo.Ant;
import com.xxx.xcloud.module.devops.model.Job;

public interface AntService {

    /**
     * 根据Job生成ant信息
     *
     * @param jobModel
     * @return Ant
     * @date: 2019年3月18日 下午3:45:37
     */
    Ant getAnt(Job jobModel);
}
