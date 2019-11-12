package com.xxx.xcloud.module.devops.scm.service.impl;

import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.GitLabModel;
import com.xxx.xcloud.module.devops.model.ScmModel;
import org.springframework.stereotype.Component;

@Component("gitLabFactory")
public class GitLabFactory extends AbstractScmFactory {

    @Override
    protected void verify(ScmModel scmModel) throws DevopsException {
        GitLabModel gitLabModel = scmModel.getGitLabModel();
        if (gitLabModel == null) {
            throw new DevopsException(500, "GitLab配置信息为空");
        }
    }

    @Override
    protected String getUrl(ScmModel scmModel) {
        return scmModel.getGitLabModel().getUrl();
    }

    @Override
    protected String getCredentialId(ScmModel scmModel) {
        return scmModel.getGitLabModel().getCredentialId();
    }

    @Override
    protected String getBranch(ScmModel scmModel) {
        return scmModel.getGitLabModel().getBranch();
    }

    @Override
    protected String getName(ScmModel scmModel) {
        return scmModel.getGitLabModel().getName();
    }

    @Override
    protected String getRefspec(ScmModel scmModel) {
        return scmModel.getGitLabModel().getRefspec();
    }

}
