package com.xxx.xcloud.module.devops.build.shell.service.impl;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.build.shell.pojo.Shell;
import com.xxx.xcloud.module.devops.build.shell.service.ShellService;
import com.xxx.xcloud.module.devops.common.DevopsConts;
import com.xxx.xcloud.module.devops.job.service.JobService;
import com.xxx.xcloud.module.devops.model.DockerModel;
import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.model.ShellModel;
import com.xxx.xcloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Service
public class ShellServiceImpl implements ShellService {

    protected static final Logger LOG = LoggerFactory.getLogger(ShellServiceImpl.class);

    @Autowired
    private JobService jobService;

    @Override
    public List<Shell> getShellModel(Job jobModel, String jobType) {
        List<Shell> shellList = new ArrayList<>();

        //go model
        Shell goShell = getGoShell(jobModel, jobType);
        if (goShell != null) {
            shellList.add(goShell);
        }

        //nodeJS model
        Shell nodeShell = getNodeShell(jobModel);
        if (nodeShell != null) {
            shellList.add(nodeShell);
        }

        //docker model
        Shell dockerShell = getDockerShell(jobModel);
        if (dockerShell != null) {
            shellList.add(dockerShell);
        }

        return shellList;
    }

    /**
     * Build shell about NodeJS project.
     *
     * @return shell model about NodeJS,or null if unnecessary.
     * @author xujiangpeng
     */
    private Shell getNodeShell(Job jobModel) {
        if (jobModel == null || jobModel.getLanguageModel() == null) {
            return null;
        }

        String languageType;
        try {
            languageType = jobModel.getLanguageModel().getLangType();
        } catch (Exception e) {
            LOG.error("Get Job languageType exception!");
            throw e;
        }

        //if not nodeJS return.
        if (!CiConstant.DEVOPS_LANG_NODEJS.equalsIgnoreCase(languageType)) {
            return null;
        }

        if (jobModel.getBuildModel() == null || jobModel.getBuildModel().getShellModel() == null) {
            return null;
        }

        String cmd;
        try {
            cmd = jobModel.getBuildModel().getShellModel().getCmd();
        } catch (Exception e) {
            LOG.error("Get Job build cmd exception! languageType is :", languageType);
            throw e;
        }

        if (StringUtils.isNotEmpty(cmd)) {
            Shell ret = new Shell();
            ret.setCommand(cmd);
        }

        return null;
    }

    /**
     * 根据Job获取
     *
     * @param job
     * @param jobType
     * @return Shell
     * @date: 2019年3月29日 下午5:32:58
     */
    private Shell getGoShell(Job job, String jobType) {
        ShellModel shellModel = null;
        if (job.getBuildModel() != null) {
            shellModel = job.getBuildModel().getShellModel();
        }

        Shell shell = null;
        if (shellModel != null) {
            shell = new Shell();
            String cmd = shellModel.getCmd();
            if (job.getLanguageModel() != null && CiConstant.DEVOPS_LANG_GO
                    .equals(job.getLanguageModel().getLangType())) {
                cmd = XcloudProperties.getConfigMap().get(Global.DEVOPS_GO_SHELL_CMD_PRE) + jobService
                        .generateJenkinsJobName(job.getNamespace(), jobType, job.getName()) + "\r\n" + cmd;
            }
            shell.setCommand(cmd);
        }
        return shell;
    }

    /**
     * Pre-process shell about dockerfile.
     *
     * @return shell model about Docker,or null if unnecessary.
     * @author xujiangpeng
     */
    private Shell getDockerShell(Job jobModel) {
        Shell shell = new Shell();
        DockerModel dockerModel = jobModel.getDockerModel();
        if (null == dockerModel) {
            return null;
        }

        //1、If input is dockerfile content.
        if (dockerModel.isUserDefined()) {
            StringBuilder sb = new StringBuilder();
            sb.append("touch Dockerfile");
            sb.append("\r\n");
            sb.append("cat > Dockerfile" + " <<EOF");
            sb.append("\r\n");
            // 特殊字符$改为\$
            sb.append(dockerModel.getDockerFileContext().replaceAll(Matcher.quoteReplacement("$"),
                    Matcher.quoteReplacement("\\$")));
            sb.append("\r\n");
            sb.append("EOF");
            shell.setCommand(sb.toString());
            return shell;
        }

        //2、if input is dockerfile dir or name.
        String dockerFileDirectory = dockerModel.getDockerFileDirectory();
        if (StringUtils.isEmpty(dockerFileDirectory)) {
            throw new RuntimeException("Dockerfile 路径为空 ！");
        }

        Map<String, String> dirAndName = getDockerfileDirAndName(dockerFileDirectory);
        String dir = dirAndName.get("dir");
        String name = dirAndName.get("name");

        //2.1、If dockerfile name is Dockerfile
        if (DevopsConts.DEFAULT_DOCKERFILE_NAME.equalsIgnoreCase(name)) {
            return null;
        }

        //2.2、if dockerfile name is user-defined.
        StringBuilder sb = new StringBuilder();
        sb.append("cd " + dir);
        sb.append("\r\n");
        sb.append("touch Dockerfile");
        sb.append("\r\n");
        sb.append("cat " + name + " > " + DevopsConts.DEFAULT_DOCKERFILE_NAME);

        shell.setCommand(sb.toString());
        return shell;
    }

    /**
     * Get dockerfile dir and name,the value same as key.
     *
     * @param dockerFileDirectory dockerfile path.
     * @return Map
     * @author xujiangpeng
     */
    public Map<String, String> getDockerfileDirAndName(String dockerFileDirectory) {
        Map<String, String> ret = new HashMap<>(2);

        String dir, name;
        if (dockerFileDirectory.startsWith("./") || dockerFileDirectory.startsWith("/")) {
            try {
                dir = dockerFileDirectory.substring(0, dockerFileDirectory.lastIndexOf("/") + 1);
                name = dockerFileDirectory.substring(dockerFileDirectory.lastIndexOf("/") + 1);
            } catch (Exception e) {
                throw new RuntimeException("Dockerfile 路径不符合规范!", e);
            }
        } else {
            dir = DevopsConts.DEFAULT_DOCKERFILE_PATH;
            name = dockerFileDirectory;
        }

        if (StringUtils.isEmpty(dir)) {
            dir = DevopsConts.DEFAULT_DOCKERFILE_PATH;
        }

        if (StringUtils.isEmpty(name)) {
            name = DevopsConts.DEFAULT_DOCKERFILE_NAME;
        }

        ret.put("dir", dir);
        ret.put("name", name);
        return ret;
    }

}
