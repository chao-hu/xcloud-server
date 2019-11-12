package com.xxx.xcloud.module.devops.docker.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.build.shell.service.impl.ShellServiceImpl;
import com.xxx.xcloud.module.devops.common.DevopsConts;
import com.xxx.xcloud.module.devops.docker.pojo.DockerBuilder;
import com.xxx.xcloud.module.devops.docker.pojo.ImageTag;
import com.xxx.xcloud.module.devops.docker.pojo.Registry;
import com.xxx.xcloud.module.devops.docker.service.DockerService;
import com.xxx.xcloud.module.devops.model.DockerModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author xujiangpeng
 * @date 2019/3/26
 */
@Service("devops_dockerServiceImpl")
public class DockerServiceImpl implements DockerService {

    @Autowired
    ShellServiceImpl shellService;

    @Override
    public DockerBuilder getDocker(DockerModel dockerModel) {
        DockerBuilder dockerBuilder = new DockerBuilder();
        dockerBuilder.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_DOCKER));
        dockerBuilder.setCloud(DevopsConts.DEFAULT_CLOUD_NAME);
        dockerBuilder.setCleanImages(dockerModel.getCleanImages());

        // push image
        dockerBuilder.setPushOnSuccess(dockerModel.isPushOnSuccess());
        String pushCredentialsId = dockerModel.getPushCredentialsId();
        if (StringUtils.isNotEmpty(pushCredentialsId)) {
            dockerBuilder.setPushCredentialsId(pushCredentialsId);
        }

        //dockerfile dir
        if (dockerModel.isUserDefined()) {
            dockerBuilder.setDockerFileDirectory(".");
        } else {
            Map<String, String> map = shellService.getDockerfileDirAndName(dockerModel.getDockerFileDirectory());
            dockerBuilder.setDockerFileDirectory(map.get("dir"));
        }

        // docker image
        ImageTag imageTag = new ImageTag();
        imageTag.setTagString(dockerModel.getImageName());
        dockerBuilder.setTags(imageTag);

        // docker registry pull base images
        Registry registry = new Registry();
        if (dockerModel.isExternalRegistry()) {
            registry.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_DOCKER_COMMONS));
            registry.setUrl(dockerModel.getExternalRegistryUrl());
            registry.setCredentialsId(dockerModel.getExternalRegistryCredentialsId());
        }
        dockerBuilder.setFromRegistry(registry);

        return dockerBuilder;
    }

}
