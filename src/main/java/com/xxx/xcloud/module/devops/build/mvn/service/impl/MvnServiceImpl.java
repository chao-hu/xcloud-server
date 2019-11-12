package com.xxx.xcloud.module.devops.build.mvn.service.impl;

import com.xxx.xcloud.module.devops.build.mvn.pojo.Maven;
import com.xxx.xcloud.module.devops.build.mvn.service.MvnService;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.model.MvnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MvnServiceImpl implements MvnService {

    private static final Logger LOG = LoggerFactory.getLogger(MvnServiceImpl.class);

    @Override
    public Maven getMvn(Job job) {
        MvnModel mvnModel = null;
        if (job.getBuildModel() != null) {
            mvnModel = job.getBuildModel().getMvnModel();
        }

        Maven maven = null;
        if (mvnModel != null) {
            maven = new Maven();
            maven.setTargets(mvnModel.getBuildcmd());
            maven.setMavenName(mvnModel.getMvnVersion());
        }
        return maven;
    }

}
