package com.xxx.xcloud.module.devops.build.shell.service;

import com.xxx.xcloud.module.devops.build.shell.pojo.Shell;
import com.xxx.xcloud.module.devops.model.Job;

import java.util.List;

public interface ShellService {

    List<Shell> getShellModel(Job jobModel, String jobType);

}
