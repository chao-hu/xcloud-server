package com.xxx.xcloud.module.devops.scm.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.ScmModel;
import com.xxx.xcloud.module.devops.scm.pojo.*;
import com.xxx.xcloud.module.devops.scm.service.ScmFactory;

/**
 * @author daien
 * @date 2019年3月15日
 */
public abstract class AbstractScmFactory implements ScmFactory {

    private static final String CLAZZ = "hudson.plugins.git.GitSCM";

    @Override
    public Scm getScm(ScmModel scmModel) throws DevopsException {
        verify(scmModel);

        UserRemoteConfig userRemoteConfig = new UserRemoteConfig();
        userRemoteConfig.setCredentialsId(getCredentialId(scmModel));
        userRemoteConfig.setUrl(getUrl(scmModel));
        userRemoteConfig.setName(getName(scmModel));
        userRemoteConfig.setRefspec(getRefspec(scmModel));

        UserRemoteConfigs userRemoteConfigs = new UserRemoteConfigs();
        userRemoteConfigs.setUserRemoteConfig(userRemoteConfig);

        BranchSpec branchSpec = new BranchSpec();
        branchSpec.setName(getBranch(scmModel));
        Branches branches = new Branches();
        branches.setBranchSpec(branchSpec);

        SubmoduleCfg submoduleCfg = new SubmoduleCfg();
        submoduleCfg.setClazz("list");

        Scm scm = new Scm();
        scm.setClazz(CLAZZ);
        scm.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_GIT));
        scm.setConfigVersion(2);
        scm.setUserRemoteConfigs(userRemoteConfigs);
        scm.setBranches(branches);
        scm.setDoGenerateSubmoduleConfigurations(false);
        scm.setSubmoduleCfg(submoduleCfg);
        scm.setExtensions("");

        return scm;
    }

    protected void verify(ScmModel scmModel) throws DevopsException {
    }

    protected abstract String getUrl(ScmModel scmModel);

    protected abstract String getCredentialId(ScmModel scmModel);

    protected abstract String getBranch(ScmModel scmModel);

    protected abstract String getName(ScmModel scmModel);

    protected abstract String getRefspec(ScmModel scmModel);
}
