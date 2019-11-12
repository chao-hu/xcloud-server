package com.xxx.xcloud.module.devops.docker.service;

import com.xxx.xcloud.module.devops.docker.pojo.DockerBuilder;
import com.xxx.xcloud.module.devops.model.DockerModel;

/**
 * @author xujiangpeng
 * @date 2019/3/26
 */
public interface DockerService {

    /**
     * Get docker config within job config.xml.
     * <p>
     * If dockerfile is user-defined ,see shell model.
     *
     * @param dockerModel The docker info model from web.
     * @return The docker xml config.
     */
    DockerBuilder getDocker(DockerModel dockerModel);

}
